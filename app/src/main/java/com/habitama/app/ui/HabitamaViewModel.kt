package com.habitama.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitama.app.HabitamaApplication
import com.habitama.app.data.DailyGoalRecordEntity
import com.habitama.app.data.GoalDraft
import com.habitama.app.data.GoalEntity
import com.habitama.app.data.GoalGain
import com.habitama.app.data.GrowthStatsEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryDay(
    val date: LocalDate,
    val records: List<DailyGoalRecordEntity>,
)

data class HabitamaUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val activeGoals: List<GoalEntity> = emptyList(),
    val pendingGoals: List<GoalEntity> = emptyList(),
    val todayRecords: List<DailyGoalRecordEntity> = emptyList(),
    val totalEnergy: Int = 0,
    val growthStats: GrowthStatsEntity = GrowthStatsEntity(),
    val history: List<HistoryDay> = emptyList(),
    val lastGains: List<GoalGain> = emptyList(),
    val lastEarned: Int = 0,
    val errorMessage: String? = null,
)

class HabitamaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HabitamaApplication).repository
    private val _state = MutableStateFlow(HabitamaUiState())
    val state: StateFlow<HabitamaUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeDashboard()
                .catch { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "データを読み込めませんでした") }
                }
                .collect { data ->
                    val byDate = data.records.groupBy { it.date }
                    val history = (0L..41L).map { offset ->
                        val date = data.today.minusDays(offset)
                        HistoryDay(date, byDate[date.toString()].orEmpty())
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            today = data.today,
                            activeGoals = data.activeGoals,
                            pendingGoals = data.pendingGoals,
                            todayRecords = data.todayRecords,
                            totalEnergy = data.totalEnergy,
                            growthStats = data.growthStats.copy(totalPoints = data.totalEnergy),
                            history = history,
                        )
                    }
                }
        }
    }

    fun createInitialGoals(drafts: List<GoalDraft>, onSuccess: () -> Unit) = launchAction(onSuccess) {
        repository.createInitialGoals(drafts)
    }

    fun addGoal(draft: GoalDraft, onSuccess: () -> Unit) = launchAction(onSuccess) {
        repository.addGoal(draft)
    }

    fun updateGoalNow(goalId: Long, draft: GoalDraft, onSuccess: () -> Unit) = launchAction(onSuccess) {
        repository.updateGoalNow(goalId, draft)
    }

    fun saveTodayRecords(actualValues: Map<Long, Long>, onSuccess: () -> Unit) = launchAction(onSuccess) {
        val result = repository.saveTodayRecords(actualValues)
        _state.update {
            it.copy(todayRecords = result.records, lastGains = result.gains, lastEarned = result.totalEarned)
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun launchAction(onSuccess: () -> Unit, action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(errorMessage = null) }
            runCatching { action() }
                .onSuccess { onSuccess() }
                .onFailure { error -> _state.update { it.copy(errorMessage = error.message ?: "処理を完了できませんでした") } }
        }
    }
}
