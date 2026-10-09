package com.a0.daleelak.features.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.data.DemoCatalog
import com.a0.daleelak.ui.components.DaleelakIcons

@Composable
fun AssistantScreen(model: DaleelakViewModel, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    var showTyping by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val typingFocus = remember { FocusRequester() }
    LaunchedEffect(showTyping) {
        if (showTyping) {
            typingFocus.requestFocus()
            keyboard?.show()
        }
    }
    LaunchedEffect(model.messages.size, model.showPlan) {
        if (listState.layoutInfo.totalItemsCount > 0) listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
    }
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("المساعد", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { model.newConversation(); showTyping = false; keyboard?.hide() }) {
                Text("محادثة جديدة")
            }
        }
        LazyColumn(Modifier.weight(1f), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(model.messages) { message ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (message.fromUser)
                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(if (message.fromUser) "أنت" else "دليلك · تجربة", style = MaterialTheme.typography.labelSmall)
                        Text(message.text)
                        if (!message.fromUser) {
                            TextButton(onClick = { model.notice = "الاستماع للردود غير متاح بعد في النسخة التجريبية. الرد مكتوب أمامك." }) {
                                Icon(DaleelakIcons.Speaker, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("استماع · قريباً")
                            }
                        }
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
        Column(Modifier.imePadding().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showTyping) {
                OutlinedTextField(value = model.draft, onValueChange = { model.draft = it },
                    label = { Text("اكتب أو عدّل رسالتك") },
                    modifier = Modifier.fillMaxWidth().focusRequester(typingFocus), maxLines = 4,
                    shape = RoundedCornerShape(16.dp))
            } else if (model.draft.isNotBlank()) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(model.draft, Modifier.fillMaxWidth().padding(12.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    keyboard?.hide()
                    model.notice = "الإملاء الصوتي غير متاح بعد. تقدر تكتب رسالتك؛ لا يتم تسجيل صوت."
                }, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(DaleelakIcons.Microphone, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("احكي لدليلك")
                }
                OutlinedButton(onClick = {
                    showTyping = !showTyping
                    if (!showTyping) keyboard?.hide()
                }, modifier = Modifier.heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(DaleelakIcons.Keyboard, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (showTyping) "إخفاء" else "كتابة")
                }
            }
            if (model.draft.isNotBlank()) {
                Button(onClick = { model.send() }, modifier = Modifier.fillMaxWidth()) { Text("إرسال") }
            }
            Text("الصوت قريباً · الكتابة متاحة الآن", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
