package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrTopBar(
    title: String,
    currentUser: UserEntity?,
    isOnline: Boolean,
    unreadNotifications: Int,
    usersList: List<UserEntity>,
    onOpenDrawer: () -> Unit,
    onToggleNetwork: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSwitchUser: (UserEntity) -> Unit,
    onLogout: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: Drawer Hamburger + Logo + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Navigation menu icon in rounded container
                Surface(
                    onClick = onOpenDrawer,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "القائمة",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Brand Emblem (Primary Blue Box as in Professional Polish design)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iso_quality_icon_1787512684201),
                        contentDescription = "HR Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Title and Subtitle Hierarchy
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (currentUser != null) "${currentUser.fullName} • ${currentUser.role}" else "بوابة الخدمات المؤسسية",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Right Group: Network Pill, Notifications Button & User Profile Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Network Mode Indicator Button
                Surface(
                    onClick = onToggleNetwork,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOnline) HrStatusSuccess.copy(alpha = 0.12f) else HrStatusWarning.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isOnline) HrStatusSuccess.copy(alpha = 0.35f) else HrStatusWarning.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = if (isOnline) "متصل" else "غير متصل",
                            tint = if (isOnline) HrStatusSuccess else HrStatusWarning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Notifications Button with badge
                Surface(
                    onClick = onOpenNotifications,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(
                                        containerColor = HrStatusError,
                                        contentColor = Color.White
                                    ) {
                                        Text(unreadNotifications.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "الإشعارات",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // User Switcher Menu
                Box {
                    Surface(
                        onClick = { showUserMenu = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "المستخدم",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false },
                        modifier = Modifier.widthIn(min = 270.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "المستخدم الحالي:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = currentUser?.fullName ?: "غير مسجل",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${currentUser?.department} • ${currentUser?.role}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {},
                            enabled = false
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "تبديل الصلاحية / المستخدم (تجربة سريعة):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            },
                            onClick = {},
                            enabled = false
                        )

                        usersList.forEach { user ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (user.id == currentUser?.id) Icons.Default.CheckCircle else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (user.id == currentUser?.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(text = user.fullName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(text = "${user.department} • ${user.role}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    onSwitchUser(user)
                                }
                            )
                        }

                        HorizontalDivider()

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = HrStatusError
                                )
                            },
                            text = { Text("تسجيل الخروج", color = HrStatusError, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showUserMenu = false
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    }
}
