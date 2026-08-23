package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.local.PenaltyEntity
import com.example.data.model.PenaltyAction
import com.example.data.model.Severity
import com.example.ui.components.InfoColumn
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissions

@Composable
fun PenaltiesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val penalties by viewModel.penalties.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showNewPenaltyDialog by remember { mutableStateOf(false) }
    val canCreatePenalty = RolePermissions.canCreatePenalty(currentUser)

    Scaffold(
        floatingActionButton = {
            if (canCreatePenalty) {
                ExtendedFloatingActionButton(
                    onClick = { showNewPenaltyDialog = true },
                    icon = { Icon(Icons.Default.Gavel, contentDescription = null) },
                    text = { Text("تسجيل واقعة / جزاء", fontWeight = FontWeight.Bold) },
                    containerColor = HrStatusError,
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
            // Security Notice Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HrStatusWarning.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, HrStatusWarning.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = HrStatusWarning)
                    Text(
                        text = "سجل الجزاءات والعقوبات التأديبية مشفر وسري للغاية، ويخضع للمادة (68) من لائحة العمل وسجل التدقيق الإداري.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "قائمة الوقائع والقرارات التأديبية (${penalties.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            if (penalties.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد أي جزاءات أو مخالفات مسجلة.",
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
                    items(penalties) { pen ->
                        PenaltyCard(pen = pen)
                    }
                }
            }
        }
    }

    if (showNewPenaltyDialog) {
        NewPenaltyDialog(
            employees = employees,
            onDismiss = { showNewPenaltyDialog = false },
            onSubmit = { empId, empName, dept, date, violType, desc, sev, act, ded, susp, ref, inv, stat ->
                viewModel.createPenalty(empId, empName, dept, date, violType, desc, sev, act, ded, susp, ref, inv, stat)
                showNewPenaltyDialog = false
            }
        )
    }
}

@Composable
fun PenaltyCard(pen: PenaltyEntity) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                        text = pen.penaltyNumber,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = HrStatusError
                    )
                    Text(
                        text = pen.employeeName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HrStatusError.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HrStatusError.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = pen.actionType,
                        color = HrStatusError,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "تاريخ الواقعة", value = pen.incidentDate)
                InfoColumn(label = "درجة المخالفة", value = pen.severity)
                if (pen.deductionAmount > 0) {
                    InfoColumn(label = "مبلغ الخصم", value = "${pen.deductionAmount} ر.ي")
                } else if (pen.suspensionDays > 0) {
                    InfoColumn(label = "مدة الإيقاف", value = "${pen.suspensionDays} أيام")
                } else {
                    InfoColumn(label = "الإجراء", value = "إنذار كتابي")
                }
            }

            Text(
                text = "نوع المخالفة: ${pen.violationType}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "الوصف: ${pen.description}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (pen.legalReference.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "المرجع النظامي: ${pen.legalReference}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (pen.employeeStatement.isNotBlank()) {
                Text(
                    text = "إفادة الموظف: ${pen.employeeStatement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NewPenaltyDialog(
    employees: List<com.example.data.local.EmployeeEntity>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String, Severity, PenaltyAction, Double, Int, String, String, String) -> Unit
) {
    if (employees.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("تنبيه") },
            text = { Text("لا يوجد موظفون مسجلون في النظام لتسجيل جزاء.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
        )
        return
    }
    var selectedEmployee by remember { mutableStateOf(employees.first()) }
    var incidentDate by remember { mutableStateOf("2026-08-21") }
    var violationType by remember { mutableStateOf("تأخر غير مبرر") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(Severity.Medium) }
    var actionType by remember { mutableStateOf(PenaltyAction.FirstWarning) }
    var deduction by remember { mutableStateOf("0.0") }
    var suspensionDays by remember { mutableStateOf("0") }
    var legalRef by remember { mutableStateOf("المادة 68 من لائحة العمل") }
    var investigation by remember { mutableStateOf("") }
    var statement by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل قرار جزاء إداري", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("الموظف المخالف:", style = MaterialTheme.typography.labelSmall)
                    var exp by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { exp = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${selectedEmployee.fullName} (${selectedEmployee.department})")
                    }
                    DropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text("${emp.fullName} - ${emp.department}") },
                                onClick = {
                                    selectedEmployee = emp
                                    exp = false
                                }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = incidentDate,
                        onValueChange = { incidentDate = it },
                        label = { Text("تاريخ الواقعة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = violationType,
                        onValueChange = { violationType = it },
                        label = { Text("نوع المخالفة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("الإجراء التأديبي المقترح:", style = MaterialTheme.typography.labelSmall)
                    var actExp by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { actExp = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(actionType.titleAr)
                    }
                    DropdownMenu(expanded = actExp, onDismissRequest = { actExp = false }) {
                        PenaltyAction.values().forEach { act ->
                            DropdownMenuItem(
                                text = { Text(act.titleAr) },
                                onClick = {
                                    actionType = act
                                    actExp = false
                                }
                            )
                        }
                    }
                }

                if (actionType == PenaltyAction.SalaryDeduction) {
                    item {
                        OutlinedTextField(
                            value = deduction,
                            onValueChange = { deduction = it },
                            label = { Text("مبلغ الخصم (ر.ي)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("تفاصيل الواقعة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = legalRef,
                        onValueChange = { legalRef = it },
                        label = { Text("المرجع النظامي / لائحة الجزاءات") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = statement,
                        onValueChange = { statement = it },
                        label = { Text("إفادة الموظف / التحقيق") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        selectedEmployee.id,
                        selectedEmployee.fullName,
                        selectedEmployee.department,
                        incidentDate,
                        violationType,
                        description,
                        severity,
                        actionType,
                        deduction.toDoubleOrNull() ?: 0.0,
                        suspensionDays.toIntOrNull() ?: 0,
                        legalRef,
                        investigation,
                        statement
                    )
                },
                enabled = description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HrStatusError)
            ) {
                Text("اعتماد الجزاء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
