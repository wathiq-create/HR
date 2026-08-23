package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.AdminTransactionEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.MealRequestEntity
import com.example.data.local.TransportationRequestEntity
import com.example.data.model.MealType
import com.example.data.model.Priority
import com.example.data.model.TransportType
import com.example.ui.components.InfoColumn
import com.example.ui.components.RequestStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissions
import com.example.util.SmartDocumentImporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedServiceTab by remember { mutableStateOf(0) }
    val tabs = listOf("الوجبات والإعاشة", "المواصلات والنقل", "المعاملات الإدارية")

    val meals by viewModel.mealRequests.collectAsState()
    val transport by viewModel.transportRequests.collectAsState()
    val transactions by viewModel.adminTransactions.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showNewServiceDialog by remember { mutableStateOf(false) }
    var selectedTxForEdit by remember { mutableStateOf<AdminTransactionEntity?>(null) }
    var selectedTxForDelete by remember { mutableStateOf<AdminTransactionEntity?>(null) }
    var selectedTxForApproval by remember { mutableStateOf<AdminTransactionEntity?>(null) }
    var approvalComment by remember { mutableStateOf("") }

    val canCreateTx = RolePermissions.canCreateTransaction(currentUser)
    val canApproveTx = RolePermissions.canApproveTransaction(currentUser)
    val canDelete = RolePermissions.canDelete(currentUser)

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewServiceDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    val label = when (selectedServiceTab) {
                        0 -> "طلب وجبات"
                        1 -> "طلب مواصلات"
                        else -> "معاملة جديدة"
                    }
                    Text(label, fontWeight = FontWeight.Bold)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
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
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedServiceTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedServiceTab == index,
                        onClick = { selectedServiceTab = index },
                        text = { Text(title, fontWeight = if (selectedServiceTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedServiceTab) {
                0 -> MealsList(meals = meals)
                1 -> TransportList(transports = transport)
                2 -> TransactionsListEnhanced(
                    transactions = transactions,
                    canApprove = canApproveTx,
                    canEdit = canCreateTx || currentUser?.role == "SuperAdmin",
                    canDelete = canDelete,
                    onApprove = { selectedTxForApproval = it },
                    onEdit = { selectedTxForEdit = it },
                    onDelete = { selectedTxForDelete = it }
                )
            }
        }
    }

    if (showNewServiceDialog) {
        when (selectedServiceTab) {
            0 -> NewMealDialog(
                employees = employees,
                onDismiss = { showNewServiceDialog = false },
                onSubmit = { empId, empName, dept, date, type, count, vendor, reason ->
                    viewModel.submitMealRequest(empId, empName, dept, date, type, count, vendor, reason)
                    showNewServiceDialog = false
                }
            )
            1 -> NewTransportDialog(
                employees = employees,
                onDismiss = { showNewServiceDialog = false },
                onSubmit = { empId, empName, dept, date, route, from, to, type, trips, cost, driver, reason ->
                    viewModel.submitTransportRequest(empId, empName, dept, date, route, from, to, type, trips, cost, driver, reason)
                    showNewServiceDialog = false
                }
            )
            2 -> NewTransactionDialogEnhanced(
                employees = employees,
                onDismiss = { showNewServiceDialog = false },
                onSubmit = { empId, empName, dept, date, sub, desc, prio, target ->
                    viewModel.submitAdminTransaction(empId, empName, dept, date, sub, desc, prio, target)
                    showNewServiceDialog = false
                }
            )
        }
    }

    selectedTxForEdit?.let { tx ->
        EditTransactionDialog(
            tx = tx,
            onDismiss = { selectedTxForEdit = null },
            onSubmit = { updated ->
                viewModel.updateAdminTransaction(updated)
                selectedTxForEdit = null
            }
        )
    }

    selectedTxForDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTxForDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = HrStatusError) },
            title = { Text("تأكيد حذف المعاملة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف المعاملة الإدارية (${tx.transactionNumber} - ${tx.subject})؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAdminTransaction(tx)
                        selectedTxForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HrStatusError)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = { TextButton(onClick = { selectedTxForDelete = null }) { Text("إلغاء") } }
        )
    }

    selectedTxForApproval?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTxForApproval = null },
            title = { Text("تحديث حالة المعاملة (${tx.transactionNumber})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الموضوع: ${tx.subject}\nالمقدم: ${tx.employeeName} (${tx.department})\nالجهة المحال إليها: ${tx.targetDepartment}")
                    OutlinedTextField(
                        value = approvalComment,
                        onValueChange = { approvalComment = it },
                        label = { Text("التوجيه والقرار الإداري") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.updateAdminTransactionApproval(tx.id, "Approved", approvalComment)
                            selectedTxForApproval = null
                            approvalComment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusSuccess)
                    ) {
                        Text("تمت المعالجة")
                    }
                    Button(
                        onClick = {
                            viewModel.updateAdminTransactionApproval(tx.id, "UnderReview", approvalComment)
                            selectedTxForApproval = null
                            approvalComment = ""
                        }
                    ) {
                        Text("قيد الدراسة")
                    }
                }
            },
            dismissButton = { TextButton(onClick = { selectedTxForApproval = null }) { Text("إغلاق") } }
        )
    }
}

@Composable
fun MealsList(meals: List<MealRequestEntity>) {
    val totalCost = meals.sumOf { it.totalCost }
    val totalMeals = meals.sumOf { it.count }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
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
                InfoColumn(label = "إجمالي الوجبات", value = "$totalMeals وجبة")
                InfoColumn(label = "تكلفة الإعاشة", value = "${"%.1f".format(totalCost)} ر.ي")
                InfoColumn(label = "عدد الطلبات", value = "${meals.size}")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(meals) { meal ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = meal.requestNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            RequestStatusBadge(statusStr = meal.status)
                        }
                        Text(text = "${meal.employeeName} • ${meal.vendor}", fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoColumn(label = "نوع الوجبة", value = meal.mealType)
                            InfoColumn(label = "العدد", value = "${meal.count} وجبات")
                            InfoColumn(label = "التكلفة", value = "${meal.totalCost} ر.ي")
                        }
                        Text(text = "المناسبة: ${meal.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun TransportList(transports: List<TransportationRequestEntity>) {
    val totalCost = transports.sumOf { it.totalCost }
    val totalTrips = transports.sumOf { it.tripsCount }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
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
                InfoColumn(label = "إجمالي الرحلات", value = "$totalTrips رحلة")
                InfoColumn(label = "تكلفة النقل", value = "${"%.1f".format(totalCost)} ر.ي")
                InfoColumn(label = "الطلبات", value = "${transports.size}")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(transports) { trans ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = trans.requestNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            RequestStatusBadge(statusStr = trans.status)
                        }
                        Text(text = "${trans.employeeName} • خط السير: ${trans.route}", fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoColumn(label = "وسيلة النقل", value = trans.transportType)
                            InfoColumn(label = "التكلفة", value = "${trans.totalCost} ر.ي")
                            InfoColumn(label = "التاريخ", value = trans.date)
                        }
                        Text(text = "الوجهة: ${trans.destination} • السبب: ${trans.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsListEnhanced(
    transactions: List<AdminTransactionEntity>,
    canApprove: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onApprove: (AdminTransactionEntity) -> Unit,
    onEdit: (AdminTransactionEntity) -> Unit,
    onDelete: (AdminTransactionEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(transactions, key = { it.id }) { trx ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = trx.transactionNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        RequestStatusBadge(statusStr = trx.status)
                    }
                    Text(text = trx.subject, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoColumn(label = "المقدم", value = trx.employeeName)
                        InfoColumn(label = "الجهة المحال إليها", value = trx.targetDepartment)
                        InfoColumn(label = "الأولوية", value = trx.priority)
                    }
                    Text(text = trx.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (canEdit) {
                                TextButton(onClick = { onEdit(trx) }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تعديل", fontSize = 12.sp)
                                }
                            }
                            if (canDelete) {
                                IconButton(onClick = { onDelete(trx) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = HrStatusError, modifier = Modifier.size(17.dp))
                                }
                            }
                        }

                        if (canApprove) {
                            Button(
                                onClick = { onApprove(trx) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("توجيه / معالجة", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewMealDialog(
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, MealType, Int, String, String) -> Unit
) {
    if (employees.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تنبيه") },
            text = { Text("لا يوجد موظفون مسجلون في النظام لتقديم الطلب.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
        )
        return
    }
    var selectedEmp by remember { mutableStateOf(employees.first()) }
    var date by remember { mutableStateOf("2026-08-21") }
    var mealType by remember { mutableStateOf(MealType.Dinner) }
    var count by remember { mutableStateOf("4") }
    var vendor by remember { mutableStateOf("مطاعم الضيافة الحديثة") }
    var reason by remember { mutableStateOf("وجبة عشاء لفريق العمل الإضافي") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("طلب وجبات وإعاشة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("التاريخ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("عدد الوجبات") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vendor, onValueChange = { vendor = it }, label = { Text("المورد / المطعم") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("السبب والمناسبة") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(selectedEmp.id, selectedEmp.fullName, selectedEmp.department, date, mealType, count.toIntOrNull() ?: 1, vendor, reason)
            }) { Text("إرسال الطلب") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun NewTransportDialog(
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String, String, TransportType, Int, Double, String, String) -> Unit
) {
    if (employees.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تنبيه") },
            text = { Text("لا يوجد موظفون مسجلون في النظام لتقديم الطلب.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
        )
        return
    }
    var selectedEmp by remember { mutableStateOf(employees.first()) }
    var date by remember { mutableStateOf("2026-08-21") }
    var route by remember { mutableStateOf("المقر الرئيسي ⟷ المطار") }
    var from by remember { mutableStateOf("المقر الرئيسي") }
    var to by remember { mutableStateOf("مطار صنعاء / عدن") }
    var transportType by remember { mutableStateOf(TransportType.Taxi) }
    var trips by remember { mutableStateOf("1") }
    var cost by remember { mutableStateOf("15000.0") }
    var driver by remember { mutableStateOf("خدمة النقل المؤسسي") }
    var reason by remember { mutableStateOf("استقبال وفد الشركة") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("طلب مواصلات ونقل", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("التاريخ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("خط السير") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("التكلفة التقديرية (ر.ي)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("الغرض من المشوار") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(selectedEmp.id, selectedEmp.fullName, selectedEmp.department, date, route, from, to, transportType, trips.toIntOrNull() ?: 1, cost.toDoubleOrNull() ?: 0.0, driver, reason)
            }) { Text("إرسال الطلب") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun NewTransactionDialogEnhanced(
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String, Priority, String) -> Unit
) {
    if (employees.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تنبيه") },
            text = { Text("لا يوجد موظفون مسجلون في النظام لتقديم الطلب.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
        )
        return
    }
    var selectedEmp by remember { mutableStateOf(employees.first()) }
    var date by remember { mutableStateOf("2026-08-21") }
    var subject by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var targetDept by remember { mutableStateOf("الموارد البشرية") }
    var priority by remember { mutableStateOf(Priority.High) }
    var showImportMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إنشاء معاملة إدارية", fontWeight = FontWeight.Bold)
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    if (showImportMenu) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("اختر خطاب/مستند رسمي لتعبئة المعاملة آلياً:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                SmartDocumentImporter.sampleTransactionPdfTemplates.forEach { t ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                subject = t.subject
                                                desc = t.description
                                                targetDept = t.targetDepartment
                                                priority = t.priority
                                                showImportMenu = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(t.subject, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
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
                    Text("الموظف مقدم المعاملة:", style = MaterialTheme.typography.labelSmall)
                    var exp by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { exp = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("${selectedEmp.fullName} (${selectedEmp.department})")
                    }
                    DropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(text = { Text("${emp.fullName} - ${emp.department}") }, onClick = { selectedEmp = emp; exp = false })
                        }
                    }
                }

                item {
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("موضوع المعاملة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = targetDept, onValueChange = { targetDept = it }, label = { Text("الجهة المحال إليها") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("شرح وتفاصيل المعاملة") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedEmp.id, selectedEmp.fullName, selectedEmp.department, date, subject, desc, priority, targetDept) },
                enabled = subject.isNotBlank()
            ) { Text("إرسال المعاملة") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun EditTransactionDialog(
    tx: AdminTransactionEntity,
    onDismiss: () -> Unit,
    onSubmit: (AdminTransactionEntity) -> Unit
) {
    var subject by remember { mutableStateOf(tx.subject) }
    var desc by remember { mutableStateOf(tx.description) }
    var targetDept by remember { mutableStateOf(tx.targetDepartment) }
    var statusStr by remember { mutableStateOf(tx.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل المعاملة (${tx.transactionNumber})", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("الموضوع") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = targetDept, onValueChange = { targetDept = it }, label = { Text("الجهة المحال إليها") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("التفاصيل") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(tx.copy(subject = subject, description = desc, targetDepartment = targetDept, status = statusStr))
                }
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
