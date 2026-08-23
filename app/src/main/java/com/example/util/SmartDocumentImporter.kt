package com.example.util

import com.example.data.model.LeaveType
import com.example.data.model.OvertimeType
import com.example.data.model.Priority
import com.example.data.model.UserRole

object SmartDocumentImporter {

    data class ImportedUserData(
        val username: String,
        val fullName: String,
        val email: String,
        val phone: String,
        val department: String,
        val role: UserRole,
        val position: String,
        val documentSource: String
    )

    data class ImportedDepartmentData(
        val departmentCode: String,
        val nameAr: String,
        val managerName: String,
        val location: String,
        val employeeCount: Int,
        val estimatedMonthlyBudget: Double,
        val notes: String,
        val documentSource: String
    )

    data class ImportedLeaveData(
        val leaveType: LeaveType,
        val startDate: String,
        val endDate: String,
        val daysCount: Int,
        val reason: String,
        val substitute: String,
        val documentSource: String
    )

    data class ImportedOvertimeData(
        val date: String,
        val startTime: String,
        val endTime: String,
        val hours: Double,
        val overtimeType: OvertimeType,
        val taskDescription: String,
        val outputOrDeliverable: String,
        val documentSource: String
    )

    data class ImportedTransactionData(
        val subject: String,
        val description: String,
        val priority: Priority,
        val targetDepartment: String,
        val documentSource: String
    )

    // Preset PDF / Document Templates for Quick Import & Auto-Filling
    val sampleUserPdfTemplates = listOf(
        ImportedUserData(
            username = "majed_qa",
            fullName = "ماجد عبدالله القاضي",
            email = "majed.qa@company.com",
            phone = "+966509988771",
            department = "الدعم الفني وضمان الجودة ISO",
            role = UserRole.Employee,
            position = "أخصائي ضمان جودة واختبار نظم",
            documentSource = "ملف PDF: استمارة_توظيف_ماجد_القاضي_2026.pdf"
        ),
        ImportedUserData(
            username = "hind_hr",
            fullName = "هند خالد الراشد",
            email = "hind.hr@company.com",
            phone = "+966504455667",
            department = "الموارد البشرية والشؤون الإدارية",
            role = UserRole.HrEmployee,
            position = "مسؤولة استقطاب الكفاءات والتدريب",
            documentSource = "ملف PDF: بيانات_الموظف_الجديد_هند_الراشد.pdf"
        ),
        ImportedUserData(
            username = "tariq_ops",
            fullName = "م. طارق سعيد الزهراني",
            email = "tariq.ops@company.com",
            phone = "+966503322119",
            department = "العمليات والتشغيل والخدمات",
            role = UserRole.DepartmentHead,
            position = "مدير تشغيل العمليات المساندة",
            documentSource = "ملف PDF: قرار_تعيين_رئيس_قسم_العمليات.pdf"
        )
    )

    val sampleDepartmentPdfTemplates = listOf(
        ImportedDepartmentData(
            departmentCode = "CYBER",
            nameAr = "الأمن السيبراني وحماية البيانات",
            managerName = "م. عبدالعزيز ناصر الفهيد",
            location = "المبنى الرئيسي - مركز البيانات",
            employeeCount = 6,
            estimatedMonthlyBudget = 550000.0,
            notes = "حماية البنية التحتية، التدقيق الأمني، والاستجابة للحوادث الرقمية",
            documentSource = "ملف PDF: هيكل_تنظيمي_الأمن_السيبراني_2026.pdf"
        ),
        ImportedDepartmentData(
            departmentCode = "LOGIST",
            nameAr = "الخدمات اللوجستية وسلاسل الإمداد",
            managerName = "أ. بدر فهد الشريف",
            location = "المستودعات المركزية - المنطقة الصناعية",
            employeeCount = 14,
            estimatedMonthlyBudget = 680000.0,
            notes = "إدارة المخازن، التوريد، التخليص الجمركي وعقود الموردين",
            documentSource = "ملف PDF: ميثاق_قسم_الخدمات_اللوجستية.pdf"
        ),
        ImportedDepartmentData(
            departmentCode = "INNOV",
            nameAr = "مركز الابتكار والذكاء الاصطناعي",
            managerName = "د. لمى سليمان التميمي",
            location = "مختبر الابتكار - الطابق الرابع",
            employeeCount = 8,
            estimatedMonthlyBudget = 820000.0,
            notes = "أتمتة الأعمال وتطبيق نماذج الذكاء الاصطناعي المؤسسي",
            documentSource = "ملف PDF: اعتماد_استحداث_مركز_الابتكار.pdf"
        )
    )

    val sampleLeavePdfTemplates = listOf(
        ImportedLeaveData(
            leaveType = LeaveType.Annual,
            startDate = "2026-09-10",
            endDate = "2026-09-20",
            daysCount = 10,
            reason = "إجازة سنوية مجدولة لقضاء العطلة العائلية بعد تسليم المرحلة الأولى من المشروع",
            substitute = "م. عمر السالم",
            documentSource = "ملف PDF: استمارة_طلب_إجازة_سنوية_موقعة.pdf"
        ),
        ImportedLeaveData(
            leaveType = LeaveType.Sick,
            startDate = "2026-08-25",
            endDate = "2026-08-27",
            daysCount = 3,
            reason = "إجازة مرضية بموجب تقرير طبي صادر من المستشفى المركزي التخصصي",
            substitute = "أحمد خالد",
            documentSource = "ملف PDF: تقرير_طبي_مستشفى_الملك_فيصل.pdf"
        ),
        ImportedLeaveData(
            leaveType = LeaveType.Emergency,
            startDate = "2026-08-30",
            endDate = "2026-08-31",
            daysCount = 2,
            reason = "ظرف عائلي طارئ يتطلب السفر خارج المدينة",
            substitute = "سارة ناصر",
            documentSource = "ملف PDF: طلب_إجازة_اضطرارية_عاجلة.pdf"
        )
    )

    val sampleOvertimePdfTemplates = listOf(
        ImportedOvertimeData(
            date = "2026-08-22",
            startTime = "17:00",
            endTime = "21:30",
            hours = 4.5,
            overtimeType = OvertimeType.NormalDay,
            taskDescription = "أعمال صيانة برمجية وتحديث خوادم النظام وإجراء النسخ الاحتياطي الشهري",
            outputOrDeliverable = "تم ترقية الخادم بنجاح ومطابقة سجلات قاعدة البيانات بنسبة 100%",
            documentSource = "ملف PDF: كشف_تكليف_إضافي_صيانة_السيرفرات.pdf"
        ),
        ImportedOvertimeData(
            date = "2026-08-29",
            startTime = "09:00",
            endTime = "15:00",
            hours = 6.0,
            overtimeType = OvertimeType.Weekend,
            taskDescription = "إقفال الحسابات المالية الربع سنوية وإعداد التقارير الضريبية وموازين المراجعة",
            outputOrDeliverable = "إنجاز ميزان المراجعة وتدقيق مستندات الرواتب والأجور بنجاح",
            documentSource = "ملف PDF: تكليف_عمل_إضافي_إقفال_مالي.pdf"
        )
    )

    val sampleTransactionPdfTemplates = listOf(
        ImportedTransactionData(
            subject = "طلب توفير تراخيص برمجية وتحديث عتاد الشبكة",
            description = "نظراً لتوسع فريق العمل، نرجو التوجيه بالتعميد لتوفير عدد 10 تراخيص برمجية إضافية وتحديث موزعات الشبكة الداخلية لضمان كفاءة الاتصال وسرعة معالجة البيانات.",
            priority = Priority.High,
            targetDepartment = "تقنية المعلومات والدعم الفني",
            documentSource = "ملف PDF: خطاب_معاملة_إدارية_رسمية_DOC992.pdf"
        ),
        ImportedTransactionData(
            subject = "طلب دورة تدريبية تخصصية في معايير الجودة ISO 9001:2015",
            description = "نرفع لسعادتكم رغبة القسم في ترشيح عدد 4 موظفين لحضور البرنامج التدريبي المتخصص في الحوكمة وضمان الجودة المؤسسية لتأهيل الكادر للتدقيق الداخلي السنوي.",
            priority = Priority.Normal,
            targetDepartment = "الموارد البشرية والشؤون الإدارية",
            documentSource = "ملف PDF: مذكرة_ترشيح_تدريب_جودة_ISO.pdf"
        )
    )
}
