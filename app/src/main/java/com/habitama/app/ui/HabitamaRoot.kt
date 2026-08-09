package com.habitama.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habitama.app.data.DailyGoalRecordEntity
import com.habitama.app.data.GoalEntity
import com.habitama.app.domain.MAX_INPUT_VALUE
import com.habitama.app.ui.theme.HabitamaAccent
import com.habitama.app.ui.theme.HabitamaBackground
import com.habitama.app.ui.theme.HabitamaPrimary
import com.habitama.app.ui.theme.HabitamaSuccess
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val ROUTE_GOAL_CREATE = "goal/create"
private const val ROUTE_GOAL_EDIT = "goal/edit"
private const val ROUTE_HOME = "home"
private const val ROUTE_RESULT = "result"
private const val ROUTE_HISTORY = "history"

@Composable
fun HabitamaRoot(viewModel: HabitamaViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(HabitamaBackground),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (state.activeGoal == null) ROUTE_GOAL_CREATE else ROUTE_HOME,
    ) {
        composable(ROUTE_GOAL_CREATE) {
            GoalEditorScreen(
                title = "最初の行動を決めよう",
                description = "結果ではなく、今日できる行動を1つ選びます。",
                initialGoal = null,
                pendingLabel = null,
                errorMessage = state.errorMessage,
                onClearError = viewModel::clearError,
                onBack = null,
                onSave = { goalTitle, target, unit ->
                    viewModel.createGoal(goalTitle, target, unit) {
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_GOAL_CREATE) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(ROUTE_GOAL_EDIT) {
            val initial = state.pendingGoal ?: state.activeGoal
            GoalEditorScreen(
                title = "明日からの目標を変更",
                description = "今日の目標と記録は変わりません。",
                initialGoal = initial,
                pendingLabel = state.pendingGoal?.let { "変更予約済み" },
                errorMessage = state.errorMessage,
                onClearError = viewModel::clearError,
                onBack = navController::popBackStack,
                onSave = { goalTitle, target, unit ->
                    viewModel.scheduleGoalUpdate(goalTitle, target, unit) {
                        navController.popBackStack()
                    }
                },
            )
        }
        composable(ROUTE_HOME) {
            val goal = state.activeGoal
            if (goal == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(ROUTE_GOAL_CREATE) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                }
            } else {
                HomeScreen(
                    today = state.today,
                    goal = goal,
                    pendingGoal = state.pendingGoal,
                    record = state.todayRecord,
                    totalEnergy = state.totalEnergy,
                    errorMessage = state.errorMessage,
                    onClearError = viewModel::clearError,
                    onSave = { actual ->
                        viewModel.saveTodayRecord(actual) {
                            navController.navigate(ROUTE_RESULT)
                        }
                    },
                    onEditGoal = { navController.navigate(ROUTE_GOAL_EDIT) },
                    onHistory = { navController.navigate(ROUTE_HISTORY) },
                )
            }
        }
        composable(ROUTE_RESULT) {
            ResultScreen(
                record = state.todayRecord,
                totalEnergy = state.totalEnergy,
                onHome = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                },
            )
        }
        composable(ROUTE_HISTORY) {
            HistoryScreen(state.history, navController::popBackStack)
        }
    }
}

@Composable
internal fun GoalEditorScreen(
    title: String,
    description: String,
    initialGoal: GoalEntity?,
    pendingLabel: String?,
    errorMessage: String?,
    onClearError: () -> Unit,
    onBack: (() -> Unit)?,
    onSave: (String, Long, String) -> Unit,
) {
    var goalTitle by remember(initialGoal?.id) { mutableStateOf(initialGoal?.title.orEmpty()) }
    var target by remember(initialGoal?.id) { mutableStateOf(initialGoal?.targetValue?.toString().orEmpty()) }
    var unit by remember(initialGoal?.id) { mutableStateOf(initialGoal?.unit.orEmpty()) }
    val parsedTarget = target.toLongOrNull()
    val valid = goalTitle.trim().isNotEmpty() &&
        unit.trim().isNotEmpty() &&
        parsedTarget != null && parsedTarget in 1..MAX_INPUT_VALUE

    ScreenScaffold(title = "ハビタマ", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (pendingLabel != null) {
                StatusPill(pendingLabel)
            }
            Text("たとえば", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExampleButton("6,000歩") {
                    goalTitle = "歩く"; target = "6000"; unit = "歩"
                }
                ExampleButton("20分") {
                    goalTitle = "運動する"; target = "20"; unit = "分"
                }
                ExampleButton("10回") {
                    goalTitle = "取り組む"; target = "10"; unit = "回"
                }
            }
            OutlinedTextField(
                value = goalTitle,
                onValueChange = { goalTitle = it.take(40); onClearError() },
                modifier = Modifier.fillMaxWidth().testTag("goal_title"),
                label = { Text("行動の名前") },
                supportingText = { Text("例：歩く、勉強する、ストレッチする") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = target,
                onValueChange = { target = it.filter(Char::isDigit).take(9); onClearError() },
                modifier = Modifier.fillMaxWidth().testTag("goal_target"),
                label = { Text("1日の目標値") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it.take(10); onClearError() },
                modifier = Modifier.fillMaxWidth().testTag("goal_unit"),
                label = { Text("単位") },
                supportingText = { Text("例：歩、回、分") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            ErrorText(errorMessage)
            Button(
                onClick = { onSave(goalTitle, parsedTarget ?: 0, unit) },
                enabled = valid,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("save_goal"),
            ) {
                Text(if (initialGoal == null) "この行動で始める" else "明日から変更する")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeScreen(
    today: LocalDate,
    goal: GoalEntity,
    pendingGoal: GoalEntity?,
    record: DailyGoalRecordEntity?,
    totalEnergy: Int,
    errorMessage: String?,
    onClearError: () -> Unit,
    onSave: (Long) -> Unit,
    onEditGoal: () -> Unit,
    onHistory: () -> Unit,
) {
    var actual by remember(record?.updatedAtEpochMillis) { mutableStateOf(record?.actualValue?.toString().orEmpty()) }
    val parsedActual = actual.toLongOrNull()
    val valid = parsedActual != null && parsedActual in 0..MAX_INPUT_VALUE

    ScreenScaffold(title = "ハビタマ") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(formatDate(today), color = MaterialTheme.colorScheme.onSurfaceVariant)
            EnergySummary(totalEnergy)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("今日の行動", style = MaterialTheme.typography.labelLarge, color = HabitamaPrimary)
                    Text(goal.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("目標 ${goal.targetValue} ${goal.unit}", style = MaterialTheme.typography.titleMedium)
                    if (record != null) {
                        StatusPill("記録済み ${record.displayPercentage}%")
                    }
                }
            }
            OutlinedTextField(
                value = actual,
                onValueChange = { actual = it.filter(Char::isDigit).take(9); onClearError() },
                modifier = Modifier.fillMaxWidth().testTag("actual_value"),
                label = { Text("今日できた分") },
                suffix = { Text(goal.unit) },
                supportingText = { Text("0でも記録できます。できた分だけ残ります。") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
            ErrorText(errorMessage)
            Button(
                onClick = { onSave(parsedActual ?: 0) },
                enabled = valid,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("save_record"),
            ) {
                Text(if (record == null) "できた分を記録" else "今日の記録を更新")
            }
            if (pendingGoal != null) {
                Text(
                    "明日から：${pendingGoal.title} ${pendingGoal.targetValue}${pendingGoal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onEditGoal, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("明日からの目標を変更")
                }
                TextButton(onClick = onHistory, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("7日間の記録")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResultScreen(record: DailyGoalRecordEntity?, totalEnergy: Int, onHome: () -> Unit) {
    ScreenScaffold(title = "今日の結果") { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (record == null) {
                Text("今日の記録はまだありません")
            } else {
                EnergyCore(record.displayPercentage)
                Spacer(Modifier.height(24.dp))
                Text("${record.actualValue} / ${record.targetValueSnapshot} ${record.unitSnapshot}")
                Text(
                    "${record.displayPercentage}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = HabitamaPrimary,
                )
                Text(
                    "+${record.energyEarned} エネルギー",
                    style = MaterialTheme.typography.titleLarge,
                    color = HabitamaSuccess,
                )
                Spacer(Modifier.height(8.dp))
                Text("累積 $totalEnergy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (record.displayPercentage < 100) {
                    Spacer(Modifier.height(12.dp))
                    Text("できた分が、今日の成長になりました。", textAlign = TextAlign.Center)
                } else if (record.displayPercentage > 100) {
                    Spacer(Modifier.height(12.dp))
                    Text("目標を超えた分も、上限の範囲で加算しました。", textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("ホームへ")
            }
        }
    }
}

@Composable
private fun HistoryScreen(history: List<HistoryDay>, onBack: () -> Unit) {
    ScreenScaffold(title = "7日間の記録", onBack = onBack) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp)) {
            Text(
                "記録がない日も失敗ではありません。",
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            history.forEachIndexed { index, day ->
                HistoryRow(day)
                if (index < history.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun HistoryRow(day: HistoryDay) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(formatDate(day.date), modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        val record = day.record
        if (record == null) {
            Text("記録なし", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text("${record.actualValue}${record.unitSnapshot}  ${record.displayPercentage}%")
                Text("+${record.energyEarned}", color = HabitamaSuccess)
            }
        }
    }
}

@Composable
private fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = HabitamaBackground,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("戻る") }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        content = content,
    )
}

@Composable
private fun EnergySummary(totalEnergy: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(HabitamaPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(20.dp).background(HabitamaSuccess, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("累積エネルギー", style = MaterialTheme.typography.labelMedium)
            Text(totalEnergy.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EnergyCore(percentage: Int) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .background(HabitamaPrimary.copy(alpha = 0.12f), CircleShape)
            .semantics { contentDescription = "達成率 $percentage パーセント" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(96.dp).background(HabitamaPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(48.dp).background(if (percentage >= 100) HabitamaAccent else HabitamaSuccess, CircleShape))
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Text(
        text,
        modifier = Modifier.background(HabitamaSuccess.copy(alpha = 0.12f), RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        color = HabitamaSuccess,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
private fun ExampleButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.heightIn(min = 48.dp)) {
        Text(text)
    }
}

@Composable
private fun ErrorText(errorMessage: String?) {
    if (errorMessage != null) {
        Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("error_message"))
    }
}

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPANESE))
