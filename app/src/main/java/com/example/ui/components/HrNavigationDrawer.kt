package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.util.RolePermissions

data class NavItem(
    val screen: AppScreen,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun HrNavigationDrawerContent(
    currentScreen: AppScreen,
    currentUser: UserEntity?,
    pendingApprovalsCount: Int,
    unreadNotificationsCount: Int,
    onSelectScreen: (AppScreen) -> Unit
) {
    val navItems = listOf(
        NavItem(AppScreen.DASHBOARD, Icons.Default.Dashboard),
        NavItem(AppScreen.EMPLOYEES, Icons.Default.People),
        NavItem(AppScreen.LEAVES, Icons.Default.DateRange),
        NavItem(AppScreen.OVERTIME, Icons.Default.AccessTime),
        NavItem(AppScreen.PENALTIES, Icons.Default.Gavel),
        NavItem(AppScreen.SERVICES, Icons.Default.LocalShipping),
        NavItem(AppScreen.APPROVALS, Icons.Default.AssignmentTurnedIn, pendingApprovalsCount),
        NavItem(AppScreen.NOTIFICATIONS, Icons.Default.Notifications, unreadNotificationsCount),
        NavItem(AppScreen.REPORTS, Icons.AutoMirrored.Filled.ReceiptLong),
        NavItem(AppScreen.USERS_ROLES, Icons.Default.AdminPanelSettings),
        NavItem(AppScreen.SYNC_CENTER, Icons.Default.Sync),
        NavItem(AppScreen.AUDIT_BACKUP, Icons.Default.Security),
        NavItem(AppScreen.SETTINGS, Icons.Default.Settings)
    )

    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Professional Polish Blue Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.iso_quality_icon_1787512684201),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column {
                            Text(
                                text = "نظام الموارد البشرية",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "بوابة الخدمات والرقابة المؤسسية",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }

                    // User Info Card inside Header
                    if (currentUser != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = currentUser.fullName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${currentUser.department} • ${currentUser.role}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Items List
            navItems.forEach { item ->
                val isSelected = currentScreen == item.screen
                val isAccessible = RolePermissions.canAccessScreen(currentUser, item.screen)

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.screen.titleAr,
                            tint = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                !isAccessible -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.screen.titleAr,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = if (!isAccessible) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else Color.Unspecified
                            )
                            if (!isAccessible) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "محمي بالصلاحيات",
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    },
                    badge = {
                        if (item.badgeCount > 0) {
                            Badge(
                                containerColor = if (item.screen == AppScreen.NOTIFICATIONS) HrStatusError else MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ) {
                                Text(item.badgeCount.toString())
                            }
                        }
                    },
                    selected = isSelected,
                    onClick = { onSelectScreen(item.screen) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(16.dp))

            // Footer Credit in Navigation Drawer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "إشراف ومتابعة أ.أحمد العمري",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    )
                    Text(
                        text = "نظام الجودة والرقابة المؤسسية ISO 9001",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
