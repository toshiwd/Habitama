package com.habitama.app.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habitama.app.data.DailyGoalRecordEntity
import com.habitama.app.data.GoalDraft
import com.habitama.app.data.GoalEntity
import com.habitama.app.data.GrowthStatsEntity
import com.habitama.app.data.GrowthType
import com.habitama.app.data.MAX_ACTIVE_GOALS
import com.habitama.app.calendar.DeviceCalendarEvent
import com.habitama.app.domain.MAX_INPUT_VALUE
import com.habitama.app.domain.GoalEvaluationMode
import com.habitama.app.domain.JapaneseHolidays
import com.habitama.app.notifications.ReminderPreferences
import com.habitama.app.notifications.ReminderScheduler
import com.habitama.app.notifications.ReminderSettings
import com.habitama.app.ui.theme.HabitamaAccent
import com.habitama.app.ui.theme.HabitamaBackground
import com.habitama.app.ui.theme.HabitamaBlue
import com.habitama.app.ui.theme.HabitamaLeaf
import com.habitama.app.ui.theme.HabitamaLine
import com.habitama.app.ui.theme.HabitamaPrimary
import com.habitama.app.ui.theme.HabitamaPrimaryDark
import com.habitama.app.ui.theme.HabitamaRose
import com.habitama.app.ui.theme.HabitamaSuccess
import java.time.LocalDate
import java.time.Instant
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_HOME = "home"
private const val ROUTE_REPORT = "report"
private const val ROUTE_RESULT = "result"
private const val ROUTE_CALENDAR = "calendar"
private const val ROUTE_GROWTH = "growth"
private const val ROUTE_GOAL_ADD = "goal/add"
private const val ROUTE_GOAL_EDIT = "goal/edit/{goalId}"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_GOAL_MANAGEMENT = "settings/goals"

@Composable
fun HabitamaRoot(viewModel: HabitamaViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.isLoading) {
        BotanicalBackground { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        return
    }

    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = if (state.activeGoals.isEmpty()) ROUTE_ONBOARDING else ROUTE_HOME) {
        composable(ROUTE_ONBOARDING) {
            OnboardingScreen(state.errorMessage, viewModel::clearError) { drafts ->
                viewModel.createInitialGoals(drafts) {
                    nav.navigate(ROUTE_HOME) { popUpTo(ROUTE_ONBOARDING) { inclusive = true } }
                }
            }
        }
        composable(ROUTE_HOME) {
            MainScreen(title = "ホーム", selected = ROUTE_HOME, onTab = { nav.navigateSingleTop(it) }, onSettings = { nav.navigate(ROUTE_SETTINGS) }) { padding ->
                HomeScreen(
                    state = state,
                    padding = padding,
                    onReport = { nav.navigate(ROUTE_REPORT) },
                )
            }
        }
        composable(ROUTE_REPORT) {
            ReportScreen(state, onBack = nav::popBackStack) { values ->
                viewModel.saveTodayRecords(values) { nav.navigate(ROUTE_RESULT) }
            }
        }
        composable(ROUTE_RESULT) {
            ResultScreen(state) {
                nav.navigate(ROUTE_HOME) { popUpTo(ROUTE_HOME) { inclusive = true } }
            }
        }
        composable(ROUTE_CALENDAR) {
            MainScreen(title = "行動カレンダー", selected = ROUTE_CALENDAR, onTab = { nav.navigateSingleTop(it) }, onSettings = { nav.navigate(ROUTE_SETTINGS) }) { padding ->
                CalendarScreen(state, padding, viewModel::refreshDeviceCalendarMonth)
            }
        }
        composable(ROUTE_GROWTH) {
            MainScreen(title = "ハビタマの成長", selected = ROUTE_GROWTH, onTab = { nav.navigateSingleTop(it) }, onSettings = { nav.navigate(ROUTE_SETTINGS) }) { padding ->
                GrowthScreen(state, padding)
            }
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(
                state = state,
                onBack = nav::popBackStack,
                onGoals = { nav.navigate(ROUTE_GOAL_MANAGEMENT) },
                onRefreshDeviceCalendar = viewModel::refreshDeviceCalendar,
                onDeviceCalendarEnabled = viewModel::setDeviceCalendarEnabled,
                onDeviceCalendarPermissionDenied = viewModel::onDeviceCalendarPermissionDenied,
                onDeviceCalendarSelected = viewModel::setDeviceCalendarSelected,
            )
        }
        composable(ROUTE_GOAL_MANAGEMENT) {
            GoalManagementScreen(
                state = state,
                onBack = nav::popBackStack,
                onAdd = { nav.navigate(ROUTE_GOAL_ADD) },
                onEdit = { nav.navigate("goal/edit/$it") },
            )
        }
        composable(ROUTE_GOAL_ADD) {
            GoalEditorScreen(
                heading = "行動を追加",
                description = "毎日続けたい小さな行動を決めます。",
                initialGoal = null,
                saveLabel = "今日から追加する",
                errorMessage = state.errorMessage,
                onClearError = viewModel::clearError,
                onBack = nav::popBackStack,
            ) { draft -> viewModel.addGoal(draft) { nav.popBackStack() } }
        }
        composable(
            ROUTE_GOAL_EDIT,
            arguments = listOf(navArgument("goalId") { type = NavType.LongType }),
        ) { entry ->
            val goalId = entry.arguments?.getLong("goalId") ?: 0
            val goal = state.activeGoals.firstOrNull { it.id == goalId }
            GoalEditorScreen(
                heading = "行動を変更",
                description = "保存すると、ホームと今日の報告へすぐに反映されます。",
                initialGoal = goal,
                saveLabel = "今すぐ変更する",
                errorMessage = state.errorMessage,
                onClearError = viewModel::clearError,
                onBack = nav::popBackStack,
            ) { draft -> viewModel.updateGoalNow(goalId, draft) { nav.popBackStack() } }
        }
    }
}

private fun androidx.navigation.NavController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true; popUpTo(ROUTE_HOME) }
}

@Composable
internal fun OnboardingScreen(errorMessage: String?, onClearError: () -> Unit, onSave: (List<GoalDraft>) -> Unit) {
    val selectedTemplates = remember { mutableStateListOf<Int>() }
    val customGoals = remember { mutableStateListOf<GoalDraft>() }
    var creatingCustomGoal by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf("recommended") }
    val selectedCount = selectedTemplates.size + customGoals.size

    if (creatingCustomGoal) {
        GoalEditorScreen(
            heading = "自分の行動を作る",
            description = "毎日続けたい行動を、好きな名前・目標値・単位で設定できます。",
            initialGoal = null,
            saveLabel = "この行動を追加",
            errorMessage = errorMessage,
            onClearError = onClearError,
            onBack = { creatingCustomGoal = false },
        ) { draft ->
            if (selectedTemplates.size + customGoals.size < MAX_ACTIVE_GOALS) customGoals.add(draft)
            creatingCustomGoal = false
        }
    } else {
        BotanicalBackground {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp, vertical = 28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StepDots(0)
                Spacer(Modifier.height(28.dp))
                Text("何を続ける？", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(8.dp))
                Text("サンプルを選ぶだけでも、名前・目標値・単位をまとめて設定できます", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                TemplateCategorySelector(selectedCategory) { selectedCategory = it }
                Spacer(Modifier.height(16.dp))
                templatesFor(selectedCategory).forEach { template ->
                    val templateIndex = goalTemplates.indexOf(template)
                    TemplateCard(template, templateIndex in selectedTemplates) {
                        onClearError()
                        if (templateIndex in selectedTemplates) {
                            selectedTemplates.remove(templateIndex)
                        } else if (selectedCount < MAX_ACTIVE_GOALS) {
                            selectedTemplates.add(templateIndex)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
                if (customGoals.isNotEmpty()) {
                    Text("自分で作った行動", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    customGoals.forEachIndexed { index, draft ->
                        CustomGoalDraftCard(draft) { customGoals.removeAt(index) }
                        Spacer(Modifier.height(12.dp))
                    }
                }
                OutlinedButton(
                    onClick = { creatingCustomGoal = true },
                    enabled = selectedCount < MAX_ACTIVE_GOALS,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).testTag("create_custom_goal"),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("自分で行動を作る")
                }
                Spacer(Modifier.height(16.dp))
                Text("$selectedCount / $MAX_ACTIVE_GOALS 選択中", color = HabitamaPrimary, fontWeight = FontWeight.Bold)
                if (selectedCount == MAX_ACTIVE_GOALS) {
                    Text(
                        "別の行動を作る場合は、選択中のサンプルか作成済みの行動を外してください。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                ErrorText(errorMessage)
                Spacer(Modifier.height(18.dp))
                PrimaryButton("これで始める", selectedCount > 0, "save_goal") {
                    onSave(selectedTemplates.sorted().map { goalTemplates[it].draft } + customGoals.toList())
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TemplateCategorySelector(selectedId: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        templateCategories.forEach { category ->
            FilterChip(
                selected = category.id == selectedId,
                onClick = { onSelect(category.id) },
                label = { Text(category.label) },
                modifier = Modifier.heightIn(min = 48.dp).testTag("category_${category.id}"),
            )
        }
    }
}

@Composable
private fun CustomGoalDraftCard(draft: GoalDraft, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            GoalTypeIcon(draft.icon, draft.growthType, Modifier.size(32.dp), HabitamaPrimaryDark)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(draft.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "目標 ${draft.targetValue}${draft.unit}${if (draft.evaluationMode == GoalEvaluationMode.AT_MOST) "以内" else "以上"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "${draft.title}を削除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TemplateCard(template: GoalTemplate, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp).clickable(onClick = onClick)
            .border(1.5.dp, if (selected) HabitamaPrimary else HabitamaLine, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            GoalTypeIcon(template.draft.icon, template.draft.growthType, Modifier.size(32.dp), HabitamaPrimaryDark)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(template.draft.title, style = MaterialTheme.typography.titleMedium)
                Text(template.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "目標 ${template.draft.targetValue}${template.draft.unit}${if (template.draft.evaluationMode == GoalEvaluationMode.AT_MOST) "以内" else "以上"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = growthColor(template.draft.growthType),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(
                Modifier.size(26.dp).border(2.dp, if (selected) HabitamaPrimary else HabitamaLine, CircleShape)
                    .background(if (selected) HabitamaPrimary else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) { if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun HomeScreen(state: HabitamaUiState, padding: PaddingValues, onReport: () -> Unit) {
    val recordMap = state.todayRecords.associateBy { it.goalId }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(formatDate(state.today), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("今日も、少しずつ。", style = MaterialTheme.typography.headlineSmall)
                }
                EggBadge(state.totalEnergy)
            }
        }
        item { SectionLabel("今日の行動", if (state.todayRecords.isNotEmpty()) "記録済み" else "未報告") }
        items(state.activeGoals, key = { it.id }) { goal ->
            GoalProgressCard(goal, recordMap[goal.id])
        }
        item { GrowthStrip(state.growthStats) }
        if (state.pendingGoals.isNotEmpty()) {
            item {
                Text("以前に予約した変更（明日から）：${state.pendingGoals.joinToString { it.title }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            PrimaryButton(if (state.todayRecords.isEmpty()) "今日の報告" else "今日の報告を更新", true, "open_report", onReport)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GoalProgressCard(goal: GoalEntity, record: DailyGoalRecordEntity?) {
    val percentage = record?.displayPercentage ?: 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(percentage, goal)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                if (record == null) {
                    Text(
                        "目標 ${goal.targetValue}${goal.unit}${if (goal.evaluationMode == GoalEvaluationMode.AT_MOST) "以内" else "以上"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(record.actualValue.toString(), fontSize = 23.sp, fontWeight = FontWeight.Bold, color = HabitamaPrimaryDark)
                        Text(
                            " / ${record.targetValueSnapshot}${record.unitSnapshot}${if (record.evaluationModeSnapshot == GoalEvaluationMode.AT_MOST) "以内" else "以上"}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportScreen(state: HabitamaUiState, onBack: () -> Unit, onSave: (Map<Long, Long>) -> Unit) {
    val values = remember { mutableStateMapOf<Long, String>() }
    LaunchedEffect(state.activeGoals, state.todayRecords) {
        state.activeGoals.forEach { goal -> values[goal.id] = state.todayRecords.firstOrNull { it.goalId == goal.id }?.actualValue?.toString() ?: "0" }
    }
    ScreenShell("今日の結果を報告", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Text("できた分を、そのまま教えてください。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.activeGoals, key = { it.id }) { goal ->
                val text = values[goal.id] ?: "0"
                val actual = text.toLongOrNull() ?: 0
                ReportGoalCard(goal, text, onValue = { values[goal.id] = it }, actual = actual)
            }
            item {
                EncouragementCard()
                ErrorText(state.errorMessage)
                Spacer(Modifier.height(8.dp))
                val valid = state.activeGoals.all { (values[it.id]?.toLongOrNull() ?: -1) in 0..MAX_INPUT_VALUE }
                PrimaryButton("結果を報告", valid, "save_record") {
                    onSave(state.activeGoals.associate { it.id to (values[it.id]?.toLongOrNull() ?: 0) })
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ReportGoalCard(goal: GoalEntity, value: String, onValue: (String) -> Unit, actual: Long) {
    val max = (goal.targetValue * 1.5f).coerceAtLeast(1f)
    val step = (goal.targetValue / 10).coerceAtLeast(1)
    val accent = growthColor(goal.growthType)
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GoalTypeIcon(goal.icon, goal.growthType, Modifier.size(30.dp), growthColor(goal.growthType))
                Spacer(Modifier.width(10.dp))
                Text(goal.title, style = MaterialTheme.typography.titleMedium, color = growthColor(goal.growthType))
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoundAction(Icons.Rounded.Remove, "減らす") { onValue((actual - step).coerceAtLeast(0).toString()) }
                OutlinedTextField(
                    value = value,
                    onValueChange = { onValue(it.filter(Char::isDigit).take(9)) },
                    modifier = Modifier.weight(1f).testTag("actual_${goal.id}"),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                    suffix = { Text("/ ${goal.targetValue}${goal.unit}${if (goal.evaluationMode == GoalEvaluationMode.AT_MOST) "以内" else "以上"}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                )
                RoundAction(Icons.Rounded.Add, "増やす") { onValue((actual + step).coerceAtMost(MAX_INPUT_VALUE).toString()) }
            }
            Slider(
                value = actual.coerceAtMost(max.toLong()).toFloat(),
                onValueChange = { onValue(it.roundToLong().toString()) },
                valueRange = 0f..max,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = accent.copy(alpha = .18f),
                ),
            )
            Text(
                if (goal.evaluationMode == GoalEvaluationMode.AT_MOST) {
                    if (actual <= goal.targetValue) "目標以内です" else "${actual - goal.targetValue}${goal.unit}オーバー"
                } else {
                    "達成率 ${(actual * 100 / goal.targetValue).coerceAtLeast(0)}%"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResultScreen(state: HabitamaUiState, onHome: () -> Unit) {
    ScreenShell("今日の成長", onBack = null) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(14.dp))
            Text(resultMessage(state.todayRecords), style = MaterialTheme.typography.headlineLarge, color = HabitamaAccent)
            Text("今日の行動が、成長につながったよ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            EggGlow()
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.lastGains.forEach { gain ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            GrowthIcon(gain.growthType, Modifier.size(22.dp))
                            Spacer(Modifier.width(9.dp))
                            Text(growthName(gain.growthType), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text(if (gain.points >= 0) "+${gain.points}" else gain.points.toString(), color = growthColor(gain.growthType), fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = HabitamaLine)
                    Row(Modifier.fillMaxWidth()) {
                        Text("今日のハビタマポイント", modifier = Modifier.weight(1f))
                        Text("+${state.lastEarned} pt", style = MaterialTheme.typography.titleLarge, color = HabitamaPrimary)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("累積ポイント", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.totalEnergy} pt", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton("ホームへ", true, "result_home", onHome)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun CalendarScreen(state: HabitamaUiState, padding: PaddingValues, onVisibleMonthChanged: (YearMonth) -> Unit = {}) {
    var visibleMonthText by rememberSaveable { mutableStateOf(YearMonth.from(state.today).toString()) }
    val month = YearMonth.parse(visibleMonthText)
    val byDate = remember(state.calendarRecords) {
        state.calendarRecords.groupBy { LocalDate.parse(it.date) }
    }
    var selectedDateText by rememberSaveable { mutableStateOf(state.today.toString()) }
    val selectedDate = LocalDate.parse(selectedDateText)
    val eventsByDate = remember(state.deviceCalendar.events) {
        buildMap<LocalDate, MutableList<DeviceCalendarEvent>> {
            state.deviceCalendar.events.forEach { event ->
                event.dates().forEach { date -> getOrPut(date) { mutableListOf() }.add(event) }
            }
        }
    }
    val leading = month.atDay(1).dayOfWeek.value - 1
    val cells = List(leading) { null } + (1..month.lengthOfMonth()).map(month::atDay)
    fun showMonth(next: YearMonth) {
        visibleMonthText = next.toString()
        selectedDateText = next.atDay(minOf(selectedDate.dayOfMonth, next.lengthOfMonth())).toString()
    }
    LaunchedEffect(month) { onVisibleMonthChanged(month) }
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showMonth(month.minusMonths(1)) },
                        modifier = Modifier.testTag("calendar_previous_month"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "前の月")
                    }
                    Text(
                        "${month.year}年 ${month.monthValue}月",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    IconButton(
                        onClick = { showMonth(month.plusMonths(1)) },
                        modifier = Modifier.testTag("calendar_next_month"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "次の月")
                    }
                }
                if (month != YearMonth.from(state.today)) {
                    TextButton(
                        onClick = {
                            visibleMonthText = YearMonth.from(state.today).toString()
                            selectedDateText = state.today.toString()
                        },
                        modifier = Modifier.align(Alignment.End).testTag("calendar_today"),
                    ) { Text("今日へ戻る") }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    listOf("月", "火", "水", "木", "金", "土", "日").forEachIndexed { index, label ->
                        Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, color = weekdayHeaderColor(index), fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(10.dp))
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                                if (date != null) {
                                    CalendarDay(
                                        date = date,
                                        records = byDate[date].orEmpty(),
                                        events = eventsByDate[date].orEmpty(),
                                        today = date == state.today,
                                        selected = date == selectedDate,
                                        onSelect = { selectedDateText = date.toString() },
                                    )
                                }
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        DeviceCalendarAgenda(
            date = selectedDate,
            calendarState = state.deviceCalendar,
            events = eventsByDate[selectedDate].orEmpty(),
        )
        Spacer(Modifier.height(18.dp))
        Text("最近の記録", style = MaterialTheme.typography.titleLarge)
        Text("記録がない日も失敗ではありません。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        state.history.take(7).forEach { day -> HistoryRow(day) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    records: List<DailyGoalRecordEntity>,
    events: List<DeviceCalendarEvent>,
    today: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val dateColor = when {
        JapaneseHolidays.isHoliday(date) || date.dayOfWeek == DayOfWeek.SUNDAY -> Color(0xFFC94F5B)
        date.dayOfWeek == DayOfWeek.SATURDAY -> HabitamaBlue
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onSelect).padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(30.dp).background(
                when {
                    selected -> HabitamaBlue.copy(alpha = .18f)
                    today -> HabitamaPrimary.copy(alpha = .16f)
                    else -> Color.Transparent
                },
                CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) { Text(date.dayOfMonth.toString(), color = dateColor, fontSize = 14.sp, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal) }
        Row(Modifier.height(5.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (records.isNotEmpty()) {
                Box(Modifier.size(4.dp).background(if (records.any { it.displayPercentage >= 100 }) HabitamaPrimary else HabitamaLeaf, CircleShape))
            }
            events.take(2).forEach { event ->
                Box(Modifier.size(4.dp).background(deviceCalendarColor(event.color), CircleShape))
            }
        }
    }
}

@Composable
private fun DeviceCalendarAgenda(
    date: LocalDate,
    calendarState: DeviceCalendarUiState,
    events: List<DeviceCalendarEvent>,
) {
    val context = LocalContext.current
    Text("${date.monthValue}月${date.dayOfMonth}日の予定", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                !calendarState.enabled -> {
                    Text("設定で「端末カレンダーの予定」をオンにすると、同期済みの予定を表示できます。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                !calendarState.permissionGranted -> {
                    Text("カレンダーの読み取り権限がありません。設定から許可してください。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                calendarState.errorMessage != null -> {
                    Text(calendarState.errorMessage, color = MaterialTheme.colorScheme.error)
                }
                events.isEmpty() -> {
                    Text("予定なし", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> events.forEachIndexed { index, event ->
                    if (index > 0) HorizontalDivider(color = HabitamaLine)
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { openDeviceCalendarEvent(context, event) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.width(4.dp).height(40.dp).background(deviceCalendarColor(event.color), RoundedCornerShape(4.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(event.title, fontWeight = FontWeight.SemiBold)
                            Text(deviceCalendarTimeLabel(event), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(day: HistoryDay) {
    Row(Modifier.fillMaxWidth().heightIn(min = 58.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            formatDate(day.date),
            Modifier.weight(1f),
            color = when {
                JapaneseHolidays.isHoliday(day.date) || day.date.dayOfWeek == DayOfWeek.SUNDAY -> Color(0xFFC94F5B)
                day.date.dayOfWeek == DayOfWeek.SATURDAY -> HabitamaBlue
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.SemiBold,
        )
        if (day.records.isEmpty()) Text("記録なし", color = MaterialTheme.colorScheme.onSurfaceVariant)
        else Text("${day.records.size}件  +${day.records.sumOf { it.energyEarned }} pt", color = HabitamaSuccess, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = HabitamaLine.copy(alpha = .7f))
}

@Composable
private fun GrowthScreen(state: HabitamaUiState, padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0D8)), elevation = CardDefaults.cardElevation(3.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                EggBadge(state.totalEnergy, large = true)
                Spacer(Modifier.width(18.dp))
                Column {
                    Text("ハビタマ Lv.${state.totalEnergy / 100 + 1}", style = MaterialTheme.typography.headlineSmall)
                    Text("累積 ${state.totalEnergy} pt", color = HabitamaPrimaryDark, fontWeight = FontWeight.Bold)
                    Text("次のレベルまで ${100 - state.totalEnergy % 100} pt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("育った力", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        GrowthType.all.forEach { type ->
            StatCard(type, state.growthStats.valueOf(type))
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("行動ごとに育つ力が決まります。「目標以上」は超過分を120%まで反映し、「目標以内」は上限内なら達成です。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
internal fun SettingsScreen(
    state: HabitamaUiState,
    onBack: () -> Unit,
    onGoals: () -> Unit,
    onRefreshDeviceCalendar: () -> Unit,
    onDeviceCalendarEnabled: (Boolean) -> Unit,
    onDeviceCalendarPermissionDenied: () -> Unit,
    onDeviceCalendarSelected: (Long, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember { ReminderPreferences(context) }
    var settings by remember { mutableStateOf(preferences.load()) }
    fun persist(next: ReminderSettings) {
        settings = next
        preferences.save(next)
        ReminderScheduler.scheduleAll(context, next)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) persist(settings.copy(dailyEnabled = false, monthlyReviewEnabled = false))
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onDeviceCalendarEnabled(true) else onDeviceCalendarPermissionDenied()
    }
    LaunchedEffect(Unit) { onRefreshDeviceCalendar() }

    ScreenShell("設定", onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("習慣の設定", style = MaterialTheme.typography.titleLarge)
            SettingsLinkCard(
                icon = Icons.Rounded.Tune,
                title = "行動目標の管理",
                subtitle = "追加・変更はここから行います",
                onClick = onGoals,
            )
            Text("カレンダー", style = MaterialTheme.typography.titleLarge)
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = HabitamaBlue)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("端末カレンダーの予定", fontWeight = FontWeight.Bold)
                            Text("同期済みの予定を読み取り専用で表示", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = state.deviceCalendar.enabled,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    onDeviceCalendarEnabled(false)
                                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                                    onDeviceCalendarEnabled(true)
                                } else {
                                    calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                                }
                            },
                            modifier = Modifier.testTag("device_calendar_toggle"),
                        )
                    }
                    if (state.deviceCalendar.enabled && !state.deviceCalendar.permissionGranted) {
                        HorizontalDivider(color = HabitamaLine)
                        OutlinedButton(
                            onClick = { calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 48.dp).testTag("grant_calendar_permission"),
                        ) { Text("カレンダーの読み取りを許可") }
                    }
                    if (state.deviceCalendar.enabled && state.deviceCalendar.permissionGranted) {
                        HorizontalDivider(color = HabitamaLine)
                        if (state.deviceCalendar.calendars.isEmpty()) {
                            Text(
                                "端末に表示できるカレンダーがありません。Googleカレンダーアプリで同期を確認してください。",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            state.deviceCalendar.calendars.forEachIndexed { index, calendar ->
                                if (index > 0) HorizontalDivider(color = HabitamaLine.copy(alpha = .7f))
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(12.dp).background(deviceCalendarColor(calendar.color), CircleShape))
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(calendar.displayName, fontWeight = FontWeight.SemiBold)
                                        if (calendar.accountName.isNotBlank()) {
                                            Text(calendar.accountName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Switch(
                                        checked = calendar.id in state.deviceCalendar.selectedCalendarIds,
                                        onCheckedChange = { onDeviceCalendarSelected(calendar.id, it) },
                                        modifier = Modifier.testTag("device_calendar_${calendar.id}"),
                                    )
                                }
                            }
                        }
                    }
                    state.deviceCalendar.errorMessage?.let { message ->
                        Text(message, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Text("予定は端末からその都度読み取り、Habitamaのデータベースには保存しません。予定の追加・変更権限も要求しません。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text("お知らせ", style = MaterialTheme.typography.titleLarge)
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, tint = HabitamaPrimary)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("毎日の報告リマインダー", fontWeight = FontWeight.Bold)
                            Text("指定した時刻に入力を促します", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = settings.dailyEnabled,
                            onCheckedChange = { checked ->
                                val next = settings.copy(dailyEnabled = checked)
                                persist(next)
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                        )
                    }
                    HorizontalDivider(color = HabitamaLine)
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(enabled = settings.dailyEnabled) {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> persist(settings.copy(dailyHour = hour, dailyMinute = minute)) },
                                settings.dailyHour,
                                settings.dailyMinute,
                                true,
                            ).show()
                        }.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = if (settings.dailyEnabled) HabitamaPrimary else HabitamaLine)
                        Spacer(Modifier.width(14.dp))
                        Text("通知時刻", Modifier.weight(1f), color = if (settings.dailyEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(Locale.JAPANESE, "%02d:%02d", settings.dailyHour, settings.dailyMinute), fontWeight = FontWeight.Bold, color = HabitamaPrimary)
                    }
                }
            }
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = HabitamaAccent)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("月1回の目標見直し", fontWeight = FontWeight.Bold)
                        Text("毎月1日 10:00にお知らせ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = settings.monthlyReviewEnabled,
                        onCheckedChange = { checked ->
                            val next = settings.copy(monthlyReviewEnabled = checked)
                            persist(next)
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                    )
                }
            }
            Text("通知は端末の省電力設定により、指定時刻から多少遅れる場合があります。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text("アプリ", style = MaterialTheme.typography.titleLarge)
            AppUpdateCard()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GoalManagementScreen(
    state: HabitamaUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    ScreenShell("行動目標の管理", onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("目標は月に一度を目安に見直しましょう。変更内容は保存後すぐに反映されます。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(state.activeGoals, key = { it.id }) { goal ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onEdit(goal.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                        GoalTypeIcon(goal.icon, goal.growthType, Modifier.size(30.dp), growthColor(goal.growthType))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(goal.title, fontWeight = FontWeight.Bold)
                            Text(
                                "${goal.targetValue}${goal.unit}${if (goal.evaluationMode == GoalEvaluationMode.AT_MOST) "以内" else "以上"}・${growthName(goal.growthType)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.Rounded.Edit, contentDescription = "変更", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (state.activeGoals.size < MAX_ACTIVE_GOALS) {
                item {
                    OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(18.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("行動を追加（あと${MAX_ACTIVE_GOALS - state.activeGoals.size}つ）")
                    }
                }
            }
            if (state.pendingGoals.isNotEmpty()) {
                item { Text("以前に予約した変更（明日から）：${state.pendingGoals.joinToString { it.title }}", color = HabitamaPrimaryDark) }
            }
        }
    }
}

@Composable
private fun SettingsLinkCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = HabitamaPrimary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatCard(type: String, points: Int) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            GrowthIcon(type, Modifier.size(30.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(growthName(type), fontWeight = FontWeight.Bold)
                Text("Lv.${points / 100 + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("$points pt", color = growthColor(type), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun GoalEditorScreen(
    heading: String,
    description: String,
    initialGoal: GoalEntity?,
    saveLabel: String,
    errorMessage: String?,
    onClearError: () -> Unit,
    onBack: () -> Unit,
    onSave: (GoalDraft) -> Unit,
) {
    var title by remember(initialGoal?.id) { mutableStateOf(initialGoal?.title.orEmpty()) }
    var target by remember(initialGoal?.id) { mutableStateOf(initialGoal?.targetValue?.toString().orEmpty()) }
    var unit by remember(initialGoal?.id) { mutableStateOf(initialGoal?.unit.orEmpty()) }
    var type by remember(initialGoal?.id) { mutableStateOf(initialGoal?.growthType ?: GrowthType.DISCIPLINE) }
    var icon by remember(initialGoal?.id) { mutableStateOf(initialGoal?.icon ?: "✓") }
    var evaluationMode by remember(initialGoal?.id) { mutableStateOf(initialGoal?.evaluationMode ?: GoalEvaluationMode.AT_LEAST) }
    var templateCategory by rememberSaveable(initialGoal?.id) { mutableStateOf("recommended") }
    val parsed = target.toLongOrNull()
    val valid = title.isNotBlank() && unit.isNotBlank() && parsed != null && parsed in 1..MAX_INPUT_VALUE
    ScreenShell("行動の設定", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Spacer(Modifier.height(8.dp))
            Text(heading, style = MaterialTheme.typography.headlineMedium)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("サンプルから入力", style = MaterialTheme.typography.labelLarge)
            TemplateCategorySelector(templateCategory) { templateCategory = it }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                templatesFor(templateCategory).forEach { template ->
                    OutlinedButton(onClick = {
                        title = template.draft.title; target = template.draft.targetValue.toString(); unit = template.draft.unit
                        type = template.draft.growthType; icon = template.draft.icon; evaluationMode = template.draft.evaluationMode; onClearError()
                    }, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.heightIn(min = 48.dp).testTag("template_${template.draft.icon}")) {
                        GoalTypeIcon(template.draft.icon, template.draft.growthType, Modifier.size(24.dp), growthColor(template.draft.growthType))
                        Spacer(Modifier.width(8.dp))
                        Text(template.draft.title)
                    }
                }
            }
            OutlinedTextField(title, { title = it.take(40); onClearError() }, Modifier.fillMaxWidth().testTag("goal_title"), label = { Text("行動の名前") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(target, { target = it.filter(Char::isDigit).take(9); onClearError() }, Modifier.weight(1f).testTag("goal_target"), label = { Text("目標値") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(unit, { unit = it.take(10); onClearError() }, Modifier.weight(1f).testTag("goal_unit"), label = { Text("単位") }, singleLine = true)
            }
            Text("よく使う単位", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                commonGoalUnits.chunked(4).forEach { rowUnits ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowUnits.forEach { candidate ->
                            FilterChip(
                                selected = unit == candidate,
                                onClick = { unit = candidate; onClearError() },
                                label = { Text(candidate) },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("unit_$candidate"),
                            )
                        }
                        repeat(4 - rowUnits.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            Text("達成のしかた", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = evaluationMode == GoalEvaluationMode.AT_LEAST,
                    onClick = { evaluationMode = GoalEvaluationMode.AT_LEAST },
                    label = { Text("目標以上をめざす") },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("mode_at_least"),
                )
                FilterChip(
                    selected = evaluationMode == GoalEvaluationMode.AT_MOST,
                    onClick = { evaluationMode = GoalEvaluationMode.AT_MOST },
                    label = { Text("目標以内におさめる") },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("mode_at_most"),
                )
            }
            Text(
                if (evaluationMode == GoalEvaluationMode.AT_MOST) "入力した実績が目標値以下なら達成です。超えた場合は割合に応じて評価します。"
                else "入力した実績が目標値に近づくほど評価され、超過分は120%まで反映されます。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text("この行動で育つ力", style = MaterialTheme.typography.labelLarge)
            GrowthType.all.forEach { candidate ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 50.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (type == candidate) growthColor(candidate).copy(alpha = .12f) else Color.Transparent)
                        .clickable { type = candidate; icon = growthToken(candidate) }.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GrowthIcon(candidate, Modifier.size(24.dp)); Spacer(Modifier.width(10.dp)); Text(growthName(candidate), Modifier.weight(1f))
                    if (type == candidate) Icon(Icons.Rounded.Check, contentDescription = null, tint = growthColor(candidate), modifier = Modifier.size(20.dp))
                }
            }
            ErrorText(errorMessage)
            PrimaryButton(saveLabel, valid, "save_goal") { onSave(GoalDraft(title, parsed ?: 0, unit, type, icon, evaluationMode)) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MainScreen(
    title: String,
    selected: String,
    onTab: (String) -> Unit,
    onSettings: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    BotanicalBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { AppHeader(title, onSettings) },
            bottomBar = { BottomBar(selected, onTab) },
            content = content,
        )
    }
}

@Composable
private fun ScreenShell(title: String, onBack: (() -> Unit)?, content: @Composable (PaddingValues) -> Unit) {
    BotanicalBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "戻る") }
                    } else Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleLarge)
                }
            },
            content = content,
        )
    }
}

@Composable
internal fun AppHeader(title: String, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 68.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Spa, contentDescription = null, tint = HabitamaPrimary, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onSettings, modifier = Modifier.size(48.dp).testTag("settings_button")) {
            Icon(Icons.Rounded.Settings, contentDescription = "設定", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BottomBar(selected: String, onTab: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = .97f)).height(72.dp).padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            Triple(ROUTE_HOME, Icons.Rounded.Home, "ホーム"),
            Triple(ROUTE_CALENDAR, Icons.Rounded.CalendarMonth, "記録"),
            Triple(ROUTE_GROWTH, Icons.AutoMirrored.Rounded.ShowChart, "成長"),
        ).forEach { (route, icon, label) ->
            Column(
                Modifier.weight(1f).heightIn(min = 54.dp).clickable { onTab(route) },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(23.dp), tint = if (selected == route) HabitamaPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(label, fontSize = 12.sp, fontWeight = if (selected == route) FontWeight.Bold else FontWeight.Normal, color = if (selected == route) HabitamaPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BotanicalBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(HabitamaBackground, Color(0xFFFFFDF7), Color(0xFFF3F4DF))))) {
        Canvas(Modifier.fillMaxSize()) {
            val leaf = HabitamaLeaf.copy(alpha = .28f)
            listOf(Offset(size.width * .06f, size.height * .82f), Offset(size.width * .9f, size.height * .18f), Offset(size.width * .82f, size.height * .9f)).forEachIndexed { i, base ->
                drawLine(leaf, base, base + Offset(if (i == 1) 45f else 60f, -110f), strokeWidth = 5f, cap = StrokeCap.Round)
                repeat(4) { n ->
                    val y = base.y - 22f * n
                    drawOval(leaf, topLeft = Offset(base.x + if (n % 2 == 0) 10f else 30f, y - 22f), size = androidx.compose.ui.geometry.Size(30f, 18f))
                }
            }
        }
        content()
    }
}

@Composable
private fun ProgressRing(percentage: Int, goal: GoalEntity) {
    Box(Modifier.size(66.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawArc(HabitamaLine, -90f, 360f, false, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
            drawArc(HabitamaPrimary, -90f, (percentage.coerceIn(0, 100) * 3.6f), false, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
        }
        GoalTypeIcon(goal.icon, goal.growthType, Modifier.size(28.dp), growthColor(goal.growthType))
    }
}

@Composable
private fun GrowthStrip(stats: GrowthStatsEntity) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6E7))) {
        Column(Modifier.padding(16.dp)) {
            Text("成長ステータス", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GrowthType.all.forEach { type ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GrowthIcon(type, Modifier.size(23.dp))
                        Text(growthName(type), fontSize = 10.sp)
                        Text(stats.valueOf(type).toString(), fontSize = 12.sp, color = growthColor(type), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String, status: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        Text(status, Modifier.background(HabitamaSuccess.copy(alpha = .13f), CircleShape).padding(horizontal = 10.dp, vertical = 5.dp), color = HabitamaSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepDots(active: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { index ->
            Box(Modifier.size(if (index == active) 10.dp else 8.dp).background(if (index <= active) HabitamaPrimary else HabitamaLine, CircleShape))
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean, tag: String, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).shadow(5.dp, RoundedCornerShape(20.dp)).testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HabitamaPrimary, disabledContainerColor = HabitamaLine),
    ) { Text(text, style = MaterialTheme.typography.titleMedium) }
}

@Composable
private fun RoundAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.size(48.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun EggBadge(points: Int, large: Boolean = false) {
    val size = if (large) 76.dp else 54.dp
    Box(Modifier.size(size).background(Color(0xFFFFE7B8), CircleShape).border(1.dp, Color(0xFFF2C77E), CircleShape), contentAlignment = Alignment.Center) {
        HabitamaEgg(Modifier.size(if (large) 48.dp else 34.dp).semantics { contentDescription = "ハビタマ 累積$points ポイント" })
    }
}

@Composable
private fun EggGlow() {
    Box(Modifier.size(150.dp).background(Brush.radialGradient(listOf(Color(0xFFFFD879), Color.Transparent)), CircleShape), contentAlignment = Alignment.Center) {
        HabitamaEgg(Modifier.size(82.dp))
    }
}

@Composable
private fun EncouragementCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F5E8)), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Spa, contentDescription = null, tint = HabitamaSuccess, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Text("いい感じだよ！\nその調子で続けていこう。", color = HabitamaPrimaryDark)
        }
    }
}

@Composable
private fun ErrorText(message: String?) {
    if (message != null) Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp).testTag("error_message"))
}

private fun resultMessage(records: List<DailyGoalRecordEntity>): String = when {
    records.isEmpty() -> "記録ありがとう"
    records.all { it.displayPercentage >= 100 } -> "すべて達成！"
    records.any { it.displayPercentage >= 100 } -> "いい調子！"
    else -> "今日も育った！"
}

private fun growthName(type: String): String = when (type) {
    GrowthType.VITALITY -> "体力"
    GrowthType.INTELLIGENCE -> "知能"
    GrowthType.BEAUTY -> "美しさ"
    GrowthType.RECOVERY -> "回復力"
    else -> "規律"
}

@Composable
private fun GrowthIcon(type: String, modifier: Modifier = Modifier) {
    Icon(growthImage(type), contentDescription = growthName(type), modifier = modifier, tint = growthColor(type))
}

@Composable
private fun GoalTypeIcon(token: String, growthType: String, modifier: Modifier = Modifier, tint: Color = growthColor(growthType)) {
    val image = when (token) {
        "walk", "👣" -> Icons.AutoMirrored.Rounded.DirectionsWalk
        "book", "📖" -> Icons.AutoMirrored.Rounded.MenuBook
        "cln", "clean", "🧹" -> Icons.Rounded.CleaningServices
        "fitness" -> Icons.Rounded.FitnessCenter
        "stretch", "meditate" -> Icons.Rounded.SelfImprovement
        "sleep" -> Icons.Rounded.Bedtime
        "water" -> Icons.Rounded.WaterDrop
        "food" -> Icons.Rounded.Restaurant
        "study" -> Icons.Rounded.School
        "journal" -> Icons.Rounded.Edit
        "saving" -> Icons.Rounded.Savings
        "wallet" -> Icons.Rounded.AccountBalanceWallet
        else -> growthImage(growthType)
    }
    Icon(image, contentDescription = null, modifier = modifier, tint = tint)
}

private fun growthImage(type: String): ImageVector = when (type) {
    GrowthType.VITALITY -> Icons.Rounded.Favorite
    GrowthType.INTELLIGENCE -> Icons.AutoMirrored.Rounded.MenuBook
    GrowthType.BEAUTY -> Icons.Rounded.AutoAwesome
    GrowthType.RECOVERY -> Icons.Rounded.WaterDrop
    else -> Icons.Rounded.Shield
}

private fun growthToken(type: String): String = when (type) {
    GrowthType.VITALITY -> "vitality"
    GrowthType.INTELLIGENCE -> "intelligence"
    GrowthType.BEAUTY -> "beauty"
    GrowthType.RECOVERY -> "recovery"
    else -> "discipline"
}

@Composable
private fun HabitamaEgg(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val egg = Path().apply {
            moveTo(size.width * .5f, size.height * .05f)
            cubicTo(size.width * .28f, size.height * .08f, size.width * .12f, size.height * .48f, size.width * .16f, size.height * .7f)
            cubicTo(size.width * .2f, size.height * .94f, size.width * .8f, size.height * .94f, size.width * .84f, size.height * .7f)
            cubicTo(size.width * .88f, size.height * .48f, size.width * .72f, size.height * .08f, size.width * .5f, size.height * .05f)
            close()
        }
        drawPath(egg, Color(0xFFFFF7E4))
        clipPath(egg) {
            drawCircle(Color(0xFF2F968A), radius = size.minDimension * .145f, center = Offset(size.width * .31f, size.height * .45f))
            drawCircle(Color(0xFFEF7182), radius = size.minDimension * .13f, center = Offset(size.width * .7f, size.height * .57f))
            drawCircle(Color(0xFFF3B65A), radius = size.minDimension * .105f, center = Offset(size.width * .44f, size.height * .77f))
            drawCircle(Color(0xFF79BBA8), radius = size.minDimension * .08f, center = Offset(size.width * .61f, size.height * .24f))
        }
        drawOval(
            Color.White.copy(alpha = .72f),
            topLeft = Offset(size.width * .32f, size.height * .2f),
            size = androidx.compose.ui.geometry.Size(size.width * .1f, size.height * .2f),
        )
        drawPath(
            egg,
            Color(0xFF287F76),
            style = Stroke(width = size.minDimension * .035f, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun weekdayHeaderColor(index: Int): Color = when (index) {
    5 -> HabitamaBlue
    6 -> Color(0xFFC94F5B)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun growthColor(type: String): Color = when (type) {
    GrowthType.VITALITY -> HabitamaRose
    GrowthType.INTELLIGENCE -> HabitamaBlue
    GrowthType.BEAUTY -> HabitamaAccent
    GrowthType.RECOVERY -> Color(0xFF4C9FBC)
    else -> HabitamaSuccess
}

private fun deviceCalendarColor(color: Int): Color = if (color == 0) HabitamaBlue else Color(color)

private fun deviceCalendarTimeLabel(event: DeviceCalendarEvent): String {
    if (event.allDay) return "終日"
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(event.startMillis).atZone(zone)
    val end = Instant.ofEpochMilli(event.endMillis).atZone(zone)
    return if (start.toLocalDate() == end.toLocalDate()) {
        "${start.format(DateTimeFormatter.ofPattern("H:mm"))}〜${end.format(DateTimeFormatter.ofPattern("H:mm"))}"
    } else {
        "${start.format(DateTimeFormatter.ofPattern("M/d H:mm"))}〜${end.format(DateTimeFormatter.ofPattern("M/d H:mm"))}"
    }
}

private fun openDeviceCalendarEvent(context: Context, event: DeviceCalendarEvent) {
    val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId)
    val intent = Intent(Intent.ACTION_VIEW, uri)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startMillis)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endMillis)
    runCatching { context.startActivity(intent) }
}

private fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPANESE))
