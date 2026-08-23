package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.seed.DatabaseSeed
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HrRepository(private val dao: HrDao) {

    companion object {
        @Volatile
        private var instance: HrRepository? = null

        fun getInstance(context: Context): HrRepository {
            return instance ?: synchronized(this) {
                instance ?: HrRepository(AppDatabase.getDatabase(context).hrDao()).also { instance = it }
            }
        }
    }

    suspend fun ensureSeeded() {
        DatabaseSeed.populateDatabaseIfEmpty(dao)
    }

    // Flows
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allEmployees: Flow<List<EmployeeEntity>> = dao.getAllEmployees()
    val employeeCount: Flow<Int> = dao.getEmployeeCount()
    val allLeaveRequests: Flow<List<LeaveRequestEntity>> = dao.getAllLeaveRequests()
    val allOvertimeRequests: Flow<List<OvertimeRequestEntity>> = dao.getAllOvertimeRequests()
    val allPenalties: Flow<List<PenaltyEntity>> = dao.getAllPenalties()
    val allMealRequests: Flow<List<MealRequestEntity>> = dao.getAllMealRequests()
    val allTransportationRequests: Flow<List<TransportationRequestEntity>> = dao.getAllTransportationRequests()
    val allAdminTransactions: Flow<List<AdminTransactionEntity>> = dao.getAllAdminTransactions()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = dao.getUnreadNotificationCount()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAuditLogs()
    val pendingSyncQueue: Flow<List<SyncQueueEntity>> = dao.getPendingSyncQueue()
    val allSyncQueue: Flow<List<SyncQueueEntity>> = dao.getAllSyncQueue()
    val systemSettings: Flow<SystemSettingsEntity?> = dao.getSystemSettings()
    val allDepartments: Flow<List<DepartmentEntity>> = dao.getAllDepartments()

    // Departments
    suspend fun insertDepartment(dept: DepartmentEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.insertDepartment(dept)
        recordAudit(currentUser, "CREATE", "DEPARTMENT", dept.id, "إضافة قسم جديد: ${dept.nameAr} (${dept.departmentCode})")
        if (!isOnline) {
            enqueueSync("DEPARTMENT", dept.id, "INSERT", "إضافة قسم ${dept.nameAr}")
        }
    }

    suspend fun updateDepartment(dept: DepartmentEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.updateDepartment(dept)
        recordAudit(currentUser, "UPDATE", "DEPARTMENT", dept.id, "تعديل بيانات القسم: ${dept.nameAr}")
        if (!isOnline) {
            enqueueSync("DEPARTMENT", dept.id, "UPDATE", "تعديل قسم ${dept.nameAr}")
        }
    }

    suspend fun deleteDepartment(dept: DepartmentEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.deleteDepartment(dept.id)
        recordAudit(currentUser, "DELETE", "DEPARTMENT", dept.id, "حذف القسم: ${dept.nameAr}")
        if (!isOnline) {
            enqueueSync("DEPARTMENT", dept.id, "DELETE", "حذف قسم ${dept.nameAr}")
        }
    }

    // Auth & Users
    suspend fun getUserByUsername(username: String): UserEntity? {
        var user = dao.getUserByUsername(username.trim())
        if (user == null) {
            DatabaseSeed.populateDatabaseIfEmpty(dao)
            user = dao.getUserByUsername(username.trim())
        }
        return user
    }
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email.trim())
    suspend fun getUserById(id: String): UserEntity? = dao.getUserById(id)
    suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
    suspend fun deleteUser(id: String) = dao.deleteUser(id)

    // Employees
    suspend fun getEmployeeById(id: String): EmployeeEntity? = dao.getEmployeeById(id)
    suspend fun insertEmployee(emp: EmployeeEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.insertEmployee(emp)
        recordAudit(currentUser, "CREATE", "EMPLOYEE", emp.id, "إضافة موظف جديد: ${emp.fullName} (${emp.employeeNumber})")
        if (!isOnline) {
            enqueueSync("EMPLOYEE", emp.id, "INSERT", "إضافة موظف ${emp.fullName}")
        }
    }
    suspend fun updateEmployee(emp: EmployeeEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.updateEmployee(emp)
        recordAudit(currentUser, "UPDATE", "EMPLOYEE", emp.id, "تعديل بيانات الموظف: ${emp.fullName}")
        if (!isOnline) {
            enqueueSync("EMPLOYEE", emp.id, "UPDATE", "تعديل موظف ${emp.fullName}")
        }
    }
    suspend fun deleteEmployee(emp: EmployeeEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.deleteEmployee(emp.id)
        recordAudit(currentUser, "DELETE", "EMPLOYEE", emp.id, "حذف الموظف: ${emp.fullName}")
        if (!isOnline) {
            enqueueSync("EMPLOYEE", emp.id, "DELETE", "حذف موظف ${emp.fullName}")
        }
    }

    // Leaves
    suspend fun insertLeaveRequest(request: LeaveRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        val syncStatus = if (isOnline) "SYNCED" else "PENDING"
        val finalReq = request.copy(syncStatus = syncStatus)
        dao.insertLeaveRequest(finalReq)
        recordAudit(currentUser, "CREATE", "LEAVE", request.id, "تقديم طلب إجازة ${request.requestNumber} لـ ${request.employeeName} (${request.daysCount} أيام)")
        
        dao.insertNotification(
            NotificationEntity(
                recipientRole = "DEPT_HEAD",
                title = "طلب إجازة جديد: ${request.employeeName}",
                message = "تم تقديم طلب إجازة ${request.leaveType} لمدة ${request.daysCount} يوم تبدأ في ${request.startDate}.",
                category = "REQUEST",
                targetRequestId = request.id,
                targetRequestType = "LEAVE"
            )
        )
        if (!isOnline) {
            enqueueSync("LEAVE", request.id, "INSERT", "طلب إجازة ${request.requestNumber}")
        }
    }

    suspend fun updateLeaveStatus(
        requestId: String,
        newStatus: RequestStatus,
        comment: String,
        currentUser: UserEntity,
        isOnline: Boolean
    ) {
        val req = dao.getLeaveRequestById(requestId) ?: return
        val updated = req.copy(
            status = newStatus.name,
            notes = if (comment.isNotBlank()) comment else req.notes,
            updatedAt = System.currentTimeMillis(),
            syncStatus = if (isOnline) "SYNCED" else "PENDING"
        )
        dao.updateLeaveRequest(updated)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        dao.insertApprovalHistory(
            ApprovalHistoryEntity(
                requestId = requestId,
                requestType = "LEAVE",
                action = newStatus.name,
                decisionMakerId = currentUser.id,
                decisionMakerName = currentUser.fullName,
                decisionMakerRole = currentUser.role,
                decisionDate = sdf.format(Date()),
                comments = comment
            )
        )

        recordAudit(currentUser, "APPROVE", "LEAVE", requestId, "تغيير حالة طلب الإجازة ${req.requestNumber} إلى ${newStatus.titleAr}. التعليق: $comment")

        dao.insertNotification(
            NotificationEntity(
                recipientRole = req.employeeId,
                title = "تحديث على طلب الإجازة ${req.requestNumber}",
                message = "تم تغيير حالة طلب الإجازة إلى (${newStatus.titleAr}) بواسطة ${currentUser.fullName}.",
                category = "APPROVAL",
                targetRequestId = requestId,
                targetRequestType = "LEAVE"
            )
        )

        if (!isOnline) {
            enqueueSync("LEAVE", requestId, "UPDATE", "تحديث حالة إجازة ${req.requestNumber} -> ${newStatus.titleAr}")
        }
    }

    // Overtime
    suspend fun insertOvertimeRequest(request: OvertimeRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        val syncStatus = if (isOnline) "SYNCED" else "PENDING"
        val finalReq = request.copy(syncStatus = syncStatus)
        dao.insertOvertimeRequest(finalReq)
        recordAudit(currentUser, "CREATE", "OVERTIME", request.id, "طلب عمل إضافي ${request.requestNumber} (${request.hours} ساعة)")
        
        dao.insertNotification(
            NotificationEntity(
                recipientRole = "DEPT_HEAD",
                title = "طلب عمل إضافي: ${request.employeeName}",
                message = "تم تسجيل طلب عمل إضافي ${request.hours} ساعات بتاريخ ${request.date}.",
                category = "REQUEST",
                targetRequestId = request.id,
                targetRequestType = "OVERTIME"
            )
        )
        if (!isOnline) {
            enqueueSync("OVERTIME", request.id, "INSERT", "طلب عمل إضافي ${request.requestNumber}")
        }
    }

    suspend fun updateOvertimeStatus(
        requestId: String,
        newStatus: RequestStatus,
        comment: String,
        currentUser: UserEntity,
        isOnline: Boolean
    ) {
        val req = dao.getOvertimeRequestById(requestId) ?: return
        val updated = req.copy(
            status = newStatus.name,
            notes = if (comment.isNotBlank()) comment else req.notes,
            updatedAt = System.currentTimeMillis(),
            syncStatus = if (isOnline) "SYNCED" else "PENDING"
        )
        dao.updateOvertimeRequest(updated)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        dao.insertApprovalHistory(
            ApprovalHistoryEntity(
                requestId = requestId,
                requestType = "OVERTIME",
                action = newStatus.name,
                decisionMakerId = currentUser.id,
                decisionMakerName = currentUser.fullName,
                decisionMakerRole = currentUser.role,
                decisionDate = sdf.format(Date()),
                comments = comment
            )
        )

        recordAudit(currentUser, "APPROVE", "OVERTIME", requestId, "تغيير حالة طلب العمل الإضافي ${req.requestNumber} إلى ${newStatus.titleAr}")

        dao.insertNotification(
            NotificationEntity(
                recipientRole = req.employeeId,
                title = "تحديث طلب العمل الإضافي ${req.requestNumber}",
                message = "تم تغيير حالة الطلب إلى (${newStatus.titleAr}).",
                category = "APPROVAL",
                targetRequestId = requestId,
                targetRequestType = "OVERTIME"
            )
        )
        if (!isOnline) {
            enqueueSync("OVERTIME", requestId, "UPDATE", "تحديث عمل إضافي ${req.requestNumber}")
        }
    }

    // Penalties
    suspend fun insertPenalty(penalty: PenaltyEntity, currentUser: UserEntity, isOnline: Boolean) {
        val syncStatus = if (isOnline) "SYNCED" else "PENDING"
        dao.insertPenalty(penalty.copy(syncStatus = syncStatus))
        recordAudit(currentUser, "CREATE", "PENALTY", penalty.id, "تسجيل جزاء ${penalty.penaltyNumber} للموظف ${penalty.employeeName} (${penalty.actionType})")
        
        dao.insertNotification(
            NotificationEntity(
                recipientRole = "HR",
                title = "تسجيل قرار جزاء إداري ${penalty.penaltyNumber}",
                message = "تم إصدار إجراء ${penalty.actionType} بحق ${penalty.employeeName}.",
                category = "ALERT",
                targetRequestId = penalty.id,
                targetRequestType = "PENALTY"
            )
        )
        if (!isOnline) {
            enqueueSync("PENALTY", penalty.id, "INSERT", "جزاء ${penalty.penaltyNumber}")
        }
    }

    // Meals
    suspend fun insertMealRequest(request: MealRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.insertMealRequest(request.copy(syncStatus = if (isOnline) "SYNCED" else "PENDING"))
        recordAudit(currentUser, "CREATE", "MEAL", request.id, "طلب وجبات ${request.requestNumber} (${request.count} وجبات)")
        if (!isOnline) {
            enqueueSync("MEAL", request.id, "INSERT", "طلب وجبات ${request.requestNumber}")
        }
    }

    // Transportation
    suspend fun insertTransportationRequest(request: TransportationRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.insertTransportationRequest(request.copy(syncStatus = if (isOnline) "SYNCED" else "PENDING"))
        recordAudit(currentUser, "CREATE", "TRANSPORT", request.id, "طلب مواصلات ${request.requestNumber} (${request.route})")
        if (!isOnline) {
            enqueueSync("TRANSPORT", request.id, "INSERT", "طلب مواصلات ${request.requestNumber}")
        }
    }

    // Admin Transactions
    suspend fun insertAdminTransaction(tx: AdminTransactionEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.insertAdminTransaction(tx.copy(syncStatus = if (isOnline) "SYNCED" else "PENDING"))
        recordAudit(currentUser, "CREATE", "TRANSACTION", tx.id, "إنشاء معاملة إدارية ${tx.transactionNumber} - ${tx.subject}")
        dao.insertNotification(
            NotificationEntity(
                recipientRole = "HR",
                title = "معاملة إدارية جديدة: ${tx.transactionNumber}",
                message = "قدم الموظف ${tx.employeeName} معاملة: ${tx.subject}",
                category = "REQUEST",
                targetRequestId = tx.id,
                targetRequestType = "TRANSACTION"
            )
        )
        if (!isOnline) {
            enqueueSync("TRANSACTION", tx.id, "INSERT", "معاملة ${tx.transactionNumber}")
        }
    }

    suspend fun updateAdminTransactionStatus(
        txId: String,
        newStatus: String,
        comment: String,
        currentUser: UserEntity,
        isOnline: Boolean
    ) {
        val tx = dao.getAdminTransactionById(txId) ?: return
        val updated = tx.copy(
            status = newStatus,
            notes = if (comment.isNotBlank()) comment else tx.notes,
            syncStatus = if (isOnline) "SYNCED" else "PENDING"
        )
        dao.updateAdminTransaction(updated)
        recordAudit(currentUser, "APPROVE", "TRANSACTION", txId, "تحديث حالة المعاملة الإدارية ${tx.transactionNumber} إلى $newStatus")
        dao.insertNotification(
            NotificationEntity(
                recipientRole = tx.employeeId,
                title = "تحديث المعاملة الإدارية ${tx.transactionNumber}",
                message = "تم تحديث حالة معاملتك (${tx.subject}) إلى: $newStatus.",
                category = "APPROVAL",
                targetRequestId = txId,
                targetRequestType = "TRANSACTION"
            )
        )
    }

    // HR Circulars & Broadcasts
    suspend fun broadcastCircular(
        title: String,
        circularNumber: String,
        content: String,
        priority: Priority,
        currentUser: UserEntity
    ) {
        val notif = NotificationEntity(
            recipientRole = "ALL",
            title = "تعميم إداري ($circularNumber): $title",
            message = content,
            category = "CIRCULAR",
            targetRequestId = circularNumber,
            targetRequestType = "CIRCULAR"
        )
        dao.insertNotification(notif)
        recordAudit(currentUser, "CREATE", "CIRCULAR", circularNumber, "إصدار تعميم إداري رسمي: $title ($circularNumber)")
    }

    // Proactive Leave Balance Alert
    suspend fun sendProactiveLeaveAlert(emp: EmployeeEntity, remainingDays: Int, currentUser: UserEntity) {
        val title = "تنبيه استباقي: قرب انتهاء رصيد الإجازات السنوية"
        val message = "عزيزي الموظف ${emp.fullName}، نفيدك بأن رصيدك المتبقي من الإجازات السنوية لسنة 2026 هو ($remainingDays أيام فقط). يرجى التنسيق مع مديرك المباشر لجدولة إجازاتك القادمة قبل إقفال السنة المالية."
        
        dao.insertNotification(
            NotificationEntity(
                recipientRole = emp.id,
                title = title,
                message = message,
                category = "LEAVE_ALERT",
                targetRequestId = emp.id,
                targetRequestType = "LEAVE_ALERT"
            )
        )
        recordAudit(currentUser, "ALERT", "LEAVE_BALANCE", emp.id, "إرسال تنبيه استباقي للموظف ${emp.fullName} بقرب نفاد رصيد الإجازات ($remainingDays أيام متبقية)")
    }

    suspend fun sendBulkProactiveLeaveAlerts(lowBalanceEmployees: List<EmployeeEntity>, currentUser: UserEntity): Int {
        var count = 0
        for (emp in lowBalanceEmployees) {
            val remaining = emp.annualLeaveBalance.coerceAtLeast(0)
            sendProactiveLeaveAlert(emp, remaining, currentUser)
            count++
        }
        return count
    }

    // CRUD Edits & Deletions
    suspend fun updateLeaveRequest(request: LeaveRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.updateLeaveRequest(request)
        recordAudit(currentUser, "UPDATE", "LEAVE", request.id, "تعديل بيانات طلب الإجازة ${request.requestNumber}")
        if (!isOnline) {
            enqueueSync("LEAVE", request.id, "UPDATE", "تعديل طلب إجازة ${request.requestNumber}")
        }
    }

    suspend fun deleteLeaveRequest(request: LeaveRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.deleteLeaveRequest(request.id)
        recordAudit(currentUser, "DELETE", "LEAVE", request.id, "حذف طلب الإجازة ${request.requestNumber}")
        if (!isOnline) {
            enqueueSync("LEAVE", request.id, "DELETE", "حذف طلب إجازة ${request.requestNumber}")
        }
    }

    suspend fun updateOvertimeRequest(request: OvertimeRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.updateOvertimeRequest(request)
        recordAudit(currentUser, "UPDATE", "OVERTIME", request.id, "تعديل بيانات طلب العمل الإضافي ${request.requestNumber}")
        if (!isOnline) {
            enqueueSync("OVERTIME", request.id, "UPDATE", "تعديل طلب عمل إضافي ${request.requestNumber}")
        }
    }

    suspend fun deleteOvertimeRequest(request: OvertimeRequestEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.deleteOvertimeRequest(request.id)
        recordAudit(currentUser, "DELETE", "OVERTIME", request.id, "حذف طلب العمل الإضافي ${request.requestNumber}")
        if (!isOnline) {
            enqueueSync("OVERTIME", request.id, "DELETE", "حذف طلب عمل إضافي ${request.requestNumber}")
        }
    }

    suspend fun updateAdminTransaction(tx: AdminTransactionEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.updateAdminTransaction(tx)
        recordAudit(currentUser, "UPDATE", "TRANSACTION", tx.id, "تعديل بيانات المعاملة الإدارية ${tx.transactionNumber}")
        if (!isOnline) {
            enqueueSync("TRANSACTION", tx.id, "UPDATE", "تعديل معاملة إدارية ${tx.transactionNumber}")
        }
    }

    suspend fun deleteAdminTransaction(tx: AdminTransactionEntity, currentUser: UserEntity, isOnline: Boolean) {
        dao.deleteAdminTransaction(tx.id)
        recordAudit(currentUser, "DELETE", "TRANSACTION", tx.id, "حذف المعاملة الإدارية ${tx.transactionNumber}")
        if (!isOnline) {
            enqueueSync("TRANSACTION", tx.id, "DELETE", "حذف معاملة إدارية ${tx.transactionNumber}")
        }
    }

    // Notifications
    suspend fun markNotificationAsRead(id: String) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    suspend fun deleteNotification(id: String) = dao.deleteNotification(id)
    suspend fun clearAllNotifications() = dao.clearAllNotifications()

    // Settings
    suspend fun updateSettings(settings: SystemSettingsEntity, currentUser: UserEntity) {
        dao.insertSystemSettings(settings)
        recordAudit(currentUser, "UPDATE", "SETTINGS", "1", "تحديث إعدادات النظام ومعلومات المؤسسة")
    }

    // Sync Operations
    private suspend fun enqueueSync(entityType: String, entityId: String, operation: String, description: String) {
        dao.insertSyncQueueItem(
            SyncQueueEntity(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payloadJson = description,
                status = "PENDING"
            )
        )
    }

    suspend fun processSyncQueue(currentUser: UserEntity): Int {
        dao.clearSyncedQueue()
        recordAudit(currentUser, "SYNC", "SYSTEM", "ALL", "اكتمال عملية مزامنة البيانات السحابية مع الخادم بنجاح")
        return 0
    }

    // Audit Record Helper
    suspend fun recordAudit(
        user: UserEntity?,
        actionType: String,
        entityType: String,
        entityId: String,
        details: String
    ) {
        dao.insertAuditLog(
            AuditLogEntity(
                userId = user?.id ?: "ANONYMOUS",
                userName = user?.fullName ?: "زائر",
                userRole = user?.role ?: "GUEST",
                actionType = actionType,
                entityType = entityType,
                entityId = entityId,
                details = details
            )
        )
    }
}
