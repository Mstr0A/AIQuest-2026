package com.a0.daleelak.features.journey

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.domain.*
import kotlinx.coroutines.launch

/** Snapping cards preserve the dependency graph without drawing a flowchart. */
@Composable
fun JourneyScreen(model: DaleelakViewModel, operation: Operation, modifier: Modifier = Modifier) {
    key(operation.id, operation.plan.version) {
        JourneyCards(model, operation, modifier)
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun JourneyCards(model: DaleelakViewModel, operation: Operation, modifier: Modifier) {
    val steps = operation.plan.steps
    val readySteps = steps.filter {
        it.id !in operation.completedStepIds && operation.completedStepIds.containsAll(it.dependsOn)
    }
    // Overview, one page per step, then operation actions. Future pages remain browsable.
    val pager = rememberPagerState(pageCount = { steps.size + 2 })
    val scope = rememberCoroutineScope()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { model.selectedId = null }) { Text("رجوع للمعاملات") }
        Text(operation.title, style = MaterialTheme.typography.titleLarge)
        Text(if (operation.plan.illustrative) "خطة تجريبية · التقدم حسب تأكيدك" else "التقدم حسب تأكيدك",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { scope.launch { pager.animateScrollToPage(0) } }) { Text("شو أعمل الآن؟") }
            Text(when (pager.currentPage) {
                0 -> "نظرة عامة"
                steps.size + 1 -> "متابعة المعاملة"
                else -> "خطوة ${pager.currentPage} من ${steps.size}"
            }, style = MaterialTheme.typography.labelMedium)
        }
        VerticalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth(),
            pageSpacing = 12.dp, contentPadding = PaddingValues(bottom = 40.dp)) { page ->
            OutlinedCard(Modifier.fillMaxSize(), shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (page) {
                        0 -> {
                            item {
                                Text("اللي تقدر تتابعه الآن", style = MaterialTheme.typography.headlineSmall)
                                Text(if (readySteps.size > 1)
                                    "هاي المهام متاحة بنفس الوقت. شوفها مع بعض قبل ما ترتّب مشوارك."
                                else "شوف الخطوة المتاحة ومتطلباتها قبل ما تبدأ.")
                            }
                            if (readySteps.isEmpty()) item {
                                Text(if (operation.completedStepIds.size == steps.size)
                                    "راجعت كل الخطوات. تقدر تؤكد اكتمال المتابعة من البطاقة الأخيرة."
                                else "ما في خطوة متاحة الآن. راجع الخطوات السابقة قبل المتابعة.")
                            }
                            readySteps.forEach { step ->
                                item(key = step.id) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(step.title, style = MaterialTheme.typography.titleMedium)
                                        Text(step.explanation, style = MaterialTheme.typography.bodyMedium)
                                        TextButton(onClick = {
                                            scope.launch { pager.animateScrollToPage(steps.indexOf(step) + 1) }
                                        }) { Text("تفاصيل الخطوة") }
                                    }
                                }
                            }
                            val currentRequirements = readySteps.flatMap { it.requirements }
                            ChecklistCategory.entries.forEach { category ->
                                val items = currentRequirements.filter { it.category == category }
                                if (items.isNotEmpty()) item {
                                    Text(category.label(), style = MaterialTheme.typography.titleMedium)
                                    items.forEach { requirement ->
                                        Text("• ${requirement.label}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            item {
                                Text("اسحب للأعلى عشان تشوف الخطوات القادمة. عرضها ما يعني إنها جاهزة للتنفيذ.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        steps.size + 1 -> {
                            item { Text("متابعة المعاملة", style = MaterialTheme.typography.headlineSmall) }
                            item {
                                Button(onClick = model::bookDemo, modifier = Modifier.fillMaxWidth()) {
                                    Text("حجز موعد · محاكاة")
                                }
                                Text("محاكاة محلية، لا تنشئ موعداً رسمياً ولا تعني أن الخدمة تتطلب حجزاً.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            operation.demoEvents.forEach { event -> item(key = event.reference) {
                                Text(event.message); Text(event.reference, style = MaterialTheme.typography.labelSmall)
                            } }
                            item {
                                if (operation.status == OperationStatus.COMPLETED) {
                                    Button(onClick = model::reopen) { Text("إعادة فتح المعاملة") }
                                } else {
                                    Button(onClick = model::setCompleted,
                                        enabled = steps.isNotEmpty() && operation.completedStepIds.size == steps.size) {
                                        Text("أؤكد اكتمال المتابعة")
                                    }
                                }
                                Text("تأكيدك محلي، ولا يمثل موافقة أو حالة حكومية رسمية.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            item { TextButton(onClick = { confirmDelete = true }) { Text("حذف المعاملة المحلية") } }
                        }
                        else -> {
                            val step = steps[page - 1]
                            val complete = step.id in operation.completedStepIds
                            val ready = operation.completedStepIds.containsAll(step.dependsOn)
                            item {
                                Text(step.title, style = MaterialTheme.typography.headlineSmall)
                                Text(when {
                                    complete -> "أكملتها حسب تأكيدك"
                                    ready -> "متاحة للمتابعة"
                                    else -> "خطوة قادمة · تقدر تشوف متطلباتها الآن"
                                }, color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge)
                                Text(step.explanation)
                            }
                            if (!ready) item {
                                val prerequisites = steps.filter {
                                    it.id in step.dependsOn && it.id !in operation.completedStepIds
                                }.joinToString("، ") { it.title }
                                Text("قبل تنفيذها: $prerequisites", style = MaterialTheme.typography.bodySmall)
                            }
                            ChecklistCategory.entries.forEach { category ->
                                val requirements = step.requirements.filter { it.category == category }
                                if (requirements.isNotEmpty()) {
                                    item { Text(category.label(), style = MaterialTheme.typography.titleMedium) }
                                    requirements.forEach { requirement -> item(key = requirement.id) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Checkbox(checked = requirement.id in operation.checkedRequirementIds,
                                                enabled = operation.status != OperationStatus.COMPLETED,
                                                onCheckedChange = { model.toggleRequirement(requirement.id) })
                                            Column(Modifier.weight(1f).padding(top = 12.dp)) {
                                                Text(requirement.label)
                                                if (requirement.format != RequirementFormat.UNSPECIFIED) {
                                                    Text(requirement.format.label(), style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    } }
                                }
                            }
                            if (step.requirements.isEmpty()) item {
                                Text(if (operation.plan.illustrative)
                                    "المتطلبات الموثقة لسه مش مضافة للخطة التجريبية."
                                else "لا توجد متطلبات إضافية مذكورة في هذه الخطوة.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            item {
                                Text("الأماكن ذات الصلة", style = MaterialTheme.typography.titleMedium)
                                Text("ما في خيارات موثقة مضافة لهذه الخطوة بعد.", style = MaterialTheme.typography.bodySmall)
                            }
                            val references = (step.sourceIds + step.requirements.flatMap { it.sourceIds }).distinct()
                            if (references.isNotEmpty()) item {
                                var showSources by rememberSaveable(step.id) { mutableStateOf(false) }
                                TextButton(onClick = { showSources = !showSources }) { Text("مصادر الخطوة") }
                                if (showSources) Text("تفاصيل المراجع غير متاحة بعد في هذه النسخة.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            item {
                                OutlinedButton(onClick = { model.toggleStep(step.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = (ready || complete) && operation.status != OperationStatus.COMPLETED) {
                                    Text(if (complete) "إعادة فتح الخطوة" else "أؤكد إكمال الخطوة")
                                }
                            }
                        }
                    }
                }
            }
        }
        // Explicit controls also support users who cannot perform pager gestures.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(enabled = pager.currentPage > 0,
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }) { Text("السابق") }
            TextButton(enabled = pager.currentPage < pager.pageCount - 1,
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) { Text("التالي") }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false },
        title = { Text("حذف المعاملة؟") }, text = { Text("سيتم حذف تقدمها من هذا الجهاز.") },
        confirmButton = { TextButton(onClick = { model.deleteSelected(); confirmDelete = false }) { Text("حذف") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("إلغاء") } })
}

private fun ChecklistCategory.label(): String = when (this) {
    ChecklistCategory.DOCUMENTS -> "الوثائق"
    ChecklistCategory.ACTIONS -> "الإجراءات"
    ChecklistCategory.PAYMENTS_AND_COMMITMENTS -> "الدفعات والالتزامات"
    ChecklistCategory.VISITS -> "الزيارات"
}

private fun RequirementFormat.label(): String = when (this) {
    RequirementFormat.ORIGINAL -> "الأصل"
    RequirementFormat.PAPER_COPY -> "نسخة ورقية"
    RequirementFormat.DIGITAL_UPLOAD -> "رفع إلكتروني"
    RequirementFormat.OFFICIAL_ELECTRONIC_RECORD -> "سجل إلكتروني رسمي"
    RequirementFormat.UNSPECIFIED -> ""
}
