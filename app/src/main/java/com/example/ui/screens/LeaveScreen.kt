package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
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
import com.example.data.local.EmployeeEntity
import com.example.data.local.LeaveRequestEntity
import com.example.data.local.UserEntity
import com.example.data.model.LeaveType
import com.example.data.model.RequestStatus
import com.example.ui.components.InfoColumn
import com.example.ui.components.RequestStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissions
import com.example.util.SmartDocumentImporter

@Composable
fun LeaveScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val leaves by viewModel.leaveRequests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val employees by viewModel.employees.collectAsState()

    var showNewLeaveDialog by remember { mutableStateOf(false) }
    var leaveSearchQuery by remember { mutableStateOf("") }
    var selectedStatusTab by remember { mutableStateOf("الكل") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }

    // Dialog state
    var selectedRequestForAction by remember { mutableStateOf<LeaveRequestEntity?>(null) }
    var selectedRequestForView by remember { mutableStateOf<LeaveRequestEntity?>(null) }
    var selectedRequestForEdit by remember { mutableStateOf<LeaveRequestEntity?>(null) }
    var selectedRequestForDelete by remember { mutableStateOf<LeaveRequestEntity?>(null) }
    var approvalComment by remember { mutableStateOf("") }

    val statusTabs = listOf("الكل", "بانتظار الاعتماد", "المعتمدة", "المرفوضة")
    val typeFilters = listOf("الكل", "سنوية", "مرضية", "طارئة", "بدون راتب")

    val filteredLeaves = leaves.filter { req ->
        val matchesSearch = leaveSearchQuery.isBlank() ||
                req.employeeName.contains(leaveSearchQuery, ignoreCase = true) ||
                req.requestNumber.contains(leaveSearchQuery, ignoreCase = true) ||
                req.department.contains(leaveSearchQuery, ignoreCase = true) ||
                req.reason.contains(leaveSearchQuery, ignoreCase = true) ||
                req.substituteEmployee.contains(leaveSearchQuery, ignoreCase = true)

        val matchesStatus = when (selectedStatusTab) {
            "بانتظار الاعتماد" -> req.status.startsWith("Pending") || req.status == "Submitted"
            "المعتمدة" -> req.status == "Approved" || req.status == "DeptApproved" || req.status == "HrApproved"
            "المرفوضة" -> req.status == "Rejected"
            else -> true
        }

        val matchesType = when (selectedTypeFilter) {
            "سنوية" -> req.leaveType.contains("Annual", ignoreCase = true) || req.leaveType.contains("سنوي") || req.leaveType.contains("اعتيادي")
            "مرضية" -> req.leaveType.contains("Sick", ignoreCase = true) || req.leaveType.contains("مرض")
            "طارئة" -> req.leaveType.contains("Emergency", ignoreCase = true) || req.leaveType.contains("طارئ")
            "بدون راتب" -> req.leaveType.contains("Unpaid", ignoreCase = true) || req.leaveType.contains("بدون")
            else -> true
        }

        matchesSearch && matchesStatus && matchesType
    }

    val canCreate = RolePermissions.canCreateLeave(currentUser)
    val canApprove = RolePermissions.canApproveLeave(currentUser)
    val canDelete = RolePermissions.canDelete(currentUser)

    Scaffold(
        floatingActionButton = {
            if (canCreate) {
                ExtendedFloatingActionButton(
                    onClick = { showNewLeaveDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("طلب إجازة جديد", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_leave_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Leave Balances Overview Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نظرة عامة على أرصدة وطلبات الإجازات",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "سنة 2026",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val totalAnnual = employees.sumOf { it.annualLeaveBalance }
                        val pendingCount = leaves.count { it.status.startsWith("Pending") || it.status == "Submitted" }
                        val approvedCount = leaves.count { it.status == "Approved" || it.status == "DeptApproved" || it.status == "HrApproved" }

                        BalancePill(label = "إجمالي الرصيد السنوي", days = totalAnnual, color = MaterialTheme.colorScheme.primary)
                        BalancePill(label = "طلبات قيد المراجعة", days = pendingCount, color = HrStatusWarning)
                        BalancePill(label = "طلبات معتمدة", days = approvedCount, color = HrStatusSuccess)
                        BalancePill(label = "إجمالي الطلبات", days = leaves.size, color = HrTertiary)
                    }
                }
            }

            // Search Field
            OutlinedTextField(
                value = leaveSearchQuery,
                onValueChange = { leaveSearchQuery = it },
                placeholder = { Text("بحث باسم الموظف، رقم الطلب، الإدارة، أو السبب...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (leaveSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { leaveSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leave_search_field")
            )

            // Status Filter Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusTabs) { tab ->
                    FilterChip(
                        selected = selectedStatusTab == tab,
                        onClick = { selectedStatusTab = tab },
                        label = { Text(tab, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Leave Type Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "نوع الإجازة:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                typeFilters.forEach { typeName ->
                    FilterChip(
                        selected = selectedTypeFilter == typeName,
                        onClick = { selectedTypeFilter = typeName },
                        label = { Text(typeName, fontSize = 11.5.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Header summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الطلبات المسجلة: ${filteredLeaves.size} طلب",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Requests List
            if (filteredLeaves.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                            Text(
                                text = "لا توجد طلبات إجازة مطابقة لخيارات البحث والفلترة.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLeaves, key = { it.id }) { req ->
                        LeaveRequestCardEnhanced(
                            req = req,
                            currentUserRole = currentUser?.role ?: "",
                            canApprove = canApprove,
                            canEdit = canCreate,
                            canDelete = canDelete,
                            onViewClick = { selectedRequestForView = req },
                            onActionClick = { selectedRequestForAction = req },
                            onEditClick = { selectedRequestForEdit = req },
                            onDeleteClick = { selectedRequestForDelete = req }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    // View Details Dialog
    selectedRequestForView?.let { req ->
        LeaveDetailsDialog(
            req = req,
            canEdit = canCreate,
            onDismiss = { selectedRequestForView = null },
            onEdit = {
                selectedRequestForView = null
                selectedRequestForEdit = req
            }
        )
    }

    // New Leave Request Dialog
    if (showNewLeaveDialog) {
        NewLeaveRequestDialogEnhanced(
            employees = employees,
            currentUser = currentUser,
            onDismiss = { showNewLeaveDialog = false },
            onSubmit = { empId, empName, dept, type, start, end, days, reason, sub ->
                viewModel.submitLeaveRequest(empId, empName, dept, type, start, end, days, reason, sub)
                showNewLeaveDialog = false
            }
        )
    }

    // Edit Leave Request Dialog
    selectedRequestForEdit?.let { req ->
        EditLeaveRequestDialog(
            leave = req,
            onDismiss = { selectedRequestForEdit = null },
            onSubmit = { updated ->
                viewModel.updateLeaveRequest(updated)
                selectedRequestForEdit = null
            }
        )
    }

    // Delete Leave Request Dialog
    selectedRequestForDelete?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestForDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = HrStatusError) },
            title = { Text("تأكيد حذف طلب الإجازة", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف طلب الإجازة رقم (${req.requestNumber}) للموظف ${req.employeeName}؟\nسيتم إلغاء تأثيرها على رصيد الإجازات وحذفها من سجل المزامنة.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLeaveRequest(req)
                        selectedRequestForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HrStatusError),
                    modifier = Modifier.testTag("confirm_delete_leave_button")
                ) {
                    Text("نعم، حذف الطلب")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestForDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Approval / Decision Dialog
    selectedRequestForAction?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestForAction = null },
            title = {
                Text(
                    text = "اتخاذ قرار بشأن طلب الإجازة ${req.requestNumber}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "الموظف: ${req.employeeName} (${req.department})\nنوع الإجازة: ${req.leaveType} (${req.daysCount} أيام)\nمن ${req.startDate} إلى ${req.endDate}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = approvalComment,
                        onValueChange = { approvalComment = it },
                        label = { Text("الملاحظات والتعليق الإداري") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val nextStatus = when (currentUser?.role) {
                                "DepartmentHead" -> RequestStatus.DeptApproved
                                "HrManager" -> RequestStatus.HrApproved
                                "GeneralManager", "SuperAdmin" -> RequestStatus.Approved
                                else -> RequestStatus.Approved
                            }
                            viewModel.updateLeaveApproval(req.id, nextStatus, approvalComment)
                            selectedRequestForAction = null
                            approvalComment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusSuccess),
                        modifier = Modifier.testTag("approve_leave_button")
                    ) {
                        Text("اعتماد الطلب")
                    }

                    Button(
                        onClick = {
                            viewModel.updateLeaveApproval(req.id, RequestStatus.Rejected, approvalComment)
                            selectedRequestForAction = null
                            approvalComment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusError),
                        modifier = Modifier.testTag("reject_leave_button")
                    ) {
                        Text("رفض")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestForAction = null }) { Text("إغلاق") }
            }
        )
    }
}

@Composable
fun BalancePill(label: String, days: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LeaveRequestCardEnhanced(
    req: LeaveRequestEntity,
    currentUserRole: String,
    canApprove: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onViewClick: () -> Unit,
    onActionClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canTakeAction = canApprove && (req.status.startsWith("Pending") || req.status == "Submitted")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() }
            .testTag("leave_card_${req.requestNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = req.requestNumber,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = req.employeeName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                RequestStatusBadge(statusStr = req.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "نوع الإجازة", value = req.leaveType)
                InfoColumn(label = "المدة", value = "${req.daysCount} أيام")
                InfoColumn(label = "الفترة", value = "${req.startDate} ⟷ ${req.endDate}")
            }

            if (req.reason.isNotBlank()) {
                Text(
                    text = "السبب: ${req.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (req.substituteEmployee.isNotBlank()) {
                Text(
                    text = "الموظف البديل: ${req.substituteEmployee}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (req.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ملاحظات الاعتماد: ${req.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onViewClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "عرض التفاصيل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    if (canEdit) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = HrStatusError, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (canTakeAction) {
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مراجعة واعتماد", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveDetailsDialog(
    req: LeaveRequestEntity,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تفاصيل طلب الإجازة", fontWeight = FontWeight.Bold)
                RequestStatusBadge(statusStr = req.status)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "رقم الطلب: ${req.requestNumber}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text(text = "الموظف مقدم الطلب: ${req.employeeName}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "القسم / الإدارة: ${req.department}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoColumn(label = "نوع الإجازة", value = req.leaveType)
                        InfoColumn(label = "عدد الأيام", value = "${req.daysCount} يوم")
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoColumn(label = "تاريخ البدء", value = req.startDate)
                        InfoColumn(label = "تاريخ الانتهاء", value = req.endDate)
                    }
                }

                item {
                    InfoColumn(label = "الموظف البديل المكلف", value = if (req.substituteEmployee.isNotBlank()) req.substituteEmployee else "لا يوجد")
                }

                item {
                    HorizontalDivider()
                    Text("سبب الإجازة والمبررات:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text(text = if (req.reason.isNotBlank()) req.reason else "إجازة اعتيادية", style = MaterialTheme.typography.bodySmall)
                }

                if (req.notes.isNotBlank()) {
                    item {
                        HorizontalDivider()
                        Text("ملاحظات الاعتماد والمسار الإداري:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        Text(text = req.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                item {
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoColumn(label = "حالة المزامنة السحابية", value = req.syncStatus)
                        InfoColumn(label = "تاريخ التقديم", value = req.startDate)
                    }
                }
            }
        },
        confirmButton = {
            if (canEdit) {
                Button(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل الطلب")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

@Composable
fun NewLeaveRequestDialogEnhanced(
    employees: List<EmployeeEntity>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, LeaveType, String, String, Int, String, String) -> Unit
) {
    if (employees.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تنبيه") },
            text = { Text("لا يوجد موظفون مسجلون في النظام لتقديم طلب إجازة.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
        )
        return
    }
    var selectedEmployee by remember { mutableStateOf(employees.first()) }
    var leaveType by remember { mutableStateOf(LeaveType.Annual) }
    var startDate by remember { mutableStateOf("2026-09-01") }
    var endDate by remember { mutableStateOf("2026-09-05") }
    var daysCount by remember { mutableStateOf("5") }
    var reason by remember { mutableStateOf("إجازة سنوية اعتيادية للراحة وقضاء شؤون عائلية") }
    var substitute by remember { mutableStateOf("م.عبدالله القحطاني") }
    var showImportMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("طلب إجازة جديد", fontWeight = FontWeight.Bold)
                FilledTonalButton(
                    onClick = { showImportMenu = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استيراد PDF", fontSize = 11.sp)
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    if (showImportMenu) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("اختر نموذج طلب إجازة رسمي لتعبئة البيانات تلقائياً:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                SmartDocumentImporter.sampleLeavePdfTemplates.forEach { t ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                leaveType = t.leaveType
                                                startDate = t.startDate
                                                endDate = t.endDate
                                                daysCount = t.daysCount.toString()
                                                reason = t.reason
                                                substitute = t.substitute
                                                showImportMenu = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.EventNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(t.leaveType.titleAr, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                                Text(t.documentSource, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("اختر الموظف مقدم الطلب:", style = MaterialTheme.typography.labelSmall)
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${selectedEmployee.fullName} (${selectedEmployee.department})")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text("${emp.fullName} - ${emp.department}") },
                                onClick = {
                                    selectedEmployee = emp
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                item {
                    Text("نوع الإجازة:", style = MaterialTheme.typography.labelSmall)
                    var typeExpanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { typeExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(leaveType.titleAr)
                    }
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        LeaveType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.titleAr) },
                                onClick = {
                                    leaveType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("تاريخ البداية") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("تاريخ النهاية") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = daysCount,
                        onValueChange = { daysCount = it },
                        label = { Text("عدد الأيام المطلوبة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("سبب الإجازة والملاحظات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = substitute,
                        onValueChange = { substitute = it },
                        label = { Text("الموظف البديل أثناء الإجازة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysCount.toIntOrNull() ?: 1
                    onSubmit(
                        selectedEmployee.id,
                        selectedEmployee.fullName,
                        selectedEmployee.department,
                        leaveType,
                        startDate,
                        endDate,
                        days,
                        reason,
                        substitute
                    )
                },
                enabled = reason.isNotBlank(),
                modifier = Modifier.testTag("submit_new_leave_button")
            ) {
                Text("إرسال الطلب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun EditLeaveRequestDialog(
    leave: LeaveRequestEntity,
    onDismiss: () -> Unit,
    onSubmit: (LeaveRequestEntity) -> Unit
) {
    var startDate by remember { mutableStateOf(leave.startDate) }
    var endDate by remember { mutableStateOf(leave.endDate) }
    var daysCountStr by remember { mutableStateOf(leave.daysCount.toString()) }
    var reason by remember { mutableStateOf(leave.reason) }
    var substitute by remember { mutableStateOf(leave.substituteEmployee) }
    var notes by remember { mutableStateOf(leave.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل طلب إجازة (${leave.requestNumber})", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("الموظف: ${leave.employeeName} (${leave.department})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("من") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("إلى") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = daysCountStr, onValueChange = { daysCountStr = it }, label = { Text("عدد الأيام") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("السبب") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = substitute, onValueChange = { substitute = it }, label = { Text("الموظف البديل") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات إضافية") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysCountStr.toIntOrNull() ?: leave.daysCount
                    onSubmit(
                        leave.copy(
                            startDate = startDate,
                            endDate = endDate,
                            daysCount = days,
                            reason = reason,
                            substituteEmployee = substitute,
                            notes = notes,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier.testTag("save_edit_leave_button")
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
