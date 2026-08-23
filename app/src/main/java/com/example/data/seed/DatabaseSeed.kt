package com.example.data.seed

import com.example.data.local.*
import java.text.SimpleDateFormat
import java.util.*

object DatabaseSeed {

    suspend fun populateDatabaseIfEmpty(dao: HrDao) {
        val adminUser = dao.getUserByUsername("admin")
        if (adminUser != null) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val today = sdf.format(Date())

        // 1. Initial Users
        val users = listOf(
            UserEntity(
                id = "USR-001",
                username = "admin",
                passwordHash = "123",
                fullName = "مدير النظام الرئيسي",
                email = "admin@company.com",
                phone = "+966501112233",
                employeeId = "EMP-001",
                department = "تقنية المعلومات",
                position = "مدير أمن ونظم المعلومات",
                role = "SuperAdmin",
                mustChangePassword = false
            ),
            UserEntity(
                id = "USR-002",
                username = "hr_manager",
                passwordHash = "123",
                fullName = "سارة ناصر المنصور",
                email = "sara.hr@company.com",
                phone = "+966502223344",
                employeeId = "EMP-002",
                department = "الموارد البشرية",
                position = "مدير إدارة الموارد البشرية",
                role = "HrManager"
            ),
            UserEntity(
                id = "USR-003",
                username = "dept_eng",
                passwordHash = "123",
                fullName = "م. خالد عبد الرحمن السالم",
                email = "khaled.eng@company.com",
                phone = "+966503334455",
                employeeId = "EMP-003",
                department = "الهندسة والبرمجيات",
                position = "رئيس قسم الهندسة والتقنية",
                role = "DepartmentHead"
            ),
            UserEntity(
                id = "USR-004",
                username = "gm_user",
                passwordHash = "123",
                fullName = "د. فهد عبدالعزيز الشمري",
                email = "fahad.gm@company.com",
                phone = "+966504445566",
                employeeId = "EMP-004",
                department = "الإدارة التنفيذية",
                position = "الرئيس التنفيذي / المدير العام",
                role = "GeneralManager"
            ),
            UserEntity(
                id = "USR-005",
                username = "emp_ahmed",
                passwordHash = "123",
                fullName = "أحمد فيصل الحربي",
                email = "ahmed.harbi@company.com",
                phone = "+966505556677",
                employeeId = "EMP-005",
                department = "الهندسة والبرمجيات",
                position = "مطور تطبيقات أول",
                role = "Employee"
            )
        )
        users.forEach { dao.insertUser(it) }

        // 2. Employees
        val employees = listOf(
            EmployeeEntity(
                id = "EMP-001",
                employeeNumber = "EMP-1001",
                fullName = "مدير النظام الرئيسي",
                nationalId = "1098234761",
                department = "تقنية المعلومات",
                division = "إدارة البنية التحتية",
                position = "مدير أمن ونظم المعلومات",
                grade = "A-1",
                contractType = "دوام كامل (غير محدد)",
                hireDate = "2021-01-15",
                contractEndDate = "2027-01-15",
                status = "Active",
                phone = "+966501112233",
                email = "admin@company.com",
                directManager = "المدير العام",
                workLocation = "المقر الرئيسي - الرياض",
                branch = "فرع الرياض",
                emergencyContact = "أخ: +966501119988",
                salaryBase = 22000.0,
                hourlyRate = 125.0,
                annualLeaveBalance = 24,
                sickLeaveBalance = 15,
                emergencyLeaveBalance = 5
            ),
            EmployeeEntity(
                id = "EMP-002",
                employeeNumber = "EMP-1002",
                fullName = "سارة ناصر المنصور",
                nationalId = "1087456321",
                department = "الموارد البشرية",
                division = "إدارة شؤون الموظفين والرواتب",
                position = "مدير إدارة الموارد البشرية",
                grade = "A-2",
                contractType = "دوام كامل (غير محدد)",
                hireDate = "2020-05-10",
                contractEndDate = "2026-05-10",
                status = "Active",
                phone = "+966502223344",
                email = "sara.hr@company.com",
                directManager = "المدير العام",
                workLocation = "المقر الرئيسي - الرياض",
                branch = "فرع الرياض",
                emergencyContact = "والد: +966502221100",
                salaryBase = 19500.0,
                hourlyRate = 110.0,
                annualLeaveBalance = 20,
                sickLeaveBalance = 12,
                emergencyLeaveBalance = 4
            ),
            EmployeeEntity(
                id = "EMP-003",
                employeeNumber = "EMP-1003",
                fullName = "م. خالد عبد الرحمن السالم",
                nationalId = "1076543219",
                department = "الهندسة والبرمجيات",
                division = "إدارة تطوير البرمجيات",
                position = "رئيس قسم الهندسة والتقنية",
                grade = "A-2",
                contractType = "دوام كامل",
                hireDate = "2019-09-01",
                contractEndDate = "2027-09-01",
                status = "Active",
                phone = "+966503334455",
                email = "khaled.eng@company.com",
                directManager = "المدير العام",
                workLocation = "مجمع الابتكار والتقنية",
                branch = "فرع الرياض",
                emergencyContact = "زوجة: +966503339911",
                salaryBase = 21000.0,
                hourlyRate = 120.0,
                annualLeaveBalance = 18,
                sickLeaveBalance = 14,
                emergencyLeaveBalance = 5
            ),
            EmployeeEntity(
                id = "EMP-004",
                employeeNumber = "EMP-1004",
                fullName = "د. فهد عبدالعزيز الشمري",
                nationalId = "1065432198",
                department = "الإدارة التنفيذية",
                division = "مجلس الإدارة والقيادة",
                position = "الرئيس التنفيذي / المدير العام",
                grade = "EXEC-1",
                contractType = "عقد إداري تنفيذي",
                hireDate = "2018-03-01",
                contractEndDate = "2028-03-01",
                status = "Active",
                phone = "+966504445566",
                email = "fahad.gm@company.com",
                directManager = "مجلس الإدارة",
                workLocation = "المقر الرئيسي - الرياض",
                branch = "فرع الرياض",
                emergencyContact = "المكتب التنفيذي: +966114440000",
                salaryBase = 35000.0,
                hourlyRate = 200.0,
                annualLeaveBalance = 30,
                sickLeaveBalance = 15,
                emergencyLeaveBalance = 5
            ),
            EmployeeEntity(
                id = "EMP-005",
                employeeNumber = "EMP-1005",
                fullName = "أحمد فيصل الحربي",
                nationalId = "1054321987",
                department = "الهندسة والبرمجيات",
                division = "إدارة تطوير البرمجيات",
                position = "مطور تطبيقات أول (Mobile & ERP)",
                grade = "B-1",
                contractType = "دوام كامل",
                hireDate = "2022-04-15",
                contractEndDate = "2026-04-15",
                status = "Active",
                phone = "+966505556677",
                email = "ahmed.harbi@company.com",
                directManager = "م. خالد عبد الرحمن السالم",
                workLocation = "مجمع الابتكار والتقنية",
                branch = "فرع الرياض",
                emergencyContact = "شقيق: +966505558899",
                salaryBase = 14500.0,
                hourlyRate = 82.5,
                annualLeaveBalance = 22,
                sickLeaveBalance = 15,
                emergencyLeaveBalance = 5
            ),
            EmployeeEntity(
                id = "EMP-006",
                employeeNumber = "EMP-1006",
                fullName = "ريم عبدالله القحطاني",
                nationalId = "1043219876",
                department = "الإدارة المالية",
                division = "إدارة الحسابات والتدقيق",
                position = "محاسب مالي أول",
                grade = "B-2",
                contractType = "دوام كامل",
                hireDate = "2021-08-01",
                contractEndDate = "2025-08-01",
                status = "Active",
                phone = "+966506667788",
                email = "reem.finance@company.com",
                directManager = "مدير الإدارة المالية",
                workLocation = "المقر الرئيسي - الرياض",
                branch = "فرع الرياض",
                emergencyContact = "والدة: +966506663322",
                salaryBase = 13000.0,
                hourlyRate = 74.0,
                annualLeaveBalance = 15,
                sickLeaveBalance = 10,
                emergencyLeaveBalance = 3
            ),
            EmployeeEntity(
                id = "EMP-007",
                employeeNumber = "EMP-1007",
                fullName = "سلطان إبراهيم الدوسري",
                nationalId = "1032198765",
                department = "العمليات واللوجستيات",
                division = "إدارة النقل والمشتريات",
                position = "مشرف الخدمات واللوجستيات",
                grade = "C-1",
                contractType = "دوام كامل",
                hireDate = "2020-11-20",
                contractEndDate = "2026-11-20",
                status = "Active",
                phone = "+966507778899",
                email = "sultan.ops@company.com",
                directManager = "مدير إدارة العمليات",
                workLocation = "المستودع المركزي - جدة",
                branch = "فرع جدة",
                emergencyContact = "عم: +966507774411",
                salaryBase = 9800.0,
                hourlyRate = 55.7,
                annualLeaveBalance = 12,
                sickLeaveBalance = 15,
                emergencyLeaveBalance = 2
            ),
            EmployeeEntity(
                id = "EMP-008",
                employeeNumber = "EMP-1008",
                fullName = "منى عثمان الغامدي",
                nationalId = "1021987654",
                department = "المبيعات والتسويق",
                division = "إدارة العلاقات العامة والتسويق",
                position = "أخصائي تسويق رقمي",
                grade = "B-3",
                contractType = "دوام كامل",
                hireDate = "2023-02-10",
                contractEndDate = "2027-02-10",
                status = "Active",
                phone = "+966508889900",
                email = "mona.marketing@company.com",
                directManager = "مدير التسويق",
                workLocation = "فرع الخبر",
                branch = "فرع الخبر",
                emergencyContact = "أخت: +966508882233",
                salaryBase = 11500.0,
                hourlyRate = 65.3,
                annualLeaveBalance = 26,
                sickLeaveBalance = 15,
                emergencyLeaveBalance = 5
            )
        )
        employees.forEach { dao.insertEmployee(it) }

        // 3. Leave Requests
        val leaveRequests = listOf(
            LeaveRequestEntity(
                id = "LEV-001",
                requestNumber = "LEAVE-2026-000001",
                employeeId = "EMP-005",
                employeeName = "أحمد فيصل الحربي",
                department = "الهندسة والبرمجيات",
                leaveType = "Annual",
                startDate = "2026-09-01",
                endDate = "2026-09-05",
                daysCount = 5,
                reason = "إجازة سنوية اعتيادية للراحة والسفر العائلي",
                previousBalance = 27,
                usedBalance = 5,
                remainingBalance = 22,
                substituteEmployee = "م. خالد عبد الرحمن السالم",
                status = "Approved",
                notes = "تم اعتماد الإجازة وتعيين البديل لإتمام مهام النشر",
                createdAt = System.currentTimeMillis() - 86400000L * 5
            ),
            LeaveRequestEntity(
                id = "LEV-002",
                requestNumber = "LEAVE-2026-000002",
                employeeId = "EMP-006",
                employeeName = "ريم عبدالله القحطاني",
                department = "الإدارة المالية",
                leaveType = "Sick",
                startDate = "2026-08-25",
                endDate = "2026-08-27",
                daysCount = 3,
                reason = "إجازة مرضية مرفق التقرير الطبي المعتمد من صحتي",
                previousBalance = 13,
                usedBalance = 3,
                remainingBalance = 10,
                substituteEmployee = "سارة ناصر المنصور",
                status = "PendingHr",
                notes = "تمت موافقة رئيس القسم وبانتظار تدقيق الموارد البشرية",
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            LeaveRequestEntity(
                id = "LEV-003",
                requestNumber = "LEAVE-2026-000003",
                employeeId = "EMP-007",
                employeeName = "سلطان إبراهيم الدوسري",
                department = "العمليات واللوجستيات",
                leaveType = "Emergency",
                startDate = "2026-08-22",
                endDate = "2026-08-23",
                daysCount = 2,
                reason = "ظرف عائلي طارئ يستدعي التواجد",
                previousBalance = 4,
                usedBalance = 2,
                remainingBalance = 2,
                substituteEmployee = "منى عثمان الغامدي",
                status = "PendingDept",
                notes = "طلب عاجل مقدم عبر النظام",
                createdAt = System.currentTimeMillis() - 3600000L * 4
            ),
            LeaveRequestEntity(
                id = "LEV-004",
                requestNumber = "LEAVE-2026-000004",
                employeeId = "EMP-008",
                employeeName = "منى عثمان الغامدي",
                department = "المبيعات والتسويق",
                leaveType = "Official",
                startDate = "2026-09-10",
                endDate = "2026-09-12",
                daysCount = 3,
                reason = "مهمة عمل رسمية للمشاركة في مؤتمر التسويق الدولي",
                previousBalance = 26,
                usedBalance = 0,
                remainingBalance = 26,
                substituteEmployee = "سلطان إبراهيم الدوسري",
                status = "PendingGm",
                notes = "معتمد من رئيس القسم والموارد البشرية وبانتظار اعتماد المدير العام",
                createdAt = System.currentTimeMillis() - 86400000L * 1
            ),
            LeaveRequestEntity(
                id = "LEV-005",
                requestNumber = "LEAVE-2026-000005",
                employeeId = "EMP-005",
                employeeName = "أحمد فيصل الحربي",
                department = "الهندسة والبرمجيات",
                leaveType = "Unpaid",
                startDate = "2026-09-20",
                endDate = "2026-09-25",
                daysCount = 6,
                reason = "إجازة استثنائية بدون أجر لإنجاز أوراق دراسية عليا",
                previousBalance = 22,
                usedBalance = 0,
                remainingBalance = 22,
                substituteEmployee = "م. خالد عبد الرحمن السالم",
                status = "Approved",
                notes = "معتمدة كإجازة بدون راتب وفق لائحة الموارد البشرية",
                createdAt = System.currentTimeMillis() - 86400000L * 3
            )
        )
        leaveRequests.forEach { dao.insertLeaveRequest(it) }

        // 4. Overtime Requests
        val overtimeRequests = listOf(
            OvertimeRequestEntity(
                id = "OT-001",
                requestNumber = "OT-2026-000001",
                employeeId = "EMP-005",
                employeeName = "أحمد فيصل الحربي",
                department = "الهندسة والبرمجيات",
                date = "2026-08-18",
                workDayType = "NormalDay",
                startTime = "17:00",
                endTime = "21:00",
                hours = 4.0,
                overtimeType = "NormalDay",
                reason = "إكمال ترقية قاعدة بيانات نظام الموارد البشرية وتجهيز بيئة الإنتاج",
                projectOrTask = "مشروع نظام ERP الموارد البشرية",
                calculatedAmount = 4.0 * 82.5 * 1.5, // 495 SAR
                status = "Approved",
                notes = "تم إنجاز المهمة البرمجية بنجاح واعتماد الاستحقاق",
                createdAt = System.currentTimeMillis() - 86400000L * 4
            ),
            OvertimeRequestEntity(
                id = "OT-002",
                requestNumber = "OT-2026-000002",
                employeeId = "EMP-006",
                employeeName = "ريم عبدالله القحطاني",
                department = "الإدارة المالية",
                date = "2026-08-20",
                workDayType = "Weekend",
                startTime = "09:00",
                endTime = "15:00",
                hours = 6.0,
                overtimeType = "Weekend",
                reason = "إعداد القوائم المالية الشهرية وإغلاق المطابقات البنكية للربع الثالث",
                projectOrTask = "الإغلاق المالي الربع سنوي",
                calculatedAmount = 6.0 * 74.0 * 2.0, // 888 SAR
                status = "PendingHr",
                notes = "تمت مراجعة الساعات من قبل الإدارة المالية",
                createdAt = System.currentTimeMillis() - 86400000L * 1
            ),
            OvertimeRequestEntity(
                id = "OT-003",
                requestNumber = "OT-2026-000003",
                employeeId = "EMP-007",
                employeeName = "سلطان إبراهيم الدوسري",
                department = "العمليات واللوجستيات",
                date = "2026-08-21",
                workDayType = "NormalDay",
                startTime = "18:00",
                endTime = "21:30",
                hours = 3.5,
                overtimeType = "NormalDay",
                reason = "استلام وتوزيع شحنة أجهزة الخوادم الجديدة بالمستودع",
                projectOrTask = "توريدات التقنية",
                calculatedAmount = 3.5 * 55.7 * 1.5, // 292.4 SAR
                status = "PendingDept",
                notes = "مقدم بانتظار موافقة مدير العمليات",
                createdAt = System.currentTimeMillis() - 3600000L * 3
            )
        )
        overtimeRequests.forEach { dao.insertOvertimeRequest(it) }

        // 5. Penalties & Disciplinary
        val penalties = listOf(
            PenaltyEntity(
                id = "PEN-001",
                penaltyNumber = "PEN-2026-000001",
                employeeId = "EMP-007",
                employeeName = "سلطان إبراهيم الدوسري",
                department = "العمليات واللوجستيات",
                incidentDate = "2026-08-10",
                violationType = "التأخر غير المبرر عن موعد استلام الوردية",
                description = "تكرار التأخر الصباحي لأكثر من 45 دقيقة بدون إذن مسبق خلال الأسبوع الأول من الشهر",
                severity = "Medium",
                actionType = "FirstWarning",
                deductionAmount = 150.0,
                suspensionDays = 0,
                legalReference = "المادة (68) من لائحة تنظيم العمل والجزاءات المعتمدة",
                investigationNotes = "تم استدعاء الموظف وسماع أقواله وإبلاغه بالالتزام باللوائح",
                employeeStatement = "أقر بالتأخر بسبب عطل مفاجئ في المركبة وأتعهد بعدم التكرار",
                status = "Closed",
                createdAt = System.currentTimeMillis() - 86400000L * 10
            )
        )
        penalties.forEach { dao.insertPenalty(it) }

        // 6. Meals
        val meals = listOf(
            MealRequestEntity(
                id = "MEL-001",
                requestNumber = "MEAL-2026-000001",
                employeeId = "EMP-005",
                employeeName = "أحمد فيصل الحربي",
                department = "الهندسة والبرمجيات",
                date = "2026-08-18",
                mealType = "Dinner",
                count = 4,
                totalCost = 160.0,
                vendor = "مطاعم الرومانسية للضيافة",
                reason = "وجبة عشاء لفريق التطوير أثناء العمل الإضافي الطارئ",
                status = "Approved"
            ),
            MealRequestEntity(
                id = "MEL-002",
                requestNumber = "MEAL-2026-000002",
                employeeId = "EMP-008",
                employeeName = "منى عثمان الغامدي",
                department = "المبيعات والتسويق",
                date = "2026-08-20",
                mealType = "Lunch",
                count = 6,
                totalCost = 270.0,
                vendor = "ضيافة كافيه & رستورانت",
                reason = "غداء عمل لاجتماع إطلاق الحملة التسويقية الجديدة مع الشركاء",
                status = "Approved"
            )
        )
        meals.forEach { dao.insertMealRequest(it) }

        // 7. Transportation
        val transport = listOf(
            TransportationRequestEntity(
                id = "TRN-001",
                requestNumber = "TRANS-2026-000001",
                employeeId = "EMP-008",
                employeeName = "منى عثمان الغامدي",
                department = "المبيعات والتسويق",
                date = "2026-09-10",
                route = "الخبر ⟷ الرياض",
                departurePoint = "مقر الشركة - الخبر",
                destination = "مركز المؤتمرات - الرياض",
                transportType = "Flight",
                tripsCount = 2,
                totalCost = 750.0,
                vendorOrDriver = "الخطوط السعودية",
                reason = "حضور مؤتمر التسويق الدولي وتمثيل الشركة",
                status = "Approved"
            ),
            TransportationRequestEntity(
                id = "TRN-002",
                requestNumber = "TRANS-2026-000002",
                employeeId = "EMP-007",
                employeeName = "سلطان إبراهيم الدوسري",
                department = "العمليات واللوجستيات",
                date = "2026-08-21",
                route = "المستودع ⟷ الميناء الجاف",
                departurePoint = "المستودع الرئيسي",
                destination = "الميناء الجاف بالرياض",
                transportType = "CompanyBus",
                tripsCount = 1,
                totalCost = 120.0,
                vendorOrDriver = "سائق الخدمات: محمد سعيد",
                reason = "مرافقة شحنة المعدات والتخليص الجمركي",
                status = "Approved"
            )
        )
        transport.forEach { dao.insertTransportationRequest(it) }

        // 8. Admin Transactions
        val transactions = listOf(
            AdminTransactionEntity(
                id = "TRX-001",
                transactionNumber = "TRX-2026-000001",
                employeeId = "EMP-005",
                employeeName = "أحمد فيصل الحربي",
                department = "الهندسة والبرمجيات",
                date = "2026-08-19",
                subject = "طلب تحديث رخص البرمجيات وبيئة السحابة للتطوير",
                description = "نظراً لمتطلبات مشروع الـ ERP الجديد، يرجى ترقية باقة السيرفرات وتجديد شهادات الأمان SSL",
                priority = "High",
                targetDepartment = "تقنية المعلومات",
                status = "Submitted",
                notes = "معاملة قيد الدراسة والمتابعة"
            ),
            AdminTransactionEntity(
                id = "TRX-002",
                transactionNumber = "TRX-2026-000002",
                employeeId = "EMP-006",
                employeeName = "ريم عبدالله القحطاني",
                department = "الإدارة المالية",
                date = "2026-08-17",
                subject = "طلب توفير أجهزة حاسب آلي عالية المواصفات للمدققين",
                description = "طلب تأمين 3 أجهزة لابتوب للأقسام المالية لإجراء مهام الإغلاق الربع سنوي",
                priority = "Normal",
                targetDepartment = "المشتريات والخدمات",
                status = "Approved",
                notes = "تمت الموافقة وتم إصدار أمر الشراء رقم PO-492"
            )
        )
        transactions.forEach { dao.insertAdminTransaction(it) }

        // 9. Notifications & HR Circulars
        val notifications = listOf(
            NotificationEntity(
                recipientRole = "ALL",
                title = "تعميم إداري (HR-CIR-2026-041): تحديث لائحة الإجازات وساعات العمل",
                message = "تود إدارة الموارد البشرية إحاطة جميع الزملاء باعتماد الدليل المحدث للإجازات السنوية والعمل الإضافي متوافقاً مع معايير الجودة ISO 9001.",
                category = "CIRCULAR",
                isRead = false,
                timestamp = System.currentTimeMillis() - 3600000L * 1
            ),
            NotificationEntity(
                recipientRole = "ALL",
                title = "تعميم إداري (HR-CIR-2026-039): تفعيل ميزة العمل دون اتصال والتخزين المحلي",
                message = "تم تفعيل قاعدة البيانات المحلية Room وقنوات الإشعارات الإلكترونية للموظفين لضمان استمرارية الأعمال دون انقطاع.",
                category = "CIRCULAR",
                isRead = false,
                timestamp = System.currentTimeMillis() - 3600000L * 4
            ),
            NotificationEntity(
                recipientRole = "HR",
                title = "طلب إجازة جديد بانتظار الاعتماد",
                message = "قدمت الموظفة ريم عبدالله القحطاني طلب إجازة مرضية لمدة 3 أيام.",
                category = "REQUEST",
                targetRequestId = "LEV-002",
                targetRequestType = "LEAVE",
                isRead = false,
                timestamp = System.currentTimeMillis() - 3600000L * 5
            ),
            NotificationEntity(
                recipientRole = "GM",
                title = "طلب مهمة رسمية معتمد من HR",
                message = "تمت مراجعة طلب المهمة الرسمية للموظفة منى الغامدي وتنتظر توقيعكم النهائي.",
                category = "APPROVAL",
                targetRequestId = "LEV-004",
                targetRequestType = "LEAVE",
                isRead = false,
                timestamp = System.currentTimeMillis() - 3600000L * 8
            )
        )
        notifications.forEach { dao.insertNotification(it) }

        // 10. Audit Logs
        val auditLogs = listOf(
            AuditLogEntity(
                userId = "USR-001",
                userName = "مدير النظام الرئيسي",
                userRole = "SuperAdmin",
                actionType = "LOGIN",
                entityType = "AUTH",
                entityId = "SESSION-01",
                details = "تسجيل دخول ناجح للنظام والتحقق من سلامة قاعدة البيانات المحلية والمزامنة"
            ),
            AuditLogEntity(
                userId = "USR-003",
                userName = "م. خالد عبد الرحمن السالم",
                userRole = "DepartmentHead",
                actionType = "APPROVE",
                entityType = "OVERTIME",
                entityId = "OT-001",
                details = "اعتماد ساعات العمل الإضافي للموظف أحمد الحربي (4 ساعات برمجية)"
            ),
            AuditLogEntity(
                userId = "USR-002",
                userName = "سارة ناصر المنصور",
                userRole = "HrManager",
                actionType = "CREATE",
                entityType = "PENALTY",
                entityId = "PEN-001",
                details = "إنشاء محضر لفت نظر وتطبيق خصم 150 ريال للواقعة PEN-2026-000001"
            )
        )
        auditLogs.forEach { dao.insertAuditLog(it) }

        // 11. System Settings
        dao.insertSystemSettings(
            SystemSettingsEntity(
                id = 1,
                companyName = "مجموعة الخليج للخدمات المؤسسية والتطوير",
                companyCommercialId = "CR-1010893421",
                currency = "ريال يمني (YER)",
                weeklyWorkDays = 5,
                dailyWorkingHours = 8.0,
                overtimeStandardRate = 1.5,
                overtimeWeekendRate = 2.0,
                overtimeHolidayRate = 2.5,
                autoSyncEnabled = true,
                supervisorName = "أ.أحمد العمري",
                lastBackupDate = "2026-08-21 14:00"
            )
        )

        // 12. Initial Departments
        val departments = listOf(
            DepartmentEntity(
                id = "DEP-001",
                departmentCode = "ENG",
                nameAr = "الهندسة والبرمجيات",
                managerName = "م. خالد عبد الرحمن السالم",
                location = "المبنى الرئيسي - الطابق الثالث",
                employeeCount = 18,
                estimatedMonthlyBudget = 850000.0,
                notes = "قسم تطوير البرمجيات والأنظمة وقواعد البيانات"
            ),
            DepartmentEntity(
                id = "DEP-002",
                departmentCode = "HR",
                nameAr = "الموارد البشرية والشؤون الإدارية",
                managerName = "سارة ناصر المنصور",
                location = "المبنى الرئيسي - الطابق الأول",
                employeeCount = 9,
                estimatedMonthlyBudget = 420000.0,
                notes = "إدارة شؤون الموظفين، التدريب، التوظيف والاستحقاقات"
            ),
            DepartmentEntity(
                id = "DEP-003",
                departmentCode = "FIN",
                nameAr = "المالية والمحاسبة",
                managerName = "أ. ياسر محمد الغامدي",
                location = "المبنى الرئيسي - الطابق الثاني",
                employeeCount = 12,
                estimatedMonthlyBudget = 600000.0,
                notes = "التدقيق المالي، الحسابات، الأجور والرواتب والضرائب"
            ),
            DepartmentEntity(
                id = "DEP-004",
                departmentCode = "MKT",
                nameAr = "المبيعات والتسويق",
                managerName = "عمر عبدالله القحطاني",
                location = "المبنى التجاري - الطابق الأرضي",
                employeeCount = 15,
                estimatedMonthlyBudget = 750000.0,
                notes = "تطوير الأعمال، التسويق الرقمي والمبيعات الميدانية"
            ),
            DepartmentEntity(
                id = "DEP-005",
                departmentCode = "OPS",
                nameAr = "العمليات والتشغيل والخدمات",
                managerName = "م. سلطان إبراهيم الدوسري",
                location = "مجمع العمليات المساندة",
                employeeCount = 24,
                estimatedMonthlyBudget = 920000.0,
                notes = "إدارة أسطول النقل، الخدمات اللوجستية والإعاشة"
            ),
            DepartmentEntity(
                id = "DEP-006",
                departmentCode = "QA",
                nameAr = "الدعم الفني وضمان الجودة ISO",
                managerName = "م. ريم عبدالعزيز الحربي",
                location = "المبنى الرئيسي - الطابق الثالث",
                employeeCount = 8,
                estimatedMonthlyBudget = 380000.0,
                notes = "مراقبة معايير الجودة ISO 9001 والدعم التقني"
            )
        )
        departments.forEach { dao.insertDepartment(it) }
    }
}
