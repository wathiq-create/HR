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
import androidx.compose.material.icons.outlined.*
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
import com.example.data.local.DepartmentEntity
import com.example.data.local.EmployeeEntity
import com.example.ui.components.InfoColumn
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissions

enum class ManagementSection(val titleAr: String) {
    EMPLOYEES("سجل الموظفين"),
    DEPARTMENTS("الأقسام والإدارات")
}

@Composable
fun EmployeesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsState()
    val departmentsList by viewModel.departments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeSection by remember { mutableStateOf(ManagementSection.EMPLOYEES) }

    // Search and filters for Employees
    var employeeSearchQuery by remember { mutableStateOf("") }
    var selectedDeptFilter by remember { mutableStateOf("الكل") }
    var selectedStatusFilter by remember { mutableStateOf("الكل") }

    // Search and filters for Departments
    var deptSearchQuery by remember { mutableStateOf("") }
    var selectedLocationFilter by remember { mutableStateOf("الكل") }

    // Employee Dialog states
    var showAddEditEmpDialog by remember { mutableStateOf(false) }
    var selectedEmpForEdit by remember { mutableStateOf<EmployeeEntity?>(null) }
    var selectedEmpForView by remember { mutableStateOf<EmployeeEntity?>(null) }
    var selectedEmpForDelete by remember { mutableStateOf<EmployeeEntity?>(null) }

    // Department Dialog states
    var showAddEditDeptDialog by remember { mutableStateOf(false) }
    var selectedDeptForEdit by remember { mutableStateOf<DepartmentEntity?>(null) }
    var selectedDeptForView by remember { mutableStateOf<DepartmentEntity?>(null) }
    var selectedDeptForDelete by remember { mutableStateOf<DepartmentEntity?>(null) }

    // Permissions
    val canCreateEditEmp = RolePermissions.canCreateEditEmployee(currentUser)
    val canDeleteEmp = RolePermissions.canDeleteEmployee(currentUser)
    val canViewSalary = RolePermissions.canViewSalary(currentUser)
    val canManageDept = RolePermissions.canManageDepartments(currentUser)
    val canDeleteDept = RolePermissions.canDeleteDepartment(currentUser)

    // Department options
    val departmentNames = listOf("الكل") + (departmentsList.map { it.nameAr }.ifEmpty {
        listOf("الهندسة والبرمجيات", "الموارد البشرية", "الإدارة المالية", "العمليات واللوجستيات", "المبيعات والتسويق", "تقنية المعلومات")
    }).distinct()

    val statusOptions = listOf("الكل", "نشط", "في إجازة", "موقوف")

    // Filtered Employees
    val filteredEmployees = employees.filter { emp ->
        val matchesSearch = employeeSearchQuery.isBlank() ||
                emp.fullName.contains(employeeSearchQuery, ignoreCase = true) ||
                emp.employeeNumber.contains(employeeSearchQuery, ignoreCase = true) ||
                emp.department.contains(employeeSearchQuery, ignoreCase = true) ||
                emp.position.contains(employeeSearchQuery, ignoreCase = true) ||
                emp.phone.contains(employeeSearchQuery, ignoreCase = true) ||
                emp.nationalId.contains(employeeSearchQuery, ignoreCase = true)

        val matchesDept = selectedDeptFilter == "الكل" || emp.department == selectedDeptFilter
        val matchesStatus = when (selectedStatusFilter) {
            "نشط" -> emp.status.equals("Active", ignoreCase = true) || emp.status == "نشط"
            "في إجازة" -> emp.status.contains("Leave", ignoreCase = true) || emp.status.contains("إجازة")
            "موقوف" -> emp.status.contains("Suspended", ignoreCase = true) || emp.status.contains("موقوف")
            else -> true
        }
        matchesSearch && matchesDept && matchesStatus
    }

    // Filtered Departments
    val locationOptions = listOf("الكل") + departmentsList.map { it.location }.filter { it.isNotBlank() }.distinct()
    val filteredDepartments = departmentsList.filter { dept ->
        val matchesSearch = deptSearchQuery.isBlank() ||
                dept.nameAr.contains(deptSearchQuery, ignoreCase = true) ||
                dept.departmentCode.contains(deptSearchQuery, ignoreCase = true) ||
                dept.managerName.contains(deptSearchQuery, ignoreCase = true) ||
                dept.location.contains(deptSearchQuery, ignoreCase = true)
        val matchesLoc = selectedLocationFilter == "الكل" || dept.location == selectedLocationFilter
        matchesSearch && matchesLoc
    }

    Scaffold(
        floatingActionButton = {
            val shouldShowFab = if (activeSection == ManagementSection.EMPLOYEES) canCreateEditEmp else canManageDept
            if (shouldShowFab) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (activeSection == ManagementSection.EMPLOYEES) {
                            selectedEmpForEdit = null
                            showAddEditEmpDialog = true
                        } else {
                            selectedDeptForEdit = null
                            showAddEditDeptDialog = true
                        }
                    },
                    icon = {
                        Icon(
                            if (activeSection == ManagementSection.EMPLOYEES) Icons.Default.PersonAdd else Icons.Default.AddBusiness,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            if (activeSection == ManagementSection.EMPLOYEES) "إضافة موظف جديد" else "إضافة قسم جديد",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_entry_fab")
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
            // Main Tab Switcher (Employees vs Departments)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                ManagementSection.values().forEachIndexed { index, section ->
                    SegmentedButton(
                        selected = activeSection == section,
                        onClick = { activeSection = section },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ManagementSection.values().size),
                        icon = {
                            Icon(
                                if (section == ManagementSection.EMPLOYEES) Icons.Default.People else Icons.Default.Domain,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    ) {
                        Text(
                            text = if (section == ManagementSection.EMPLOYEES)
                                "${section.titleAr} (${employees.size})"
                            else
                                "${section.titleAr} (${departmentsList.size})",
                            fontWeight = if (activeSection == section) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = activeSection,
                label = "SectionTransition"
            ) { section ->
                when (section) {
                    ManagementSection.EMPLOYEES -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Search Box
                            OutlinedTextField(
                                value = employeeSearchQuery,
                                onValueChange = { employeeSearchQuery = it },
                                placeholder = { Text("بحث بالاسم، الرقم الوظيفي، الهوية، أو التخصص...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (employeeSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { employeeSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = null)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("employee_search_field")
                            )

                            // Filter Chips Row (Department)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "تصفية حسب القسم:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(departmentNames) { dept ->
                                        FilterChip(
                                            selected = selectedDeptFilter == dept,
                                            onClick = { selectedDeptFilter = dept },
                                            label = { Text(dept, fontSize = 12.sp) },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            // Filter Chips Row (Status)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "الحالة:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                statusOptions.forEach { status ->
                                    FilterChip(
                                        selected = selectedStatusFilter == status,
                                        onClick = { selectedStatusFilter = status },
                                        label = { Text(status, fontSize = 11.5.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            // Summary Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "نتائج البحث: ${filteredEmployees.size} موظف",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Employee List
                            if (filteredEmployees.isEmpty()) {
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
                                            Icon(Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                                            Text(
                                                text = "لا يوجد موظفون يطابقون معايير البحث والفلترة.",
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
                                    items(filteredEmployees, key = { it.id }) { emp ->
                                        EmployeeCard(
                                            emp = emp,
                                            canEdit = canCreateEditEmp,
                                            canDelete = canDeleteEmp,
                                            canViewSalary = canViewSalary,
                                            onView = { selectedEmpForView = emp },
                                            onEdit = {
                                                selectedEmpForEdit = emp
                                                showAddEditEmpDialog = true
                                            },
                                            onDelete = { selectedEmpForDelete = emp }
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(72.dp)) }
                                }
                            }
                        }
                    }

                    ManagementSection.DEPARTMENTS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Search Box
                            OutlinedTextField(
                                value = deptSearchQuery,
                                onValueChange = { deptSearchQuery = it },
                                placeholder = { Text("بحث برمز القسم، اسم الإدارة، أو المدير المسؤول...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (deptSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { deptSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = null)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("department_search_field")
                            )

                            // Location Filter Chips
                            if (locationOptions.size > 1) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(locationOptions) { loc ->
                                        FilterChip(
                                            selected = selectedLocationFilter == loc,
                                            onClick = { selectedLocationFilter = loc },
                                            label = { Text(loc, fontSize = 12.sp) },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            // Summary Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الأقسام المسجلة: ${filteredDepartments.size} قسم/إدارة",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (filteredDepartments.isEmpty()) {
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
                                            Icon(Icons.Default.DomainDisabled, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                                            Text(
                                                text = "لا توجد أقسام مسجلة مطابقة لمعايير البحث.",
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
                                    items(filteredDepartments, key = { it.id }) { dept ->
                                        DepartmentCard(
                                            dept = dept,
                                            canEdit = canManageDept,
                                            canDelete = canDeleteDept,
                                            onView = { selectedDeptForView = dept },
                                            onEdit = {
                                                selectedDeptForEdit = dept
                                                showAddEditDeptDialog = true
                                            },
                                            onDelete = { selectedDeptForDelete = dept }
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(72.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS: EMPLOYEE VIEW / EDIT / DELETE
    // ==========================================

    // Employee Details View Dialog
    selectedEmpForView?.let { emp ->
        EmployeeDetailsDialog(
            emp = emp,
            canEdit = canCreateEditEmp,
            canViewSalary = canViewSalary,
            onDismiss = { selectedEmpForView = null },
            onEdit = {
                selectedEmpForView = null
                selectedEmpForEdit = emp
                showAddEditEmpDialog = true
            }
        )
    }

    // Add / Edit Employee Dialog
    if (showAddEditEmpDialog) {
        AddEditEmployeeDialog(
            employee = selectedEmpForEdit,
            departmentOptions = departmentNames.filter { it != "الكل" },
            onDismiss = { showAddEditEmpDialog = false },
            onSave = { updatedEmp, isEdit ->
                viewModel.saveEmployee(updatedEmp, isEdit)
                showAddEditEmpDialog = false
            }
        )
    }

    // Delete Employee Confirmation Dialog
    selectedEmpForDelete?.let { emp ->
        AlertDialog(
            onDismissRequest = { selectedEmpForDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = HrStatusError) },
            title = { Text("تأكيد حذف الموظف", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف سجل الموظف (${emp.fullName} - ${emp.employeeNumber}) نهائياً من النظام؟\nسيتم تسجيل هذه العملية في سجل التدقيق والمطابقة.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmployee(emp)
                        selectedEmpForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HrStatusError),
                    modifier = Modifier.testTag("confirm_delete_employee_button")
                ) {
                    Text("نعم، حذف الموظف")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEmpForDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ==========================================
    // DIALOGS: DEPARTMENT VIEW / EDIT / DELETE
    // ==========================================

    // Department Details View Dialog
    selectedDeptForView?.let { dept ->
        DepartmentDetailsDialog(
            dept = dept,
            employeesInDept = employees.filter { it.department == dept.nameAr },
            canEdit = canManageDept,
            onDismiss = { selectedDeptForView = null },
            onEdit = {
                selectedDeptForView = null
                selectedDeptForEdit = dept
                showAddEditDeptDialog = true
            }
        )
    }

    // Add / Edit Department Dialog
    if (showAddEditDeptDialog) {
        AddEditDepartmentDialog(
            department = selectedDeptForEdit,
            onDismiss = { showAddEditDeptDialog = false },
            onSave = { code, name, manager, location, empCount, budget, notes ->
                if (selectedDeptForEdit != null) {
                    viewModel.updateDepartment(
                        selectedDeptForEdit!!.copy(
                            departmentCode = code,
                            nameAr = name,
                            managerName = manager,
                            location = location,
                            employeeCount = empCount,
                            estimatedMonthlyBudget = budget,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.createDepartment(code, name, manager, location, empCount, budget, notes)
                }
                showAddEditDeptDialog = false
            }
        )
    }

    // Delete Department Confirmation Dialog
    selectedDeptForDelete?.let { dept ->
        AlertDialog(
            onDismissRequest = { selectedDeptForDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = HrStatusError) },
            title = { Text("تأكيد حذف القسم", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف قسم (${dept.nameAr} - ${dept.departmentCode})؟\nيرجى التأكد من إعادة توزيع الموظفين المرتبطين بهذا القسم قبل المتابعة.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDepartment(dept)
                        selectedDeptForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HrStatusError),
                    modifier = Modifier.testTag("confirm_delete_department_button")
                ) {
                    Text("نعم، حذف القسم")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDeptForDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// ==========================================
// EMPLOYEE CARD & DETAILS COMPOSABLES
// ==========================================

@Composable
fun EmployeeCard(
    emp: EmployeeEntity,
    canEdit: Boolean,
    canDelete: Boolean,
    canViewSalary: Boolean,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("employee_card_${emp.employeeNumber}")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emp.fullName.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = emp.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (emp.status == "Active" || emp.status == "نشط")
                                    HrStatusSuccess.copy(alpha = 0.15f)
                                else
                                    HrStatusWarning.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (emp.status == "Active") "نشط" else emp.status,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (emp.status == "Active" || emp.status == "نشط") HrStatusSuccess else HrStatusWarning,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${emp.employeeNumber} • ${emp.position}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Actions (View, Edit, Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onView,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "عرض التفاصيل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    if (canEdit) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = HrStatusError, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "القسم / الإدارة", value = emp.department)
                InfoColumn(label = "الفرع", value = emp.branch)
                InfoColumn(
                    label = "الراتب الأساسي",
                    value = if (canViewSalary) "${"%.0f".format(emp.salaryBase)} ر.ي" else "*** ر.ي"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "رصيد السنوية", value = "${emp.annualLeaveBalance} يوم")
                InfoColumn(label = "المرضية", value = "${emp.sickLeaveBalance} يوم")
                InfoColumn(
                    label = "أجر الساعة",
                    value = if (canViewSalary) "${emp.hourlyRate} ر.ي" else "*** ر.ي"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text(text = emp.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text(text = emp.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EmployeeDetailsDialog(
    emp: EmployeeEntity,
    canEdit: Boolean,
    canViewSalary: Boolean,
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
                Text("الملف التعريفي للموظف", fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = emp.employeeNumber,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = emp.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "${emp.position} • ${emp.department}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text(text = "رقم الهوية الوطنية / الإقامة: ${emp.nationalId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Text("البيانات التعاقدية والإدارية", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoColumn(label = "نوع العقد", value = emp.contractType)
                            InfoColumn(label = "الدرجة الوظيفية", value = emp.grade)
                            InfoColumn(label = "تاريخ التعيين", value = emp.hireDate)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoColumn(label = "المدير المباشر", value = emp.directManager)
                            InfoColumn(label = "الفرع", value = emp.branch)
                            InfoColumn(label = "حالة الحساب", value = emp.status)
                        }
                    }
                }

                item {
                    HorizontalDivider()
                    Text("البيانات المالية والأجور", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (canViewSalary) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            InfoColumn(label = "الراتب الأساسي", value = "${"%.0f".format(emp.salaryBase)} ر.ي")
                            InfoColumn(label = "أجر الساعة", value = "${emp.hourlyRate} ر.ي")
                            InfoColumn(label = "أجر الإضافي القياسي", value = "${"%.1f".format(emp.hourlyRate * 1.5)} ر.ي")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "البيانات المالية والرواتب محمية بصلاحيات أمان خاصة.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider()
                    Text("أرصدة الإجازات المتاحة", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoColumn(label = "الإجازة السنوية", value = "${emp.annualLeaveBalance} يوم")
                        InfoColumn(label = "الإجازة المرضية", value = "${emp.sickLeaveBalance} يوم")
                        InfoColumn(label = "الإجازة الطارئة", value = "${emp.emergencyLeaveBalance} يوم")
                    }
                }

                item {
                    HorizontalDivider()
                    Text("معلومات التواصل والطوارئ", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "رقم الجوال: ${emp.phone}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "البريد الإلكتروني: ${emp.email}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "جهة الطوارئ: ${emp.emergencyContact}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            if (canEdit) {
                Button(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل البيانات")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeDialog(
    employee: EmployeeEntity?,
    departmentOptions: List<String>,
    onDismiss: () -> Unit,
    onSave: (EmployeeEntity, Boolean) -> Unit
) {
    var fullName by remember { mutableStateOf(employee?.fullName ?: "") }
    var empNumber by remember { mutableStateOf(employee?.employeeNumber ?: "EMP-${(1000..9999).random()}") }
    var nationalId by remember { mutableStateOf(employee?.nationalId ?: "10${(10000000..99999999).random()}") }
    var department by remember { mutableStateOf(employee?.department ?: departmentOptions.firstOrNull() ?: "الهندسة والبرمجيات") }
    var position by remember { mutableStateOf(employee?.position ?: "") }
    var phone by remember { mutableStateOf(employee?.phone ?: "+96650") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var salary by remember { mutableStateOf(employee?.salaryBase?.toString() ?: "12000") }
    var hourlyRate by remember { mutableStateOf(employee?.hourlyRate?.toString() ?: "68") }
    var branch by remember { mutableStateOf(employee?.branch ?: "المركز الرئيسي") }
    var status by remember { mutableStateOf(employee?.status ?: "Active") }
    var annualBalance by remember { mutableStateOf(employee?.annualLeaveBalance?.toString() ?: "30") }
    var sickBalance by remember { mutableStateOf(employee?.sickLeaveBalance?.toString() ?: "15") }
    var directManager by remember { mutableStateOf(employee?.directManager ?: "مدير الإدارة") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (employee == null) "إضافة موظف جديد" else "تعديل بيانات الموظف",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل للموظف *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = empNumber,
                            onValueChange = { empNumber = it },
                            label = { Text("الرقم الوظيفي *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = nationalId,
                            onValueChange = { nationalId = it },
                            label = { Text("رقم الهوية / الإقامة") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("القسم / الإدارة *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = position,
                        onValueChange = { position = it },
                        label = { Text("المسمى الوظيفي *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الهاتف") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("البريد الإلكتروني") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = salary,
                            onValueChange = { salary = it },
                            label = { Text("الراتب الأساسي (ر.ي)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = hourlyRate,
                            onValueChange = { hourlyRate = it },
                            label = { Text("أجر الساعة (ر.ي)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = annualBalance,
                            onValueChange = { annualBalance = it },
                            label = { Text("رصيد السنوية") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sickBalance,
                            onValueChange = { sickBalance = it },
                            label = { Text("رصيد المرضية") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = branch,
                            onValueChange = { branch = it },
                            label = { Text("الفرع") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = directManager,
                            onValueChange = { directManager = it },
                            label = { Text("المدير المباشر") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sal = salary.toDoubleOrNull() ?: 12000.0
                    val rate = hourlyRate.toDoubleOrNull() ?: (sal / (30 * 8))
                    val annBal = annualBalance.toIntOrNull() ?: 30
                    val sickBal = sickBalance.toIntOrNull() ?: 15

                    val entity = (employee ?: EmployeeEntity(
                        employeeNumber = empNumber,
                        fullName = fullName,
                        nationalId = nationalId,
                        department = department,
                        division = department,
                        position = position,
                        grade = "B-1",
                        contractType = "دوام كامل",
                        hireDate = "2026-01-01",
                        contractEndDate = "2028-01-01",
                        phone = phone,
                        email = email,
                        directManager = directManager,
                        workLocation = branch,
                        branch = branch,
                        emergencyContact = "طوارئ: $phone",
                        salaryBase = sal,
                        hourlyRate = rate,
                        annualLeaveBalance = annBal,
                        sickLeaveBalance = sickBal
                    )).copy(
                        fullName = fullName,
                        employeeNumber = empNumber,
                        nationalId = nationalId,
                        department = department,
                        position = position,
                        phone = phone,
                        email = email,
                        branch = branch,
                        directManager = directManager,
                        salaryBase = sal,
                        hourlyRate = rate,
                        annualLeaveBalance = annBal,
                        sickLeaveBalance = sickBal,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(entity, employee != null)
                },
                enabled = fullName.isNotBlank() && empNumber.isNotBlank() && position.isNotBlank(),
                modifier = Modifier.testTag("save_employee_form_button")
            ) {
                Text(if (employee == null) "إضافة الموظف" else "حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ==========================================
// DEPARTMENT CARD & DETAILS COMPOSABLES
// ==========================================

@Composable
fun DepartmentCard(
    dept: DepartmentEntity,
    canEdit: Boolean,
    canDelete: Boolean,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("department_card_${dept.departmentCode}")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = dept.nameAr,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = dept.departmentCode,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "مدير الإدارة: ${dept.managerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Actions (View, Edit, Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onView,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "عرض التفاصيل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    if (canEdit) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = HrStatusError, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "الموقع / الفرع", value = dept.location)
                InfoColumn(label = "عدد الكادر", value = "${dept.employeeCount} موظف")
                InfoColumn(label = "الميزانية الشهرية", value = "${"%.0f".format(dept.estimatedMonthlyBudget)} ر.ي")
            }

            if (dept.notes.isNotBlank()) {
                Text(
                    text = "ملاحظات: ${dept.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DepartmentDetailsDialog(
    dept: DepartmentEntity,
    employeesInDept: List<EmployeeEntity>,
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
                Text("بيانات القسم والهيكل التنظيمي", fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = dept.departmentCode,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = dept.nameAr, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "مدير الإدارة: ${dept.managerName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text(text = "الموقع الجغرافي: ${dept.location}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoColumn(label = "إجمالي الموظفين الفعلي", value = "${employeesInDept.size} موظف")
                        InfoColumn(label = "الميزانية الشهرية المقدرة", value = "${"%.0f".format(dept.estimatedMonthlyBudget)} ر.ي")
                    }
                }

                if (dept.notes.isNotBlank()) {
                    item {
                        HorizontalDivider()
                        Text("الأهداف والملاحظات", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = dept.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                item {
                    HorizontalDivider()
                    Text("الموظفون المنتسبون للقسم (${employeesInDept.size})", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }

                if (employeesInDept.isEmpty()) {
                    item {
                        Text(
                            text = "لا يوجد موظفون مسجلون تحت هذا القسم حالياً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(employeesInDept) { emp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(emp.fullName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(emp.position, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(emp.employeeNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (canEdit) {
                Button(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل القسم")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

@Composable
fun AddEditDepartmentDialog(
    department: DepartmentEntity?,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, manager: String, location: String, empCount: Int, budget: Double, notes: String) -> Unit
) {
    var code by remember { mutableStateOf(department?.departmentCode ?: "DEPT-${(10..99).random()}") }
    var name by remember { mutableStateOf(department?.nameAr ?: "") }
    var manager by remember { mutableStateOf(department?.managerName ?: "") }
    var location by remember { mutableStateOf(department?.location ?: "المقر الرئيسي") }
    var empCountStr by remember { mutableStateOf(department?.employeeCount?.toString() ?: "5") }
    var budgetStr by remember { mutableStateOf(department?.estimatedMonthlyBudget?.toString() ?: "150000") }
    var notes by remember { mutableStateOf(department?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (department == null) "إضافة قسم / إدارة جديدة" else "تعديل بيانات القسم",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القسم / الإدارة *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("رمز القسم *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("الموقع / الفرع") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = manager,
                    onValueChange = { manager = it },
                    label = { Text("مدير الإدارة المسؤول *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = empCountStr,
                        onValueChange = { empCountStr = it },
                        label = { Text("عدد الكادر المقدر") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it },
                        label = { Text("الميزانية الشهرية (ر.ي)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات واختصاصات القسم") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = empCountStr.toIntOrNull() ?: 0
                    val budget = budgetStr.toDoubleOrNull() ?: 0.0
                    onSave(code, name, manager, location, count, budget, notes)
                },
                enabled = name.isNotBlank() && code.isNotBlank() && manager.isNotBlank(),
                modifier = Modifier.testTag("save_department_form_button")
            ) {
                Text(if (department == null) "إضافة القسم" else "حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
