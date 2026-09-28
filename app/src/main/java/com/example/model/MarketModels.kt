package com.example.model

enum class Timeframe(val label: String, val role: String) {
  HTF_4H("4H", "Major Structure & Trend"),
  MTF_15M("15M", "Minor Structure & Zones"),
  LTF_1M("1M", "Entry Precision & Sweeps")
}

enum class MarketTrend(val title: String) {
  UPTREND("Bullish (Breaking Highs, Holding Lows)"),
  DOWNTREND("Bearish (Breaking Lows, Holding Highs)"),
  RANGING("Consolidation / Liquidity Build")
}

enum class StrongWeakType {
  STRONG_HIGH, // Broke structure downward (caused new LL) -> Protects short
  STRONG_LOW,  // Broke structure upward (caused new HH) -> Protects long
  WEAK_HIGH,   // Failed to break structure -> Target for longs
  WEAK_LOW     // Failed to break structure -> Target for shorts
}

enum class FormationType(val code: String, val title: String) {
  MSS("MSS", "Market Structure Shift (Early warning)"),
  TC("TC", "Trend Change (Confirmed reversal)"),
  SB("SB", "Structure Break (Trend continuation)")
}

data class PDRange(
  val high: Double,
  val low: Double,
  val equilibrium: Double = (high + low) / 2.0
) {
  fun isDiscount(price: Double): Boolean = price < equilibrium
  fun isPremium(price: Double): Boolean = price > equilibrium
  fun fibLevel(price: Double): Double {
    val span = high - low
    if (span <= 0) return 0.5
    return (price - low) / span
  }
}

enum class OrderBlockType {
  BULLISH_DEMAND, // Last sell candle before big upward move
  BEARISH_SUPPLY  // Last buy candle before big downward move
}

data class OrderBlock(
  val id: String,
  val type: OrderBlockType,
  val topPrice: Double,
  val bottomPrice: Double,
  val equilibrium50: Double = (topPrice + bottomPrice) / 2.0,
  val timeframe: Timeframe,
  val isEngulfed: Boolean = true,
  val hasMomentum: Boolean = true,
  val isMitigated: Boolean = false,
  val formation: FormationType = FormationType.SB
)

enum class LiquidityType(val label: String) {
  EQH("Equal Highs (EQH)"),
  EQL("Equal Lows (EQL)"),
  TRENDLINE_LQ("Trendline Liquidity"),
  LIQUIDITY_GRAB("Liquidity Grab (Sweep / Stop Hunt)"),
  ASIAN_RANGE("Asian Range Bounds"),
  FAKE_TC("Fake Trend Change Trap")
}

data class LiquidityEvent(
  val type: LiquidityType,
  val price: Double,
  val description: String,
  val timestamp: Long
)

enum class StrategyModel(val title: String, val shortName: String, val targetR: Double) {
  SAF("Set And Forget", "SAF", 4.5),
  PRECISION("The Precision Entry (Fanatic Model)", "PRECISION", 5.0)
}

enum class TradeDirection {
  BUY, SELL
}

data class TradingSignal(
  val id: String,
  val timestamp: Long,
  val symbol: String,
  val direction: TradeDirection,
  val strategy: StrategyModel,
  val entryPrice: Double,
  val stopLoss: Double,
  val takeProfit: Double,
  val targetR: Double,
  val pipsRisk: Double,
  val reasonChecklist: List<RuleCheckResult>,
  val isExecutable: Boolean
)

data class RuleCheckResult(
  val ruleName: String,
  val isPassed: Boolean,
  val detail: String
)

enum class TradeWindowSession(
  val title: String,
  val gmtTimeRange: String,
  val isTradeAllowed: Boolean,
  val advice: String
) {
  ASIAN_RANGE("Asian Range", "01:00 - 05:00 GMT", false, "Accumulation phase. Mark high/low liquidity; do not trade."),
  PRE_LONDON("Pre-London Prep", "06:00 - 07:00 GMT", false, "Prepare charts, locate HTF 4H zones and mark Asian highs/lows."),
  LONDON_WINDOW("London Window (FO to Lunch)", "07:00 - 11:00 GMT", true, "Prime Trading Window! Look for Asian sweeps and 15M/1M alignment."),
  PRE_NY("Pre-NY Prep", "12:00 - 13:00 GMT", false, "Mid-day recap. Review morning executions; prep for NY open."),
  NY_WINDOW("New York Window (NYO to LC)", "13:00 - 16:00 GMT", true, "High Volatility Session! High probability reversals and continuation."),
  CUT_TRADING("Cut Trading Window", "16:00 - 18:00 GMT", false, "Market volume fading. Manage open positions; no new entries."),
  EOD_REVIEW("End of Day Journal & Review", "18:00 - 19:00 GMT", false, "Mandatory journal logging, EOD markup and emotional recap."),
  OFF_HOURS("Off-Market / Spread Hours", "19:00 - 01:00 GMT", false, "Spreads widen and liquidity dries up. Strictly avoid trading.")
}

data class ActivePosition(
  val id: Long,
  val symbol: String,
  val direction: TradeDirection,
  val strategy: StrategyModel,
  val entryPrice: Double,
  var currentPrice: Double,
  val stopLoss: Double,
  val takeProfit: Double,
  val lotSize: Double,
  val riskAmount: Double,
  val openTimestamp: Long,
  var pnl: Double = 0.0,
  var currentR: Double = 0.0,
  var isPartialsTaken: Boolean = false,
  var isBreakeven: Boolean = false
)
