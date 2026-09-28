package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TradeRepository(private val db: AppDatabase) {
  val allTrades: Flow<List<TradeEntity>> = db.tradeDao().getAllTrades()
  val botSettings: Flow<BotSettingsEntity?> = db.botSettingsDao().getSettings()
  val allRoutines: Flow<List<RoutineEntity>> = db.routineDao().getAllRoutines()

  fun getRoutineForDate(dateString: String): Flow<RoutineEntity?> {
    return db.routineDao().getRoutineForDate(dateString)
  }

  suspend fun insertTrade(trade: TradeEntity): Long = db.tradeDao().insertTrade(trade)

  suspend fun updateTrade(trade: TradeEntity) = db.tradeDao().updateTrade(trade)

  suspend fun deleteTrade(id: Long) = db.tradeDao().deleteTradeById(id)

  suspend fun updateSettings(settings: BotSettingsEntity) = db.botSettingsDao().insertOrUpdate(settings)

  suspend fun updateRoutine(routine: RoutineEntity) = db.routineDao().insertOrUpdateRoutine(routine)

  suspend fun initializeDefaultsIfNeeded() {
    val existingSettings = db.botSettingsDao().getSettings().firstOrNull()
    if (existingSettings == null) {
      val initial = BotSettingsEntity(
        id = 1,
        balance = 30000.0,
        initialBalance = 30000.0,
        peakBalance = 30750.0,
        baseRiskPercent = 0.25,
        maxDailyLossPercent = 1.0,
        maxWeeklyLossPercent = 3.5,
        maxDailyLossStrikes = 4,
        drawdownDeriskThreshold = 4.0,
        deriskedRiskPercent = 0.12,
        scalingProfitThreshold = 5.0,
        scaledRiskPercent = 0.50,
        isAutoTradingActive = true,
        activePair = "EURUSD",
        autoCloseAtBreakeven = true,
        autoPartialsAt3R = true
      )
      db.botSettingsDao().insertOrUpdate(initial)
    }

    // Check if initial trade history exists
    val trades = db.tradeDao().getAllTrades().firstOrNull()
    if (trades.isNullOrEmpty()) {
      val now = System.currentTimeMillis()
      val dayMillis = 86400000L
      // Seed educational baseline trades reflecting the book's exact statistics:
      // Author stats: 35-48% pure win rate, 54% with partials, 5R fixed, 2.46 profit factor!
      val seedTrades = listOf(
        TradeEntity(
          timestamp = now - dayMillis * 3 - 3600000 * 4,
          symbol = "EURUSD",
          direction = "BUY",
          strategy = "PRECISION",
          entryPrice = 1.08500,
          stopLoss = 1.08460, // 4 pips SL (< 5 pips -> 5R TP)
          takeProfit = 1.08700,
          exitPrice = 1.08700,
          lotSize = 1.85,
          riskPercent = 0.25,
          pnl = 375.0,
          rMultiple = 5.0,
          status = "CLOSED",
          isRuleCompliant = true,
          emotionTag = "Disciplined",
          exitReason = "TP Hit (5R Target reached at Weak High)",
          notes = "Clean 15M demand tap, 1M Asian low sweep LG, 1M TC entry at 50% OB mitigation."
        ),
        TradeEntity(
          timestamp = now - dayMillis * 2 - 3600000 * 6,
          symbol = "US30",
          direction = "SELL",
          strategy = "SAF",
          entryPrice = 39250.0,
          stopLoss = 39310.0,
          takeProfit = 39010.0,
          exitPrice = 39010.0,
          lotSize = 1.25,
          riskPercent = 0.25,
          pnl = 300.0,
          rMultiple = 4.0,
          status = "CLOSED",
          isRuleCompliant = true,
          emotionTag = "Calm",
          exitReason = "TP Hit (4R Target at 4H Weak Low)",
          notes = "SAF model pro-trend 4H bearish in premium zone. Set and forget executed."
        ),
        TradeEntity(
          timestamp = now - dayMillis * 2 - 3600000 * 2,
          symbol = "EURUSD",
          direction = "BUY",
          strategy = "PRECISION",
          entryPrice = 1.08620,
          stopLoss = 1.08580,
          takeProfit = 1.08820,
          exitPrice = 1.08580,
          lotSize = 1.85,
          riskPercent = 0.25,
          pnl = -75.0,
          rMultiple = -1.0,
          status = "CLOSED",
          isRuleCompliant = true,
          emotionTag = "Neutral",
          exitReason = "SL Hit (Normal 1R loss - Casino edge intact)",
          notes = "1M TC formed but next candle failed momentum. Controlled 0.25% risk loss."
        ),
        TradeEntity(
          timestamp = now - dayMillis - 3600000 * 5,
          symbol = "EURUSD",
          direction = "BUY",
          strategy = "PRECISION",
          entryPrice = 1.08740,
          stopLoss = 1.08705,
          takeProfit = 1.08915,
          exitPrice = 1.08860,
          lotSize = 2.10,
          riskPercent = 0.25,
          pnl = 255.0,
          rMultiple = 3.4,
          status = "CLOSED",
          isRuleCompliant = true,
          emotionTag = "Neutral",
          exitReason = "Partial profit taken at 3.4R before consolidation",
          notes = "Took partials at 3.4R according to book rule: 'You cannot go broke taking profits'."
        )
      )
      seedTrades.forEach { db.tradeDao().insertTrade(it) }
    }

    // Seed today's routine if absent
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val todayRoutine = db.routineDao().getRoutineForDate(todayStr).firstOrNull()
    if (todayRoutine == null) {
      db.routineDao().insertOrUpdateRoutine(
        RoutineEntity(
          dateString = todayStr,
          hydrateDone = true,
          exerciseDone = true,
          meditateDone = true,
          chartPrepDone = true,
          alertsSet = true,
          tradeWindowObserved = true,
          midDayBreakDone = false,
          eodJournalDone = false,
          eodMarkupDone = false,
          mindsetRating = 5,
          reflectionNotes = "Mindset calm and neutral. Ready for London & NY trade windows. Risk locked at 0.25%."
        )
      )
    }
  }
}
