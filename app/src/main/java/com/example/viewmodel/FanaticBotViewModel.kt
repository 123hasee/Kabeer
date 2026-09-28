package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BotSettingsEntity
import com.example.data.RoutineEntity
import com.example.data.TradeEntity
import com.example.data.TradeRepository
import com.example.engine.TradingBotEngine
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FanaticBotViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: TradeRepository
  val engine = TradingBotEngine()

  val allTrades: StateFlow<List<TradeEntity>>
  val botSettings: StateFlow<BotSettingsEntity>
  val todayRoutine: StateFlow<RoutineEntity>

  val selectedPair = engine.selectedPair
  val selectedTimeframe = engine.selectedTimeframe
  val candles = engine.candles
  val marketTrend = engine.marketTrend
  val pdRange = engine.pdRange
  val orderBlocks = engine.orderBlocks
  val liquidityEvents = engine.liquidityEvents
  val currentSignal = engine.currentSignal
  val activePositions = engine.activePositions
  val currentWindow = engine.currentWindow
  val dailyLossStreak = engine.dailyLossStreak
  val dailyLossPercent = engine.dailyLossPercent
  val circuitBreakerReason = engine.circuitBreakerHaltReason

  // Navigation tab state
  private val _currentTab = MutableStateFlow(BotNavTab.DASHBOARD)
  val currentTab: StateFlow<BotNavTab> = _currentTab.asStateFlow()

  // Selected book chapter for viewing in Rulebook screen
  private val _selectedChapterId = MutableStateFlow<String?>("risk_masterclass")
  val selectedChapterId: StateFlow<String?> = _selectedChapterId.asStateFlow()

  // Pre-flight checklist interactive state
  private val _checklistState = MutableStateFlow(
    BookRulesData.preFlightChecklist.associateWith { false }
  )
  val checklistState: StateFlow<Map<String, Boolean>> = _checklistState.asStateFlow()

  init {
    val db = AppDatabase.getDatabase(application)
    repository = TradeRepository(db)

    allTrades = repository.allTrades
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    botSettings = repository.botSettings
      .filterNotNull()
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BotSettingsEntity()
      )

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    todayRoutine = repository.getRoutineForDate(todayStr)
      .map { it ?: RoutineEntity(dateString = todayStr) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RoutineEntity(dateString = todayStr)
      )

    viewModelScope.launch {
      repository.initializeDefaultsIfNeeded()
    }

    startMarketSimulationLoop()
  }

  fun setTab(tab: BotNavTab) {
    _currentTab.value = tab
  }

  fun selectChapter(chapterId: String?) {
    _selectedChapterId.value = chapterId
  }

  fun toggleChecklistItem(item: String) {
    val current = _checklistState.value.toMutableMap()
    current[item] = !(current[item] ?: false)
    _checklistState.value = current
  }

  fun resetChecklist() {
    _checklistState.value = BookRulesData.preFlightChecklist.associateWith { false }
  }

  fun selectPair(pair: String) {
    engine.selectPair(pair)
  }

  fun selectTimeframe(tf: Timeframe) {
    engine.selectTimeframe(tf)
  }

  fun toggleAutoTrading() {
    viewModelScope.launch {
      val current = botSettings.value
      val updated = current.copy(isAutoTradingActive = !current.isAutoTradingActive)
      repository.updateSettings(updated)
    }
  }

  fun deleteTrade(id: Long) {
    viewModelScope.launch {
      repository.deleteTrade(id)
    }
  }

  fun updateSettings(settings: BotSettingsEntity) {
    viewModelScope.launch {
      repository.updateSettings(settings)
    }
  }

  fun scanForSignal() {
    engine.generateNewSignal(botSettings.value)
  }

  fun dismissSignal() {
    engine.generateNewSignal(botSettings.value) // or clear
  }

  fun executeCurrentSignal() {
    val sig = currentSignal.value ?: return
    val settings = botSettings.value
    engine.executeSignal(sig, settings)
  }

  fun takePartials(posId: Long) {
    engine.takePartials(posId)
  }

  fun moveBreakeven(posId: Long) {
    engine.moveBreakeven(posId)
  }

  fun closePosition(posId: Long) {
    val closedTrade = engine.closePosition(posId)
    if (closedTrade != null) {
      viewModelScope.launch {
        repository.insertTrade(closedTrade)
        updateBalanceAfterTrade(closedTrade.pnl)
      }
    }
  }

  fun toggleRoutine(key: String) {
    val current = todayRoutine.value
    val updated = when (key) {
      "hydrate" -> current.copy(hydrateDone = !current.hydrateDone)
      "exercise" -> current.copy(exerciseDone = !current.exerciseDone)
      "meditate" -> current.copy(meditateDone = !current.meditateDone)
      "chartPrep" -> current.copy(chartPrepDone = !current.chartPrepDone)
      "alertsSet" -> current.copy(alertsSet = !current.alertsSet)
      "tradeWindow" -> current.copy(tradeWindowObserved = !current.tradeWindowObserved)
      "midDayBreak" -> current.copy(midDayBreakDone = !current.midDayBreakDone)
      "eodJournal" -> current.copy(eodJournalDone = !current.eodJournalDone)
      "eodMarkup" -> current.copy(eodMarkupDone = !current.eodMarkupDone)
      else -> current
    }
    viewModelScope.launch {
      repository.updateRoutine(updated)
    }
  }

  fun setMindsetRating(rating: Int) {
    val current = todayRoutine.value
    viewModelScope.launch {
      repository.updateRoutine(current.copy(mindsetRating = rating))
    }
  }

  private fun updateBalanceAfterTrade(pnl: Double) {
    viewModelScope.launch {
      val current = botSettings.value
      val newBalance = current.balance + pnl
      val newPeak = maxOf(current.peakBalance, newBalance)
      repository.updateSettings(
        current.copy(balance = newBalance, peakBalance = newPeak)
      )
    }
  }

  private fun startMarketSimulationLoop() {
    viewModelScope.launch {
      var tickCount = 0
      while (true) {
        delay(2500L) // Market ticks every 2.5 seconds
        tickCount++

        val settings = botSettings.value
        engine.refreshWindow()
        engine.evaluateCircuitBreakers(settings, allTrades.value)

        // Advance live candles and active positions
        engine.tickLiveMarket(settings) { closedTrade ->
          viewModelScope.launch {
            repository.insertTrade(closedTrade)
            updateBalanceAfterTrade(closedTrade.pnl)
          }
        }

        // Periodically check or generate signals if bot is enabled and no active positions
        if (settings.isAutoTradingActive && engine.circuitBreakerHaltReason.value == null) {
          if (engine.activePositions.value.isEmpty() && engine.currentSignal.value == null && tickCount % 5 == 0) {
            val signal = engine.generateNewSignal(settings)
            // If strictly executable and all rules pass, auto-execute!
            if (signal.isExecutable) {
              engine.executeSignal(signal, settings)
            }
          }
        }
      }
    }
  }
}

enum class BotNavTab(val label: String) {
  DASHBOARD("Scanner & Bot"),
  RULES_GUARDIAN("Rulebook"),
  JOURNAL("Trade Journal"),
  GOALS_CALC("Goals & LE"),
  ROUTINE("Daily Habits")
}
