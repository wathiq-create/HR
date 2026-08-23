package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.InfoColumn
import com.example.ui.components.OfficialDocumentHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val kpis by viewModel.dashboardKpis.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val leaves by viewModel.leaveRequests.collectAsState()
    val overtimes by viewModel.overtimeRequests.collectAsState()
    val penalties by viewModel.penalties.collectAsState()

    var showPrintPreview by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "محرك التقارير والطباعة الرسمية",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "التقرير الشهري الموحد للموارد البشرية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showPrintPreview = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("معاينة الطباعة الرسمية")
            }
        }

        // BI Summary Cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ملخص المؤشرات التنفيذية للربع الثالث 2026",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoColumn(label = "الموظفون الإجمالي", value = "${kpis.totalEmployees}")
                            InfoColumn(label = "الطلبات المعتمدة", value = "${leaves.count { it.status == "Approved" } + overtimes.count { it.status == "Approved" }}")
                            InfoColumn(label = "إجمالي تكاليف الإضافي", value = "${"%.1f".format(kpis.totalOvertimeCost)} ر.ي")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoColumn(label = "تكلفة الوجبات", value = "${"%.1f".format(kpis.totalMealCost)} ر.ي")
                            InfoColumn(label = "تكلفة النقل", value = "${"%.1f".format(kpis.totalTransportCost)} ر.ي")
                            InfoColumn(label = "الجزاءات الصادرة", value = "${penalties.size}")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "توزيع الموظفين والطلبات حسب الإدارات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            val departments = listOf("الهندسة والبرمجيات", "الموارد البشرية", "الإدارة المالية", "العمليات واللوجستيات", "المبيعات والتسويق")
            items(departments) { dept ->
                val deptEmps = employees.count { it.department == dept }
                val deptLeaves = leaves.count { it.department == dept }
                val deptOts = overtimes.count { it.department == dept }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = dept, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$deptEmps موظفين • $deptLeaves طلبات إجازة • $deptOts عمل إضافي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // Official Print Document Dialog
    if (showPrintPreview) {
        AlertDialog(
            onDismissRequest = { showPrintPreview = false },
            title = {
                Text(
                    text = "معاينة الوثيقة الرسمية للطباعة والتصدير",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OfficialDocumentHeader(
                            title = "تقرير الموارد البشرية والعمليات الشامل",
                            serialNumber = "REP-2026-00984",
                            date = "2026-08-21"
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("إجمالي القوى العاملة: ${kpis.totalEmployees} موظف", fontWeight = FontWeight.Bold)
                                Text("إجمالي العمل الإضافي المعتمد: ${"%.1f".format(kpis.totalOvertimeCost)} ريال يمني")
                                Text("إجمالي الخدمات اللوجستية (وجبات ومواصلات): ${"%.1f".format(kpis.totalMealCost + kpis.totalTransportCost)} ريال يمني")
                                Text("نسبة التزام الحضور وسير العمل: 98.4%", color = HrStatusSuccess, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // QR Verification & Signatures Box
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "التوقيع والاعتماد الرسمي:", fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "إشراف ومتابعة أ.أحمد العمري",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                    }
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "رئيس القسم", style = MaterialTheme.typography.labelSmall)
                                        Text(text = "[معتمد إلكترونياً]", style = MaterialTheme.typography.labelSmall, color = HrStatusSuccess)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "مدير الموارد البشرية", style = MaterialTheme.typography.labelSmall)
                                        Text(text = "[معتمد إلكترونياً]", style = MaterialTheme.typography.labelSmall, color = HrStatusSuccess)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "المدير العام", style = MaterialTheme.typography.labelSmall)
                                        Text(text = "[معتمد إلكترونياً]", style = MaterialTheme.typography.labelSmall, color = HrStatusSuccess)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrintPreview = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تصدير وطباعة PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintPreview = false }) { Text("إغلاق") }
            }
        )
    }
}
