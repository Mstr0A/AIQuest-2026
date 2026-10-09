package com.a0.daleelak.features.locations

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LocationsScreen(modifier: Modifier = Modifier) {
    Column(modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("الأماكن ذات الصلة", style = MaterialTheme.typography.headlineSmall)
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("دليل الأماكن قيد الإعداد", style = MaterialTheme.typography.titleMedium)
            Text("ستظهر حتى ثلاثة خيارات موثقة لكل خطوة، مرتبة حسب المسافة من موقع تختاره.")
            Text("لم تُضف مواقع أو ساعات عمل موثقة بعد. لا يوجد اتصال بخدمة خرائط أو توافر مواعيد حي.")
        } }
    }
}
