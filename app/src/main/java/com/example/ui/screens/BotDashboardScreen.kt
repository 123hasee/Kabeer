package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.BotSettingsEntity
import com.example.model.*
import com.example.ui.components.ActivePositionsList
import com.example.ui.components.CandlestickChart
import com.example.ui.components.RuleReminderCard
import com.example.ui.components.TradingSignalCard
import com.example.ui.theme.*

@Composable
fun BotDashboardScreen(
  settings: BotSettingsEntity,
  selectedPair: String,
  selectedTimeframe: Timeframe,
  candles: List<Candle>,
  orderBlocks: List<OrderBlock>,
  pdRange: PDRange?,
  currentSignal: TradingSignal?,
  activePositions: List<ActivePosition>,
  currentWindow: TradeWindowSession,
  dailyLossPercent: Double,
  dailyLossStreak: Int,
  circuitBreakerReason: String?,
  onToggleAutoTrading: () -> Unit,
  onSelectPair: (String) -> Unit,
  onSelectTimeframe: (Timeframe) -> Unit,
  onScanForSignal: () -> Unit,
  onExecuteSignal: () -> Unit,
  onDismissSignal: () -> Unit,
  onTakePartials: (Long) -> Unit,
  onMoveBreakeven: (Long) -> Unit,
  onClosePosition: (Long) -> Unit,
  onViewAllRules: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Header: Account Balance & Bot Status Toggle
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(Slate800)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Text(text = "TOTAL ACCOUNT BALANCE", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              Text(
                text = "$${String.format("%,.2f", settings.balance)}",
                color = Slate100,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${if (settings.totalProfitPercent >= 0) "+" else ""}${String.format("%.2f", settings.totalProfitPercent)}% total return",
                  color = if (settings.totalProfitPercent >= 0) Emerald400 else Rose400,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "• Peak: $${String.format("%,.0f", settings.peakBalance)}",
                  color = Slate400,
                  fontSize = 11.sp
                )
              }
            }

            // Bot Auto Trading Toggle Switch
            Column(horizontalAlignment = Alignment.End) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (settings.isAutoTradingActive) Emerald500 else Slate600)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (settings.isAutoTradingActive) "BOT ACTIVE" else "PAUSED",
                  color = if (settings.isAutoTradingActive) Emerald400 else Slate400,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              }
              Switch(
                checked = settings.isAutoTradingActive,
                onCheckedChange = { onToggleAutoTrading() },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Emerald500,
                  checkedTrackColor = Emerald500.copy(alpha = 0.3f),
                  uncheckedThumbColor = Slate400,
                  uncheckedTrackColor = Slate800
                ),
                modifier = Modifier.testTag("auto_trading_switch")
              )
            }
          }
        }
      }
    }

    // Rule Reminder Guardian Card
    item {
      RuleReminderCard(
        settings = settings,
        dailyLossPercent = dailyLossPercent,
        dailyLossStreak = dailyLossStreak,
        currentWindow = currentWindow,
        circuitBreakerReason = circuitBreakerReason,
        onViewAllRules = onViewAllRules
      )
    }

    // Pair and Timeframe Selector Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Pairs (EURUSD, US30, GBPUSD)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("EURUSD", "US30", "GBPUSD").forEach { pair ->
            val isSelected = pair == selectedPair
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Blue500 else Slate900)
                .clickable { onSelectPair(pair) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("pair_selector_$pair")
            ) {
              Text(
                text = pair,
                color = if (isSelected) Color.White else Slate300,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }
        }

        // Timeframes (4H, 15M, 1M)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Timeframe.values().forEach { tf ->
            val isSelected = tf == selectedTimeframe
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Emerald500 else Slate900)
                .clickable { onSelectTimeframe(tf) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("tf_selector_${tf.name}")
            ) {
              Text(
                text = tf.label,
                color = if (isSelected) Color.Black else Slate300,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }

    // Candlestick Chart with SMC Overlays
    item {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
          Text(
            text = "$selectedPair • ${selectedTimeframe.label} (${selectedTimeframe.role})",
            color = Slate300,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )

          Button(
            onClick = onScanForSignal,
            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp).testTag("scan_signal_button")
          ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Scan Setup", fontSize = 11.sp)
          }
        }

        CandlestickChart(
          candles = candles,
          orderBlocks = orderBlocks,
          pdRange = pdRange,
          symbol = selectedPair
        )
      }
    }

    // Active Signal Card if generated
    item {
      currentSignal?.let { signal ->
        TradingSignalCard(
          signal = signal,
          onExecute = onExecuteSignal,
          onDismiss = onDismissSignal
        )
      }
    }

    // Active Positions
    item {
      ActivePositionsList(
        positions = activePositions,
        onTakePartials = onTakePartials,
        onMoveBreakeven = onMoveBreakeven,
        onClosePosition = onClosePosition
      )
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }
}
