package com.example.model

data class BookChapter(
  val id: String,
  val bookNumber: Int,
  val title: String,
  val subtitle: String,
  val keyRules: List<String>,
  val detailedExplanation: String
)

object BookRulesData {
  val chapters = listOf(
    BookChapter(
      id = "risk_masterclass",
      bookNumber = 1,
      title = "Risk & Money Management Plan",
      subtitle = "The Absolute Core of Trading — Impossible to Blow Up",
      keyRules = listOf(
        "Risk per trade: 0.25% to 1.0% (Author standard: 0.25%).",
        "Max Daily Loss: 1.0% (or 4 consecutive losses) -> HARD STOP FOR THE DAY.",
        "Max Weekly Loss: 3.5% (or 14 losses) -> HARD STOP FOR THE WEEK.",
        "Drawdown De-risking: At 4.0% Drawdown, cut risk in half to 0.12% until DD recovers above 3.8%.",
        "Scaling Profits: When balance is +5.0% in profit, scale risk to 0.50% while holding > 2% profit.",
        "Fixed Profit Targets: 5R TP when SL < 5 pips, 4R TP when SL > 5 pips. Take partials at 3-4R.",
        "The Casino Edge: Win rate of 35-48% with 4R-5R reward makes consistent profitability mathematical certainty."
      ),
      detailedExplanation = """
        Trading is a business. Without a structured risk plan, any trader is doomed to fail.
        The Three Pillars:
        1. Risk Management (Protects capital, dictates survival)
        2. Market Structure (The foundation of price movement)
        3. Supply & Demand (The cherry on top for precision entries)
        
        Monetary Goals & Living Expenses (LE):
        Calculate monthly essentials: Mortgage/Rent + Utilities + Food + Transport + Insurance.
        Multiply by 12 for annual costs.
        Required Account Size = Monthly LE / 0.05 (assuming conservative 5% monthly return).
        Example: If you need $1,500/month for LE, you need an account of $30,000 ($1,500 / 0.05).
      """.trimIndent()
    ),
    BookChapter(
      id = "market_structure",
      bookNumber = 2,
      title = "Market Structure & Trend Identification",
      subtitle = "The Foundation of All Technical Analysis",
      keyRules = listOf(
        "Uptrend: Market breaks Highs (HH) and maintains Lows (HL). HL must never be broken.",
        "Downtrend: Market breaks Lows (LL) and maintains Highs (LH). LH must never be broken.",
        "Consolidation / Range: Failing to break both Highs and Lows. Do not trade ranges on LTF.",
        "Strong High: Broke structure to the downside and created a confirmed LL (Protected by Institutions).",
        "Strong Low: Broke structure to the upside and created a confirmed HH (Protected by Institutions).",
        "Weak High / Low: Failed to break structure. These are target liquidity pools we trade TOWARDS.",
        "The Golden Law: Always trade FROM Strong Structure TOWARDS Weak Structure!",
        "Multi-Timeframe Fractal: 4H dictates Major Trend & Range. 15M dictates Minor Structure & Pullbacks. 1M is for Entries."
      ),
      detailedExplanation = """
        The market oscillates between its Mean Value and Fear/Greed in cycles.
        
        Premium & Discount (PD Range):
        - Using the 0, 0.5, 1.0 Fibonacci tool:
        - Above 0.5 is Premium (Expensive) -> Only look for SELLS.
        - Below 0.5 is Discount (Cheap) -> Only look for BUYS.
        
        Key Formations:
        - MSS (Market Structure Shift): Failure to break the last HH/LL, first sign of potential reversal.
        - TC (Trend Change): Break of the last Major HL (in uptrend) or Major LH (in downtrend). Confirmed by candle body close.
        - SB (Structure Break): Continuation break of previous HH or LL in an already established trend.
      """.trimIndent()
    ),
    BookChapter(
      id = "supply_demand",
      bookNumber = 3,
      title = "Supply & Demand Concepts",
      subtitle = "Institutional Footprints & Order Blocks",
      keyRules = listOf(
        "Bullish Order Block (Demand): The last SELL candle before the big BUY impulse move.",
        "Bearish Order Block (Supply): The last BUY candle before the big SELL impulse move.",
        "Engulfing Requirement: The Order Block candle MUST be fully engulfed by subsequent candles.",
        "Momentum Requirement: The breakout from the OB must be strong and impulsive. Choppy breaks are invalid.",
        "Structure Causation: A valid Order Block must cause a Structure Break (SB) or Trend Change (TC).",
        "50% Equilibrium Rule: Optimal mitigation happens at the 50% midpoint of the Order Block zone.",
        "Higher Timeframe Dominance: HTF Order Blocks (4H/15M) always trump LTF (1M) zones."
      ),
      detailedExplanation = """
        Banks & Financial Institutions (BFIs) trade millions of lots. To buy 1 million lots, they need 1 million lots of sell orders.
        They engineer liquidity by creating chart patterns (double tops, trendlines) to trap retail traders.
        Once retail stops are triggered, institutions launch their true impulsive move.
        The origin candle where they accumulated sell orders before buying is the Demand Order Block (S2B - Sell to Buy).
        When price retraces into this zone, institutions mitigate their remaining drawdown and drive price higher.
      """.trimIndent()
    ),
    BookChapter(
      id = "liquidity_mastery",
      bookNumber = 4,
      title = "Introduction to Types of Liquidity",
      subtitle = "Spot the Traps Before Becoming the Liquidity",
      keyRules = listOf(
        "Liquidity Definition: Stop Losses and Stop Orders resting above highs and below lows.",
        "Equal Highs (EQH) & Equal Lows (EQL): Major liquidity magnets that banks will sweep before reversing.",
        "Trendline Liquidity: Corrective channels induce retail trendline buyers/sellers whose SLs will be hunted.",
        "Liquidity Grab (LG / Sweep): Sharp wick breaking a liquidity point followed by immediate aggressive reversal.",
        "Fake TC Trap: A false trend change on lower timeframes engineered to trick retail before the real liquidity sweep.",
        "Asian Range (01:00-05:00 GMT): High & Low of the Asian session are prime targets for London Open manipulation."
      ),
      detailedExplanation = """
        The market cannot push forever without recharging.
        Liquidity Grabs (LG) are the recharge mechanism of the financial markets.
        If you enter before the liquidity grab occurs, YOU are the liquidity!
        Always wait for the Liquidity Grab (LG) + Trend Change (TC) on the 1M chart before executing.
      """.trimIndent()
    ),
    BookChapter(
      id = "entry_systems",
      bookNumber = 5,
      title = "The Two Proven Entry Systems",
      subtitle = "SAF (Set & Forget) vs Precision Entry (Fanatic Model)",
      keyRules = listOf(
        "System 1: SAF (Set And Forget):",
        "  1. 4H Trend established (Bullish or Bearish).",
        "  2. Price is in Discount (< 50% Fib) for Buy, or Premium (> 50% Fib) for Sell.",
        "  3. 15M S&D Zone tapped (mitigated at 50%).",
        "  4. Safe SL placed behind swing point. Fixed 4R to 5R TP at weak high/low.",
        "System 2: Precision Entry (The Fanatic Model):",
        "  1. 4H Trend and P/D criteria fulfilled.",
        "  2. 15M S&D Zone tapped.",
        "  3. Drop to 1M entry timeframe.",
        "  4. Wait for Liquidity Grab (LG) sweeping early retail stops.",
        "  5. Wait for 1M Trend Change (TC) aligning with 15M & 4H.",
        "  6. Enter on 50% retest of the 1M S&D zone that caused the TC. SL tight below sweep low. Fixed 5R TP."
      ),
      detailedExplanation = """
        Top-Down Analysis Routine:
        W1/D1 -> Determine macro bias.
        4H -> Determine Major Trend, P/D Range (Fib 50%), and Major Strong/Weak boundaries.
        15M -> Identify intraday Order Blocks, minor structure alignment.
        1M -> Execute only after the Liquidity Grab + Trend Change sequence completes.
      """.trimIndent()
    ),
    BookChapter(
      id = "routine_mindset",
      bookNumber = 6,
      title = "Habits, Routines & The Trader's Mindset",
      subtitle = "Anton Kreil: 'A Professional Trader's Life is Simple & Structured'",
      keyRules = listOf(
        "Morning Routine: Wake earlier, Hydrate, Exercise, Meditate to achieve a neutral mental state.",
        "Never trade with negative emotions from prior days.",
        "Trade Windows: Trade ONLY during London Window (07:00-11:00 GMT) or NY Window (13:00-16:00 GMT).",
        "Instruments: Max 2-3 pairs (EURUSD, US30). 'Less is More.'",
        "Little Screen Time = High Quality Trades. Set alerts at Areas of Interest (AOI) and step away!",
        "Mid-Day Reset: Take a break mid-day to refresh before the New York session.",
        "End of Day: Journal ALL trades, PnL, emotions, and perform an EOD chart markup.",
        "Sleep: Prioritize 7-8 hours of sleep for razor-sharp cognitive execution."
      ),
      detailedExplanation = """
        You cannot focus and win against institutional traders with an unorganized, chaotic life.
        Think in probabilities, like a casino. Individual trade outcomes are random; long-term edge is deterministic.
        Detach from social media hype (cars, beach trading). Real trading is disciplined risk management and patience.
      """.trimIndent()
    )
  )

  val preFlightChecklist = listOf(
    "1. Higher Timeframe (4H) Trend established (Bullish/Bearish)?",
    "2. Price inside Discount (< 50% Fib) for Buy or Premium (> 50% Fib) for Sell?",
    "3. Valid 15M S&D Order Block (Engulfed candle + Impulsive Momentum + Caused SB/TC)?",
    "4. Liquidity Grab (LG) / Sweep completed (Early traders wiped out)?",
    "5. Lower Timeframe (1M) Trend Change (TC) confirmed in direction of HTF?",
    "6. Risk locked strictly to 0.25% - 1.0% with minimum 4R - 5R reward target?",
    "7. Current time is within London (07:00-11:00 GMT) or NY (13:00-16:00 GMT) window?",
    "8. Daily loss cap (1.0% / 4 losses) & Weekly loss cap (3.5%) NOT breached?"
  )
}
