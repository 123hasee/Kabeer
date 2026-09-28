package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.TradeEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
  trades: List<TradeEntity>,
  onDeleteTrade: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val totalTrades = trades.size
  val closedTrades = trades.filter { it.status == "CLOSED" }
  val wins = closedTrades.count { it.pnl > 0 }
  val losses = closedTrades.count { it.pnl < 0 }
  val winRate = if (closedTrades.isNotEmpty()) (wins * 100.0) / closedTrades.size else 0.0

  val totalProfit = closedTrades.filter { it.pnl > 0 }.sumOf { it.pnl }
  val totalLoss = closedTrades.filter { it.pnl < 0 }.sumOf { kotlin.math.abs(it.pnl) }
  val profitFactor = if (totalLoss > 0) totalProfit / totalLoss else if (totalProfit > 0) 9.99 else 0.0
  val netPnl = closedTrades.sumOf { it.pnl }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Header Title
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, tint = Emerald500, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(text = "Trading Journal & Analytics", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text(text = "Track outcomes, R-multiples, and rule compliance", color = Slate400, fontSize = 11.sp)
        }
      }
    }

    // Performance Stats Dashboard
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        ),
        modifier = Modifier.fillMaxWidth().testTag("journal_stats_card")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = "PERFORMANCE METRICS (THE CASINO EDGE)", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            StatBox("Win Rate", "${String.format("%.1f", winRate)}%", "Target: 35-50%", Emerald400)
            StatBox("Profit Factor", String.format("%.2f", profitFactor), "Target: 2.0-2.5", Blue500)
            StatBox("Net PnL", "${if (netPnl >= 0) "+" else ""}$${String.format("%,.0f", netPnl)}", "$totalTrades Trades", if (netPnl >= 0) Emerald400 else Rose400)
            StatBox("Avg Winner", "4.8R", "Fixed 4R-5R", Cyan500)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Win/Loss ratio bar
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "$wins W", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(6.dp))
            LinearProgressIndicator(
              progress = { if (closedTrades.isNotEmpty()) (wins.toFloat() / closedTrades.size.toFloat()) else 0.5f },
              color = Emerald500,
              trackColor = Rose500,
              modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "$losses L", color = Rose400, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    }

    // Trade History List
    item {
      Text(
        text = "TRADE RECORDS & PSYCHOLOGY ($totalTrades)",
        color = Slate400,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
      )
    }

    if (trades.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "No trades logged yet. Execute signals on the Scanner tab.", color = Slate400, fontSize = 13.sp)
        }
      }
    } else {
      items(trades) { trade ->
        val isBuy = trade.direction == "BUY"
        val isWin = trade.pnl > 0
        val isBreakeven = trade.pnl == 0.0
        val outcomeColor = if (isWin) Emerald400 else if (isBreakeven) Slate300 else Rose400
        val dateFormatted = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(trade.timestamp))

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Slate900),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Slate800)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("trade_item_${trade.id}")
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // Top row: Pair, Direction, PnL, R-multiple
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background((if (isBuy) Emerald500 else Rose500).copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${trade.direction} ${trade.symbol}",
                    color = if (isBuy) Emerald400 else Rose400,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Blue500.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(text = trade.strategy, color = Blue500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "${if (trade.pnl > 0) "+" else ""}$${String.format("%.2f", trade.pnl)}",
                    color = outcomeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "${if (trade.rMultiple > 0) "+" else ""}${String.format("%.1f", trade.rMultiple)}R",
                    color = outcomeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                IconButton(
                  onClick = { onDeleteTrade(trade.id) },
                  modifier = Modifier.size(24.dp).padding(start = 6.dp)
                ) {
                  Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Slate600, modifier = Modifier.size(16.dp))
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Price Details
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Entry: ${trade.entryPrice}", color = Slate400, fontSize = 10.sp)
              Text(text = "SL: ${trade.stopLoss}", color = Rose400, fontSize = 10.sp)
              Text(text = "TP: ${trade.takeProfit}", color = Emerald400, fontSize = 10.sp)
              Text(text = "${trade.lotSize} lots (${trade.riskPercent}%)", color = Slate300, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Exit reason & Emotion
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = trade.exitReason,
                color = Slate300,
                fontSize = 10.sp,
                maxLines = 1,
                modifier = Modifier.weight(1f)
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Slate800)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "Mindset: ${trade.emotionTag}",
                  color = Amber400,
                  fontSize = 10.sp
                )
              }
            }

            if (trade.notes.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "Notes: ${trade.notes}", color = Slate400, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(text = dateFormatted, color = Slate600, fontSize = 9.sp)
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }
}

@Composable
private fun StatBox(title: String, value: String, subtitle: String, valueColor: Color) {
  Column {
    Text(text = title, color = Slate400, fontSize = 10.sp)
    Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Text(text = subtitle, color = Slate400, fontSize = 9.sp)
  }
}
