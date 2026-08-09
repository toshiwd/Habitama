package com.habitama.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.habitama.app.domain.MAX_INPUT_VALUE
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
import java.time.YearMonth
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

private data class GoalTemplate(val draft: GoalDraft, val subtitle: String)

private val templates = listOf(
    GoalTemplate(GoalDraft("6,000歩あるく", 6_000, "歩", GrowthType.VITALITY, "👣"), "からだを動かす習慣"),
    GoalTemplate(GoalDraft("単語を5個おぼえる", 5, "個", GrowthType.INTELLIGENCE, "📖"), "小さく学びを積み重ねる"),
    GoalTemplate(GoalDraft("10分片づける", 10, "分", GrowthType.DISCIPLINE, "🧹"), "暮らしを整える習慣"),
)

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
            MainScreen(title = "ホーム", selected = ROUTE_HOME, onTab = { nav.navigateSingleTop(it) }) { padding ->
                HomeScreen(
                    state = state,
                    padding = padding,
                    onReport = { nav.navigate(ROUTE_REPORT) },
                    onAdd = { nav.navigate(ROUTE_GOAL_ADD) },
                    onEdit = { nav.navigate("goal/edit/$it") },
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
            MainScreen(title = "行動カレンダー", selected = ROUTE_CALENDAR, onTab = { nav.navigateSingleTop(it) }) { padding ->
                CalendarScreen(state, padding)
            }
        }
        composable(ROUTE_GROWTH) {
            MainScreen(title = "ハビタマの成長", selected = ROUTE_GROWTH, onTab = { nav.navigateSingleTop(it) }) { padding ->
                GrowthScreen(state, padding)
            }
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
                heading = "明日からの行動を変更",
                description = "今日の目標と記録は変わりません。",
                initialGoal = goal,
                saveLabel = "明日から変更する",
                errorMessage = state.errorMessage,
                onClearError = viewModel::clearError,
                onBack = nav::popBackStack,
            ) { draft -> viewModel.scheduleGoalUpdate(goalId, draft) { nav.popBackStack() } }
        }
    }
}

private fun androidx.navigation.NavController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true; popUpTo(ROUTE_HOME) }
}

@Composable
private fun OnboardingScreen(errorMessage: String?, onClearError: () -> Unit, onSave: (List<GoalDraft>) -> Unit) {
    val selected = remember { mutableStateListOf(0, 1, 2) }
    BotanicalBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepDots(0)
            Spacer(Modifier.height(28.dp))
            Text("何を続ける？", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("結果ではなく、毎日の行動を選ぼう", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            templates.forEachIndexed { index, template ->
                TemplateCard(template, index in selected) {
                    onClearError()
                    if (index in selected) selected.remove(index) else if (selected.size < MAX_ACTIVE_GOALS) selected.add(index)
                }
                Spacer(Modifier.height(14.dp))
            }
            Text("${selected.size} / $MAX_ACTIVE_GOALS 選択中", color = HabitamaPrimary, fontWeight = FontWeight.Bold)
            ErrorText(errorMessage)
            Spacer(Modifier.height(18.dp))
            PrimaryButton("これで始める", selected.isNotEmpty(), "save_goal") {
                onSave(selected.sorted().map { templates[it].draft })
            }
            Spacer(Modifier.height(24.dp))
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
            Text(template.draft.icon, fontSize = 30.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(template.draft.title, style = MaterialTheme.typography.titleMedium)
                Text(template.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                Modifier.size(26.dp).border(2.dp, if (selected) HabitamaPrimary else HabitamaLine, CircleShape)
                    .background(if (selected) HabitamaPrimary else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) { if (selected) Text("✓", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun HomeScreen(state: HabitamaUiState, padding: PaddingValues, onReport: () -> Unit, onAdd: () -> Unit, onEdit: (Long) -> Unit) {
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
            GoalProgressCard(goal, recordMap[goal.id], onClick = { onEdit(goal.id) })
        }
        if (state.activeGoals.size < MAX_ACTIVE_GOALS) {
            item {
                OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(18.dp)) {
                    Text("＋ 行動を追加（あと${MAX_ACTIVE_GOALS - state.activeGoals.size}つ）")
                }
            }
        }
        item { GrowthStrip(state.growthStats) }
        if (state.pendingGoals.isNotEmpty()) {
            item {
                Text("明日から変更予定：${state.pendingGoals.joinToString { it.title }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            PrimaryButton(if (state.todayRecords.isEmpty()) "今日の報告" else "今日の報告を更新", true, "open_report", onReport)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GoalProgressCard(goal: GoalEntity, record: DailyGoalRecordEntity?, onClick: () -> Unit) {
    val percentage = record?.displayPercentage ?: 0
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(percentage, goal.icon)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                if (record == null) {
                    Text("目標 ${goal.targetValue}${goal.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(record.actualValue.toString(), fontSize = 23.sp, fontWeight = FontWeight.Bold, color = HabitamaPrimaryDark)
                        Text(" / ${record.targetValueSnapshot}${record.unitSnapshot}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text("›", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text(goal.icon, fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Text(goal.title, style = MaterialTheme.typography.titleMedium, color = growthColor(goal.growthType))
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoundAction("−") { onValue((actual - step).coerceAtLeast(0).toString()) }
                OutlinedTextField(
                    value = value,
                    onValueChange = { onValue(it.filter(Char::isDigit).take(9)) },
                    modifier = Modifier.weight(1f).testTag("actual_${goal.id}"),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                    suffix = { Text("/ ${goal.targetValue}${goal.unit}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                )
                RoundAction("＋") { onValue((actual + step).coerceAtMost(MAX_INPUT_VALUE).toString()) }
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
            Text("達成率 ${(actual * 100 / goal.targetValue).coerceAtLeast(0)}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Row(Modifier.fillMaxWidth()) {
                            Text("${growthIcon(gain.growthType)} ${growthName(gain.growthType)}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
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
private fun CalendarScreen(state: HabitamaUiState, padding: PaddingValues) {
    val month = YearMonth.from(state.today)
    val byDate = state.history.associateBy { it.date }
    val leading = month.atDay(1).dayOfWeek.value - 1
    val cells = List(leading) { null } + (1..month.lengthOfMonth()).map(month::atDay)
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("${month.year}年 ${month.monthValue}月", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) { listOf("月", "火", "水", "木", "金", "土", "日").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                Spacer(Modifier.height(10.dp))
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                                if (date != null) CalendarDay(date, byDate[date]?.records.orEmpty(), date == state.today)
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("最近の記録", style = MaterialTheme.typography.titleLarge)
        Text("記録がない日も失敗ではありません。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        state.history.take(7).forEach { day -> HistoryRow(day) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CalendarDay(date: LocalDate, records: List<DailyGoalRecordEntity>, today: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(30.dp).background(if (today) HabitamaPrimary.copy(alpha = .16f) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(date.dayOfMonth.toString(), fontWeight = if (today) FontWeight.Bold else FontWeight.Normal) }
        if (records.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { records.take(3).forEach { Box(Modifier.size(4.dp).background(if (it.displayPercentage >= 100) HabitamaPrimary else HabitamaLeaf, CircleShape)) } }
    }
}

@Composable
private fun HistoryRow(day: HistoryDay) {
    Row(Modifier.fillMaxWidth().heightIn(min = 58.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(formatDate(day.date), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
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
        Text("行動ごとに育つ力が決まります。目標を超えた分は120%までポイントに反映されます。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun StatCard(type: String, points: Int) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(growthIcon(type), fontSize = 28.sp)
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
    val parsed = target.toLongOrNull()
    val valid = title.isNotBlank() && unit.isNotBlank() && parsed != null && parsed in 1..MAX_INPUT_VALUE
    ScreenShell("行動の設定", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Spacer(Modifier.height(8.dp))
            Text(heading, style = MaterialTheme.typography.headlineMedium)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("かんたん設定", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                templates.forEach { template ->
                    OutlinedButton(onClick = {
                        title = template.draft.title; target = template.draft.targetValue.toString(); unit = template.draft.unit
                        type = template.draft.growthType; icon = template.draft.icon; onClearError()
                    }, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.heightIn(min = 48.dp)) { Text(template.draft.icon) }
                }
            }
            OutlinedTextField(title, { title = it.take(40); onClearError() }, Modifier.fillMaxWidth().testTag("goal_title"), label = { Text("行動の名前") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(target, { target = it.filter(Char::isDigit).take(9); onClearError() }, Modifier.weight(1f).testTag("goal_target"), label = { Text("目標値") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(unit, { unit = it.take(10); onClearError() }, Modifier.weight(1f).testTag("goal_unit"), label = { Text("単位") }, singleLine = true)
            }
            Text("この行動で育つ力", style = MaterialTheme.typography.labelLarge)
            GrowthType.all.forEach { candidate ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 50.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (type == candidate) growthColor(candidate).copy(alpha = .12f) else Color.Transparent)
                        .clickable { type = candidate; icon = growthIcon(candidate) }.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(growthIcon(candidate)); Spacer(Modifier.width(10.dp)); Text(growthName(candidate), Modifier.weight(1f))
                    if (type == candidate) Text("✓", color = growthColor(candidate), fontWeight = FontWeight.Bold)
                }
            }
            ErrorText(errorMessage)
            PrimaryButton(saveLabel, valid, "save_goal") { onSave(GoalDraft(title, parsed ?: 0, unit, type, icon)) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MainScreen(title: String, selected: String, onTab: (String) -> Unit, content: @Composable (PaddingValues) -> Unit) {
    BotanicalBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { AppHeader(title) },
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
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("‹ 戻る") } else Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleLarge)
                }
            },
            content = content,
        )
    }
}

@Composable
private fun AppHeader(title: String) {
    Row(Modifier.fillMaxWidth().heightIn(min = 68.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("🌱", fontSize = 26.sp)
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun BottomBar(selected: String, onTab: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = .97f)).height(72.dp).padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(Triple(ROUTE_HOME, "⌂", "ホーム"), Triple(ROUTE_CALENDAR, "▦", "記録"), Triple(ROUTE_GROWTH, "♢", "成長")).forEach { (route, icon, label) ->
            Column(
                Modifier.weight(1f).heightIn(min = 54.dp).clickable { onTab(route) },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                Text(icon, fontSize = 22.sp, color = if (selected == route) HabitamaPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun ProgressRing(percentage: Int, icon: String) {
    Box(Modifier.size(66.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawArc(HabitamaLine, -90f, 360f, false, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
            drawArc(HabitamaPrimary, -90f, (percentage.coerceIn(0, 100) * 3.6f), false, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
        }
        Text(icon, fontSize = 25.sp)
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
                        Text(growthIcon(type), fontSize = 22.sp)
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
private fun RoundAction(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.size(48.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) { Text(text, fontSize = 22.sp) }
}

@Composable
private fun EggBadge(points: Int, large: Boolean = false) {
    val size = if (large) 76.dp else 54.dp
    Box(Modifier.size(size).background(Color(0xFFFFE7B8), CircleShape).border(1.dp, Color(0xFFF2C77E), CircleShape), contentAlignment = Alignment.Center) {
        Text("🥚", fontSize = if (large) 42.sp else 29.sp, modifier = Modifier.semantics { contentDescription = "ハビタマ 累積$points ポイント" })
    }
}

@Composable
private fun EggGlow() {
    Box(Modifier.size(150.dp).background(Brush.radialGradient(listOf(Color(0xFFFFD879), Color.Transparent)), CircleShape), contentAlignment = Alignment.Center) { Text("🥚", fontSize = 78.sp) }
}

@Composable
private fun EncouragementCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F5E8)), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🌿", fontSize = 25.sp); Spacer(Modifier.width(10.dp)); Text("いい感じだよ！\nその調子で続けていこう。", color = HabitamaPrimaryDark)
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

private fun growthIcon(type: String): String = when (type) {
    GrowthType.VITALITY -> "♥"
    GrowthType.INTELLIGENCE -> "📘"
    GrowthType.BEAUTY -> "✦"
    GrowthType.RECOVERY -> "💧"
    else -> "🛡"
}

private fun growthColor(type: String): Color = when (type) {
    GrowthType.VITALITY -> HabitamaRose
    GrowthType.INTELLIGENCE -> HabitamaBlue
    GrowthType.BEAUTY -> HabitamaAccent
    GrowthType.RECOVERY -> Color(0xFF4C9FBC)
    else -> HabitamaSuccess
}

private fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPANESE))
