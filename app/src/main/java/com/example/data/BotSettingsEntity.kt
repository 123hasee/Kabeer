package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bot_settings")
data class BotSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val balance: Double = 30000.0,
  val initialBalance: Double = 30000.0,
  val peakBalance: Double = 30000.0,
  val baseRiskPercent: Double = 0.25,          // 0.25% standard
  val maxDailyLossPercent: Double = 1.0,       // 1.0% limit -> Stop for day
  val maxWeeklyLossPercent: Double = 3.5,      // 3.5% limit -> Stop for week
  val maxDailyLossStrikes: Int = 4,            // 4 losing trades in a row
  val drawdownDeriskThreshold: Double = 4.0,   // If DD >= 4%, cut risk to 0.12%
  val deriskedRiskPercent: Double = 0.12,
  val scalingProfitThreshold: Double = 5.0,    // If profit >= 5%, scale risk to 0.50%
  val scaledRiskPercent: Double = 0.50,
  val isAutoTradingActive: Boolean = true,
  val activePair: String = "EURUSD",           // "EURUSD", "US30", "GBPUSD"
  val autoCloseAtBreakeven: Boolean = true,
  val autoPartialsAt3R: Boolean = true
) {
  val currentDrawdownPercent: Double
    get() {
      if (peakBalance <= 0) return 0.0
      val dd = ((peakBalance - balance) / peakBalance) * 100.0
      return dd.coerceAtLeast(0.0)
    }

  val totalProfitPercent: Double
    get() {
      if (initialBalance <= 0) return 0.0
      return ((balance - initialBalance) / initialBalance) * 100.0
    }

  // Effective risk calculated according to the book's strict rules
  val effectiveRiskPercent: Double
    get() {
      return when {
        currentDrawdownPercent >= drawdownDeriskThreshold -> deriskedRiskPercent // 0.12%
        totalProfitPercent >= scalingProfitThreshold && totalProfitPercent > 2.0 -> scaledRiskPercent // 0.50%
        else -> baseRiskPercent // 0.25%
      }
    }
}
