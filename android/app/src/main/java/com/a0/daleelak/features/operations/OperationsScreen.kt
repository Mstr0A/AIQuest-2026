package com.a0.daleelak.features.operations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.domain.OperationStatus
import com.a0.daleelak.features.journey.JourneyScreen

fun OperationStatus.label(): String = when (this) {
    OperationStatus.NEW -> "جديدة"
    OperationStatus.ONGOING -> "جارية"
    OperationStatus.COMPLETED -> "مكتملة"
}

enum class OperationsFilter(val label: String) {
    ALL("الكل"), CURRENT("الحالية"), COMPLETED("المنتهية");

    fun includes(status: OperationStatus): Boolean = when (this) {
        ALL -> true
        CURRENT -> status != OperationStatus.COMPLETED
        COMPLETED -> status == OperationStatus.COMPLETED
    }
}

@Composable
fun OperationsScreen(
    model: DaleelakViewModel,
    modifier: Modifier = Modifier,
    initialFilter: OperationsFilter = OperationsFilter.ALL,
) {
    var filter by rememberSaveable(initialFilter) { mutableStateOf(initialFilter) }
    val selected = model.selected
    if (selected != null) {
        JourneyScreen(model, selected, modifier)
        return
    }
    Column(modifier) {
        Text("معاملاتي", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OperationsFilter.entries.forEach { option ->
                FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option.label) })
            }
        }
        val visible = model.operations.filter { filter.includes(it.status) }.sortedByDescending { it.updatedAt }
        if (visible.isEmpty()) Text(
            when (filter) {
                OperationsFilter.ALL -> "ما عندك معاملات محفوظة بعد. ابدأ معاملة جديدة من الرئيسية."
                OperationsFilter.CURRENT -> "ما عندك معاملات حالية."
                OperationsFilter.COMPLETED -> "ما عندك معاملات منتهية بعد."
            }, Modifier.padding(vertical = 16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visible, key = { it.id }) { operation ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(operation.title, style = MaterialTheme.typography.titleMedium)
                        Text(operation.status.label() + " · تقدم أبلغ عنه المستخدم")
                        Text("${operation.completedStepIds.size} / ${operation.plan.steps.size} خطوات")
                        Button(onClick = { model.selectedId = operation.id }) { Text("متابعة") }
                    }
                }
            }
        }
    }
}
