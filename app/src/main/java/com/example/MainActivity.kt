package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AccessDeniedView
import com.example.ui.components.ConnectivityStatusBanner
import com.example.ui.components.HrBottomSupervisorBar
import com.example.ui.components.HrNavigationDrawerContent
import com.example.ui.components.HrTopBar
import com.example.ui.screens.*
import com.example.ui.theme.HrTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissions
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val globalErrorState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Setup global uncaught exception handler to prevent silent crash exits
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                Log.e("MainActivity", "Uncaught exception caught in thread ${thread.name}", throwable)
                runOnUiThread {
                    globalErrorState.value = "${throwable.javaClass.simpleName}: ${throwable.localizedMessage ?: throwable.message ?: "حدث خطأ داخلي غير معروف"}"
                }
            }

            enableEdgeToEdge()

            setContent {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HrTheme {
                        val activeError = globalErrorState.value
                        if (activeError != null) {
                            AppCrashFallbackScreen(
                                errorMessage = activeError,
                                onRestart = {
                                    globalErrorState.value = null
                                    recreate()
                                }
                            )
                        } else {
                            val mainViewModel: MainViewModel = viewModel()
                            HrMainApp(viewModel = mainViewModel)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e("MainActivity", "Critical error in onCreate", e)
            setContent {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HrTheme {
                        AppCrashFallbackScreen(
                            errorMessage = "${e.javaClass.simpleName}: ${e.localizedMessage ?: e.message ?: "فشل تشغيل التطبيق"}",
                            onRestart = { recreate() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppCrashFallbackScreen(
    errorMessage: String,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Alert Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "تحذير خطأ",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Title
                    Text(
                        text = "تنبيه: حدث خطأ غير متوقع",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Description
                    Text(
                        text = "واجه النظام خطأ أثناء تنفيذ العملية، وتم إيقافه بأمان لمنع الخروج المفاجئ وفقدان البيانات.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    // Error Message Container
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "تفاصيل الخطأ:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إعادة تشغيل النظام", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("خطأ النظام", errorMessage)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ تفاصيل الخطأ", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ تفاصيل الخطأ للدعم الفني", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HrMainApp(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val kpis by viewModel.dashboardKpis.collectAsState()
    val usersList by viewModel.users.collectAsState()
    val pendingSyncQueue by viewModel.pendingSyncQueue.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (currentUser == null) {
        // Show Login Screen with Pinned Supervisor Bottom Bar
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                HrBottomSupervisorBar(
                    isOnline = isOnline,
                    pendingSyncCount = pendingSyncQueue.size,
                    onSyncClick = { viewModel.triggerSync() }
                )
            }
        ) { innerPadding ->
            LoginScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        // Logged In ERP Interface
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                HrNavigationDrawerContent(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    pendingApprovalsCount = kpis.pendingApprovalsTotal,
                    unreadNotificationsCount = unreadNotifications,
                    onSelectScreen = { screen ->
                        viewModel.navigateTo(screen)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    HrTopBar(
                        title = currentScreen.titleAr,
                        currentUser = currentUser,
                        isOnline = isOnline,
                        unreadNotifications = unreadNotifications,
                        usersList = usersList,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onToggleNetwork = { viewModel.toggleNetwork() },
                        onOpenNotifications = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
                        onSwitchUser = { user -> viewModel.switchUser(user) },
                        onLogout = { viewModel.logout() }
                    )
                },
                bottomBar = {
                    HrBottomSupervisorBar(
                        isOnline = isOnline,
                        pendingSyncCount = pendingSyncQueue.size,
                        onSyncClick = { viewModel.triggerSync() }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Top Connectivity Indicator Banner across all screens
                    if (!isOnline || pendingSyncQueue.isNotEmpty() || isSyncing) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ConnectivityStatusBanner(
                                isOnline = isOnline,
                                pendingSyncCount = pendingSyncQueue.size,
                                isSyncing = isSyncing,
                                onToggleNetwork = { viewModel.toggleNetwork() },
                                onSyncClick = { viewModel.triggerSync() }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            if (!RolePermissions.canAccessScreen(currentUser, screen)) {
                                AccessDeniedView(
                                    currentUser = currentUser,
                                    screen = screen,
                                    onGoToDashboard = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                                )
                            } else {
                                when (screen) {
                                    AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
                                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                                    AppScreen.EMPLOYEES -> EmployeesScreen(viewModel = viewModel)
                                    AppScreen.LEAVES -> LeaveScreen(viewModel = viewModel)
                                    AppScreen.OVERTIME -> OvertimeScreen(viewModel = viewModel)
                                    AppScreen.PENALTIES -> PenaltiesScreen(viewModel = viewModel)
                                    AppScreen.SERVICES -> ServicesScreen(viewModel = viewModel)
                                    AppScreen.APPROVALS -> ApprovalsScreen(viewModel = viewModel)
                                    AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                                    AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                                    AppScreen.USERS_ROLES -> UsersScreen(viewModel = viewModel)
                                    AppScreen.SYNC_CENTER -> SyncScreen(viewModel = viewModel)
                                    AppScreen.AUDIT_BACKUP -> BackupAuditScreen(viewModel = viewModel)
                                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
