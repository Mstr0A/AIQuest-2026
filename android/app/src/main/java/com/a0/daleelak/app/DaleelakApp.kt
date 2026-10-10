package com.a0.daleelak.app

import com.a0.daleelak.ui.LocalUiStrings
import com.a0.daleelak.ui.LocalUiPreferences

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a0.daleelak.domain.OperationStatus
import com.a0.daleelak.features.assistant.AssistantScreen
import com.a0.daleelak.features.home.HomeScreen
import com.a0.daleelak.features.operations.OperationsFilter
import com.a0.daleelak.features.operations.OperationsScreen
import com.a0.daleelak.ui.components.DaleelakIcons
import com.a0.daleelak.ui.components.DaleelakLogo

@Composable
fun DaleelakApp(model: DaleelakViewModel) {
    val ui = LocalUiStrings.current
    val display = LocalUiPreferences.current
    // UI-only routes keep the assistant agent's existing ViewModel interface intact.
    var showHome by rememberSaveable { mutableStateOf(true) }
    var operationsFilter by rememberSaveable { mutableStateOf(OperationsFilter.ALL) }
    var savedDestination by rememberSaveable { mutableStateOf(model.destination.name) }
    LaunchedEffect(Unit) { model.destination = Destination.valueOf(savedDestination) }
    LaunchedEffect(model.destination) { savedDestination = model.destination.name }
    fun openOperations(filter: OperationsFilter) {
        operationsFilter = filter
        model.selectedId = null
        model.destination = Destination.OPERATIONS
        showHome = false
    }
    CompositionLocalProvider(LocalLayoutDirection provides if (ui.english) LayoutDirection.Ltr else LayoutDirection.Rtl) {
        BackHandler(enabled = !showHome) {
            if (model.selectedId != null) model.selectedId = null else showHome = true
        }
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DaleelakLogo(Modifier.size(if (showHome) 72.dp else 36.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ui.text("دليلك"), style = MaterialTheme.typography.headlineLarge,
                                fontSize = if (ui.english) { if (showHome) 32.sp else 20.sp } else { if (showHome) 64.sp else 32.sp },
                                lineHeight = if (ui.english) { if (showHome) 44.sp else 28.sp } else { if (showHome) 88.sp else 44.sp },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            Text(ui.text("DALEELAK · نسخة تجريبية"), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (!showHome) {
                        TextButton(onClick = { model.selectedId = null; showHome = true }) {
                            Icon(DaleelakIcons.Home, contentDescription = null,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(ui.text("الرئيسية"))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { model.stopVoice(); display.changeLanguage() }) {
                        Text(if (ui.english) "العربية" else "English")
                    }
                    TextButton(onClick = display.changeTextSize) {
                        Text(ui.choose("النص: ", "Text: ") + when (display.textSize) {
                            1 -> ui.choose("كبير", "Large")
                            2 -> ui.choose("أكبر", "Larger")
                            else -> ui.choose("عادي", "Default")
                        })
                    }
                }
                model.notice?.let { notice ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(ui.text(notice), Modifier.padding(12.dp))
                        TextButton(onClick = { model.notice = null }) { Text(ui.text("إغلاق")) }
                    }
                }
                if (showHome) {
                    val active = model.operations.filterNot { it.archived }
                    val finished = active.count { it.status == OperationStatus.COMPLETED }
                    HomeScreen(
                        archiveCount = model.operations.count { it.archived },
                        currentCount = active.size - finished,
                        finishedCount = finished,
                        onNewOperation = {
                            operationsFilter = OperationsFilter.ALL
                            model.selectedId = null
                            model.newConversation()
                            model.destination = Destination.ASSISTANT
                            showHome = false
                        },
                        onArchive = { openOperations(OperationsFilter.ARCHIVED) },
                        onCurrent = { openOperations(OperationsFilter.CURRENT) },
                        onFinished = { openOperations(OperationsFilter.COMPLETED) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    when (model.destination) {
                        Destination.ASSISTANT -> AssistantScreen(model, Modifier.weight(1f))
                        Destination.OPERATIONS -> OperationsScreen(model, Modifier.weight(1f), operationsFilter)
                        // Retain the old route value for restored sessions, without a Places screen.
                        Destination.LOCATIONS -> OperationsScreen(model, Modifier.weight(1f), operationsFilter)
                    }
                }
            }
        }
    }
}
