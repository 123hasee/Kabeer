package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BotSettingsEntity
import com.example.model.BookRulesData
import com.example.model.TradeWindowSession
import com.example.ui.theme.*

@Composable
fun RuleGuardianScreen(
  settings: BotSettingsEntity,
  dailyLossPercent: Double,
  dailyLossStreak: Int,
  currentWindow: TradeWindowSession,
  circuitBreakerReason: String?,
  checklistState: Map<String, Boolean>,
  selectedChapterId: String?,
  onToggleChecklistItem: (String) -> Unit,
  onResetChecklist: () -> Unit,
  onSelectChapter: (String?) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Book Chapters, 1: Pre-Trade Checklist, 2: Risk Parameters

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
      .padding(horizontal = 14.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Header Title
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        imageVector = Icons.Default.MenuBook,
        contentDescription = null,
        tint = Emerald500,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = "Trading Fanatic Rulebook & Guardian",
          color = Slate100,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
        Text(
          text = "Strict regulations & knowledge from all 4 books",
          color = Slate400,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Sub Tabs: [Books Library, Pre-Flight Checklist, Live Limits]
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Slate900,
      contentColor = Emerald500,
      modifier = Modifier.clip(RoundedCornerShape(10.dp))
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("The Books", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Pre-Trade Checklist", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text("Risk Circuit Breakers", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (selectedTab) {
      0 -> {
        // Tab 0: The 4 Books Chapters
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(BookRulesData.chapters) { chapter ->
            val isExpanded = selectedChapterId == chapter.id
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Slate900),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                  if (isExpanded) Blue500.copy(alpha = 0.6f) else Slate800
                )
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectChapter(if (isExpanded) null else chapter.id)
                }
                .testTag("chapter_card_${chapter.id}")
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Blue500.copy(alpha = 0.2f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "${chapter.bookNumber}",
                        color = Blue500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = chapter.title,
                        color = Slate100,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                      Text(
                        text = chapter.subtitle,
                        color = Slate400,
                        fontSize = 11.sp
                      )
                    }
                  }

                  Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Slate400
                  )
                }

                AnimatedVisibility(visible = isExpanded) {
                  Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                      text = "CORE RULES & REGULATIONS:",
                      color = Emerald400,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    chapter.keyRules.forEach { rule ->
                      Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                      ) {
                        Text("• ", color = Blue500, fontWeight = FontWeight.Bold)
                        Text(text = rule, color = Slate300, fontSize = 12.sp)
                      }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                      text = "DETAILED TEACHING:",
                      color = Amber400,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = chapter.detailedExplanation,
                      color = Slate300,
                      fontSize = 12.sp,
                      lineHeight = 18.sp
                    )
                  }
                }
              }
            }
          }
          item { Spacer(modifier = Modifier.height(20.dp)) }
        }
      }

      1 -> {
        // Tab 1: Pre-Flight Trade Verification Checklist
        val passedCount = checklistState.values.count { it }
        val totalCount = checklistState.size
        val allPassed = passedCount == totalCount

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Slate900),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (allPassed) Emerald500 else Amber500)
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
                    Text(
                      text = "CHECKLIST READINESS: $passedCount / $totalCount",
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = if (allPassed) Emerald400 else Amber400
                    )
                    Text(
                      text = if (allPassed) "All book rules satisfied! Clear to trade." else "Mandatory: Tick all rules before taking trade.",
                      color = Slate400,
                      fontSize = 11.sp
                    )
                  }

                  TextButton(onClick = onResetChecklist) {
                    Text(text = "Reset", color = Blue500, fontSize = 12.sp)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                  progress = { if (totalCount > 0) passedCount.toFloat() / totalCount.toFloat() else 0f },
                  color = if (allPassed) Emerald500 else Amber500,
                  trackColor = Slate800,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                )
              }
            }
          }

          items(checklistState.keys.toList()) { ruleText ->
            val isChecked = checklistState[ruleText] ?: false
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = if (isChecked) Slate900 else Slate900.copy(alpha = 0.7f)),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (isChecked) Emerald500.copy(alpha = 0.5f) else Slate800)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleChecklistItem(ruleText) }
                .testTag("checklist_item_${ruleText.take(15)}")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
              ) {
                Checkbox(
                  checked = isChecked,
                  onCheckedChange = { onToggleChecklistItem(ruleText) },
                  colors = CheckboxDefaults.colors(
                    checkedColor = Emerald500,
                    uncheckedColor = Slate600
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = ruleText,
                  color = if (isChecked) Slate100 else Slate400,
                  fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                  fontSize = 12.sp
                )
              }
            }
          }
          item { Spacer(modifier = Modifier.height(20.dp)) }
        }
      }

      2 -> {
        // Tab 2: Risk Parameters & Circuit Breakers Status
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            CircuitBreakerStatusCard(
              title = "Daily Loss Circuit Breaker",
              ruleDesc = "Max 1.0% loss or 4 consecutive losses in a day. When reached, you MUST stop trading for the day!",
              status = "${String.format("%.2f", dailyLossPercent)}% loss / ${dailyLossStreak} losses",
              limit = "Limit: ${settings.maxDailyLossPercent}% / ${settings.maxDailyLossStrikes} strikes",
              isBreached = dailyLossPercent >= settings.maxDailyLossPercent || dailyLossStreak >= settings.maxDailyLossStrikes
            )
          }

          item {
            CircuitBreakerStatusCard(
              title = "Weekly Loss Circuit Breaker",
              ruleDesc = "Max 3.5% loss in a single calendar week (14 consecutive losses). Protects capital from multi-day drawdown.",
              status = "Safe (< 3.5%)",
              limit = "Limit: ${settings.maxWeeklyLossPercent}%",
              isBreached = false
            )
          }

          item {
            CircuitBreakerStatusCard(
              title = "Drawdown Flatlining Rule",
              ruleDesc = "Whenever balance falls below 4.0% drawdown, immediately reduce risk per trade from 0.25% down to 0.12% to avoid digging a deeper hole.",
              status = "-${String.format("%.2f", settings.currentDrawdownPercent)}% current DD",
              limit = "Threshold: ${settings.drawdownDeriskThreshold}% DD",
              isBreached = settings.currentDrawdownPercent >= settings.drawdownDeriskThreshold
            )
          }

          item {
            CircuitBreakerStatusCard(
              title = "Profit Scaling Rule",
              ruleDesc = "Whenever balance is above +5.0% profit, you are allowed to increase risk to 0.50% as long as you maintain > 2.0% profit.",
              status = "+${String.format("%.2f", settings.totalProfitPercent)}% total profit",
              limit = "Unlock: ${settings.scalingProfitThreshold}% profit",
              isBreached = false,
              isPositive = settings.totalProfitPercent >= settings.scalingProfitThreshold
            )
          }

          item {
            CircuitBreakerStatusCard(
              title = "Session Trade Windows",
              ruleDesc = "Frankfurt Open (07:00 GMT) to London Lunch (11:00 GMT), and New York Window (13:00 to 16:00 GMT). Trading outside these hours is strictly discouraged.",
              status = "${currentWindow.title} (${currentWindow.gmtTimeRange})",
              limit = if (currentWindow.isTradeAllowed) "Trade Window Active" else "Market Closed / Spread Hours",
              isBreached = !currentWindow.isTradeAllowed
            )
          }
          item { Spacer(modifier = Modifier.height(20.dp)) }
        }
      }
    }
  }
}

@Composable
private fun CircuitBreakerStatusCard(
  title: String,
  ruleDesc: String,
  status: String,
  limit: String,
  isBreached: Boolean,
  isPositive: Boolean = false
) {
  val accent = when {
    isBreached -> Rose500
    isPositive -> Emerald500
    else -> Blue500
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Slate900),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(accent.copy(alpha = 0.5f))
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = title, color = Slate100, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = if (isBreached) "RESTRICTED" else if (isPositive) "SCALING UNLOCKED" else "NORMAL",
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(text = ruleDesc, color = Slate400, fontSize = 11.sp, lineHeight = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = status, color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(text = limit, color = Slate400, fontSize = 11.sp)
      }
    }
  }
}
