package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NotificationEntity
import com.example.data.model.Priority
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var selectedNotifForDetails by remember { mutableStateOf<NotificationEntity?>(null) }

    val canBroadcast = currentUser?.role in listOf("SuperAdmin", "Admin", "مدير النظام", "HrManager", "HrOfficer")

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "CIRCULAR" -> notifications.filter { it.category == "CIRCULAR" || it.title.contains("تعميم") }
            "APPROVAL" -> notifications.filter { it.category == "APPROVAL" || it.title.contains("تحديث") || it.title.contains("اعتماد") }
            "REQUEST" -> notifications.filter { it.category == "REQUEST" || it.title.contains("طلب") }
            "ALERT" -> notifications.filter { it.category == "ALERT" || it.category == "SYSTEM" }
            else -> notifications
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مركز الإشعارات والتعاميم",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "يوجد $unreadCount إشعار غير مقروء في حسابك",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (canBroadcast) {
                    FilledTonalButton(
                        onClick = { showBroadcastDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إصدار تعميم", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                if (unreadCount > 0) {
                    IconButton(onClick = { viewModel.markAllNotificationsRead() }) {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "تحديد الكل كمقروء",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf(
                "ALL" to "الكل (${notifications.size})",
                "CIRCULAR" to "التعاميم (${notifications.count { it.category == "CIRCULAR" || it.title.contains("تعميم") }})",
                "APPROVAL" to "الطلبات (${notifications.count { it.category == "APPROVAL" || it.title.contains("تحديث") }})",
                "ALERT" to "التنبيهات (${notifications.count { it.category == "ALERT" || it.category == "SYSTEM" }})"
            )

            chips.forEach { (code, title) ->
                val isSelected = selectedFilter == code
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFilter = code }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                    )
                }
            }
        }

        // Notifications List
        if (filteredNotifications.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "لا توجد إشعارات أو تعاميم مطابقة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "سيتم إشعارك فور ورود أي مستجدات على طلباتك أو صدور تعاميم جديدة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredNotifications) { notif ->
                    NotificationCard(
                        notif = notif,
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            selectedNotifForDetails = notif
                        }
                    )
                }
            }
        }
    }

    // Detail Dialog for Notifications & Circulars
    if (selectedNotifForDetails != null) {
        val notif = selectedNotifForDetails!!
        val isCircular = notif.category == "CIRCULAR" || notif.title.contains("تعميم")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        val timeStr = sdf.format(Date(notif.timestamp))

        AlertDialog(
            onDismissRequest = { selectedNotifForDetails = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isCircular) Icons.Default.Campaign else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isCircular) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isCircular) "تعميم إداري رسمي" else "تفاصيل الإشعار",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCircular) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isCircular) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "التاريخ والوقت: $timeStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = notif.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (isCircular) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "صادر عن: إدارة الموارد البشرية والشؤون الإدارية\nالاعتماد: معتمد وموجه لكافة منسوبي المؤسسة",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedNotifForDetails = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Broadcast Circular Dialog (For HR / Admin)
    if (showBroadcastDialog) {
        var cirTitle by remember { mutableStateOf("") }
        var cirNumber by remember { mutableStateOf("HR-CIR-2026-${(100..999).random()}") }
        var cirMessage by remember { mutableStateOf("") }
        var cirPriority by remember { mutableStateOf(Priority.High) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFD97706))
                    Text("إصدار ونشر تعميم إداري جديد", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (errorMsg != null) {
                        Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = cirNumber,
                        onValueChange = { cirNumber = it },
                        label = { Text("رقم التعميم الإداري") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cirTitle,
                        onValueChange = { cirTitle = it; errorMsg = null },
                        label = { Text("موضوع التعميم") },
                        placeholder = { Text("مثال: تعليمات الدوام الرسمي في شهر رمضان") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cirMessage,
                        onValueChange = { cirMessage = it; errorMsg = null },
                        label = { Text("نص وتفاصيل التعميم") },
                        placeholder = { Text("اكتب التعليمات والقرارات الموجهة لكافة الموظفين...") },
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cirTitle.isBlank() || cirMessage.isBlank()) {
                            errorMsg = "يرجى تعبئة موضوع التعميم وتفاصيله"
                        } else {
                            viewModel.broadcastHrCircular(
                                title = cirTitle,
                                circularNumber = cirNumber,
                                message = cirMessage,
                                priority = cirPriority
                            )
                            showBroadcastDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("نشر وتعميم للجميع")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBroadcastDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun NotificationCard(
    notif: NotificationEntity,
    onClick: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
    val timeStr = sdf.format(Date(notif.timestamp))

    val isCircular = notif.category == "CIRCULAR" || notif.title.contains("تعميم")

    val (icon, iconColor, categoryName) = when {
        isCircular -> Triple(Icons.Default.Campaign, Color(0xFFD97706), "تعميم رسمي")
        notif.category == "REQUEST" -> Triple(Icons.Default.PostAdd, MaterialTheme.colorScheme.primary, "طلب جديد")
        notif.category == "APPROVAL" -> Triple(Icons.Default.CheckCircle, HrStatusSuccess, "تحديث حالة")
        notif.category == "ALERT" -> Triple(Icons.Default.Warning, HrTertiary, "تنبيه إداري")
        else -> Triple(Icons.Default.Info, MaterialTheme.colorScheme.secondary, "إشعار عام")
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !notif.isRead && isCircular -> Color(0xFFFFFBEB)
                !notif.isRead -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                !notif.isRead && isCircular -> Color(0xFFFCD34D)
                !notif.isRead -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isCircular) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = notif.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = iconColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (!notif.isRead) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
            }
        }
    }
}

