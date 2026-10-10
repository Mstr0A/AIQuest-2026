package com.a0.daleelak.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.a0.daleelak.ui.components.DaleelakIcons

/** Entry point for starting a conversation or returning to saved operations. */
@Composable
fun HomeScreen(
    totalCount: Int,
    currentCount: Int,
    finishedCount: Int,
    onNewOperation: () -> Unit,
    onHistory: () -> Unit,
    onCurrent: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("شو بدك تنجز اليوم؟", style = MaterialTheme.typography.headlineMedium)
                Text("ابدأ معاملة جديدة، أو كمل من وين وقفت.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Button(onClick = onNewOperation,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
                Icon(DaleelakIcons.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("معاملة جديدة", style = MaterialTheme.typography.titleMedium)
            }
        }
        item {
            Text("معاملاتك", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp))
        }
        item {
            OperationEntry("المعاملات الحالية", "الجديدة والجارية", currentCount,
                DaleelakIcons.Current, onCurrent)
        }
        item {
            OperationEntry("المعاملات المنتهية", "اللي أكملت متابعتها", finishedCount,
                DaleelakIcons.Finished, onFinished)
        }
        item {
            OperationEntry("سجل المعاملات", "كل معاملاتك المحفوظة", totalCount,
                DaleelakIcons.History, onHistory)
        }
    }
}

@Composable
private fun OperationEntry(
    title: String, description: String, count: Int, icon: ImageVector, onClick: () -> Unit,
) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(12.dp).size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(count.toString(), style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}
