package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val employeeId: String = "",
    val department: String,
    val position: String,
    val role: String, // from UserRole
    val status: String = "Active", // from AccountStatus
    val mustChangePassword: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val employeeNumber: String,
    val fullName: String,
    val nationalId: String,
    val department: String,
    val division: String,
    val position: String,
    val grade: String,
    val contractType: String,
    val hireDate: String,
    val contractEndDate: String,
    val status: String = "Active", // from EmployeeStatus
    val phone: String,
    val email: String,
    val directManager: String,
    val workLocation: String,
    val branch: String,
    val emergencyContact: String,
    val salaryBase: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val annualLeaveBalance: Int = 30,
    val sickLeaveBalance: Int = 15,
    val emergencyLeaveBalance: Int = 5,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestNumber: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val leaveType: String, // from LeaveType
    val startDate: String,
    val endDate: String,
    val daysCount: Int,
    val reason: String,
    val previousBalance: Int,
    val usedBalance: Int,
    val remainingBalance: Int,
    val substituteEmployee: String = "",
    val status: String = "Submitted", // from RequestStatus
    val currentStepRole: String = "DepartmentHead",
    val notes: String = "",
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "overtime_requests")
data class OvertimeRequestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestNumber: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val date: String,
    val workDayType: String = "NormalDay",
    val startTime: String,
    val endTime: String,
    val hours: Double,
    val overtimeType: String, // from OvertimeType
    val reason: String,
    val projectOrTask: String,
    val calculatedAmount: Double,
    val status: String = "Submitted",
    val currentStepRole: String = "DepartmentHead",
    val notes: String = "",
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "penalties")
data class PenaltyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val penaltyNumber: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val incidentDate: String,
    val violationType: String,
    val description: String,
    val severity: String, // from Severity
    val actionType: String, // from PenaltyAction
    val deductionAmount: Double = 0.0,
    val suspensionDays: Int = 0,
    val legalReference: String = "",
    val investigationNotes: String = "",
    val employeeStatement: String = "",
    val status: String = "Approved",
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_requests")
data class MealRequestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestNumber: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val date: String,
    val mealType: String, // from MealType
    val count: Int,
    val totalCost: Double,
    val vendor: String,
    val reason: String,
    val status: String = "Approved",
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transportation_requests")
data class TransportationRequestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestNumber: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val date: String,
    val route: String,
    val departurePoint: String,
    val destination: String,
    val transportType: String, // from TransportType
    val tripsCount: Int,
    val totalCost: Double,
    val vendorOrDriver: String,
    val reason: String,
    val status: String = "Approved",
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_transactions")
data class AdminTransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionNumber: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val date: String,
    val subject: String,
    val description: String,
    val priority: String, // from Priority
    val targetDepartment: String,
    val status: String = "Submitted",
    val notes: String = "",
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "approval_history")
data class ApprovalHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val requestType: String, // LEAVE, OVERTIME, PENALTY, MEAL, TRANSPORT, TRANSACTION
    val action: String, // SUBMIT, DEPT_APPROVE, HR_APPROVE, GM_APPROVE, REJECT, RETURN
    val decisionMakerId: String,
    val decisionMakerName: String,
    val decisionMakerRole: String,
    val decisionDate: String,
    val comments: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val recipientRole: String = "ALL", // ALL, HR, DEPT_HEAD, GM, or specific employee
    val title: String,
    val message: String,
    val category: String, // from NotificationCategory
    val targetRequestId: String = "",
    val targetRequestType: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val userRole: String,
    val actionType: String, // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, APPROVE, REJECT, SYNC, BACKUP, RESTORE, EXPORT
    val entityType: String,
    val entityId: String,
    val details: String,
    val deviceId: String = "DEVICE-ANDROID-POS",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val payloadJson: String,
    val status: String = "PENDING",
    val attempts: Int = 0,
    val errorMessage: String = "",
    val lastAttemptAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "مجموعة الخليج للخدمات المؤسسية والتطوير",
    val companyCommercialId: String = "CR-1010893421",
    val currency: String = "ريال يمني (YER)",
    val weeklyWorkDays: Int = 5,
    val dailyWorkingHours: Double = 8.0,
    val overtimeStandardRate: Double = 1.5,
    val overtimeWeekendRate: Double = 2.0,
    val overtimeHolidayRate: Double = 2.5,
    val autoSyncEnabled: Boolean = true,
    val supervisorName: String = "أ.أحمد العمري",
    val lastBackupDate: String = "2026-08-21 14:00"
)

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val departmentCode: String,
    val nameAr: String,
    val managerName: String,
    val location: String = "المقر الرئيسي",
    val employeeCount: Int = 0,
    val estimatedMonthlyBudget: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

