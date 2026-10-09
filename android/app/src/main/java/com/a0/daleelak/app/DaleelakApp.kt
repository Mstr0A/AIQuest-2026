package com.a0.daleelak.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.a0.daleelak.features.assistant.AssistantScreen
import com.a0.daleelak.features.operations.OperationsScreen
import com.a0.daleelak.features.locations.LocationsScreen

@Composable
fun DaleelakApp(model: DaleelakViewModel) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BackHandler(enabled = model.selectedId != null || model.destination != Destination.ASSISTANT) {
            if (model.selectedId != null) model.selectedId = null
            else model.destination = Destination.ASSISTANT
        }
        Scaffold(bottomBar = {
            NavigationBar {
                Destination.entries.forEach { destination ->
                    val label = when (destination) {
                        Destination.ASSISTANT -> "المساعد"
                        Destination.OPERATIONS -> "معاملاتي"
                        Destination.LOCATIONS -> "الأماكن"
                    }
                    NavigationBarItem(selected = model.destination == destination,
                        onClick = { model.destination = destination; model.selectedId = null },
                        icon = { Text(when (destination) {
                            Destination.ASSISTANT -> "◌"
                            Destination.OPERATIONS -> "▤"
                            Destination.LOCATIONS -> "⌖"
                        }) }, label = { Text(label) })
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Text("دليلك · DALEELAK", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
                Text("نموذج هاكاثون · غير رسمي · بيانات تجريبية", style = MaterialTheme.typography.labelMedium)
                model.notice?.let { notice ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(notice, Modifier.padding(12.dp))
                        TextButton(onClick = { model.notice = null }) { Text("إغلاق") }
                    }
                }
                when (model.destination) {
                    Destination.ASSISTANT -> AssistantScreen(model, Modifier.weight(1f))
                    Destination.OPERATIONS -> OperationsScreen(model, Modifier.weight(1f))
                    Destination.LOCATIONS -> LocationsScreen(Modifier.weight(1f))
                }
            }
        }
    }
}
