package com.a0.daleelak.features.journey

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.domain.Operation
import com.a0.daleelak.domain.OperationStatus

@Composable
fun JourneyScreen(model: DaleelakViewModel, operation: Operation, modifier: Modifier = Modifier) {
    var confirmDelete by remember { mutableStateOf(false) }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            TextButton(onClick = { model.selectedId = null }) { Text("رجوع للمعاملات") }
            Text(operation.title, style = MaterialTheme.typography.headlineSmall)
            Text("خطة عرض توضيحية · التقدم محلي ويؤكده المستخدم")
            operation.answers.forEach { (key, value) -> Text("$key: $value", style = MaterialTheme.typography.bodySmall) }
        }
        operation.plan.steps.forEachIndexed { index, step ->
            item(key = step.id) {
                val complete = step.id in operation.completedStepIds
                val ready = operation.completedStepIds.containsAll(step.dependsOn)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(step.title, style = MaterialTheme.typography.titleMedium)
                        Text(step.explanation)
                        Text(if (complete) "أكملها المستخدم" else if (ready) "جاهزة للمتابعة" else "بانتظار الخطوة السابقة")
                        step.requirements.forEach { requirement ->
                            Row {
                                Checkbox(checked = requirement.id in operation.checkedRequirementIds,
                                    onCheckedChange = { model.toggleRequirement(requirement.id) })
                                Text(requirement.label, Modifier.padding(top = 12.dp))
                            }
                        }
                        if (step.requirements.isEmpty()) Text("ستظهر قوائم الوثائق والإجراءات والدفعات والزيارات بعد ربط المتطلبات الموثقة.", style = MaterialTheme.typography.bodySmall)
                        Text("الأماكن: لا توجد خيارات موثقة مضافة لهذه الخطوة بعد.", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { model.toggleStep(step.id) }, enabled = (ready || complete) && operation.status != OperationStatus.COMPLETED) {
                            Text(if (complete) "إعادة فتح الخطوة" else "أؤكد إكمال الخطوة")
                        }
                    }
                }
                if (index < operation.plan.steps.lastIndex) Text("↓", Modifier.padding(horizontal = 24.dp))
            }
        }
        item {
            Button(onClick = model::bookDemo) { Text("حجز موعد · محاكاة محلية") }
            Text("هذا الزر لا ينشئ موعداً رسمياً ولا يدل على أن الخدمة تتطلب حجزاً.", style = MaterialTheme.typography.bodySmall)
        }
        operation.demoEvents.forEach { event -> item(key = event.reference) {
            Card { Column(Modifier.padding(12.dp)) { Text(event.message); Text(event.reference) } }
        } }
        item {
            if (operation.status == OperationStatus.COMPLETED) {
                Button(onClick = model::reopen) { Text("إعادة فتح المعاملة") }
            } else {
                Button(onClick = model::setCompleted, enabled = operation.completedStepIds.size == operation.plan.steps.size) {
                    Text("أؤكد اكتمال المتابعة")
                }
            }
            Text("لا يمثل ذلك موافقة أو حالة رسمية من الحكومة.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { confirmDelete = true }) { Text("حذف المعاملة المحلية") }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false },
        title = { Text("حذف المعاملة؟") }, text = { Text("سيتم حذف تقدمها من هذا الجهاز.") },
        confirmButton = { TextButton(onClick = { model.deleteSelected(); confirmDelete = false }) { Text("حذف") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("إلغاء") } })
}
