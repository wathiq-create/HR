package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmployeeEntity
import com.example.data.local.OvertimeRequestEntity
import com.example.data.model.OvertimeType
import com.example.data.model.RequestStatus
import com.example.ui.components.InfoColumn
import com.example.ui.components.RequestStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissions
import com.example.util.SmartDocumentImporter

@Composable
fun OvertimeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val overtimes by viewModel.overtimeRequests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val employees by viewModel.employees.collectAsState()

    var showNewOvertimeDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("الكل") }
    var selectedRequestForAction by remember { mutableStateOf<OvertimeRequestEntity?>(null) }
    var selectedRequestForEdit by remember { mutableStateOf<OvertimeRequestEntity?>(null) }
    var selectedRequestForDelete by remember { mutableStateOf<OvertimeRequestEntity?>(null) }
    var approvalComment by remember { mutableStateOf("") }

    val filterTabs = listOf("الكل", "بانتظار الاعتماد", "المعتمدة", "المرفوضة")

    val filteredOvertimes = overtimes.filter { req ->
        when (selectedTab) {
            "بانتظار الاعتماد" -> req.status.startsWith("Pending") || req.status == "Submitted"
            "المعتمدة" -> req.status == "Approved" || req.status == "DeptApproved" || req.status == "HrApproved"
            "المرفوضة" -> req.status == "Rejected"
            else -> true
        }
    }

    val totalHoursApproved = overtimes.filter { it.status == "Approved" || it.status == "HrApproved" || it.status == "DeptApproved" }.sumOf { it.hours }
    val totalCostApproved = overtimes.filter { it.status == "Approved" || it.status == "HrApproved" || it.status == "DeptApproved" }.sumOf { it.calculatedAmount }

    val canCreate = RolePermissions.canCreateOvertime(currentUser)
    val canApprove = RolePermissions.canApproveOvertime(currentUser)
    val canDelete = RolePermissions.canDelete(currentUser)

    Scaffold(
        floatingActionButton = {
            if (canCreate) {
                ExtendedFloatingActionButton(
                    onClick = { showNewOvertimeDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("تسجيل عمل إضافي", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
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
            // Overtime Statistics Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي الساعات المعتمدة",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$totalHoursApproved ساعة",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "إجمالي الاستحقاق المالي",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${"%.1f".format(totalCostApproved)} ر.ي",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = HrStatusSuccess
                        )
                    }
                }
            }

            // Filter Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterTabs) { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Requests List
            if (filteredOvertimes.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد طلبات أجر إضافي في هذا القسم.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredOvertimes, key = { it.id }) { req ->
                        OvertimeRequestCardEnhanced(
                            req = req,
                            currentUserRole = currentUser?.role ?: "",
                            canApprove = canApprove,
                            canEdit = canCreate,
                            canDelete = canDelete,
                            onActionClick = { selectedRequestForAction = req },
                            onEditClick = { selectedRequestForEdit = req },
                            onDeleteClick = { selectedRequestForDelete = req }
                        )
                    }
                }
            }
        }
    }

    if (showNewOvertimeDialog) {
        NewOvertimeRequestDialogEnhanced(
            employees = employees,
            onDismiss = { showNewOvertimeDialog = false },
            onSubmit = { empId, empName, dept, date, dayType, start, end, hrs, type, reason, task, rate ->
                viewModel.submitOvertimeRequest(empId, empName, dept, date, dayType, start, end, hrs, type, reason, task, rate)
                showNewOvertimeDialog = false
            }
        )
    }

    selectedRequestForEdit?.let { req ->
        EditOvertimeRequestDialog(
            ot = req,
            onDismiss = { selectedRequestForEdit = null },
            onSubmit = { updated ->
                viewModel.updateOvertimeRequest(updated)
                selectedRequestForEdit = null
            }
        )
    }

    selectedRequestForDelete?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestForDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = HrStatusError) },
            title = { Text("تأكيد حذف طلب العمل الإضافي", fontWeight = FontWeight.Bold) },
            text = {
                Text("هل أنت متأكد من رغبتك في حذف طلب العمل الإضافي رقم (${req.requestNumber}) للموظف ${req.employeeName}؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteOvertimeRequest(req)
                        selectedRequestForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HrStatusError)
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

    if (selectedRequestForAction != null) {
        val req = selectedRequestForAction!!
        AlertDialog(
            onDismissRequest = { selectedRequestForAction = null },
            title = {
                Text(
                    text = "اتخاذ قرار بشأن طلب العمل الإضافي ${req.requestNumber}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "الموظف: ${req.employeeName} (${req.department})\nالساعات: ${req.hours} ساعة (${req.date})\nالاستحقاق: ${"%.1f".format(req.calculatedAmount)} ر.ي",
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
                            viewModel.updateOvertimeApproval(req.id, nextStatus, approvalComment)
                            selectedRequestForAction = null
                            approvalComment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusSuccess)
                    ) {
                        Text("اعتماد الطلب")
                    }

                    Button(
                        onClick = {
                            viewModel.updateOvertimeApproval(req.id, RequestStatus.Rejected, approvalComment)
                            selectedRequestForAction = null
                            approvalComment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusError)
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
fun OvertimeRequestCardEnhanced(
    req: OvertimeRequestEntity,
    currentUserRole: String,
    canApprove: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onActionClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canTakeAction = canApprove && (req.status.startsWith("Pending") || req.status == "Submitted")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
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

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "التاريخ", value = req.date)
                InfoColumn(label = "الساعات", value = "${req.hours} س")
                InfoColumn(label = "الاستحقاق المالي", value = "${"%.1f".format(req.calculatedAmount)} ر.ي")
            }

            if (req.projectOrTask.isNotBlank()) {
                Text(
                    text = "المهام المنجزة: ${req.projectOrTask}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (req.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
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
                    if (canEdit) {
                        TextButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تعديل", fontSize = 12.sp)
                        }
                    }
                    if (canDelete) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = HrStatusError, modifier = Modifier.size(17.dp))
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
fun NewOvertimeRequestDialogEnhanced(
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String, String, Double, OvertimeType, String, String, Double) -> Unit
) {
    if (employees.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تنبيه") },
            text = { Text("لا يوجد موظفون مسجلون في النظام.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
        )
        return
    }

    var selectedEmployee by remember { mutableStateOf(employees.first()) }
    var date by remember { mutableStateOf("2026-08-22") }
    var dayType by remember { mutableStateOf("يوم عمل عادي") }
    var startTime by remember { mutableStateOf("17:00") }
    var endTime by remember { mutableStateOf("20:30") }
    var hours by remember { mutableStateOf("3.5") }
    var overtimeType by remember { mutableStateOf(OvertimeType.NormalDay) }
    var reason by remember { mutableStateOf("استكمال تقارير الإقفال المالي السنوي") }
    var tasksPerformed by remember { mutableStateOf("إعداد كشوفات الرواتب وتسوية العهد") }
    var hourlyRate by remember { mutableStateOf("1500.0") }
    var showImportMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تسجيل عمل إضافي", fontWeight = FontWeight.Bold)
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
                                Text("اختر كشف ساعات إضافية لتعبئة النموذج تلقائياً:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                SmartDocumentImporter.sampleOvertimePdfTemplates.forEach { t ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                date = t.date
                                                startTime = t.startTime
                                                endTime = t.endTime
                                                hours = t.hours.toString()
                                                overtimeType = t.overtimeType
                                                reason = t.taskDescription
                                                tasksPerformed = t.outputOrDeliverable
                                                showImportMenu = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text("${t.taskDescription} (${t.hours} ساعات)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
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
                    Text("اختر الموظف:", style = MaterialTheme.typography.labelSmall)
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
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("التاريخ (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("من الساعة") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("إلى الساعة") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it },
                            label = { Text("عدد الساعات") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = hourlyRate,
                            onValueChange = { hourlyRate = it },
                            label = { Text("سعر الساعة (ر.ي)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("مبررات التكليف بالعمل الإضافي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = tasksPerformed,
                        onValueChange = { tasksPerformed = it },
                        label = { Text("المهام والإنجازات المنفذة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hrs = hours.toDoubleOrNull() ?: 1.0
                    val rate = hourlyRate.toDoubleOrNull() ?: 1500.0
                    onSubmit(
                        selectedEmployee.id,
                        selectedEmployee.fullName,
                        selectedEmployee.department,
                        date,
                        dayType,
                        startTime,
                        endTime,
                        hrs,
                        overtimeType,
                        reason,
                        tasksPerformed,
                        rate
                    )
                },
                enabled = hours.isNotBlank() && reason.isNotBlank()
            ) {
                Text("تسجيل الطلب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun EditOvertimeRequestDialog(
    ot: OvertimeRequestEntity,
    onDismiss: () -> Unit,
    onSubmit: (OvertimeRequestEntity) -> Unit
) {
    var date by remember { mutableStateOf(ot.date) }
    var startTime by remember { mutableStateOf(ot.startTime) }
    var endTime by remember { mutableStateOf(ot.endTime) }
    var hoursStr by remember { mutableStateOf(ot.hours.toString()) }
    var reason by remember { mutableStateOf(ot.reason) }
    var tasks by remember { mutableStateOf(ot.projectOrTask) }
    var rateStr by remember { mutableStateOf((if (ot.hours > 0) (ot.calculatedAmount / ot.hours / 1.5) else 1500.0).toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل طلب العمل الإضافي (${ot.requestNumber})", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("الموظف: ${ot.employeeName} (${ot.department})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                item {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("التاريخ") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("من") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("إلى") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = hoursStr, onValueChange = { hoursStr = it }, label = { Text("الساعات") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text("سعر الساعة") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("السبب") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = tasks, onValueChange = { tasks = it }, label = { Text("المهام المنجزة") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hrs = hoursStr.toDoubleOrNull() ?: ot.hours
                    val rate = rateStr.toDoubleOrNull() ?: 1500.0
                    val calc = hrs * rate * 1.5
                    onSubmit(
                        ot.copy(
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            hours = hrs,
                            calculatedAmount = calc,
                            reason = reason,
                            projectOrTask = tasks,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
