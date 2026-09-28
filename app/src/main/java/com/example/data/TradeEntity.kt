package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trades")
data class TradeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long,
  val symbol: String,
  val direction: String,        // "BUY" or "SELL"
  val strategy: String,         // "SAF" or "PRECISION"
  val entryPrice: Double,
  val stopLoss: Double,
  val takeProfit: Double,
  val exitPrice: Double = 0.0,
  val lotSize: Double,
  val riskPercent: Double,      // e.g. 0.25
  val pnl: Double = 0.0,
  val rMultiple: Double = 0.0,  // e.g. +5.0 or -1.0
  val status: String = "CLOSED", // "OPEN" or "CLOSED"
  val isRuleCompliant: Boolean = true,
  val emotionTag: String = "Neutral", // "Neutral", "Disciplined", "FOMO", "Fear", "Greedy"
  val exitReason: String = "TP Hit (5R)",
  val notes: String = ""
)
