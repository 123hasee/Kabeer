package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.example.model.Candle
import com.example.model.OrderBlock
import com.example.model.OrderBlockType
import com.example.model.PDRange
import com.example.ui.theme.*

@Composable
fun CandlestickChart(
  candles: List<Candle>,
  orderBlocks: List<OrderBlock>,
  pdRange: PDRange?,
  symbol: String,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(260.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(Slate900)
      .testTag("candlestick_chart_container")
  ) {
    if (candles.isEmpty()) {
      Text(
        text = "Loading market data for $symbol...",
        color = Slate400,
        modifier = Modifier.align(Alignment.Center)
      )
      return@Box
    }

    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp, vertical = 12.dp)
        .testTag("candlestick_canvas")
    ) {
      val canvasWidth = size.width
      val canvasHeight = size.height
      val rightAxisWidth = 60f
      val plotWidth = canvasWidth - rightAxisWidth

      val minPrice = candles.minOf { it.low }
      val maxPrice = candles.maxOf { it.high }
      val priceSpan = (maxPrice - minPrice).coerceAtLeast(0.0001)

      fun priceToY(price: Double): Float {
        val normalized = (price - minPrice) / priceSpan
        return (canvasHeight - (normalized * canvasHeight)).toFloat()
      }

      // Draw subtle horizontal grid lines (3 lines)
      val gridSteps = 4
      for (i in 0..gridSteps) {
        val p = minPrice + (priceSpan / gridSteps) * i
        val y = priceToY(p)
        drawLine(
          color = Slate800,
          start = Offset(0f, y),
          end = Offset(plotWidth, y),
          strokeWidth = 1f
        )
        // Price label on axis
        val formatted = if (symbol == "US30") String.format("%.0f", p) else String.format("%.5f", p)
        drawText(
          textMeasurer = textMeasurer,
          text = formatted,
          topLeft = Offset(plotWidth + 8f, y - 8f),
          style = TextStyle(color = Slate400, fontSize = 9.sp)
        )
      }

      // Draw Fib 50% Equilibrium & Premium/Discount zone if available
      pdRange?.let { range ->
        val eqY = priceToY(range.equilibrium)
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        // Equilibrium line
        drawLine(
          color = EquilibriumColor.copy(alpha = 0.8f),
          start = Offset(0f, eqY),
          end = Offset(plotWidth, eqY),
          strokeWidth = 2f,
          pathEffect = dashedEffect
        )

        drawText(
          textMeasurer = textMeasurer,
          text = "50% EQ (${String.format(if (symbol == "US30") "%.0f" else "%.4f", range.equilibrium)})",
          topLeft = Offset(8f, eqY - 16f),
          style = TextStyle(color = EquilibriumColor, fontSize = 9.sp)
        )

        // Premium tag at top
        drawText(
          textMeasurer = textMeasurer,
          text = "PREMIUM (Sell Zone)",
          topLeft = Offset(plotWidth - 110f, 6f),
          style = TextStyle(color = Rose400.copy(alpha = 0.6f), fontSize = 8.sp)
        )

        // Discount tag at bottom
        drawText(
          textMeasurer = textMeasurer,
          text = "DISCOUNT (Buy Zone)",
          topLeft = Offset(plotWidth - 110f, canvasHeight - 20f),
          style = TextStyle(color = Emerald400.copy(alpha = 0.6f), fontSize = 8.sp)
        )
      }

      // Draw Order Blocks (OB)
      for (ob in orderBlocks) {
        val topY = priceToY(ob.topPrice)
        val bottomY = priceToY(ob.bottomPrice)
        val obHeight = (bottomY - topY).coerceAtLeast(4f)
        val midY = priceToY(ob.equilibrium50)

        val obColor = if (ob.type == OrderBlockType.BULLISH_DEMAND) DemandZoneColor else SupplyZoneColor
        val obBorder = if (ob.type == OrderBlockType.BULLISH_DEMAND) DemandZoneBorder else SupplyZoneBorder

        drawRect(
          color = obColor,
          topLeft = Offset(0f, topY),
          size = Size(plotWidth, obHeight)
        )

        drawRect(
          color = obBorder,
          topLeft = Offset(0f, topY),
          size = Size(plotWidth, obHeight),
          style = Stroke(width = 1.5f)
        )

        // 50% OB equilibrium line
        drawLine(
          color = obBorder.copy(alpha = 0.8f),
          start = Offset(0f, midY),
          end = Offset(plotWidth, midY),
          strokeWidth = 1f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

        val obTitle = if (ob.type == OrderBlockType.BULLISH_DEMAND) "15M Demand OB (50% Entry)" else "15M Supply OB (50% Entry)"
        drawText(
          textMeasurer = textMeasurer,
          text = obTitle,
          topLeft = Offset(8f, topY + 2f),
          style = TextStyle(color = obBorder, fontSize = 9.sp)
        )
      }

      // Draw Candlesticks
      val candleCount = candles.size
      val candleSpacing = plotWidth / candleCount
      val candleBodyWidth = (candleSpacing * 0.65f).coerceAtLeast(3f)

      candles.forEachIndexed { index, candle ->
        val xCenter = (index * candleSpacing) + (candleSpacing / 2f)
        val highY = priceToY(candle.high)
        val lowY = priceToY(candle.low)
        val openY = priceToY(candle.open)
        val closeY = priceToY(candle.close)

        val candleColor = if (candle.isBullish) Emerald500 else Rose500

        // Wick
        drawLine(
          color = candleColor,
          start = Offset(xCenter, highY),
          end = Offset(xCenter, lowY),
          strokeWidth = 1.5f
        )

        // Body
        val bodyTopY = minOf(openY, closeY)
        val bodyHeight = (abs(openY - closeY)).coerceAtLeast(2f)

        drawRect(
          color = candleColor,
          topLeft = Offset(xCenter - (candleBodyWidth / 2f), bodyTopY),
          size = Size(candleBodyWidth, bodyHeight)
        )
      }
    }
  }
}
