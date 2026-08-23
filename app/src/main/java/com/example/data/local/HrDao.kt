package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HrDao {
    // Users
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    // Employees
    @Query("SELECT * FROM employees ORDER BY employeeNumber ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployee(id: String)

    @Query("SELECT COUNT(*) FROM employees")
    fun getEmployeeCount(): Flow<Int>

    // Leave Requests
    @Query("SELECT * FROM leave_requests ORDER BY createdAt DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY createdAt DESC")
    fun getLeaveRequestsByEmployee(employeeId: String): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE id = :id LIMIT 1")
    suspend fun getLeaveRequestById(id: String): LeaveRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequestEntity)

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequestEntity)

    @Query("DELETE FROM leave_requests WHERE id = :id")
    suspend fun deleteLeaveRequest(id: String)

    // Overtime Requests
    @Query("SELECT * FROM overtime_requests ORDER BY createdAt DESC")
    fun getAllOvertimeRequests(): Flow<List<OvertimeRequestEntity>>

    @Query("SELECT * FROM overtime_requests WHERE employeeId = :employeeId ORDER BY createdAt DESC")
    fun getOvertimeRequestsByEmployee(employeeId: String): Flow<List<OvertimeRequestEntity>>

    @Query("SELECT * FROM overtime_requests WHERE id = :id LIMIT 1")
    suspend fun getOvertimeRequestById(id: String): OvertimeRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOvertimeRequest(request: OvertimeRequestEntity)

    @Update
    suspend fun updateOvertimeRequest(request: OvertimeRequestEntity)

    @Query("DELETE FROM overtime_requests WHERE id = :id")
    suspend fun deleteOvertimeRequest(id: String)

    // Penalties
    @Query("SELECT * FROM penalties ORDER BY createdAt DESC")
    fun getAllPenalties(): Flow<List<PenaltyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenalty(penalty: PenaltyEntity)

    @Update
    suspend fun updatePenalty(penalty: PenaltyEntity)

    @Query("DELETE FROM penalties WHERE id = :id")
    suspend fun deletePenalty(id: String)

    // Meals
    @Query("SELECT * FROM meal_requests ORDER BY createdAt DESC")
    fun getAllMealRequests(): Flow<List<MealRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealRequest(request: MealRequestEntity)

    @Update
    suspend fun updateMealRequest(request: MealRequestEntity)

    @Query("DELETE FROM meal_requests WHERE id = :id")
    suspend fun deleteMealRequest(id: String)

    // Transportation
    @Query("SELECT * FROM transportation_requests ORDER BY createdAt DESC")
    fun getAllTransportationRequests(): Flow<List<TransportationRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransportationRequest(request: TransportationRequestEntity)

    @Update
    suspend fun updateTransportationRequest(request: TransportationRequestEntity)

    @Query("DELETE FROM transportation_requests WHERE id = :id")
    suspend fun deleteTransportationRequest(id: String)

    // Admin Transactions
    @Query("SELECT * FROM admin_transactions ORDER BY createdAt DESC")
    fun getAllAdminTransactions(): Flow<List<AdminTransactionEntity>>

    @Query("SELECT * FROM admin_transactions WHERE id = :id LIMIT 1")
    suspend fun getAdminTransactionById(id: String): AdminTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminTransaction(tx: AdminTransactionEntity)

    @Update
    suspend fun updateAdminTransaction(tx: AdminTransactionEntity)

    @Query("DELETE FROM admin_transactions WHERE id = :id")
    suspend fun deleteAdminTransaction(id: String)

    // Approval History
    @Query("SELECT * FROM approval_history WHERE requestId = :requestId ORDER BY timestamp ASC")
    fun getApprovalHistory(requestId: String): Flow<List<ApprovalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalHistory(history: ApprovalHistoryEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 300")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Sync Queue
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingSyncQueue(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun getAllSyncQueue(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueueItem(item: SyncQueueEntity)

    @Update
    suspend fun updateSyncQueueItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncQueueItem(id: String)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun clearSyncedQueue()

    // System Settings
    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    fun getSystemSettings(): Flow<SystemSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemSettings(settings: SystemSettingsEntity)

    // Departments
    @Query("SELECT * FROM departments ORDER BY departmentCode ASC")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    suspend fun getDepartmentById(id: String): DepartmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity)

    @Update
    suspend fun updateDepartment(department: DepartmentEntity)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartment(id: String)

    @Query("DELETE FROM departments")
    suspend fun deleteAllDepartments()

    // Raw Wipe for DB restore
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    @Query("DELETE FROM employees")
    suspend fun deleteAllEmployees()
    @Query("DELETE FROM leave_requests")
    suspend fun deleteAllLeaves()
    @Query("DELETE FROM overtime_requests")
    suspend fun deleteAllOvertimes()
    @Query("DELETE FROM penalties")
    suspend fun deleteAllPenalties()
    @Query("DELETE FROM meal_requests")
    suspend fun deleteAllMeals()
    @Query("DELETE FROM transportation_requests")
    suspend fun deleteAllTransportation()
    @Query("DELETE FROM admin_transactions")
    suspend fun deleteAllTransactions()
}
