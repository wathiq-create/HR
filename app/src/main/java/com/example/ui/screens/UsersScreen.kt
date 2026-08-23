package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DepartmentEntity
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissions
import com.example.util.SmartDocumentImporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.users.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("المستخدمين (${users.size})", "الأقسام والهيكل (${departments.size})", "مصفوفة الصلاحيات")

    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddDeptDialog by remember { mutableStateOf(false) }

    var selectedUserForView by remember { mutableStateOf<UserEntity?>(null) }
    var selectedUserForEdit by remember { mutableStateOf<UserEntity?>(null) }
    var selectedUserForDelete by remember { mutableStateOf<UserEntity?>(null) }

    var selectedDeptForEdit by remember { mutableStateOf<DepartmentEntity?>(null) }
    var selectedDeptForDelete by remember { mutableStateOf<DepartmentEntity?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    val canManage = RolePermissions.canManageUsers(currentUser)
    val canManageDept = RolePermissions.canManageDepartments(currentUser)

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0 && canManage) {
                ExtendedFloatingActionButton(
                    onClick = { showAddUserDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("إضافة مستخدم جديد", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            } else if (selectedTab == 1 && canManageDept) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDeptDialog = true },
                    icon = { Icon(Icons.Default.DomainAdd, contentDescription = null) },
                    text = { Text("إضافة قسم جديد", fontWeight = FontWeight.Bold) },
                    containerColor = Color(0xFF0284C7),
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
            // Header Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Users Tab
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث عن مستخدم بالاسم أو المعرف أو القسم...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredUsers = users.filter {
                        searchQuery.isBlank() ||
                                it.fullName.contains(searchQuery, ignoreCase = true) ||
                                it.username.contains(searchQuery, ignoreCase = true) ||
                                it.department.contains(searchQuery, ignoreCase = true) ||
                                it.role.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            UserCardEnhanced(
                                user = user,
                                canManage = canManage,
                                onView = { selectedUserForView = user },
                                onEdit = { selectedUserForEdit = user },
                                onDelete = { selectedUserForDelete = user }
                            )
                        }
                    }
                }
                1 -> {
                    // Departments Tab
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(departments, key = { it.id }) { dept ->
                            DepartmentCard(
                                dept = dept,
                                canManage = canManageDept,
                                onEdit = { selectedDeptForEdit = dept },
                                onDelete = { selectedDeptForDelete = dept }
                            )
                        }
                    }
                }
                2 -> {
                    // Permissions Matrix Tab
                    PermissionsMatrixView()
                }
            }
        }
    }

    // Dialogs
    if (showAddUserDialog) {
        NewUserDialogEnhanced(
            onDismiss = { showAddUserDialog = false },
            onSubmit = { uName, fName, email, phone, dept, role, pass ->
                viewModel.createUser(uName, fName, email, phone, dept, role, pass)
                showAddUserDialog = false
            }
        )
    }

    selectedUserForEdit?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { selectedUserForEdit = null },
            onSubmit = { updatedUser ->
                viewModel.updateUser(updatedUser)
                selectedUserForEdit = null
            }
        )
    }

    selectedUserForDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { selectedUserForDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = HrStatusError) },
            title = { Text("تأكيد حذف المستخدم", fontWeight = FontWeight.Bold) },
            text = {
                Text("هل أنت متأكد من رغبتك في حذف حساب المستخدم (${user.fullName} - @${user.username}) نهائياً؟ سيتم تسجيل عملية الحذف في سجل التدقيق الأمني.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user)
                        selectedUserForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HrStatusError)
                ) {
                    Text("نعم، حذف الحساب")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    selectedUserForView?.let { user ->
        UserDetailsDialog(user = user, onDismiss = { selectedUserForView = null })
    }

    if (showAddDeptDialog) {
        NewDepartmentDialog(
            onDismiss = { showAddDeptDialog = false },
            onSubmit = { code, name, manager, loc, empCount, budget, notes ->
                viewModel.createDepartment(code, name, manager, loc, empCount, budget, notes)
                showAddDeptDialog = false
            }
        )
    }

    selectedDeptForEdit?.let { dept ->
        EditDepartmentDialog(
            dept = dept,
            onDismiss = { selectedDeptForEdit = null },
            onSubmit = { updated ->
                viewModel.updateDepartment(updated)
                selectedDeptForEdit = null
            }
        )
    }

    selectedDeptForDelete?.let { dept ->
        AlertDialog(
            onDismissRequest = { selectedDeptForDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = HrStatusError) },
            title = { Text("تأكيد حذف القسم", fontWeight = FontWeight.Bold) },
            text = {
                Text("هل أنت متأكد من حذف قسم (${dept.nameAr} - ${dept.departmentCode})؟ لن يؤثر الحذف على الموظفين المسجلين حالياً وسيتم أرشفته في سجل التدقيق.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDepartment(dept)
                        selectedDeptForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HrStatusError)
                ) {
                    Text("تأكيد الحذف")
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

@Composable
fun UserCardEnhanced(
    user: UserEntity,
    canManage: Boolean,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = user.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = user.role,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "@${user.username} • ${user.department}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${user.email} • ${user.phone}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onView) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الصلاحيات والتفاصيل", fontSize = 12.sp)
                }

                if (canManage) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تعديل", fontSize = 12.sp)
                    }

                    if (user.username != "admin") {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = HrStatusError, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentCard(
    dept: DepartmentEntity,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = dept.departmentCode,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = dept.nameAr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${dept.employeeCount} موظف",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("المدير المباشر", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dept.managerName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("المقر / الموقع", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dept.location, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("الميزانية الشهرية", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.0f".format(dept.estimatedMonthlyBudget)} ر.ي", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = HrStatusSuccess)
                }
            }

            if (dept.notes.isNotBlank()) {
                Text(
                    text = dept.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canManage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تعديل القسم", fontSize = 12.sp)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = HrStatusError, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionsMatrixView() {
    val permissions = RolePermissions.allPermissions
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "دليل مصفوفة الصلاحيات والحوكمة الأمنية (RBAC)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "يحدد النظام مستويات الوصول بدقة بين مدراء النظام، مدراء الموارد البشرية، رؤساء الأقسام والموظفين وفق معايير الجودة المؤسسية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        items(permissions) { perm ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = perm.titleAr,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = perm.key,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = perm.descriptionAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الأدوار المصرح لها:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        perm.allowedRoles.forEach { role ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HrSecondary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = role,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = HrSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewUserDialogEnhanced(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, UserRole, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("الهندسة والبرمجيات") }
    var role by remember { mutableStateOf(UserRole.Employee) }
    var password by remember { mutableStateOf("123") }
    var showImportMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إنشاء مستخدم جديد", fontWeight = FontWeight.Bold)
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
                                Text("اختر مستند PDF لاستخراج البيانات وتعبئة الحقول آلياً:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                SmartDocumentImporter.sampleUserPdfTemplates.forEach { template ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                username = template.username
                                                fullName = template.fullName
                                                email = template.email
                                                phone = template.phone
                                                department = template.department
                                                role = template.role
                                                password = "123"
                                                showImportMenu = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(template.fullName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                                Text(template.documentSource, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("اسم المستخدم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف / الجوال") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("الإدارة / القسم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("الصلاحية والدور:", style = MaterialTheme.typography.labelSmall)
                    var exp by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { exp = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(role.titleAr)
                    }
                    DropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                        UserRole.values().forEach { r ->
                            DropdownMenuItem(text = { Text(r.titleAr) }, onClick = { role = r; exp = false })
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور المؤقتة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(username.trim(), fullName, email, phone, department, role, password) },
                enabled = username.isNotBlank() && fullName.isNotBlank()
            ) { Text("إنشاء الحساب") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun EditUserDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSubmit: (UserEntity) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var department by remember { mutableStateOf(user.department) }
    var roleStr by remember { mutableStateOf(user.role) }
    var statusStr by remember { mutableStateOf(user.status) }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل بيانات المستخدم", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("اسم المستخدم: @${user.username}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                item {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("الاسم الكامل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("البريد الإلكتروني") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("الهاتف") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("القسم / الإدارة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Text("الدور والصلاحية:", style = MaterialTheme.typography.labelSmall)
                    var exp by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { exp = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(roleStr)
                    }
                    DropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                        UserRole.values().forEach { r ->
                            DropdownMenuItem(text = { Text("${r.name} (${r.titleAr})") }, onClick = { roleStr = r.name; exp = false })
                        }
                    }
                }
                item {
                    Text("حالة الحساب:", style = MaterialTheme.typography.labelSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = statusStr == "Active",
                            onClick = { statusStr = "Active" },
                            label = { Text("نشط (Active)") }
                        )
                        FilterChip(
                            selected = statusStr == "Suspended",
                            onClick = { statusStr = "Suspended" },
                            label = { Text("موقوف (Suspended)") }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("تغيير كلمة المرور (اتركه فارغاً للإبقاء على الحالية)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = user.copy(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        department = department,
                        role = roleStr,
                        status = statusStr,
                        passwordHash = if (newPassword.isNotBlank()) newPassword else user.passwordHash
                    )
                    onSubmit(updated)
                }
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun UserDetailsDialog(user: UserEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(user.fullName, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("اسم المستخدم: @${user.username}", style = MaterialTheme.typography.bodyMedium)
                Text("البريد: ${user.email}", style = MaterialTheme.typography.bodySmall)
                Text("الهاتف: ${user.phone}", style = MaterialTheme.typography.bodySmall)
                Text("القسم: ${user.department}", style = MaterialTheme.typography.bodySmall)
                Text("الدور في النظام: ${user.role}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Text("حالة الحساب: ${user.status}", style = MaterialTheme.typography.bodySmall)

                HorizontalDivider()

                Text("الصلاحيات الممنوحة لهذا المستخدم:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                val userPermissions = RolePermissions.allPermissions.filter {
                    user.role == "SuperAdmin" || user.role == "SystemAdmin" || it.allowedRoles.any { r -> r.equals(user.role, ignoreCase = true) }
                }
                userPermissions.forEach { p ->
                    Text("• ${p.titleAr}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
fun NewDepartmentDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, Int, Double, String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("المقر الرئيسي") }
    var empCountStr by remember { mutableStateOf("10") }
    var budgetStr by remember { mutableStateOf("500000") }
    var notes by remember { mutableStateOf("") }
    var showImportMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إضافة قسم جديد", fontWeight = FontWeight.Bold)
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
                            color = Color(0xFF0284C7).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("اختر وثيقة هيكل تنظيمي لاستخراج بيانات القسم:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                SmartDocumentImporter.sampleDepartmentPdfTemplates.forEach { t ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                code = t.departmentCode
                                                name = t.nameAr
                                                manager = t.managerName
                                                location = t.location
                                                empCountStr = t.employeeCount.toString()
                                                budgetStr = t.estimatedMonthlyBudget.toInt().toString()
                                                notes = t.notes
                                                showImportMenu = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Domain, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(t.nameAr, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("كود القسم (مثال: ENG)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = empCountStr,
                            onValueChange = { empCountStr = it },
                            label = { Text("عدد الموظفين") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم القسم بالعربية") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = manager,
                        onValueChange = { manager = it },
                        label = { Text("اسم المدير المباشر") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("مقر / موقع القسم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it },
                        label = { Text("الميزانية الشهرية التقديرية (ر.ي)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("الوصف والمهام والملاحظات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = empCountStr.toIntOrNull() ?: 0
                    val budget = budgetStr.toDoubleOrNull() ?: 0.0
                    onSubmit(code, name, manager, location, count, budget, notes)
                },
                enabled = code.isNotBlank() && name.isNotBlank() && manager.isNotBlank()
            ) {
                Text("إضافة القسم")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun EditDepartmentDialog(
    dept: DepartmentEntity,
    onDismiss: () -> Unit,
    onSubmit: (DepartmentEntity) -> Unit
) {
    var name by remember { mutableStateOf(dept.nameAr) }
    var manager by remember { mutableStateOf(dept.managerName) }
    var location by remember { mutableStateOf(dept.location) }
    var empCountStr by remember { mutableStateOf(dept.employeeCount.toString()) }
    var budgetStr by remember { mutableStateOf(dept.estimatedMonthlyBudget.toInt().toString()) }
    var notes by remember { mutableStateOf(dept.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل بيانات قسم (${dept.departmentCode})", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم القسم") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = manager, onValueChange = { manager = it }, label = { Text("المدير المباشر") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("الموقع") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = empCountStr, onValueChange = { empCountStr = it }, label = { Text("عدد الموظفين") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = budgetStr, onValueChange = { budgetStr = it }, label = { Text("الميزانية الشهرية (ر.ي)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = empCountStr.toIntOrNull() ?: dept.employeeCount
                    val budget = budgetStr.toDoubleOrNull() ?: dept.estimatedMonthlyBudget
                    onSubmit(dept.copy(nameAr = name, managerName = manager, location = location, employeeCount = count, estimatedMonthlyBudget = budget, notes = notes))
                }
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
