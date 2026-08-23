package com.example.data.model

enum class UserRole(val titleAr: String) {
    SuperAdmin("مدير النظام الأعلى (Super Admin)"),
    SystemAdmin("مدير النظام (System Administrator)"),
    HrManager("مدير الموارد البشرية (HR Manager)"),
    DepartmentHead("رئيس قسم (Department Head)"),
    GeneralManager("المدير العام (General Manager)"),
    HrEmployee("أخصائي موارد بشرية (HR Specialist)"),
    Employee("موظف (Employee)")
}

enum class AccountStatus(val titleAr: String) {
    Active("نشط"),
    Inactive("غير نشط"),
    Suspended("موقوف"),
    Blocked("محظور")
}

enum class EmployeeStatus(val titleAr: String) {
    Active("على رأس العمل"),
    OnLeave("في إجازة"),
    Suspended("موقوف"),
    Transferred("منقول"),
    Resigned("مستقيل"),
    Terminated("منتهي الخدمة")
}

enum class LeaveType(val titleAr: String, val defaultDays: Int) {
    Annual("إجازة سنوية", 30),
    Sick("إجازة مرضية", 15),
    Emergency("إجازة طارئة", 5),
    Unpaid("إجازة بدون راتب", 0),
    Official("مهمة رسمية", 0),
    Marriage("إجازة زواج", 5),
    Bereavement("إجازة وفاة", 3),
    Maternity("إجازة أمومة", 70),
    Paternity("إجازة أبوة", 3),
    Other("إجازة أخرى", 0)
}

enum class RequestStatus(val titleAr: String) {
    Draft("مسودة"),
    Submitted("تم التقديم"),
    PendingDept("بانتظار موافقة رئيس القسم"),
    DeptApproved("معتمد من رئيس القسم"),
    PendingHr("بانتظار موافقة الموارد البشرية"),
    HrApproved("معتمد من الموارد البشرية"),
    PendingGm("بانتظار موافقة المدير العام"),
    Approved("معتمد نهائياً"),
    Rejected("مرفوض"),
    Returned("معاد للمراجعة"),
    Cancelled("ملغي"),
    Closed("مغلق")
}

enum class OvertimeType(val titleAr: String, val multiplier: Double) {
    NormalDay("يوم عمل عادي (1.5x)", 1.5),
    Weekend("عطلة نهاية أسبوع (2.0x)", 2.0),
    OfficialHoliday("عطلة رسمية / أعياد (2.5x)", 2.5)
}

enum class Severity(val titleAr: String) {
    Low("بسيطة"),
    Medium("متوسطة"),
    High("جسيمة"),
    Critical("خطيرة جداً")
}

enum class PenaltyAction(val titleAr: String) {
    Notice("لفت نظر"),
    FirstWarning("إنذار كتابي أول"),
    FinalWarning("إنذار نهائي"),
    SalaryDeduction("خصم من الراتب"),
    Suspension("إيقاف مؤقت عن العمل"),
    AdministrativeAction("إجراء إداري تأديبي")
}

enum class MealType(val titleAr: String, val defaultPrice: Double) {
    Breakfast("إفطار عمل", 25.0),
    Lunch("غداء عمل", 45.0),
    Dinner("عشاء عمل إضافي", 40.0),
    SnackPack("وجبة خفيفة / ضيافة", 20.0)
}

enum class TransportType(val titleAr: String) {
    CompanyBus("حافلة الشركة"),
    Taxi("سيارة أجرة / تاكسي"),
    RentalCar("سيارة مستأجرة"),
    Flight("تذكرة طيران"),
    Allowance("بدل مواصلات نقدي")
}

enum class Priority(val titleAr: String) {
    Low("منخفضة"),
    Normal("عادية"),
    High("عالية"),
    Urgent("عاجلة جداً")
}

enum class SyncState(val titleAr: String) {
    PENDING("قيد الانتظار"),
    SYNCED("تمت المزامنة"),
    FAILED("فشل"),
    CONFLICT("تعارض بيانات")
}

enum class NotificationCategory(val titleAr: String) {
    REQUEST("طلب جديد"),
    APPROVAL("تحديث موافقة"),
    ALERT("تنبيه هام"),
    SYSTEM("إشعار نظام")
}
