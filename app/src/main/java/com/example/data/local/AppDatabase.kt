package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        EmployeeEntity::class,
        LeaveRequestEntity::class,
        OvertimeRequestEntity::class,
        PenaltyEntity::class,
        MealRequestEntity::class,
        TransportationRequestEntity::class,
        AdminTransactionEntity::class,
        ApprovalHistoryEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        SyncQueueEntity::class,
        SystemSettingsEntity::class,
        DepartmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hrDao(): HrDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hr_erp_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
