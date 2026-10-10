package com.a0.daleelak.features.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.app.VoiceState
import com.a0.daleelak.ui.components.DaleelakIcons

@Composable
fun AssistantScreen(model: DaleelakViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val plan = model.currentPlan
    val listState = rememberLazyListState()
    var showTyping by rememberSaveable { mutableStateOf(false) }
    var showApiKeyDialog by rememberSaveable { mutableStateOf(false) }
    var apiKeyDraft by remember { mutableStateOf("") }
    var voicePresetDraft by remember { mutableStateOf(model.voicePreset) }
    val keyboard = LocalSoftwareKeyboardController.current
    val typingFocus = remember { FocusRequester() }
    var listening by rememberSaveable { mutableStateOf(false) }
    var shouldStartListening by remember { mutableStateOf(false) }
    var draftBeforeSpeech by remember { mutableStateOf("") }
    val recognizer = remember(context) {
        runCatching {
            if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
        }.getOrNull()
    }
    val speechPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            if (model.hasOpenRouterKey) model.startCloudDictation() else shouldStartListening = true
        }
        else model.notice = "لم يُسمح باستخدام الميكروفون. تقدر تكتب رسالتك بدلاً من ذلك."
    }

    DisposableEffect(lifecycleOwner, model, recognizer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                model.stopVoice()
                recognizer?.cancel()
                listening = false
                shouldStartListening = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(recognizer, model) {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true }
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) model.draft = joinTranscript(draftBeforeSpeech, partial)
            }
            override fun onResults(results: Bundle?) {
                val transcript = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                listening = false
                shouldStartListening = false
                if (transcript.isNullOrBlank()) {
                    model.draft = draftBeforeSpeech
                    model.notice = "ما وصلني كلام واضح. جرّب مرة ثانية أو اكتب رسالتك."
                } else model.draft = joinTranscript(draftBeforeSpeech, transcript)
            }
            override fun onError(error: Int) {
                listening = false
                shouldStartListening = false
                model.draft = draftBeforeSpeech
                model.notice = when (error) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "يلزم السماح بالميكروفون للإملاء الصوتي."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "تعذر الاتصال بخدمة التعرف الصوتي. تقدر تكتب رسالتك."
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ما وصلني كلام واضح. جرّب مرة ثانية أو اكتب رسالتك."
                    else -> "تعذر تشغيل الإملاء الصوتي؛ تقدر تكتب رسالتك."
                }
            }
        })
        onDispose {
            model.stopVoice()
            recognizer?.cancel()
            recognizer?.destroy()
            listening = false
        }
    }
    LaunchedEffect(shouldStartListening, recognizer) {
        if (shouldStartListening) {
            val service = recognizer
            if (service == null) {
                shouldStartListening = false
                model.notice = "خدمة التعرف الصوتي غير متاحة على هذا الجهاز. تقدر تكتب رسالتك."
            } else {
                draftBeforeSpeech = model.draft
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-JO")
                    if (Build.VERSION.SDK_INT >= 34) {
                        putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_SWITCH, RecognizerIntent.LANGUAGE_SWITCH_BALANCED)
                        putStringArrayListExtra(RecognizerIntent.EXTRA_LANGUAGE_SWITCH_ALLOWED_LANGUAGES, arrayListOf("ar-JO", "en-US"))
                    }
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                runCatching { service.startListening(intent) }.onFailure {
                    shouldStartListening = false
                    model.notice = "تعذر بدء الإملاء الصوتي؛ تقدر تكتب رسالتك."
                }
            }
        }
    }
    LaunchedEffect(showTyping) {
        if (showTyping) {
            typingFocus.requestFocus()
            keyboard?.show()
        }
    }
    LaunchedEffect(model.messages.size, plan, model.isResponding) {
        if (listState.layoutInfo.totalItemsCount > 0) listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
    }
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("المساعد", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { model.configureReadReplies(!model.readReplies) }) {
                    Text(if (model.readReplies) "كتم" else "صوت")
                }
                TextButton(onClick = { showApiKeyDialog = true }) {
                    Text(if (model.hasOpenRouterKey) "AI مفعّل" else "إعداد AI")
                }
                TextButton(onClick = {
                    recognizer?.cancel(); listening = false; shouldStartListening = false
                    model.newConversation(); showTyping = false; keyboard?.hide()
                }) {
                    Text("محادثة جديدة")
                }
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
                            val activeReply = model.playingReplyText == message.text
                            TextButton(onClick = {
                                recognizer?.cancel(); listening = false; shouldStartListening = false
                                model.playReply(message.text)
                            }) {
                                Icon(DaleelakIcons.Speaker, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(when {
                                    activeReply && model.voiceState == VoiceState.PREPARING_REPLY -> "إلغاء تجهيز الصوت"
                                    activeReply && model.voiceState == VoiceState.PLAYING_REPLY -> "إيقاف"
                                    else -> "استماع"
                                })
                            }
                        }
                    }
                }
            }
            if (model.isResponding) item {
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("جارٍ تجهيز الرد…", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (plan != null) item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(plan.title, style = MaterialTheme.typography.titleMedium)
                        if (plan.illustrative) Text("نموذج عرض للخطة؛ ليس إجراءً حكومياً موثقاً.")
                        if (plan.summary.isNotBlank()) Text(plan.summary)
                        val uncertainties = plan.uncertainties.filter { it.isNotBlank() }
                        if (uncertainties.isNotEmpty()) {
                            Text("تفاصيل تحتاج تأكيد", style = MaterialTheme.typography.titleSmall)
                            uncertainties.forEach { uncertainty ->
                                Text("• $uncertainty", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        plan.steps.forEach { Text(it.title) }
                        Button(onClick = model::savePlan, enabled = !model.isResponding) {
                            Text("حفظ الخطة ومتابعة الخطوات")
                        }
                    }
                }
            }
            item {
                Column {
                    Text("اقتراحات", style = MaterialTheme.typography.labelMedium)
                    model.suggestedPrompts.forEach { prompt ->
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
                    if (model.voiceState == VoiceState.RECORDING) model.finishCloudDictation()
                    else if (listening) recognizer?.stopListening()
                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        if (model.hasOpenRouterKey) model.startCloudDictation() else shouldStartListening = true
                    } else speechPermission.launch(Manifest.permission.RECORD_AUDIO)
                }, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp),
                    enabled = (model.hasOpenRouterKey || recognizer != null) && !model.isResponding && model.voiceState != VoiceState.TRANSCRIBING) {
                    Icon(DaleelakIcons.Microphone, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(when {
                        model.voiceState == VoiceState.TRANSCRIBING -> "جارٍ تفريغ الصوت…"
                        model.voiceState == VoiceState.RECORDING -> "إنهاء التسجيل"
                        listening -> "إنهاء الإملاء"
                        else -> "احكي لدليلك"
                    })
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
                Button(onClick = { model.send() }, enabled = !model.isResponding && !model.isDictating && !listening,
                    modifier = Modifier.fillMaxWidth()) { Text("إرسال") }
            }
            Text(when {
                model.hasOpenRouterKey -> "الصوت عبر OpenRouter. سجّل حتى ٣٠ ثانية، وراجع النص قبل إرساله. صوت الرد مولّد آلياً."
                recognizer == null -> "فعّل مفتاح OpenRouter لاستخدام الصوت، أو اكتب رسالتك."
                else -> "الإملاء يضيف النص للمراجعة ولا يرسله تلقائياً. قد يعالج جهازك الصوت عبر خدمة التعرف."
            }, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("OpenRouter · لهذه الجلسة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("يستخدم التطبيق OpenRouter لفهم الرسائل، وتفريغ التسجيل بعد إنهائه، وقراءة الردود. راجع النص قبل إرساله. تبقى الخطوات الحكومية من الوثائق المحلية المراجعة، والمفتاح لهذه الجلسة فقط.")
                    OutlinedTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        label = { Text("مفتاح OpenRouter") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(value = voicePresetDraft, onValueChange = { voicePresetDraft = it },
                        label = { Text("الصوت (اسم أو معرّف ElevenLabs)") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                    if (model.hasOpenRouterKey) Text("اترك الحقل فارغاً ثم اختر مسح المفتاح لتعطيل الاتصال.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (apiKeyDraft.isNotBlank()) model.configureOpenRouterApiKey(apiKeyDraft)
                    model.configureVoicePreset(voicePresetDraft)
                    apiKeyDraft = ""
                    showApiKeyDialog = false
                }) { Text("تفعيل") }
            },
            dismissButton = {
                Row {
                    if (model.hasOpenRouterKey) TextButton(onClick = {
                        model.configureOpenRouterApiKey("")
                        apiKeyDraft = ""
                        showApiKeyDialog = false
                    }) { Text("مسح المفتاح") }
                    TextButton(onClick = { apiKeyDraft = ""; showApiKeyDialog = false }) { Text("إلغاء") }
                }
            },
        )
    }
}

private fun joinTranscript(existing: String, spoken: String): String =
    listOf(existing.trim(), spoken.trim()).filter(String::isNotEmpty).joinToString(" ")
