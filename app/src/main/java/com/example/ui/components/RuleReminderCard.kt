package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.example.model.TradeWindowSession
import com.example.ui.theme.*

@Composable
fun RuleReminderCard(
  settings: BotSettingsEntity,
  dailyLossPercent: Double,
  dailyLossStreak: Int,
  currentWindow: TradeWindowSession,
  circuitBreakerReason: String?,
  onViewAllRules: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(
        if (circuitBreakerReason != null) Rose500 else Slate800
      )
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("rule_reminder_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Risk Guardian",
            tint = if (circuitBreakerReason != null) Rose500 else Emerald500,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Risk Guardian & Rules Engine",
            color = Slate100,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        TextButton(
          onClick = onViewAllRules,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.testTag("rulebook_button")
        ) {
          Text(text = "View Rulebook", color = Blue500, fontSize = 12.sp)
        }
      }

      // Circuit Breaker Halt Alert if active
      if (circuitBreakerReason != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Rose500.copy(alpha = 0.15f))
            .border(1.dp, Rose500.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("circuit_breaker_banner")
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Alert",
            tint = Rose500,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = circuitBreakerReason,
            color = Rose400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4 Rule Gauges in a 2x2 grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Daily Loss Cap
        RuleMetricChip(
          title = "Daily Loss Limit",
          current = "${String.format("%.2f", dailyLossPercent)}%",
          limit = "Max ${settings.maxDailyLossPercent}% (${dailyLossStreak}/${settings.maxDailyLossStrikes} strikes)",
          isWarning = dailyLossPercent >= (settings.maxDailyLossPercent * 0.7),
          modifier = Modifier.weight(1f)
        )

        // Effective Risk
        RuleMetricChip(
          title = "Active Risk / Trade",
          current = "${String.format("%.2f", settings.effectiveRiskPercent)}%",
          limit = if (settings.effectiveRiskPercent <= 0.15) "De-risked (<4% DD)" else if (settings.effectiveRiskPercent >= 0.40) "Scaling (+5% Profit)" else "Standard 0.25%",
          isWarning = false,
          accentColor = if (settings.effectiveRiskPercent <= 0.15) Amber500 else Emerald500,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Trade Window
        RuleMetricChip(
          title = "Trade Window",
          current = currentWindow.title,
          limit = currentWindow.gmtTimeRange,
          isWarning = !currentWindow.isTradeAllowed,
          accentColor = if (currentWindow.isTradeAllowed) Emerald500 else Amber500,
          leadingIcon = Icons.Default.AccessTime,
          modifier = Modifier.weight(1f)
        )

        // Drawdown & Profit
        RuleMetricChip(
          title = "Drawdown / Profit",
          current = "-${String.format("%.1f", settings.currentDrawdownPercent)}% / +${String.format("%.1f", settings.totalProfitPercent)}%",
          limit = "Derisk trigger at 4.0% DD",
          isWarning = settings.currentDrawdownPercent >= 3.5,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun RuleMetricChip(
  title: String,
  current: String,
  limit: String,
  isWarning: Boolean,
  accentColor: Color = Slate100,
  leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Slate800)
      .padding(8.dp)
  ) {
    Text(text = title, color = Slate400, fontSize = 10.sp)
    Spacer(modifier = Modifier.height(2.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = if (isWarning) Rose500 else accentColor,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = current,
        color = if (isWarning) Rose500 else accentColor,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        maxLines = 1
      )
    }
    Spacer(modifier = Modifier.height(1.dp))
    Text(
      text = limit,
      color = Slate400,
      fontSize = 9.sp,
      maxLines = 1
    )
  }
}
