package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoutineEntity
import com.example.model.TradeWindowSession
import com.example.ui.theme.*

@Composable
fun RoutineScreen(
  routine: RoutineEntity,
  currentWindow: TradeWindowSession,
  onToggleRoutine: (String) -> Unit,
  onSetMindsetRating: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val completion = routine.completionPercent

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
        Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = null, tint = Emerald500, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(text = "Daily Routine & Habits", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text(text = "Anton Kreil: 'A professional trader\'s life is Simple & Structured'", color = Slate400, fontSize = 11.sp, fontStyle = FontStyle.Italic)
        }
      }
    }

    // Daily Completion Progress Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(if (completion >= 80) Emerald500 else Blue500)
        ),
        modifier = Modifier.fillMaxWidth().testTag("routine_progress_card")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Text(text = "DAILY DISCIPLINE SCORE", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text(
                text = "$completion% COMPLETED",
                color = if (completion >= 80) Emerald400 else Slate100,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (completion >= 80) Emerald500.copy(alpha = 0.2f) else Slate800)
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = if (completion >= 80) "DISCIPLINED" else "IN PROGRESS",
                color = if (completion >= 80) Emerald400 else Slate300,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          LinearProgressIndicator(
            progress = { completion / 100f },
            color = if (completion >= 80) Emerald500 else Blue500,
            trackColor = Slate800,
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
          )
        }
      }
    }

    // Mindset Rating (1-5 Stars)
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = "EMOTIONAL & MENTAL STATE CHECK-IN", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Are you calm, neutral, and detached from fear/greed?",
            color = Slate300,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            listOf(
              1 to "Fearful",
              2 to "Anxious",
              3 to "Neutral",
              4 to "Focused",
              5 to "Mastery"
            ).forEach { (rating, label) ->
              val isSelected = routine.mindsetRating == rating
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clickable { onSetMindsetRating(rating) }
                  .padding(4.dp)
              ) {
                Icon(
                  imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = label,
                  tint = if (isSelected) Amber400 else Slate600,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = label,
                  color = if (isSelected) Amber400 else Slate400,
                  fontSize = 9.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }
      }
    }

    // Daily Routine Checklist Items
    item {
      Text(text = "THE 4 PHASES ROUTINE:", color = Slate400, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }

    item {
      RoutineCheckCard(
        phase = "1. Morning Routine",
        title = "Hydration & Energy",
        desc = "Wake earlier, hydrate, light movement. Do NOT stare at screens immediately.",
        isChecked = routine.hydrateDone,
        icon = Icons.Default.WaterDrop,
        onToggle = { onToggleRoutine("hydrate") }
      )
    }

    item {
      RoutineCheckCard(
        phase = "1. Morning Routine",
        title = "Mindset Neutralization (10m Meditation)",
        desc = "Achieve a neutral psychological state. Negative emotions lead to revenge trading.",
        isChecked = routine.meditateDone,
        icon = Icons.Default.SelfImprovement,
        onToggle = { onToggleRoutine("meditate") }
      )
    }

    item {
      RoutineCheckCard(
        phase = "2. Chart Prep",
        title = "Top-Down Markup (4H & 15M)",
        desc = "Identify 4H major trend, draw 0.5 Fib P/D range, mark clear 15M S&D zones.",
        isChecked = routine.chartPrepDone,
        icon = Icons.Default.ShowChart,
        onToggle = { onToggleRoutine("chartPrep") }
      )
    }

    item {
      RoutineCheckCard(
        phase = "2. Chart Prep",
        title = "Set Alerts at Areas of Interest (AOI)",
        desc = "'Little screen time = High quality trades'. Set alerts and step away until pinged.",
        isChecked = routine.alertsSet,
        icon = Icons.Default.NotificationsActive,
        onToggle = { onToggleRoutine("alertsSet") }
      )
    }

    item {
      RoutineCheckCard(
        phase = "3. In-Session Execution",
        title = "Trade Windows Strict Adherence",
        desc = "Trade only during Frankfurt/London (07:00-11:00 GMT) or NY (13:00-16:00 GMT). Current: ${currentWindow.title}.",
        isChecked = routine.tradeWindowObserved,
        icon = Icons.Default.AccessTime,
        onToggle = { onToggleRoutine("tradeWindow") }
      )
    }

    item {
      RoutineCheckCard(
        phase = "3. Mid-Day Reset",
        title = "Mid-Day Step-Back & Refresh",
        desc = "Take a break after London lunch. Clear mind before the New York opening bell.",
        isChecked = routine.midDayBreakDone,
        icon = Icons.Default.Coffee,
        onToggle = { onToggleRoutine("midDayBreak") }
      )
    }

    item {
      RoutineCheckCard(
        phase = "4. End of Day Habits",
        title = "Trade Journal & PnL Logging",
        desc = "Log all trade executions, R-multiples, psychological emotions, and lessons learned.",
        isChecked = routine.eodJournalDone,
        icon = Icons.Default.FactCheck,
        onToggle = { onToggleRoutine("eodJournal") }
      )
    }

    item {
      RoutineCheckCard(
        phase = "4. End of Day Habits",
        title = "EOD Chart Markup Review",
        desc = "Re-analyze today's price action. Spot setups you missed or took impulsively.",
        isChecked = routine.eodMarkupDone,
        icon = Icons.Default.RateReview,
        onToggle = { onToggleRoutine("eodMarkup") }
      )
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }
}

@Composable
private fun RoutineCheckCard(
  phase: String,
  title: String,
  desc: String,
  isChecked: Boolean,
  icon: ImageVector,
  onToggle: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(if (isChecked) Emerald500.copy(alpha = 0.5f) else Slate800)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
      .testTag("routine_item_${title.take(10)}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(12.dp)
    ) {
      Checkbox(
        checked = isChecked,
        onCheckedChange = { onToggle() },
        colors = CheckboxDefaults.colors(checkedColor = Emerald500, uncheckedColor = Slate600)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = phase.uppercase(), color = Blue500, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(
          text = title,
          color = if (isChecked) Slate100 else Slate300,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
        Text(text = desc, color = Slate400, fontSize = 10.sp, lineHeight = 14.sp)
      }
      Icon(imageVector = icon, contentDescription = null, tint = if (isChecked) Emerald500 else Slate600, modifier = Modifier.size(20.dp))
    }
  }
}
