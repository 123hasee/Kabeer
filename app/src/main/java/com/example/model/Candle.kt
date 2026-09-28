package com.example.model

data class Candle(
  val time: Long,
  val open: Double,
  val high: Double,
  val low: Double,
  val close: Double,
  val volume: Double = 100.0
) {
  val isBullish: Boolean get() = close >= open
  val bodyTop: Double get() = maxOf(open, close)
  val bodyBottom: Double get() = minOf(open, close)
  val bodyRange: Double get() = (bodyTop - bodyBottom).coerceAtLeast(0.00001)
  val totalRange: Double get() = (high - low).coerceAtLeast(0.00001)
  val upperWick: Double get() = high - bodyTop
  val lowerWick: Double get() = bodyBottom - low
}
