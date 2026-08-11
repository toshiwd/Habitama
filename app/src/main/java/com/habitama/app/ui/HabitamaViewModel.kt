package com.habitama.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitama.app.HabitamaApplication
import com.habitama.app.calendar.DeviceCalendar
import com.habitama.app.calendar.DeviceCalendarEvent
import com.habitama.app.data.DailyGoalRecordEntity
import com.habitama.app.data.GoalDraft
import com.habitama.app.data.GoalEntity
import com.habitama.app.data.GoalGain
import com.habitama.app.data.GrowthStatsEntity
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
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

data class DeviceCalendarUiState(
    val enabled: Boolean = false,
    val permissionGranted: Boolean = false,
    val calendars: List<DeviceCalendar> = emptyList(),
    val selectedCalendarIds: Set<Long> = emptySet(),
    val events: List<DeviceCalendarEvent> = emptyList(),
    val visibleMonth: YearMonth? = null,
    val errorMessage: String? = null,
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
    val calendarRecords: List<DailyGoalRecordEntity> = emptyList(),
    val lastGains: List<GoalGain> = emptyList(),
    val lastEarned: Int = 0,
    val errorMessage: String? = null,
    val deviceCalendar: DeviceCalendarUiState = DeviceCalendarUiState(),
)

class HabitamaViewModel(application: Application) : AndroidViewModel(application) {
    private val habitamaApplication = application as HabitamaApplication
    private val repository = habitamaApplication.repository
    private val deviceCalendarRepository = habitamaApplication.deviceCalendarRepository
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
                            calendarRecords = data.records,
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

    fun refreshDeviceCalendar() {
        refreshDeviceCalendarMonth(_state.value.deviceCalendar.visibleMonth ?: YearMonth.from(_state.value.today))
    }

    fun refreshDeviceCalendarMonth(month: YearMonth) {
        _state.update { it.copy(deviceCalendar = it.deviceCalendar.copy(visibleMonth = month)) }
        viewModelScope.launch(Dispatchers.IO) {
            val settings = deviceCalendarRepository.loadSettings()
            val permissionGranted = deviceCalendarRepository.hasReadPermission()
            if (!settings.enabled || !permissionGranted) {
                _state.update {
                    it.copy(
                        deviceCalendar = it.deviceCalendar.copy(
                            enabled = settings.enabled,
                            permissionGranted = permissionGranted,
                            calendars = emptyList(),
                            selectedCalendarIds = emptySet(),
                            events = emptyList(),
                            visibleMonth = month,
                            errorMessage = null,
                        ),
                    )
                }
                return@launch
            }

            runCatching {
                val calendars = deviceCalendarRepository.listVisibleCalendars()
                val availableIds = calendars.mapTo(mutableSetOf()) { it.id }
                val selectedIds = settings.selectedCalendarIds?.intersect(availableIds) ?: availableIds
                if (settings.selectedCalendarIds == null && selectedIds.isNotEmpty()) {
                    deviceCalendarRepository.setSelectedCalendarIds(selectedIds)
                }
                val events = deviceCalendarRepository.eventsBetween(month.atDay(1), month.plusMonths(1).atDay(1), selectedIds)
                DeviceCalendarUiState(
                    enabled = true,
                    permissionGranted = true,
                    calendars = calendars,
                    selectedCalendarIds = selectedIds,
                    events = events,
                    visibleMonth = month,
                )
            }.onSuccess { calendarState ->
                _state.update {
                    if (it.deviceCalendar.visibleMonth == month) it.copy(deviceCalendar = calendarState) else it
                }
            }.onFailure { error ->
                _state.update {
                    if (it.deviceCalendar.visibleMonth != month) return@update it
                    it.copy(
                        deviceCalendar = it.deviceCalendar.copy(
                            enabled = true,
                            permissionGranted = true,
                            events = emptyList(),
                            visibleMonth = month,
                            errorMessage = error.message ?: "端末カレンダーを読み込めませんでした",
                        ),
                    )
                }
            }
        }
    }

    fun setDeviceCalendarEnabled(enabled: Boolean) {
        deviceCalendarRepository.setEnabled(enabled)
        if (enabled) {
            refreshDeviceCalendar()
        } else {
            _state.update { it.copy(deviceCalendar = DeviceCalendarUiState(permissionGranted = deviceCalendarRepository.hasReadPermission())) }
        }
    }

    fun onDeviceCalendarPermissionDenied() {
        deviceCalendarRepository.setEnabled(false)
        _state.update {
            it.copy(
                deviceCalendar = DeviceCalendarUiState(
                    permissionGranted = false,
                    errorMessage = "カレンダー権限が許可されていないため、予定は表示しません。",
                ),
            )
        }
    }

    fun setDeviceCalendarSelected(calendarId: Long, selected: Boolean) {
        val current = _state.value.deviceCalendar.selectedCalendarIds
        val next = if (selected) current + calendarId else current - calendarId
        deviceCalendarRepository.setSelectedCalendarIds(next)
        _state.update { it.copy(deviceCalendar = it.deviceCalendar.copy(selectedCalendarIds = next)) }
        refreshDeviceCalendar()
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
