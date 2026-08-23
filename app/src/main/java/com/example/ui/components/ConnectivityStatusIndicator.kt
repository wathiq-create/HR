package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HrStatusError
import com.example.ui.theme.HrStatusSuccess
import com.example.ui.theme.HrStatusWarning

/**
 * A reusable top status indicator component that notifies the user
 * when the application is operating in offline mode vs. online mode,
 * and clarifies whether data is up-to-date or queued for synchronization.
 */
@Composable
fun ConnectivityStatusBanner(
    isOnline: Boolean,
    pendingSyncCount: Int = 0,
    isSyncing: Boolean = false,
    onToggleNetwork: (() -> Unit)? = null,
    onSyncClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    AnimatedContent(
        targetState = isOnline,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "ConnectivityBannerTransition",
        modifier = modifier.fillMaxWidth()
    ) { online ->
        if (!online) {
            // Offline Mode Banner (Amber / Warning)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HrStatusWarning.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, HrStatusWarning.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_status_indicator")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(HrStatusWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "وضع عدم الاتصال",
                                tint = HrStatusWarning,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "وضع عدم الاتصال (Offline Mode)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    ),
                                    color = HrStatusWarning
                                )
                                if (pendingSyncCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = HrStatusWarning,
                                        modifier = Modifier.padding(start = 2.dp)
                                    ) {
                                        Text(
                                            text = "$pendingSyncCount معلقة",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (pendingSyncCount > 0)
                                    "البيانات محفوظة محلياً وفي انتظار المزامنة التلقائية عند عودة الاتصال"
                                else
                                    "أنت تعمل دون اتصال بالخادم - البيانات الحالية مخزنة محلياً",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (onToggleNetwork != null) {
                        FilledTonalButton(
                            onClick = onToggleNetwork,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = HrStatusWarning.copy(alpha = 0.2f),
                                contentColor = HrStatusWarning
                            ),
                            modifier = Modifier.testTag("toggle_network_button")
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إعادة الاتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Online Mode Banner (Subtle Emerald Success)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HrStatusSuccess.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, HrStatusSuccess.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("online_status_indicator")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(HrStatusSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "متصل بالخادم",
                                tint = HrStatusSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "متصل بالخادم (Online)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = HrStatusSuccess
                                )
                                Text(
                                    text = if (isSyncing) "• جاري المزامنة..." else if (pendingSyncCount > 0) "• $pendingSyncCount بانتظار الرفع" else "• محدثة بالكامل",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (pendingSyncCount > 0) HrStatusWarning else HrStatusSuccess
                                )
                            }
                            Text(
                                text = if (pendingSyncCount > 0) "توجد عمليات قيد الإرسال إلى قاعدة البيانات السحابية" else "جميع السجلات والعمليات متزامنة ومحدثة في الوقت الفعلي",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (onSyncClick != null && (pendingSyncCount > 0 || isSyncing)) {
                        Button(
                            onClick = onSyncClick,
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("sync_now_banner_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مزامنة...", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مزامنة الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact connectivity badge suitable for embedding inside headers or cards.
 */
@Composable
fun ConnectivityStatusChip(
    isOnline: Boolean,
    pendingSyncCount: Int = 0,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isOnline) HrStatusSuccess.copy(alpha = 0.12f) else HrStatusWarning.copy(alpha = 0.15f),
        border = BorderStroke(
            1.dp,
            if (isOnline) HrStatusSuccess.copy(alpha = 0.35f) else HrStatusWarning.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("connectivity_status_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = null,
                tint = if (isOnline) HrStatusSuccess else HrStatusWarning,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isOnline) {
                    if (pendingSyncCount > 0) "متصل ($pendingSyncCount معلقة)" else "متصل ومحدث"
                } else {
                    if (pendingSyncCount > 0) "غير متصل ($pendingSyncCount بانتظار المزامنة)" else "غير متصل (Offline)"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = if (isOnline) HrStatusSuccess else HrStatusWarning
            )
        }
    }
}
