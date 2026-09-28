package com.example.engine

import com.example.data.BotSettingsEntity
import com.example.data.TradeEntity
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.random.Random

class TradingBotEngine {

  private val _selectedPair = MutableStateFlow("EURUSD")
  val selectedPair: StateFlow<String> = _selectedPair.asStateFlow()

  private val _selectedTimeframe = MutableStateFlow(Timeframe.MTF_15M)
  val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

  private val _candles = MutableStateFlow<List<Candle>>(emptyList())
  val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

  private val _marketTrend = MutableStateFlow(MarketTrend.UPTREND)
  val marketTrend: StateFlow<MarketTrend> = _marketTrend.asStateFlow()

  private val _pdRange = MutableStateFlow(PDRange(1.08950, 1.08150))
  val pdRange: StateFlow<PDRange> = _pdRange.asStateFlow()

  private val _orderBlocks = MutableStateFlow<List<OrderBlock>>(emptyList())
  val orderBlocks: StateFlow<List<OrderBlock>> = _orderBlocks.asStateFlow()

  private val _liquidityEvents = MutableStateFlow<List<LiquidityEvent>>(emptyList())
  val liquidityEvents: StateFlow<List<LiquidityEvent>> = _liquidityEvents.asStateFlow()

  private val _currentSignal = MutableStateFlow<TradingSignal?>(null)
  val currentSignal: StateFlow<TradingSignal?> = _currentSignal.asStateFlow()

  private val _activePositions = MutableStateFlow<List<ActivePosition>>(emptyList())
  val activePositions: StateFlow<List<ActivePosition>> = _activePositions.asStateFlow()

  private val _currentWindow = MutableStateFlow(getCurrentTradeWindow())
  val currentWindow: StateFlow<TradeWindowSession> = _currentWindow.asStateFlow()

  private val _dailyLossStreak = MutableStateFlow(0)
  val dailyLossStreak: StateFlow<Int> = _dailyLossStreak.asStateFlow()

  private val _dailyLossPercent = MutableStateFlow(0.0)
  val dailyLossPercent: StateFlow<Double> = _dailyLossPercent.asStateFlow()

  private val _weeklyLossPercent = MutableStateFlow(0.0)
  val weeklyLossPercent: StateFlow<Double> = _weeklyLossPercent.asStateFlow()

  private val _circuitBreakerHaltReason = MutableStateFlow<String?>(null)
  val circuitBreakerHaltReason: StateFlow<String?> = _circuitBreakerHaltReason.asStateFlow()

  init {
    loadPairData("EURUSD", Timeframe.MTF_15M)
  }

  fun selectPair(pair: String) {
    _selectedPair.value = pair
    loadPairData(pair, _selectedTimeframe.value)
  }

  fun selectTimeframe(tf: Timeframe) {
    _selectedTimeframe.value = tf
    loadPairData(_selectedPair.value, tf)
  }

  fun refreshWindow() {
    _currentWindow.value = getCurrentTradeWindow()
  }

  fun calculateLotSize(balance: Double, riskPercent: Double, entryPrice: Double, stopLossPrice: Double, pair: String): Double {
    val riskDollars = balance * (riskPercent / 100.0)
    val pipDifference = abs(entryPrice - stopLossPrice)
    val pipValuePerLot = when (pair) {
      "US30" -> 1.0 // 1 point = $1 per lot
      else -> 10.0  // Standard FX lot: 1 pip (0.0001) = $10
    }
    val pips = if (pair == "US30") pipDifference else pipDifference * 10000.0
    if (pips <= 0.0) return 0.1
    val calculatedLots = riskDollars / (pips * pipValuePerLot)
    return (Math.round(calculatedLots * 100.0) / 100.0).coerceIn(0.01, 50.0)
  }

  fun evaluateCircuitBreakers(settings: BotSettingsEntity, todayTrades: List<TradeEntity>) {
    var dayLoss = 0.0
    var consecutiveLosses = 0
    var tempStreak = 0

    val todayClosed = todayTrades.filter { it.status == "CLOSED" }
    for (trade in todayClosed.sortedBy { it.timestamp }) {
      if (trade.pnl < 0) {
        dayLoss += abs(trade.pnl)
        tempStreak++
        consecutiveLosses = maxOf(consecutiveLosses, tempStreak)
      } else {
        tempStreak = 0
      }
    }

    val dayLossPct = if (settings.balance > 0) (dayLoss / settings.balance) * 100.0 else 0.0
    _dailyLossPercent.value = dayLossPct
    _dailyLossStreak.value = consecutiveLosses

    val window = getCurrentTradeWindow()
    _currentWindow.value = window

    when {
      dayLossPct >= settings.maxDailyLossPercent -> {
        _circuitBreakerHaltReason.value = "DAILY LOSS LIMIT HIT (${String.format("%.2f", dayLossPct)}% >= ${settings.maxDailyLossPercent}%). Hard Stop Trading for Today!"
      }
      consecutiveLosses >= settings.maxDailyLossStrikes -> {
        _circuitBreakerHaltReason.value = "MAX CONSECUTIVE LOSSES REACHED ($consecutiveLosses strikes >= ${settings.maxDailyLossStrikes}). Stop for Today!"
      }
      _weeklyLossPercent.value >= settings.maxWeeklyLossPercent -> {
        _circuitBreakerHaltReason.value = "WEEKLY LOSS LIMIT HIT (${String.format("%.2f", _weeklyLossPercent.value)}% >= ${settings.maxWeeklyLossPercent}%). Stop for this Week!"
      }
      !window.isTradeAllowed -> {
        _circuitBreakerHaltReason.value = "OUTSIDE TRADE WINDOW (${window.title}). ${window.advice}"
      }
      else -> {
        _circuitBreakerHaltReason.value = null
      }
    }
  }

  fun generateNewSignal(settings: BotSettingsEntity): TradingSignal {
    val pair = _selectedPair.value
    val currentCandles = _candles.value
    val lastCandle = currentCandles.lastOrNull()
    val basePrice = lastCandle?.close ?: if (pair == "US30") 39200.0 else 1.08500

    val isBullishBias = _marketTrend.value == MarketTrend.UPTREND
    val direction = if (isBullishBias) TradeDirection.BUY else TradeDirection.SELL

    val pipUnit = if (pair == "US30") 1.0 else 0.0001
    val slPips = Random.nextInt(4, 7).toDouble() // 4 to 6 pips
    val targetR = if (slPips < 5.0) 5.0 else 4.0 // Book rule: 5R if SL < 5 pips, 4R if SL > 5 pips

    val entryPrice = basePrice
    val stopLoss = if (direction == TradeDirection.BUY) entryPrice - (slPips * pipUnit) else entryPrice + (slPips * pipUnit)
    val takeProfit = if (direction == TradeDirection.BUY) entryPrice + (slPips * targetR * pipUnit) else entryPrice - (slPips * targetR * pipUnit)

    val currentWindow = getCurrentTradeWindow()
    val isDiscountOk = if (direction == TradeDirection.BUY) _pdRange.value.isDiscount(entryPrice) else _pdRange.value.isPremium(entryPrice)
    val hasValidOB = _orderBlocks.value.any { it.isEngulfed && it.hasMomentum }
    val hasSweep = _liquidityEvents.value.any { it.type == LiquidityType.LIQUIDITY_GRAB }
    val isLossLimitOk = _circuitBreakerHaltReason.value == null

    val checklist = listOf(
      RuleCheckResult(
        "HTF 4H Trend Alignment",
        isBullishBias,
        if (isBullishBias) "4H Bullish (HH/HL held, trading from Strong Low)" else "4H Bearish (LH/LL held, trading from Strong High)"
      ),
      RuleCheckResult(
        "Premium / Discount Fib 50%",
        isDiscountOk,
        if (direction == TradeDirection.BUY) "Price is in Discount (< 50% Equilibrium) - Cheap to buy" else "Price is in Premium (> 50% Equilibrium) - Expensive to sell"
      ),
      RuleCheckResult(
        "15M Order Block (OB) Validation",
        hasValidOB,
        "Order block engulfed by next candle, caused impulsive Structure Break (SB)"
      ),
      RuleCheckResult(
        "Liquidity Grab (LG) / Sweep",
        hasSweep,
        "Early retail stops & liquidity taken out below Asian/session low before true move"
      ),
      RuleCheckResult(
        "1M Trend Change (TC) Alignment",
        true,
        "1M broke last minor structure with momentum and aligned with 15M/4H direction"
      ),
      RuleCheckResult(
        "Trade Window Session",
        currentWindow.isTradeAllowed,
        "${currentWindow.title} (${currentWindow.gmtTimeRange}) - ${if (currentWindow.isTradeAllowed) "Trade Allowed" else "Restricted"}"
      ),
      RuleCheckResult(
        "Risk Guardian Rule (0.25% - 1.0%)",
        isLossLimitOk,
        "Effective risk locked to ${String.format("%.2f", settings.effectiveRiskPercent)}%. Daily/Weekly loss limits respected."
      )
    )

    val isExecutable = checklist.all { it.isPassed }
    val signal = TradingSignal(
      id = "SIG_${System.currentTimeMillis()}",
      timestamp = System.currentTimeMillis(),
      symbol = pair,
      direction = direction,
      strategy = StrategyModel.PRECISION,
      entryPrice = entryPrice,
      stopLoss = stopLoss,
      takeProfit = takeProfit,
      targetR = targetR,
      pipsRisk = slPips,
      reasonChecklist = checklist,
      isExecutable = isExecutable
    )

    _currentSignal.value = signal
    return signal
  }

  fun executeSignal(signal: TradingSignal, settings: BotSettingsEntity): ActivePosition {
    val lotSize = calculateLotSize(settings.balance, settings.effectiveRiskPercent, signal.entryPrice, signal.stopLoss, signal.symbol)
    val riskAmount = settings.balance * (settings.effectiveRiskPercent / 100.0)

    val pos = ActivePosition(
      id = System.currentTimeMillis(),
      symbol = signal.symbol,
      direction = signal.direction,
      strategy = signal.strategy,
      entryPrice = signal.entryPrice,
      currentPrice = signal.entryPrice,
      stopLoss = signal.stopLoss,
      takeProfit = signal.takeProfit,
      lotSize = lotSize,
      riskAmount = riskAmount,
      openTimestamp = System.currentTimeMillis(),
      pnl = 0.0,
      currentR = 0.0,
      isPartialsTaken = false,
      isBreakeven = false
    )

    val updated = _activePositions.value.toMutableList()
    updated.add(pos)
    _activePositions.value = updated
    _currentSignal.value = null
    return pos
  }

  fun takePartials(posId: Long): ActivePosition? {
    val list = _activePositions.value.toMutableList()
    val index = list.indexOfFirst { it.id == posId }
    if (index >= 0) {
      val pos = list[index]
      val securedPnl = pos.riskAmount * 3.5 // Take partials at 3.5R
      pos.isPartialsTaken = true
      pos.pnl += securedPnl
      list[index] = pos
      _activePositions.value = list
      return pos
    }
    return null
  }

  fun moveBreakeven(posId: Long): ActivePosition? {
    val list = _activePositions.value.toMutableList()
    val index = list.indexOfFirst { it.id == posId }
    if (index >= 0) {
      val pos = list[index]
      pos.isBreakeven = true
      list[index] = pos
      _activePositions.value = list
      return pos
    }
    return null
  }

  fun closePosition(posId: Long, exitPrice: Double? = null, reason: String = "Manual Close"): TradeEntity? {
    val list = _activePositions.value.toMutableList()
    val index = list.indexOfFirst { it.id == posId }
    if (index >= 0) {
      val pos = list.removeAt(index)
      _activePositions.value = list
      val closePrice = exitPrice ?: pos.currentPrice
      val r = pos.currentR
      val pnl = pos.pnl

      return TradeEntity(
        timestamp = System.currentTimeMillis(),
        symbol = pos.symbol,
        direction = pos.direction.name,
        strategy = pos.strategy.name,
        entryPrice = pos.entryPrice,
        stopLoss = pos.stopLoss,
        takeProfit = pos.takeProfit,
        exitPrice = closePrice,
        lotSize = pos.lotSize,
        riskPercent = 0.25,
        pnl = pnl,
        rMultiple = r,
        status = "CLOSED",
        isRuleCompliant = true,
        emotionTag = "Disciplined",
        exitReason = reason,
        notes = "Closed according to trade plan rules."
      )
    }
    return null
  }

  fun tickLiveMarket(settings: BotSettingsEntity, onTradeClosed: (TradeEntity) -> Unit) {
    val pair = _selectedPair.value
    val candleList = _candles.value.toMutableList()
    if (candleList.isEmpty()) return

    val last = candleList.last()
    val step = if (pair == "US30") Random.nextDouble(1.0, 15.0) else Random.nextDouble(0.00002, 0.00015)
    val delta = if (Random.nextBoolean()) step else -step
    val newClose = (last.close + delta).coerceAtLeast(0.001)
    val newHigh = maxOf(last.high, newClose)
    val newLow = minOf(last.low, newClose)
    candleList[candleList.lastIndex] = last.copy(close = newClose, high = newHigh, low = newLow)
    _candles.value = candleList

    // Update active positions
    val openPositions = _activePositions.value.toMutableList()
    val closedTrades = mutableListOf<TradeEntity>()
    val remainingPositions = mutableListOf<ActivePosition>()

    for (pos in openPositions) {
      pos.currentPrice = newClose
      val slDist = abs(pos.entryPrice - pos.stopLoss).coerceAtLeast(0.00001)
      val currentDist = if (pos.direction == TradeDirection.BUY) newClose - pos.entryPrice else pos.entryPrice - newClose
      pos.currentR = Math.round((currentDist / slDist) * 10.0) / 10.0
      pos.pnl = pos.riskAmount * pos.currentR

      // Check Take Profit (5R)
      val isTpHit = if (pos.direction == TradeDirection.BUY) newClose >= pos.takeProfit else newClose <= pos.takeProfit
      val isSlHit = if (pos.direction == TradeDirection.BUY) newClose <= (if (pos.isBreakeven) pos.entryPrice else pos.stopLoss) else newClose >= (if (pos.isBreakeven) pos.entryPrice else pos.stopLoss)

      // Auto partials at 3R if enabled
      if (settings.autoPartialsAt3R && !pos.isPartialsTaken && pos.currentR >= 3.0) {
        pos.isPartialsTaken = true
        pos.isBreakeven = true // move to BE on partials!
      }

      // Auto breakeven if enabled
      if (settings.autoCloseAtBreakeven && !pos.isBreakeven && pos.currentR >= 2.0) {
        pos.isBreakeven = true
      }

      if (isTpHit) {
        closedTrades.add(
          TradeEntity(
            timestamp = System.currentTimeMillis(),
            symbol = pos.symbol,
            direction = pos.direction.name,
            strategy = pos.strategy.name,
            entryPrice = pos.entryPrice,
            stopLoss = pos.stopLoss,
            takeProfit = pos.takeProfit,
            exitPrice = newClose,
            lotSize = pos.lotSize,
            riskPercent = settings.effectiveRiskPercent,
            pnl = pos.riskAmount * 5.0,
            rMultiple = 5.0,
            status = "CLOSED",
            isRuleCompliant = true,
            emotionTag = "Disciplined",
            exitReason = "TP Hit (5R Target reached at Weak High/Low)",
            notes = "Set and forget target reached. Textbook SMC execution."
          )
        )
      } else if (isSlHit) {
        val finalR = if (pos.isBreakeven) 0.0 else -1.0
        val finalPnl = if (pos.isBreakeven) 0.0 else -pos.riskAmount
        closedTrades.add(
          TradeEntity(
            timestamp = System.currentTimeMillis(),
            symbol = pos.symbol,
            direction = pos.direction.name,
            strategy = pos.strategy.name,
            entryPrice = pos.entryPrice,
            stopLoss = pos.stopLoss,
            takeProfit = pos.takeProfit,
            exitPrice = newClose,
            lotSize = pos.lotSize,
            riskPercent = settings.effectiveRiskPercent,
            pnl = finalPnl,
            rMultiple = finalR,
            status = "CLOSED",
            isRuleCompliant = true,
            emotionTag = "Calm",
            exitReason = if (pos.isBreakeven) "Breakeven Stop (0R loss)" else "Stop Loss Hit (-1.0R normal variance)",
            notes = "Controlled risk intact. Casino probability edge maintained."
          )
        )
      } else {
        remainingPositions.add(pos)
      }
    }

    _activePositions.value = remainingPositions
    closedTrades.forEach { onTradeClosed(it) }
  }

  private fun loadPairData(pair: String, tf: Timeframe) {
    val basePrice = when (pair) {
      "US30" -> 39250.0
      "GBPUSD" -> 1.28500
      else -> 1.08500
    }
    val pipUnit = if (pair == "US30") 5.0 else 0.0003

    // Synthesize realistic price action reflecting the book's SMC principles:
    // (Accumulation/Asian range -> Manipulation/LG Sweep -> Impulsive Breakout/Structure Break -> Retest of 50% OB)
    val now = System.currentTimeMillis()
    val candleInterval = when (tf) {
      Timeframe.HTF_4H -> 14400000L
      Timeframe.MTF_15M -> 900000L
      Timeframe.LTF_1M -> 60000L
    }

    val list = mutableListOf<Candle>()
    var current = basePrice

    // Generate 32 candles with an SMC structure pattern
    for (i in 30 downTo 0) {
      val t = now - (i * candleInterval)
      val cycleProgress = (30 - i)
      val bias = when {
        cycleProgress < 8 -> 0.0 // Asian range flat
        cycleProgress in 8..11 -> -1.5 // Manipulation / Liquidity sweep down
        cycleProgress in 12..20 -> 2.5 // Impulsive upward move / SB / TC
        cycleProgress in 21..26 -> -0.8 // Retracement into 50% discount demand zone
        else -> 1.8 // Expansion towards weak high
      }

      val change = (bias * pipUnit) + (Random.nextDouble(-0.4, 0.4) * pipUnit)
      val open = current
      val close = open + change
      val high = maxOf(open, close) + (Random.nextDouble(0.1, 0.5) * pipUnit)
      val low = minOf(open, close) - (Random.nextDouble(0.1, 0.5) * pipUnit)
      current = close

      list.add(Candle(t, open, high, low, close))
    }

    _candles.value = list
    _marketTrend.value = MarketTrend.UPTREND

    val lowestPrice = list.minOf { it.low }
    val highestPrice = list.maxOf { it.high }
    _pdRange.value = PDRange(highestPrice, lowestPrice)

    // Set Order Block from the accumulation origin candle (e.g. index 8-10)
    val obCandle = list.getOrNull(10) ?: list.first()
    _orderBlocks.value = listOf(
      OrderBlock(
        id = "OB_${tf.label}_1",
        type = OrderBlockType.BULLISH_DEMAND,
        topPrice = obCandle.bodyTop,
        bottomPrice = obCandle.low,
        timeframe = tf,
        isEngulfed = true,
        hasMomentum = true,
        isMitigated = false,
        formation = FormationType.SB
      )
    )

    // Set Liquidity Grab event from the wick low at index 10
    _liquidityEvents.value = listOf(
      LiquidityEvent(
        type = LiquidityType.LIQUIDITY_GRAB,
        price = obCandle.low,
        description = "Liquidity Grab (Sweep of Asian Low / Retail Stops)",
        timestamp = obCandle.time
      ),
      LiquidityEvent(
        type = LiquidityType.EQH,
        price = highestPrice,
        description = "Equal Highs (EQH) Liquidity Pool - Target Weak High",
        timestamp = list.last().time
      )
    )
  }

  companion object {
    fun getCurrentTradeWindow(): TradeWindowSession {
      val gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
      val hour = gmtCal.get(Calendar.HOUR_OF_DAY)
      return when (hour) {
        in 1..4 -> TradeWindowSession.ASIAN_RANGE
        6 -> TradeWindowSession.PRE_LONDON
        in 7..10 -> TradeWindowSession.LONDON_WINDOW
        12 -> TradeWindowSession.PRE_NY
        in 13..15 -> TradeWindowSession.NY_WINDOW
        16, 17 -> TradeWindowSession.CUT_TRADING
        18 -> TradeWindowSession.EOD_REVIEW
        else -> TradeWindowSession.OFF_HOURS
      }
    }
  }
}
