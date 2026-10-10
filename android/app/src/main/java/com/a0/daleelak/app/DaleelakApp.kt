package com.a0.daleelak.app

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
import com.a0.daleelak.domain.OperationStatus
import com.a0.daleelak.features.assistant.AssistantScreen
import com.a0.daleelak.features.home.HomeScreen
import com.a0.daleelak.features.operations.OperationsFilter
import com.a0.daleelak.features.operations.OperationsScreen
import com.a0.daleelak.ui.components.DaleelakIcons

@Composable
fun DaleelakApp(model: DaleelakViewModel) {
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
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BackHandler(enabled = !showHome) {
            if (model.selectedId != null) model.selectedId = null else showHome = true
        }
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("دليلك", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Text("DALEELAK · نسخة تجريبية", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!showHome) {
                        TextButton(onClick = { model.selectedId = null; showHome = true }) {
                            Icon(DaleelakIcons.Home, contentDescription = null,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("الرئيسية")
                        }
                    }
                }
                model.notice?.let { notice ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(notice, Modifier.padding(12.dp))
                        TextButton(onClick = { model.notice = null }) { Text("إغلاق") }
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
