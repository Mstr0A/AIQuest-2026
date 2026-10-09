package com.a0.daleelak.features.operations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun OperationsScreen(model: DaleelakViewModel, modifier: Modifier = Modifier) {
    val selected = model.selected
    if (selected != null) {
        JourneyScreen(model, selected, modifier)
        return
    }
    var filter by remember { mutableStateOf<OperationStatus?>(null) }
    Column(modifier) {
        Text("معاملاتي", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("الكل") })
            OperationStatus.entries.forEach { status ->
                FilterChip(selected = filter == status, onClick = { filter = status }, label = { Text(status.label()) })
            }
        }
        val visible = model.operations.filter { filter == null || it.status == filter }
        if (visible.isEmpty()) Text("لا توجد معاملات هنا. احفظ خطة من المساعد للمتابعة لاحقاً.", Modifier.padding(vertical = 16.dp))
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
