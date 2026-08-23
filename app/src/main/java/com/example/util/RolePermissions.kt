package com.example.util

import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AppScreen

data class PermissionItem(
    val key: String,
    val titleAr: String,
    val descriptionAr: String,
    val categoryAr: String,
    val allowedRoles: List<String>
)

data class RoleDefinition(
    val roleKey: String,
    val titleAr: String,
    val descriptionAr: String,
    val clearanceLevel: Int, // 1 (Highest) to 6 (Employee)
    val colorHex: Long
)

object RolePermissions {

    val roleDefinitions: List<RoleDefinition> = listOf(
        RoleDefinition(
            roleKey = "SuperAdmin",
            titleAr = "مدير النظام الأعلى (Super Admin)",
            descriptionAr = "صلاحيات شاملة ومطلقة لكافة شاشات النظام، العمليات الحساسة، الحذف الجذري والتحكم الأمني",
            clearanceLevel = 1,
            colorHex = 0xFF7C3AED
        ),
        RoleDefinition(
            roleKey = "SystemAdmin",
            titleAr = "مدير النظام التقني (System Administrator)",
            descriptionAr = "إدارة المستخدمين، المزامنة السحابية، النسخ الاحتياطي، وسجلات التدقيق وإعدادات الخادم",
            clearanceLevel = 2,
            colorHex = 0xFF2563EB
        ),
        RoleDefinition(
            roleKey = "GeneralManager",
            titleAr = "المدير العام (General Manager)",
            descriptionAr = "الاعتماد النهائي للطلبات والمعاملات، استعراض لوحات المؤشرات والتقارير المالية والتعاميم",
            clearanceLevel = 2,
            colorHex = 0xFF0D9488
        ),
        RoleDefinition(
            roleKey = "HrManager",
            titleAr = "مدير الموارد البشرية (HR Manager)",
            descriptionAr = "إدارة ملفات الموظفين، الأقسام، اعتماد الإجازات والعمل الإضافي، توقيع الجزاءات وإصدار التقارير",
            clearanceLevel = 3,
            colorHex = 0xFFEA580C
        ),
        RoleDefinition(
            roleKey = "DepartmentHead",
            titleAr = "رئيس قسم (Department Head)",
            descriptionAr = "متابعة موظفي القسم، التوصية باعتماد الإجازات والإضافي، رفع طلبات الخدمات والمعاملات",
            clearanceLevel = 4,
            colorHex = 0xFF0284C7
        ),
        RoleDefinition(
            roleKey = "HrEmployee",
            titleAr = "أخصائي موارد بشرية (HR Specialist)",
            descriptionAr = "إدخال بيانات الموظفين وتحديثها، جدولة الخدمات، متابعة الإجازات والوثائق دون صلاحية الحذف",
            clearanceLevel = 5,
            colorHex = 0xFF16A34A
        ),
        RoleDefinition(
            roleKey = "Employee",
            titleAr = "موظف (Employee)",
            descriptionAr = "بوابة الخدمة الذاتية: تقديم طلبات الإجازات، تسجيل الإضافي، طلب الوجبات والمواصلات ومتابعة الإشعارات",
            clearanceLevel = 6,
            colorHex = 0xFF64748B
        )
    )

    val allPermissions: List<PermissionItem> = listOf(
        // Security & User Management
        PermissionItem(
            key = "USERS_MANAGE",
            titleAr = "إدارة المستخدمين والحسابات",
            descriptionAr = "إنشاء مستخدمين جدد، تعديل الصلاحيات، إيقاف الحسابات، وتعيين كلمات المرور",
            categoryAr = "الحوكمة والأمان",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin")
        ),
        PermissionItem(
            key = "USERS_DELETE",
            titleAr = "حذف حسابات المستخدمين",
            descriptionAr = "الحذف النهائي لحسابات الدخول من النظام مع تسجيل العملية في سجل التدقيق",
            categoryAr = "الحوكمة والأمان",
            allowedRoles = listOf("SuperAdmin")
        ),
        PermissionItem(
            key = "AUDIT_VIEW_RESTORE",
            titleAr = "سجل التدقيق والنسخ الاحتياطي",
            descriptionAr = "استعراض السجلات الأمنية والعمليات وتوليد واستعادة النسخ الاحتياطية",
            categoryAr = "الحوكمة والأمان",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin")
        ),
        PermissionItem(
            key = "SYSTEM_SETTINGS",
            titleAr = "إعدادات النظام ومعايير الجودة",
            descriptionAr = "تعديل سياسات العمل، أوقات الدوام، وتكاملات الربط السحابي",
            categoryAr = "الحوكمة والأمان",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin")
        ),

        // Departments & Organization
        PermissionItem(
            key = "DEPARTMENTS_MANAGE",
            titleAr = "إدارة الهيكل التنظيمي والأقسام",
            descriptionAr = "إضافة وتعديل الأقسام وتحديد المدراء والميزانيات التقديرية",
            categoryAr = "الهيكل التنظيمي",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "HrManager")
        ),
        PermissionItem(
            key = "DEPARTMENTS_DELETE",
            titleAr = "حذف الأقسام التنظيمية",
            descriptionAr = "إلغاء وأرشفة الأقسام من الهيكل الإداري",
            categoryAr = "الهيكل التنظيمي",
            allowedRoles = listOf("SuperAdmin", "HrManager")
        ),

        // Employees Management
        PermissionItem(
            key = "EMPLOYEES_VIEW",
            titleAr = "عرض سجلات الموظفين",
            descriptionAr = "استعراض القوائم والبيانات التعريفية للموظفين",
            categoryAr = "إدارة الموظفين",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "GeneralManager", "HrManager", "DepartmentHead", "HrEmployee")
        ),
        PermissionItem(
            key = "EMPLOYEES_CREATE_EDIT",
            titleAr = "إضافة وتعديل بيانات الموظفين",
            descriptionAr = "إدخال موظف جديد، تحديث البيانات الوظيفية، المستندات الرسمية، والعقود",
            categoryAr = "إدارة الموظفين",
            allowedRoles = listOf("SuperAdmin", "HrManager", "HrEmployee")
        ),
        PermissionItem(
            key = "EMPLOYEES_DELETE",
            titleAr = "حذف وإنهاء سجلات الموظفين",
            descriptionAr = "حذف الموظف نهائياً من قاعدة البيانات وسجل الموارد البشرية",
            categoryAr = "إدارة الموظفين",
            allowedRoles = listOf("SuperAdmin", "HrManager")
        ),
        PermissionItem(
            key = "SALARY_VIEW",
            titleAr = "الاطلاع على الرواتب والبيانات المالية الحساسة",
            descriptionAr = "عرض الرواتب الأساسية، البدلات، المكافآت والخصومات للموظفين",
            categoryAr = "إدارة الموظفين",
            allowedRoles = listOf("SuperAdmin", "GeneralManager", "HrManager")
        ),

        // Leaves
        PermissionItem(
            key = "LEAVES_CREATE",
            titleAr = "تقديم طلبات الإجازات",
            descriptionAr = "إنشاء طلب إجازة سنوية، مرضية، طارئة أو رسمية",
            categoryAr = "الإجازات والغياب",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "GeneralManager", "HrManager", "DepartmentHead", "HrEmployee", "Employee")
        ),
        PermissionItem(
            key = "LEAVES_APPROVE",
            titleAr = "اعتماد ومراجعة طلبات الإجازات",
            descriptionAr = "مراجعة أرصدة الإجازات، الموافقة، الرفض، وإعادة الطلب مع الملاحظات",
            categoryAr = "الإجازات والغياب",
            allowedRoles = listOf("SuperAdmin", "GeneralManager", "HrManager", "DepartmentHead")
        ),
        PermissionItem(
            key = "LEAVES_DELETE",
            titleAr = "حذف وإلغاء طلبات الإجازات",
            descriptionAr = "إلغاء أو حذف طلب إجازة مسجل بالنظام",
            categoryAr = "الإجازات والغياب",
            allowedRoles = listOf("SuperAdmin", "HrManager")
        ),

        // Overtime
        PermissionItem(
            key = "OVERTIME_CREATE",
            titleAr = "تسجيل ساعات العمل الإضافي",
            descriptionAr = "إدخال طلب تكليف بساعات عمل إضافية مع تحديد طبيعة اليوم",
            categoryAr = "العمل الإضافي",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "GeneralManager", "HrManager", "DepartmentHead", "HrEmployee", "Employee")
        ),
        PermissionItem(
            key = "OVERTIME_APPROVE",
            titleAr = "اعتماد واحتساب العمل الإضافي",
            descriptionAr = "تدقيق ساعات الإضافي، وتطبيق نسب المضاعفة والاعتماد النهائي",
            categoryAr = "العمل الإضافي",
            allowedRoles = listOf("SuperAdmin", "GeneralManager", "HrManager", "DepartmentHead")
        ),
        PermissionItem(
            key = "OVERTIME_DELETE",
            titleAr = "حذف سجلات العمل الإضافي",
            descriptionAr = "حذف تكليف عمل إضافي من السجلات",
            categoryAr = "العمل الإضافي",
            allowedRoles = listOf("SuperAdmin", "HrManager")
        ),

        // Penalties
        PermissionItem(
            key = "PENALTIES_VIEW",
            titleAr = "عرض سجل الجزاءات والمخالفات",
            descriptionAr = "استعراض وقائع المخالفات والعقوبات التأديبية المسجلة",
            categoryAr = "الجزاءات والانضباط",
            allowedRoles = listOf("SuperAdmin", "GeneralManager", "HrManager", "DepartmentHead")
        ),
        PermissionItem(
            key = "PENALTIES_CREATE",
            titleAr = "إيقاع وتسجيل الجزاءات التأديبية",
            descriptionAr = "تسجيل لفت نظر، إنذار، خصم راتب، أو إيقاف وفق لائحة العمل",
            categoryAr = "الجزاءات والانضباط",
            allowedRoles = listOf("SuperAdmin", "HrManager")
        ),
        PermissionItem(
            key = "PENALTIES_DELETE",
            titleAr = "إلغاء وحذف الجزاءات التأديبية",
            descriptionAr = "حذف المخالفة أو رد الاعتبار للموظف وإلغاء الخصم",
            categoryAr = "الجزاءات والانضباط",
            allowedRoles = listOf("SuperAdmin", "HrManager")
        ),

        // Logistics & Services
        PermissionItem(
            key = "SERVICES_CREATE",
            titleAr = "طلب خدمات الإعاشة والمواصلات",
            descriptionAr = "تقديم طلب وجبات عمل أو حجز وسيلة نقل أو حافلة",
            categoryAr = "الخدمات واللوجستيات",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "GeneralManager", "HrManager", "DepartmentHead", "HrEmployee", "Employee")
        ),
        PermissionItem(
            key = "SERVICES_MANAGE",
            titleAr = "إدارة واعتماد الخدمات اللوجستية",
            descriptionAr = "الموافقة على طلبات التموين، تعيين السائقين، وتغيير حالات التسليم",
            categoryAr = "الخدمات واللوجستيات",
            allowedRoles = listOf("SuperAdmin", "HrManager", "DepartmentHead", "HrEmployee")
        ),

        // Reports & Exports
        PermissionItem(
            key = "REPORTS_VIEW",
            titleAr = "الاطلاع على التقارير التشغيلية",
            descriptionAr = "استعراض تقارير الحضور، الإجازات، وإحصائيات القوى العاملة",
            categoryAr = "التقارير والمؤشرات",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "GeneralManager", "HrManager", "DepartmentHead", "HrEmployee")
        ),
        PermissionItem(
            key = "FINANCIAL_REPORTS_VIEW",
            titleAr = "الاطلاع على التقارير المالية والرواتب",
            descriptionAr = "استعراض تكاليف العمل الإضافي، مسيرات الرواتب، والبدلات",
            categoryAr = "التقارير والمؤشرات",
            allowedRoles = listOf("SuperAdmin", "GeneralManager", "HrManager")
        ),
        PermissionItem(
            key = "PDF_EXPORT",
            titleAr = "تصدير لوحة المؤشرات والتقارير إلى PDF",
            descriptionAr = "توليد وتصدير ملفات PDF الرسمية للتقارير ومؤشرات الأداء",
            categoryAr = "التقارير والمؤشرات",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "HrManager", "GeneralManager", "DepartmentHead")
        ),
        PermissionItem(
            key = "CIRCULARS_BROADCAST",
            titleAr = "إصدار وبث التعاميم والتنبيهات الإدارية",
            descriptionAr = "بث قرارات وتعاميم رسمية وإرسال تنبيهات استباقية لكافة الموظفين",
            categoryAr = "التعاميم والتنبيهات",
            allowedRoles = listOf("SuperAdmin", "SystemAdmin", "GeneralManager", "HrManager")
        )
    )

    fun hasPermission(user: UserEntity?, permissionKey: String): Boolean {
        if (user == null) return false
        val role = user.role
        if (role == "SuperAdmin" || role == "مدير النظام الأعلى" || role == "مدير النظام" || role == "SystemAdmin") return true
        val item = allPermissions.find { it.key == permissionKey } ?: return false
        return item.allowedRoles.any { it.equals(role, ignoreCase = true) }
    }

    /**
     * Determines whether the given user role is authorized to view a specific AppScreen.
     */
    fun canAccessScreen(user: UserEntity?, screen: AppScreen): Boolean {
        if (user == null) return screen == AppScreen.LOGIN
        val role = user.role
        if (role == "SuperAdmin" || role == "SystemAdmin") return true

        return when (screen) {
            AppScreen.LOGIN -> true
            AppScreen.DASHBOARD -> true
            AppScreen.EMPLOYEES -> hasPermission(user, "EMPLOYEES_VIEW")
            AppScreen.LEAVES -> true
            AppScreen.OVERTIME -> true
            AppScreen.PENALTIES -> hasPermission(user, "PENALTIES_VIEW")
            AppScreen.SERVICES -> true
            AppScreen.APPROVALS -> hasPermission(user, "LEAVES_APPROVE") || hasPermission(user, "OVERTIME_APPROVE")
            AppScreen.NOTIFICATIONS -> true
            AppScreen.REPORTS -> hasPermission(user, "REPORTS_VIEW")
            AppScreen.USERS_ROLES -> hasPermission(user, "USERS_MANAGE") || role in listOf("GeneralManager", "HrManager")
            AppScreen.SYNC_CENTER -> true
            AppScreen.AUDIT_BACKUP -> hasPermission(user, "AUDIT_VIEW_RESTORE")
            AppScreen.SETTINGS -> hasPermission(user, "SYSTEM_SETTINGS")
        }
    }

    // Specific Action Guards
    fun canManageUsers(user: UserEntity?): Boolean = hasPermission(user, "USERS_MANAGE")
    fun canDeleteUser(user: UserEntity?): Boolean = hasPermission(user, "USERS_DELETE")

    fun canManageDepartments(user: UserEntity?): Boolean = hasPermission(user, "DEPARTMENTS_MANAGE")
    fun canDeleteDepartment(user: UserEntity?): Boolean = hasPermission(user, "DEPARTMENTS_DELETE")

    fun canViewEmployees(user: UserEntity?): Boolean = hasPermission(user, "EMPLOYEES_VIEW")
    fun canCreateEditEmployee(user: UserEntity?): Boolean = hasPermission(user, "EMPLOYEES_CREATE_EDIT")
    fun canDeleteEmployee(user: UserEntity?): Boolean = hasPermission(user, "EMPLOYEES_DELETE")
    fun canViewSalary(user: UserEntity?): Boolean = hasPermission(user, "SALARY_VIEW")

    fun canCreateLeave(user: UserEntity?): Boolean = hasPermission(user, "LEAVES_CREATE")
    fun canApproveLeave(user: UserEntity?): Boolean = hasPermission(user, "LEAVES_APPROVE")
    fun canDeleteLeave(user: UserEntity?): Boolean = hasPermission(user, "LEAVES_DELETE")

    fun canCreateOvertime(user: UserEntity?): Boolean = hasPermission(user, "OVERTIME_CREATE")
    fun canApproveOvertime(user: UserEntity?): Boolean = hasPermission(user, "OVERTIME_APPROVE")
    fun canDeleteOvertime(user: UserEntity?): Boolean = hasPermission(user, "OVERTIME_DELETE")

    fun canViewPenalties(user: UserEntity?): Boolean = hasPermission(user, "PENALTIES_VIEW")
    fun canCreatePenalty(user: UserEntity?): Boolean = hasPermission(user, "PENALTIES_CREATE")
    fun canDeletePenalty(user: UserEntity?): Boolean = hasPermission(user, "PENALTIES_DELETE")

    fun canCreateService(user: UserEntity?): Boolean = hasPermission(user, "SERVICES_CREATE")
    fun canManageServices(user: UserEntity?): Boolean = hasPermission(user, "SERVICES_MANAGE")
    fun canCreateTransaction(user: UserEntity?): Boolean = canCreateService(user)
    fun canApproveTransaction(user: UserEntity?): Boolean = canManageServices(user)

    fun canViewReports(user: UserEntity?): Boolean = hasPermission(user, "REPORTS_VIEW")
    fun canViewFinancialReports(user: UserEntity?): Boolean = hasPermission(user, "FINANCIAL_REPORTS_VIEW")

    fun canExportPdf(user: UserEntity?): Boolean = hasPermission(user, "PDF_EXPORT")
    fun canBroadcastCirculars(user: UserEntity?): Boolean = hasPermission(user, "CIRCULARS_BROADCAST")
    fun canSendProactiveAlerts(user: UserEntity?): Boolean = hasPermission(user, "CIRCULARS_BROADCAST") || canApproveLeave(user)
    fun canAccessAuditAndBackup(user: UserEntity?): Boolean = hasPermission(user, "AUDIT_VIEW_RESTORE")
    fun canAccessSettings(user: UserEntity?): Boolean = hasPermission(user, "SYSTEM_SETTINGS")
    fun canDelete(user: UserEntity?): Boolean = canDeleteEmployee(user) || canDeleteLeave(user) || canDeleteOvertime(user)
}
