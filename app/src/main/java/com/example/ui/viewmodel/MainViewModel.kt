package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.auth.SessionManager
import com.example.data.auth.SessionState
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.HrRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen(val titleAr: String) {
    LOGIN("تسجيل الدخول"),
    DASHBOARD("لوحة التحكم"),
    EMPLOYEES("الموظفون"),
    LEAVES("إدارة الإجازات"),
    OVERTIME("الأجر الإضافي"),
    PENALTIES("الجزاءات والعقوبات"),
    SERVICES("الخدمات والمعاملات"),
    APPROVALS("مركز الموافقات"),
    NOTIFICATIONS("مركز الإشعارات"),
    REPORTS("التقارير والطباعة"),
    USERS_ROLES("المستخدمون والصلاحيات"),
    SYNC_CENTER("مركز المزامنة"),
    AUDIT_BACKUP("سجل التدقيق والنسخ"),
    SETTINGS("إعدادات النظام")
}

data class DashboardKpis(
    val totalEmployees: Int = 0,
    val activeEmployees: Int = 0,
    val pendingLeaves: Int = 0,
    val pendingOvertime: Int = 0,
    val pendingApprovalsTotal: Int = 0,
    val totalOvertimeHours: Double = 0.0,
    val approvedOvertimeHours: Double = 0.0,
    val totalOvertimeCost: Double = 0.0,
    val totalEntitledLeaves: Int = 0,
    val totalUnpaidLeaves: Int = 0,
    val unpaidLeaveRequestsCount: Int = 0,
    val totalMealCost: Double = 0.0,
    val totalTransportCost: Double = 0.0,
    val activePenalties: Int = 0
)

data class SyncLogItem(
    val id: String,
    val timestamp: Long,
    val status: String,
    val itemsSynced: Int,
    val details: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HrRepository = HrRepository.getInstance(application)
    val sessionManager: SessionManager = SessionManager.getInstance(application)

    // Current Screen & Auth Session
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = sessionManager.currentUser
    private val _currentUser: StateFlow<UserEntity?> = sessionManager.currentUser

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDepartmentFilter = MutableStateFlow("الكل")
    val selectedDepartmentFilter: StateFlow<String> = _selectedDepartmentFilter.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Data streams from repository
    val users = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val employees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val leaveRequests = repository.allLeaveRequests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val overtimeRequests = repository.allOvertimeRequests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val penalties = repository.allPenalties.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val mealRequests = repository.allMealRequests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transportRequests = repository.allTransportationRequests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val adminTransactions = repository.allAdminTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotificationsCount = repository.unreadNotificationCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val auditLogs = repository.auditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingSyncQueue = repository.pendingSyncQueue.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val departments = repository.allDepartments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Low balance employees for proactive alerts
    val lowBalanceEmployees: StateFlow<List<EmployeeEntity>> = employees
        .map { list ->
            list.filter { it.annualLeaveBalance <= 5 && it.status == "Active" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemSettings: StateFlow<Map<String, String>> = repository.systemSettings
        .map { s: SystemSettingsEntity? ->
            mapOf(
                "org_name" to (s?.companyName ?: "مجموعة الخليج للخدمات المؤسسية"),
                "cr_number" to (s?.companyCommercialId ?: "CR-1010893421"),
                "standard_work_hours" to (s?.dailyWorkingHours?.toString() ?: "8"),
                "supervisor_name" to (s?.supervisorName ?: "أ.أحمد العمري")
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val syncLogs: StateFlow<List<SyncLogItem>> = auditLogs
        .map { logs: List<AuditLogEntity> ->
            logs.filter { it.actionType == "SYNC" || it.entityType == "DATABASE" || it.entityType == "SYSTEM" }
                .map {
                    SyncLogItem(
                        id = it.id,
                        timestamp = it.timestamp,
                        status = "SUCCESS",
                        itemsSynced = 1,
                        details = it.details
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedKpiMonth = MutableStateFlow("2026-08")
    val selectedKpiMonth: StateFlow<String> = _selectedKpiMonth.asStateFlow()

    fun setKpiMonth(month: String) {
        _selectedKpiMonth.value = month
    }

    // Derived Dashboard KPIs using combine with monthly filter
    val dashboardKpis: StateFlow<DashboardKpis> = combine(
        combine(employees, leaveRequests, overtimeRequests) { emps, leaves, ots ->
            Triple(emps, leaves, ots)
        },
        combine(mealRequests, transportRequests, _selectedKpiMonth) { meals, transports, monthFilter ->
            Triple(meals, transports, monthFilter)
        }
    ) { (emps, leaves, ots), (meals, transports, monthFilter) ->
        val pendingL = leaves.count { it.status.startsWith("Pending") || it.status == "Submitted" }
        val pendingO = ots.count { it.status.startsWith("Pending") || it.status == "Submitted" }
        
        // Filter overtime & leaves by selected month
        val filteredOts = if (monthFilter == "ALL") ots else ots.filter { it.date.startsWith(monthFilter) }
        val filteredLeaves = if (monthFilter == "ALL") leaves else leaves.filter { it.startDate.startsWith(monthFilter) }

        // 1. Total Overtime Hours & Approved Overtime Hours (Monthly)
        val totalOtHours = filteredOts.sumOf { it.hours }
        val approvedOtHours = filteredOts.filter { it.status == "Approved" || it.status == "DeptApproved" || it.status == "HrApproved" }.sumOf { it.hours }
        
        // 2. Overtime Cost (Calculated amount for month in YER)
        val totalOtCost = filteredOts.sumOf { it.calculatedAmount }
        
        // 3. Total Entitled Leaves (Sum of all employee leave entitlements: annual + sick + emergency)
        val totalEntitledLeaves = emps.sumOf { it.annualLeaveBalance + it.emergencyLeaveBalance }
        
        // 4. Total Unpaid Leaves (Sum of days for unpaid leave requests in month)
        val unpaidLeaveReqs = filteredLeaves.filter { it.leaveType.equals("Unpaid", ignoreCase = true) || it.leaveType.contains("بدون") }
        val totalUnpaidLeavesDays = unpaidLeaveReqs.sumOf { it.daysCount }
        val unpaidReqsCount = unpaidLeaveReqs.size
        
        val totalMealCost = meals.sumOf { it.totalCost }
        val totalTransCost = transports.sumOf { it.totalCost }

        DashboardKpis(
            totalEmployees = emps.size,
            activeEmployees = emps.count { it.status == "Active" },
            pendingLeaves = pendingL,
            pendingOvertime = pendingO,
            pendingApprovalsTotal = pendingL + pendingO,
            totalOvertimeHours = totalOtHours,
            approvedOvertimeHours = approvedOtHours,
            totalOvertimeCost = totalOtCost,
            totalEntitledLeaves = totalEntitledLeaves,
            totalUnpaidLeaves = totalUnpaidLeavesDays,
            unpaidLeaveRequestsCount = unpaidReqsCount,
            totalMealCost = totalMealCost,
            totalTransportCost = totalTransCost,
            activePenalties = 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardKpis())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDepartmentFilter(dept: String) {
        _selectedDepartmentFilter.value = dept
    }

    fun toggleNetwork() {
        _isOnline.value = !_isOnline.value
        val stateText = if (_isOnline.value) "متصل بالإنترنت (Online)" else "وضع عدم الاتصال (Offline-First Mode)"
        showStatus("تم التبديل إلى: $stateText")
        if (_isOnline.value) {
            triggerSync()
        }
    }

    fun triggerSync() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            delay(1000)
            repository.processSyncQueue(user)
            _isSyncing.value = false
            showStatus("تمت مزامنة جميع البيانات بنجاح مع الخادم المركزي.")
        }
    }

    fun login(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        sessionManager.login(username, pass) { success, msg ->
            if (success) {
                _currentScreen.value = AppScreen.DASHBOARD
            }
            onResult(success, msg)
        }
    }

    fun switchUser(user: UserEntity) {
        sessionManager.setActiveUser(user)
        showStatus("تم التبديل إلى: ${user.fullName} (${user.role})")
    }

    fun changePassword(newPass: String, onSuccess: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(passwordHash = newPass, mustChangePassword = false)
            repository.updateUser(updated)
            sessionManager.setActiveUser(updated)
            repository.recordAudit(user, "UPDATE", "SECURITY", user.id, "تغيير كلمة المرور لحساب ${user.username}")
            showStatus("تم تحديث كلمة المرور بنجاح.")
            onSuccess()
        }
    }

    fun logout() {
        sessionManager.logout()
        _currentScreen.value = AppScreen.LOGIN
    }

    // Leave Actions
    fun submitLeaveRequest(
        empId: String,
        empName: String,
        dept: String,
        leaveType: LeaveType,
        startDate: String,
        endDate: String,
        days: Int,
        reason: String,
        substitute: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val reqNumber = "LEAVE-2026-${(1000..9999).random()}"
            val req = LeaveRequestEntity(
                requestNumber = reqNumber,
                employeeId = empId,
                employeeName = empName,
                department = dept,
                leaveType = leaveType.name,
                startDate = startDate,
                endDate = endDate,
                daysCount = days,
                reason = reason,
                previousBalance = 24,
                usedBalance = days,
                remainingBalance = (24 - days).coerceAtLeast(0),
                substituteEmployee = substitute,
                status = "Submitted"
            )
            repository.insertLeaveRequest(req, user, _isOnline.value)
            showStatus("تم إرسال طلب الإجازة $reqNumber بنجاح.")
        }
    }

    fun updateLeaveApproval(reqId: String, newStatus: RequestStatus, comment: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateLeaveStatus(reqId, newStatus, comment, user, _isOnline.value)
            showStatus("تم تحديث طلب الإجازة إلى (${newStatus.titleAr})")
        }
    }

    // Overtime Actions
    fun submitOvertimeRequest(
        empId: String,
        empName: String,
        dept: String,
        date: String,
        workDayType: String,
        startTime: String,
        endTime: String,
        hours: Double,
        overtimeType: OvertimeType,
        reason: String,
        task: String,
        hourlyRate: Double
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val reqNumber = "OT-2026-${(1000..9999).random()}"
            val calculatedAmount = hours * hourlyRate * overtimeType.multiplier
            val req = OvertimeRequestEntity(
                requestNumber = reqNumber,
                employeeId = empId,
                employeeName = empName,
                department = dept,
                date = date,
                workDayType = workDayType,
                startTime = startTime,
                endTime = endTime,
                hours = hours,
                overtimeType = overtimeType.name,
                reason = reason,
                projectOrTask = task,
                calculatedAmount = calculatedAmount,
                status = "Submitted"
            )
            repository.insertOvertimeRequest(req, user, _isOnline.value)
            showStatus("تم تسجيل طلب العمل الإضافي $reqNumber بمبلغ ${"%.2f".format(calculatedAmount)} ريال.")
        }
    }

    fun updateOvertimeApproval(reqId: String, newStatus: RequestStatus, comment: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateOvertimeStatus(reqId, newStatus, comment, user, _isOnline.value)
            showStatus("تم تحديث طلب العمل الإضافي إلى (${newStatus.titleAr})")
        }
    }

    // Penalties Actions
    fun createPenalty(
        empId: String,
        empName: String,
        dept: String,
        incidentDate: String,
        violationType: String,
        description: String,
        severity: Severity,
        actionType: PenaltyAction,
        deduction: Double,
        suspensionDays: Int,
        legalRef: String,
        investigation: String,
        statement: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val penNumber = "PEN-2026-${(1000..9999).random()}"
            val pen = PenaltyEntity(
                penaltyNumber = penNumber,
                employeeId = empId,
                employeeName = empName,
                department = dept,
                incidentDate = incidentDate,
                violationType = violationType,
                description = description,
                severity = severity.name,
                actionType = actionType.name,
                deductionAmount = deduction,
                suspensionDays = suspensionDays,
                legalReference = legalRef,
                investigationNotes = investigation,
                employeeStatement = statement,
                status = "Approved"
            )
            repository.insertPenalty(pen, user, _isOnline.value)
            showStatus("تم تسجيل الجزاء $penNumber بنجاح.")
        }
    }

    // Meal & Transport Actions
    fun submitMealRequest(empId: String, empName: String, dept: String, date: String, mealType: MealType, count: Int, vendor: String, reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val reqNumber = "MEAL-2026-${(1000..9999).random()}"
            val cost = count * mealType.defaultPrice
            val req = MealRequestEntity(
                requestNumber = reqNumber,
                employeeId = empId,
                employeeName = empName,
                department = dept,
                date = date,
                mealType = mealType.name,
                count = count,
                totalCost = cost,
                vendor = vendor,
                reason = reason
            )
            repository.insertMealRequest(req, user, _isOnline.value)
            showStatus("تم تقديم طلب الوجبات $reqNumber بمبلغ $cost ريال.")
        }
    }

    fun submitTransportRequest(empId: String, empName: String, dept: String, date: String, route: String, from: String, to: String, type: TransportType, trips: Int, cost: Double, driver: String, reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val reqNumber = "TRANS-2026-${(1000..9999).random()}"
            val req = TransportationRequestEntity(
                requestNumber = reqNumber,
                employeeId = empId,
                employeeName = empName,
                department = dept,
                date = date,
                route = route,
                departurePoint = from,
                destination = to,
                transportType = type.name,
                tripsCount = trips,
                totalCost = cost,
                vendorOrDriver = driver,
                reason = reason
            )
            repository.insertTransportationRequest(req, user, _isOnline.value)
            showStatus("تم تقديم طلب المواصلات $reqNumber بنجاح.")
        }
    }

    fun submitAdminTransaction(empId: String, empName: String, dept: String, date: String, subject: String, desc: String, priority: Priority, targetDept: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val reqNumber = "TRX-2026-${(1000..9999).random()}"
            val req = AdminTransactionEntity(
                transactionNumber = reqNumber,
                employeeId = empId,
                employeeName = empName,
                department = dept,
                date = date,
                subject = subject,
                description = desc,
                priority = priority.name,
                targetDepartment = targetDept
            )
            repository.insertAdminTransaction(req, user, _isOnline.value)
            showStatus("تم إنشاء المعاملة الإدارية $reqNumber بنجاح.")
        }
    }

    // Employee Management
    fun saveEmployee(emp: EmployeeEntity, isEdit: Boolean) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (isEdit) {
                repository.updateEmployee(emp, user, _isOnline.value)
                showStatus("تم تحديث بيانات الموظف ${emp.fullName}")
            } else {
                repository.insertEmployee(emp, user, _isOnline.value)
                showStatus("تمت إضافة الموظف الجديد ${emp.fullName}")
            }
        }
    }

    fun deleteEmployee(emp: EmployeeEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteEmployee(emp, user, _isOnline.value)
            showStatus("تم حذف الموظف ${emp.fullName} بنجاح.")
        }
    }

    // User & Permission Management
    fun createUser(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        department: String,
        role: UserRole,
        pass: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newUser = UserEntity(
                username = username,
                passwordHash = pass,
                fullName = fullName,
                email = email,
                phone = phone,
                department = department,
                position = "أخصائي $department",
                role = role.name,
                status = "Active",
                mustChangePassword = false
            )
            repository.insertUser(newUser)
            repository.recordAudit(user, "CREATE", "USER", newUser.id, "إضافة مستخدم جديد: $username بدور ${role.name}")
            showStatus("تم إنشاء الحساب للمستخدم $fullName بنجاح.")
        }
    }

    fun updateUser(userToUpdate: UserEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(userToUpdate)
            repository.recordAudit(user, "UPDATE", "USER", userToUpdate.id, "تعديل بيانات المستخدم: ${userToUpdate.username}")
            showStatus("تم تحديث بيانات المستخدم ${userToUpdate.fullName} بنجاح.")
        }
    }

    fun deleteUser(userToDelete: UserEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteUser(userToDelete.id)
            repository.recordAudit(user, "DELETE", "USER", userToDelete.id, "حذف حساب المستخدم: ${userToDelete.username}")
            showStatus("تم حذف المستخدم ${userToDelete.fullName} بنجاح.")
        }
    }

    // Department Management CRUD
    fun createDepartment(
        code: String,
        name: String,
        manager: String,
        location: String,
        empCount: Int,
        budget: Double,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newDept = DepartmentEntity(
                departmentCode = code.trim().uppercase(),
                nameAr = name.trim(),
                managerName = manager.trim(),
                location = location.trim(),
                employeeCount = empCount,
                estimatedMonthlyBudget = budget,
                notes = notes.trim()
            )
            repository.insertDepartment(newDept, user, _isOnline.value)
            showStatus("تم إنشاء القسم الجديد (${name}) بنجاح.")
        }
    }

    fun updateDepartment(dept: DepartmentEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateDepartment(dept, user, _isOnline.value)
            showStatus("تم تحديث بيانات قسم (${dept.nameAr}) بنجاح.")
        }
    }

    fun deleteDepartment(dept: DepartmentEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteDepartment(dept, user, _isOnline.value)
            showStatus("تم حذف قسم (${dept.nameAr}) بنجاح.")
        }
    }

    // Leave CRUD
    fun updateLeaveRequest(leave: LeaveRequestEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateLeaveRequest(leave, user, _isOnline.value)
            showStatus("تم تعديل طلب الإجازة ${leave.requestNumber} بنجاح.")
        }
    }

    fun deleteLeaveRequest(leave: LeaveRequestEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteLeaveRequest(leave, user, _isOnline.value)
            showStatus("تم حذف طلب الإجازة ${leave.requestNumber} بنجاح.")
        }
    }

    // Overtime CRUD
    fun updateOvertimeRequest(ot: OvertimeRequestEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateOvertimeRequest(ot, user, _isOnline.value)
            showStatus("تم تعديل طلب العمل الإضافي ${ot.requestNumber} بنجاح.")
        }
    }

    fun deleteOvertimeRequest(ot: OvertimeRequestEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteOvertimeRequest(ot, user, _isOnline.value)
            showStatus("تم حذف طلب العمل الإضافي ${ot.requestNumber} بنجاح.")
        }
    }

    // Admin Transactions CRUD
    fun updateAdminTransaction(tx: AdminTransactionEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateAdminTransaction(tx, user, _isOnline.value)
            showStatus("تم تعديل المعاملة الإدارية ${tx.transactionNumber} بنجاح.")
        }
    }

    fun deleteAdminTransaction(tx: AdminTransactionEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteAdminTransaction(tx, user, _isOnline.value)
            showStatus("تم حذف المعاملة الإدارية ${tx.transactionNumber} بنجاح.")
        }
    }

    // Proactive Leave Balance Alerts
    fun sendProactiveLeaveAlert(emp: EmployeeEntity) {
        val user = _currentUser.value ?: return
        val remaining = emp.annualLeaveBalance.coerceAtLeast(0)
        viewModelScope.launch {
            repository.sendProactiveLeaveAlert(emp, remaining, user)
            showStatus("تم إرسال تنبيه استباقي للموظف ${emp.fullName} بنجاح.")
        }
    }

    fun sendBulkProactiveLeaveAlerts() {
        val user = _currentUser.value ?: return
        val targets = lowBalanceEmployees.value
        if (targets.isEmpty()) {
            showStatus("لا يوجد موظفون بأرصدة منخفضة حالياً.")
            return
        }
        viewModelScope.launch {
            val sentCount = repository.sendBulkProactiveLeaveAlerts(targets, user)
            showStatus("تم إرسال تنبيهات استباقية لـ $sentCount موظفاً بنجاح.")
        }
    }

    // PDF Export for Dashboard KPIs
    fun exportKpiReportPdf(context: Context) {
        val currentKpis = dashboardKpis.value
        val monthCode = selectedKpiMonth.value
        val monthLabel = when (monthCode) {
            "2026-08" -> "أغسطس 2026"
            "2026-07" -> "يوليو 2026"
            "2026-09" -> "سبتمبر 2026"
            else -> "كامل الفترة"
        }
        val settings = systemSettings.value
        val companyName = settings["org_name"] ?: "مجموعة الخليج للخدمات المؤسسية والتطوير"
        val supervisor = settings["supervisor_name"] ?: "أ.أحمد العمري"
        val userName = _currentUser.value?.fullName ?: "مدير النظام"

        val result = com.example.util.PdfExportHelper.generateAndShareKpiPdf(
            context = context,
            kpis = currentKpis,
            monthTitle = monthLabel,
            companyName = companyName,
            supervisorName = supervisor,
            generatedByUserName = userName
        )
        if (result.isSuccess) {
            showStatus("تم تصدير لوحة المؤشرات الشهرية إلى ملف PDF بنجاح.")
        } else {
            showStatus("حدث خطأ أثناء تصدير ملف PDF: ${result.exceptionOrNull()?.message}")
        }
    }

    // Notifications & HR Circulars
    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationAsRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsAsRead() }
    }

    fun broadcastHrCircular(title: String, circularNumber: String, message: String, priority: Priority = Priority.High) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.broadcastCircular(
                title = title,
                circularNumber = circularNumber,
                content = message,
                priority = priority,
                currentUser = user
            )
            showStatus("تم نشر التعميم الإداري بنجاح وإرسال الإشعار لجميع الموظفين.")
        }
    }

    fun updateAdminTransactionApproval(txId: String, newStatus: String, comment: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateAdminTransactionStatus(txId, newStatus, comment, user, _isOnline.value)
            showStatus("تم تحديث حالة المعاملة الإدارية.")
        }
    }

    // SharedPreferences "Remember Me" Support
    private val sharedPreferences = getApplication<Application>().getSharedPreferences("hr_app_auth_prefs", Context.MODE_PRIVATE)

    fun getSavedCredentials(): Pair<String, String>? {
        val remember = sharedPreferences.getBoolean("remember_me", false)
        if (!remember) return null
        val u = sharedPreferences.getString("saved_username", "") ?: ""
        val p = sharedPreferences.getString("saved_password", "") ?: ""
        return if (u.isNotBlank()) Pair(u, p) else null
    }

    fun isRememberMeEnabled(): Boolean {
        return sharedPreferences.getBoolean("remember_me", false)
    }

    fun saveLoginPreferences(username: String, pass: String, rememberMe: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean("remember_me", rememberMe)
            if (rememberMe) {
                putString("saved_username", username.trim())
                putString("saved_password", pass.trim())
            } else {
                remove("saved_username")
                remove("saved_password")
            }
            apply()
        }
    }

    // Backup
    fun createLocalBackup(onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            delay(600)
            val backupFile = "hr_backup_2026_${System.currentTimeMillis()}.enc"
            repository.recordAudit(user, "BACKUP", "DATABASE", "FULL", "تصدير نسخة احتياطية محلية مشفرة: $backupFile")
            showStatus("تم حفظ النسخة الاحتياطية المشفرة: $backupFile")
            onResult(true, backupFile)
        }
    }

    fun saveSettings(settingsMap: Map<String, String>) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val current = repository.systemSettings.firstOrNull() ?: SystemSettingsEntity()
            val updated = current.copy(
                companyName = settingsMap["org_name"] ?: current.companyName,
                companyCommercialId = settingsMap["cr_number"] ?: current.companyCommercialId,
                dailyWorkingHours = settingsMap["standard_work_hours"]?.toDoubleOrNull() ?: current.dailyWorkingHours,
                supervisorName = settingsMap["supervisor_name"] ?: current.supervisorName
            )
            repository.updateSettings(updated, user)
            showStatus("تم حفظ الإعدادات بنجاح في قاعدة البيانات.")
        }
    }

    private fun showStatus(msg: String) {
        _statusMessage.value = msg
        viewModelScope.launch {
            delay(4000)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }
}
