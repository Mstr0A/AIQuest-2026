package com.a0.daleelak.features.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.data.DemoCatalog

@Composable
fun AssistantScreen(model: DaleelakViewModel, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(model.messages.size, model.showPlan) {
        if (listState.layoutInfo.totalItemsCount > 0) listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
    }
    Column(modifier.fillMaxWidth()) {
        TextButton(onClick = model::newConversation) { Text("محادثة جديدة") }
        LazyColumn(Modifier.weight(1f), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(model.messages) { message ->
                Card(colors = CardDefaults.cardColors(containerColor = if (message.fromUser)
                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(if (message.fromUser) "أنت" else "دليلك · تجربة", style = MaterialTheme.typography.labelSmall)
                        Text(message.text)
                    }
                }
            }
            if (model.showPlan) item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(DemoCatalog.plan.title, style = MaterialTheme.typography.titleMedium)
                        Text("نموذج عرض للخطة؛ ليس إجراءً حكومياً موثقاً.")
                        DemoCatalog.plan.steps.forEach { Text(it.title) }
                        Button(onClick = model::savePlan) { Text("حفظ الخطة ومتابعة الخطوات") }
                    }
                }
            }
            item {
                Column {
                    Text("اقتراحات", style = MaterialTheme.typography.labelMedium)
                    DemoCatalog.prompts.forEach { prompt ->
                        TextButton(onClick = { model.draft = prompt }) { Text(prompt) }
                    }
                }
            }
        }
        Column(Modifier.imePadding().padding(vertical = 8.dp)) {
            OutlinedTextField(value = model.draft, onValueChange = { model.draft = it },
                label = { Text("اكتب أو عدّل رسالتك") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { model.send() }, enabled = model.draft.isNotBlank()) { Text("إرسال") }
                OutlinedButton(onClick = { model.notice = "الإملاء الصوتي سيُربط بخدمة التفريغ. الكتابة متاحة الآن؛ لا يتم تسجيل صوت." }) {
                    Text("إملاء صوتي")
                }
            }
        }
    }
}
