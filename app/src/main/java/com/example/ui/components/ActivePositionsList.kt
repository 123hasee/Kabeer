package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivePosition
import com.example.model.TradeDirection
import com.example.ui.theme.*

@Composable
fun ActivePositionsList(
  positions: List<ActivePosition>,
  onTakePartials: (Long) -> Unit,
  onMoveBreakeven: (Long) -> Unit,
  onClosePosition: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  if (positions.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "ACTIVE POSITIONS (${positions.size})",
      color = Slate400,
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp,
      modifier = Modifier.padding(bottom = 6.dp)
    )

    positions.forEach { pos ->
      val isBuy = pos.direction == TradeDirection.BUY
      val isProfitable = pos.pnl >= 0
      val pnlColor = if (isProfitable) Emerald400 else Rose400

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .testTag("active_position_${pos.id}")
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Row 1: Pair, Direction, PnL
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${pos.symbol} • ${pos.direction.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isBuy) Emerald400 else Rose400
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${pos.lotSize} lots",
                color = Slate400,
                fontSize = 11.sp
              )
            }

            // PnL & R multiple
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${if (pos.pnl >= 0) "+" else ""}$${String.format("%.2f", pos.pnl)}",
                color = pnlColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "${if (pos.currentR >= 0) "+" else ""}${String.format("%.1f", pos.currentR)}R",
                color = if (pos.currentR >= 3.0) Emerald400 else if (pos.currentR < 0) Rose400 else Slate300,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Row 2: Entry, Current, SL, TP
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Entry: ${String.format(if (pos.symbol == "US30") "%.0f" else "%.5f", pos.entryPrice)}",
              color = Slate400,
              fontSize = 10.sp
            )
            Text(
              text = "Now: ${String.format(if (pos.symbol == "US30") "%.0f" else "%.5f", pos.currentPrice)}",
              color = Slate100,
              fontSize = 10.sp
            )
            Text(
              text = "SL: ${String.format(if (pos.symbol == "US30") "%.0f" else "%.5f", pos.stopLoss)}",
              color = Rose400,
              fontSize = 10.sp
            )
            Text(
              text = "TP (5R): ${String.format(if (pos.symbol == "US30") "%.0f" else "%.5f", pos.takeProfit)}",
              color = Emerald400,
              fontSize = 10.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Row 3: Action buttons (Partials, BE, Close)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Partials button
            OutlinedButton(
              onClick = { onTakePartials(pos.id) },
              enabled = !pos.isPartialsTaken && pos.currentR >= 2.5,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(imageVector = Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (pos.isPartialsTaken) "Partials Taken" else "Take 3R Partials",
                fontSize = 10.sp
              )
            }

            // Breakeven button
            OutlinedButton(
              onClick = { onMoveBreakeven(pos.id) },
              enabled = !pos.isBreakeven,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (pos.isBreakeven) "Locked at BE" else "Lock BE (0 Risk)",
                fontSize = 10.sp
              )
            }

            // Close button
            Button(
              onClick = { onClosePosition(pos.id) },
              colors = ButtonDefaults.buttonColors(containerColor = Slate800),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.weight(0.7f)
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text(text = "Close", fontSize = 10.sp)
            }
          }
        }
      }
    }
  }
}
