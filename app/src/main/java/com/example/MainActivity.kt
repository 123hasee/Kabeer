package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.BotNavTab
import com.example.viewmodel.FanaticBotViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: FanaticBotViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        MainAppContent(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: FanaticBotViewModel) {
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val settings by viewModel.botSettings.collectAsStateWithLifecycle()
  val selectedPair by viewModel.selectedPair.collectAsStateWithLifecycle()
  val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
  val candles by viewModel.candles.collectAsStateWithLifecycle()
  val orderBlocks by viewModel.orderBlocks.collectAsStateWithLifecycle()
  val pdRange by viewModel.pdRange.collectAsStateWithLifecycle()
  val currentSignal by viewModel.currentSignal.collectAsStateWithLifecycle()
  val activePositions by viewModel.activePositions.collectAsStateWithLifecycle()
  val currentWindow by viewModel.currentWindow.collectAsStateWithLifecycle()
  val dailyLossStreak by viewModel.dailyLossStreak.collectAsStateWithLifecycle()
  val dailyLossPercent by viewModel.dailyLossPercent.collectAsStateWithLifecycle()
  val circuitBreakerReason by viewModel.circuitBreakerReason.collectAsStateWithLifecycle()
  val allTrades by viewModel.allTrades.collectAsStateWithLifecycle()
  val todayRoutine by viewModel.todayRoutine.collectAsStateWithLifecycle()
  val checklistState by viewModel.checklistState.collectAsStateWithLifecycle()
  val selectedChapterId by viewModel.selectedChapterId.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.TrendingUp,
              contentDescription = null,
              tint = Emerald500,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "FanaticBot",
                color = Slate100,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
              )
              Text(
                text = "SMC Rule-Based Trading Guardian",
                color = Slate400,
                fontSize = 10.sp
              )
            }
          }
        },
        actions = {
          // Effective risk badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (settings.effectiveRiskPercent <= 0.15) Amber500.copy(alpha = 0.2f) else Emerald500.copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Risk: ${String.format("%.2f", settings.effectiveRiskPercent)}%",
              color = if (settings.effectiveRiskPercent <= 0.15) Amber400 else Emerald400,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = Slate950,
        contentColor = Slate300,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
      ) {
        BotNavTab.values().forEach { tab ->
          val isSelected = tab == currentTab
          val icon = when (tab) {
            BotNavTab.DASHBOARD -> Icons.Default.CandlestickChart
            BotNavTab.RULES_GUARDIAN -> Icons.Default.Shield
            BotNavTab.JOURNAL -> Icons.Default.FactCheck
            BotNavTab.GOALS_CALC -> Icons.Default.Calculate
            BotNavTab.ROUTINE -> Icons.Default.SelfImprovement
          }

          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setTab(tab) },
            icon = { Icon(imageVector = icon, contentDescription = tab.label) },
            label = { Text(text = tab.label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Emerald500,
              selectedTextColor = Emerald400,
              unselectedIconColor = Slate400,
              unselectedTextColor = Slate400,
              indicatorColor = Emerald500.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_${tab.name}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(Slate950)
    ) {
      when (currentTab) {
        BotNavTab.DASHBOARD -> {
          BotDashboardScreen(
            settings = settings,
            selectedPair = selectedPair,
            selectedTimeframe = selectedTimeframe,
            candles = candles,
            orderBlocks = orderBlocks,
            pdRange = pdRange,
            currentSignal = currentSignal,
            activePositions = activePositions,
            currentWindow = currentWindow,
            dailyLossPercent = dailyLossPercent,
            dailyLossStreak = dailyLossStreak,
            circuitBreakerReason = circuitBreakerReason,
            onToggleAutoTrading = { viewModel.toggleAutoTrading() },
            onSelectPair = { viewModel.selectPair(it) },
            onSelectTimeframe = { viewModel.selectTimeframe(it) },
            onScanForSignal = { viewModel.scanForSignal() },
            onExecuteSignal = { viewModel.executeCurrentSignal() },
            onDismissSignal = { viewModel.dismissSignal() },
            onTakePartials = { viewModel.takePartials(it) },
            onMoveBreakeven = { viewModel.moveBreakeven(it) },
            onClosePosition = { viewModel.closePosition(it) },
            onViewAllRules = { viewModel.setTab(BotNavTab.RULES_GUARDIAN) }
          )
        }
        BotNavTab.RULES_GUARDIAN -> {
          RuleGuardianScreen(
            settings = settings,
            dailyLossPercent = dailyLossPercent,
            dailyLossStreak = dailyLossStreak,
            currentWindow = currentWindow,
            circuitBreakerReason = circuitBreakerReason,
            checklistState = checklistState,
            selectedChapterId = selectedChapterId,
            onToggleChecklistItem = { viewModel.toggleChecklistItem(it) },
            onResetChecklist = { viewModel.resetChecklist() },
            onSelectChapter = { viewModel.selectChapter(it) }
          )
        }
        BotNavTab.JOURNAL -> {
          JournalScreen(
            trades = allTrades,
            onDeleteTrade = { tradeId ->
              viewModel.deleteTrade(tradeId)
            }
          )
        }
        BotNavTab.GOALS_CALC -> {
          GoalsCalcScreen()
        }
        BotNavTab.ROUTINE -> {
          RoutineScreen(
            routine = todayRoutine,
            currentWindow = currentWindow,
            onToggleRoutine = { viewModel.toggleRoutine(it) },
            onSetMindsetRating = { viewModel.setMindsetRating(it) }
          )
        }
      }
    }
  }
}
