package com.habitama.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitama.app.HabitamaApplication
import com.habitama.app.data.DailyGoalRecordEntity
import com.habitama.app.data.GoalDraft
import com.habitama.app.data.GoalEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryDay(
    val date: LocalDate,
    val record: DailyGoalRecordEntity?,
)

data class HabitamaUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val activeGoal: GoalEntity? = null,
    val pendingGoal: GoalEntity? = null,
    val todayRecord: DailyGoalRecordEntity? = null,
    val totalEnergy: Int = 0,
    val history: List<HistoryDay> = emptyList(),
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
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "データを読み込めませんでした")
                    }
                }
                .collect { data ->
                    val recordByDate = data.records.associateBy { it.date }
                    val history = (0L..6L).map { offset ->
                        val date = data.today.minusDays(offset)
                        HistoryDay(date, recordByDate[date.toString()])
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            today = data.today,
                            activeGoal = data.activeGoal,
                            pendingGoal = data.pendingGoal,
                            todayRecord = data.todayRecord,
                            totalEnergy = data.totalEnergy,
                            history = history,
                        )
                    }
                }
        }
    }

    fun createGoal(title: String, targetValue: Long, unit: String, onSuccess: () -> Unit) {
        launchAction(onSuccess) {
            repository.createInitialGoal(GoalDraft(title, targetValue, unit))
        }
    }

    fun scheduleGoalUpdate(title: String, targetValue: Long, unit: String, onSuccess: () -> Unit) {
        launchAction(onSuccess) {
            repository.scheduleGoalUpdate(GoalDraft(title, targetValue, unit))
        }
    }

    fun saveTodayRecord(actualValue: Long, onSuccess: () -> Unit) {
        launchAction(onSuccess) {
            val record = repository.saveTodayRecord(actualValue)
            _state.update { it.copy(todayRecord = record) }
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
                .onFailure { error ->
                    _state.update {
                        it.copy(errorMessage = error.message ?: "処理を完了できませんでした")
                    }
                }
        }
    }
}
