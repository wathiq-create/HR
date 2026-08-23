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
import com.example.data.model.RequestStatus
import com.example.ui.components.RequestStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ApprovalsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val leaves by viewModel.leaveRequests.collectAsState()
    val overtimes by viewModel.overtimeRequests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val pendingLeaves = leaves.filter { it.status.startsWith("Pending") || it.status == "Submitted" }
    val pendingOvertimes = overtimes.filter { it.status.startsWith("Pending") || it.status == "Submitted" }

    var selectedActionType by remember { mutableStateOf<String?>(null) } // "LEAVE" or "OVERTIME"
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var selectedItemSummary by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
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
                        text = "مركز الموافقات وسير العمل",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "مسار الصلاحيات: ${currentUser?.role ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HrTertiary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${pendingLeaves.size + pendingOvertimes.size} بانتظار الاعتماد",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HrTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (pendingLeaves.isEmpty() && pendingOvertimes.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = HrStatusSuccess, modifier = Modifier.size(40.dp))
                    Text(
                        text = "رائع! لا توجد طلبات معلقة بانتظار موافقتك.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "تمت معالجة كافة طلبات الإجازات والأجر الإضافي للموظفين.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (pendingLeaves.isNotEmpty()) {
                    item {
                        Text(
                            text = "طلبات الإجازات المعلقة (${pendingLeaves.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(pendingLeaves) { req ->
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
                                        Text(text = req.requestNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(text = req.employeeName, fontWeight = FontWeight.Bold)
                                    }
                                    RequestStatusBadge(statusStr = req.status)
                                }
                                Text(
                                    text = "إجازة ${req.leaveType} • ${req.daysCount} أيام • من ${req.startDate} إلى ${req.endDate}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (req.reason.isNotBlank()) {
                                    Text(text = "السبب: ${req.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedActionType = "LEAVE"
                                            selectedItemId = req.id
                                            selectedItemSummary = "إجازة ${req.employeeName} (${req.daysCount} أيام)"
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusSuccess)
                                    ) {
                                        Text("اعتماد / رفض")
                                    }
                                }
                            }
                        }
                    }
                }

                if (pendingOvertimes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "طلبات الأجر الإضافي المعلقة (${pendingOvertimes.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(pendingOvertimes) { req ->
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
                                        Text(text = req.requestNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        Text(text = req.employeeName, fontWeight = FontWeight.Bold)
                                    }
                                    RequestStatusBadge(statusStr = req.status)
                                }
                                Text(
                                    text = "عمل إضافي • ${req.hours} ساعات بتاريخ ${req.date} • استحقاق ${"%.1f".format(req.calculatedAmount)} ر.ي",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(text = "المهمة: ${req.projectOrTask}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedActionType = "OVERTIME"
                                            selectedItemId = req.id
                                            selectedItemSummary = "عمل إضافي ${req.employeeName} (${req.hours} ساعات)"
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusSuccess)
                                    ) {
                                        Text("اعتماد / رفض")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedItemId != null) {
        AlertDialog(
            onDismissRequest = { selectedItemId = null },
            title = { Text("قرار الاعتماد والموافقة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = selectedItemSummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("التعليق والملاحظات الإدارية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val next = when (currentUser?.role) {
                                "DepartmentHead" -> RequestStatus.DeptApproved
                                "HrManager" -> RequestStatus.HrApproved
                                else -> RequestStatus.Approved
                            }
                            if (selectedActionType == "LEAVE") {
                                viewModel.updateLeaveApproval(selectedItemId!!, next, comment)
                            } else {
                                viewModel.updateOvertimeApproval(selectedItemId!!, next, comment)
                            }
                            selectedItemId = null
                            comment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusSuccess)
                    ) {
                        Text("اعتماد")
                    }

                    Button(
                        onClick = {
                            if (selectedActionType == "LEAVE") {
                                viewModel.updateLeaveApproval(selectedItemId!!, RequestStatus.Rejected, comment)
                            } else {
                                viewModel.updateOvertimeApproval(selectedItemId!!, RequestStatus.Rejected, comment)
                            }
                            selectedItemId = null
                            comment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HrStatusError)
                    ) {
                        Text("رفض")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemId = null }) { Text("إلغاء") }
            }
        )
    }
}
