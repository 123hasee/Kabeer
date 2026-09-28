package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.model.TradeDirection
import com.example.model.TradingSignal
import com.example.ui.theme.*

@Composable
fun TradingSignalCard(
  signal: TradingSignal,
  onExecute: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isBuy = signal.direction == TradeDirection.BUY
  val dirColor = if (isBuy) Emerald500 else Rose500

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(dirColor.copy(alpha = 0.5f))
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("trading_signal_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: Symbol, Direction, Model
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(dirColor.copy(alpha = 0.2f))
              .border(1.dp, dirColor, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${signal.direction.name} ${signal.symbol}",
              color = dirColor,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = signal.strategy.title,
            color = Slate300,
            fontSize = 11.sp
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Slate400)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Parameters Row: Entry, SL, TP, Target R
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        ParamColumn("Entry", String.format(if (signal.symbol == "US30") "%.0f" else "%.5f", signal.entryPrice), Slate100)
        ParamColumn("Stop Loss", String.format(if (signal.symbol == "US30") "%.0f" else "%.5f", signal.stopLoss), Rose400)
        ParamColumn("Take Profit", String.format(if (signal.symbol == "US30") "%.0f" else "%.5f", signal.takeProfit), Emerald400)
        ParamColumn("Target", "${signal.targetR}R (${String.format("%.1f", signal.pipsRisk)} pips)", Blue500)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // SMC Rules Checklist
      Text(
        text = "SMC RULES VERIFICATION (${signal.reasonChecklist.count { it.isPassed }}/${signal.reasonChecklist.size} Passed):",
        color = Slate400,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        signal.reasonChecklist.forEach { check ->
          Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = if (check.isPassed) Icons.Default.Check else Icons.Default.Close,
              contentDescription = null,
              tint = if (check.isPassed) Emerald500 else Rose500,
              modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = check.ruleName,
                color = if (check.isPassed) Slate100 else Rose400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = check.detail,
                color = Slate400,
                fontSize = 10.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Execute button
      Button(
        onClick = onExecute,
        colors = ButtonDefaults.buttonColors(containerColor = if (signal.isExecutable) dirColor else Slate700),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("execute_signal_button")
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (signal.isExecutable) "Execute Trade Following All Rules" else "Warning: Not All Rules Passed (Execute Anyway)",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }
    }
  }
}

@Composable
private fun ParamColumn(label: String, value: String, valueColor: Color) {
  Column {
    Text(text = label, color = Slate400, fontSize = 10.sp)
    Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
  }
}
