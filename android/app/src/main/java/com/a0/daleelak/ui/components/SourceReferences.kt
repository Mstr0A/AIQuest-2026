package com.a0.daleelak.ui.components

import com.a0.daleelak.ui.LocalUiStrings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.a0.daleelak.data.ReviewedCatalog
import com.a0.daleelak.data.ReviewedSource

/**
 * Read-only references to reviewed sources. Supplied IDs resolve against the bundled
 * ReviewedCatalog loaded from local assets; unresolved IDs and unreadable catalogs stay
 * honest notes instead of raw internal IDs or invented records. Official links open only
 * through an ACTION_VIEW intent with local failure feedback; nothing is fetched in-app.
 */
@Composable
fun SourceReferences(
    sourceIds: List<String>,
    sourceVersions: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val ui = LocalUiStrings.current
    val context = LocalContext.current
    val catalog = remember(context) { runCatching { ReviewedCatalog(context.assets) }.getOrNull() }
    var openFailure by remember { mutableStateOf(false) }

    if (catalog == null) {
        Text(
            ui.text("تعذر فتح قائمة المصادر المراجعة المحلية؛ لا نعرض تفاصيل غير مؤكدة."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    val distinctIds = sourceIds.distinct()
    if (distinctIds.isEmpty()) return
    val resolved: List<Pair<ReviewedSource, String>> = distinctIds.mapNotNull { id ->
        catalog.sources.firstOrNull { it.id == id }
            ?.let { source -> source to (sourceVersions[id] ?: source.version) }
    }
    val unresolvedCount = distinctIds.size - resolved.size

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        resolved.forEach { (source, version) ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(source.title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    ui.choose("الإصدار: $version · تاريخ المراجعة: ${source.accessedAt}", "Version: $version · Reviewed: ${source.accessedAt}"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (source.url.isBlank()) {
                    Text(
                        ui.text("لا يتوفر رابط رسمي مذكور لهذا المصدر في الملفات المراجعة."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    TextButton(onClick = { openFailure = !openOfficialLink(context, source.url) }) {
                        Text(ui.text("فتح المصدر الرسمي"))
                    }
                }
            }
        }
        if (unresolvedCount > 0) {
            Text(
                ui.choose("هناك $unresolvedCount مصدر مذكور في هذه الخطوة غير موجود في قائمة المراجعة المحلية؛ لا نعرض تفاصيل غير مؤكدة.", "$unresolvedCount cited sources are missing from the reviewed catalog. Unverified details are hidden."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (openFailure) {
            Text(
                ui.text("لم نتمكن من فتح الرابط في تطبيق على هذا الجهاز."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/** Returns true when a viewer accepted the intent; failures surface as local UI feedback. */
private fun openOfficialLink(context: Context, url: String): Boolean = try {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url.trim())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (error: ActivityNotFoundException) {
    false
} catch (error: Exception) {
    false
}
