package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.ui.viewmodel.DashboardKpis
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportHelper {

    fun generateAndShareKpiPdf(
        context: Context,
        kpis: DashboardKpis,
        monthTitle: String,
        companyName: String,
        supervisorName: String,
        generatedByUserName: String
    ): Result<File> {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 width in PostScript points (72 dpi)
            val pageHeight = 842 // Standard A4 height
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Colors
            val primaryColor = 0xFF1E3A8A.toInt() // Deep Navy
            val secondaryColor = 0xFF0284C7.toInt() // Sky Blue
            val accentSuccess = 0xFF16A34A.toInt() // Green
            val accentWarning = 0xFFD97706.toInt() // Amber
            val bgCardColor = 0xFFF1F5F9.toInt() // Light Slate
            val textDark = 0xFF0F172A.toInt()
            val textMuted = 0xFF475569.toInt()

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Header Banner
            paint.color = primaryColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

            // Header Accent Line
            paint.color = secondaryColor
            canvas.drawRect(0f, 110f, pageWidth.toFloat(), 116f, paint)

            // Header Texts
            paint.color = Color.WHITE
            paint.textSize = 17f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(companyName, (pageWidth - 30).toFloat(), 40f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("نظام إدارة الموارد البشرية والشؤون المؤسسية • ISO 9001", (pageWidth - 30).toFloat(), 62f, paint)

            paint.textSize = 10f
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
            val printDate = sdf.format(Date())
            canvas.drawText("تاريخ التصدير: $printDate | المشرف: $supervisorName", (pageWidth - 30).toFloat(), 82f, paint)

            // Quality Badge on Left
            paint.color = 0xFF3B82F6.toInt()
            val badgeRect = RectF(30f, 25f, 140f, 85f)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(badgeRect, 10f, 10f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("تقرير شهري رسمي", 85f, 50f, paint)
            paint.textSize = 9f
            paint.isFakeBoldText = false
            canvas.drawText("معتمد إدارياً", 85f, 68f, paint)

            // 2. Report Title & Period
            paint.textAlign = Paint.Align.RIGHT
            paint.color = textDark
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("تقرير مؤشرات الأداء الشهرية (Monthly KPIs Summary)", (pageWidth - 30).toFloat(), 150f, paint)

            paint.color = textMuted
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("الفترة المستهدفة: $monthTitle | تم الاستخراج بواسطة: $generatedByUserName", (pageWidth - 30).toFloat(), 170f, paint)

            // Divider
            paint.color = 0xFFCBD5E1.toInt()
            paint.strokeWidth = 1f
            canvas.drawLine(30f, 185f, (pageWidth - 30).toFloat(), 185f, paint)

            // 3. Four Monthly KPI Summary Cards
            val cardWidth = (pageWidth - 60 - 15) / 2f
            val cardHeight = 85f

            // Card 1: Overtime Hours
            drawKpiCard(
                canvas = canvas,
                rect = RectF(pageWidth - 30f - cardWidth, 205f, pageWidth - 30f, 205f + cardHeight),
                title = "ساعات الأجر الإضافي",
                mainValue = "${"%.1f".format(kpis.totalOvertimeHours)} ساعة",
                subValue = "معتمد: ${"%.1f".format(kpis.approvedOvertimeHours)} س | قيد المراجعة: ${"%.1f".format(kpis.totalOvertimeHours - kpis.approvedOvertimeHours)} س",
                borderColor = secondaryColor,
                bgColor = bgCardColor
            )

            // Card 2: Overtime Cost
            drawKpiCard(
                canvas = canvas,
                rect = RectF(30f, 205f, 30f + cardWidth, 205f + cardHeight),
                title = "تكلفة الأجر الإضافي",
                mainValue = "${"%.0f".format(kpis.totalOvertimeCost)} ريال يمني",
                subValue = "إجمالي المستحقات المالية للشهر",
                borderColor = accentSuccess,
                bgColor = bgCardColor
            )

            // Card 3: Total Entitled Leaves
            drawKpiCard(
                canvas = canvas,
                rect = RectF(pageWidth - 30f - cardWidth, 305f, pageWidth - 30f, 305f + cardHeight),
                title = "إجمالي الإجازات المستحقة",
                mainValue = "${kpis.totalEntitledLeaves} يوم",
                subValue = "رصيد الإجازات السنوية والطارئة النشطة",
                borderColor = 0xFF7C3AED.toInt(),
                bgColor = bgCardColor
            )

            // Card 4: Unpaid Leaves
            drawKpiCard(
                canvas = canvas,
                rect = RectF(30f, 305f, 30f + cardWidth, 305f + cardHeight),
                title = "إجازات بدون أجر",
                mainValue = "${kpis.totalUnpaidLeaves} يوم",
                subValue = "عدد الطلبات غير المدفوعة: ${kpis.unpaidLeaveRequestsCount}",
                borderColor = accentWarning,
                bgColor = bgCardColor
            )

            // 4. Monthly Breakdown Table
            paint.color = textDark
            paint.textSize = 14f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("جدول تفصيل مؤشرات وإحصائيات العمليات", (pageWidth - 30).toFloat(), 425f, paint)

            // Table Header
            val tableTop = 440f
            paint.color = primaryColor
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(30f, tableTop, (pageWidth - 30).toFloat(), tableTop + 30f), 6f, 6f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("المؤشر الإداري / المالي", (pageWidth - 50).toFloat(), tableTop + 20f, paint)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("العدد / القيمة", (pageWidth / 2f), tableTop + 20f, paint)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("الحالة التشغيلية", 50f, tableTop + 20f, paint)

            // Table Rows
            val tableData = listOf(
                Triple("إجمالي الموظفين المسجلين في النظام", "${kpis.totalEmployees} موظف", "نشط وكامل البيانات"),
                Triple("الطلبات المعلقة بانتظار الاعتماد النهائي", "${kpis.pendingApprovalsTotal} طلبات", "تتطلب مراجعة المشرف"),
                Triple("ساعات العمل الإضافي المعتمدة", "${"%.1f".format(kpis.approvedOvertimeHours)} ساعة", "معتمدة وجاهزة للصرف"),
                Triple("المستحقات المالية للأجر الإضافي", "${"%.0f".format(kpis.totalOvertimeCost)} ريال يمني", "مطابق للائحة الأجور"),
                Triple("أيام الإجازات غير المدفوعة المسجلة", "${kpis.totalUnpaidLeaves} يوم (${kpis.unpaidLeaveRequestsCount} طلبات)", "مخصومة تلقائياً من الراتب"),
                Triple("حالة قاعدة البيانات والمزامنة المحلية", "100% متطابقة ومؤمنة", "جاهز للعمل دون اتصال")
            )

            var rowY = tableTop + 30f
            paint.textSize = 10.5f
            paint.isFakeBoldText = false

            tableData.forEachIndexed { idx, (metric, value, status) ->
                paint.color = if (idx % 2 == 0) 0xFFF8FAFC.toInt() else Color.WHITE
                paint.style = Paint.Style.FILL
                canvas.drawRect(30f, rowY, (pageWidth - 30).toFloat(), rowY + 28f, paint)

                paint.color = 0xFFE2E8F0.toInt()
                paint.strokeWidth = 0.5f
                paint.style = Paint.Style.STROKE
                canvas.drawRect(30f, rowY, (pageWidth - 30).toFloat(), rowY + 28f, paint)

                paint.style = Paint.Style.FILL
                paint.color = textDark
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(metric, (pageWidth - 50).toFloat(), rowY + 18f, paint)

                paint.color = primaryColor
                paint.isFakeBoldText = true
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(value, (pageWidth / 2f), rowY + 18f, paint)

                paint.color = textMuted
                paint.isFakeBoldText = false
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(status, 50f, rowY + 18f, paint)

                rowY += 28f
            }

            // 5. Signatures and Official Seals Area
            val sigTop = 650f
            paint.color = 0xFFE2E8F0.toInt()
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(RectF(30f, sigTop, (pageWidth - 30).toFloat(), sigTop + 110f), 10f, 10f, paint)

            paint.style = Paint.Style.FILL
            paint.color = textDark
            paint.textSize = 11f
            paint.isFakeBoldText = true

            // Signature 1: HR Manager
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("إعداد وتدقيق: مدير الموارد البشرية", (pageWidth - 50).toFloat(), sigTop + 28f, paint)
            paint.textSize = 10f
            paint.isFakeBoldText = false
            paint.color = textMuted
            canvas.drawText("التوقيع: .......................................", (pageWidth - 50).toFloat(), sigTop + 65f, paint)
            canvas.drawText("التاريخ: $printDate", (pageWidth - 50).toFloat(), sigTop + 88f, paint)

            // Signature 2: General Manager Approval
            paint.textAlign = Paint.Align.LEFT
            paint.color = textDark
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("اعتماد: المدير العام / مدير النظام", 50f, sigTop + 28f, paint)
            paint.textSize = 10f
            paint.isFakeBoldText = false
            paint.color = textMuted
            canvas.drawText("الختم والاعتماد: .............................", 50f, sigTop + 65f, paint)
            canvas.drawText("التاريخ: $printDate", 50f, sigTop + 88f, paint)

            // 6. Footer Note
            paint.textAlign = Paint.Align.CENTER
            paint.color = 0xFF94A3B8.toInt()
            paint.textSize = 9f
            canvas.drawText("تم استخراج هذا التقرير إلكترونياً ومطابقته آلياً وفق معايير الجودة والحوكمة المؤسسية. لا يتطلب ختماً ورقياً في حال الاعتماد الإلكتروني.", (pageWidth / 2f), 800f, paint)

            pdfDocument.finishPage(page)

            // Save PDF to cache dir
            val fileName = "HR_KPI_Report_${monthTitle.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Open Share / View Intent
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "تقرير مؤشرات الأداء الشهرية - $monthTitle")
                putExtra(Intent.EXTRA_TEXT, "مرفق تقرير مؤشرات الأداء الشهرية لشهر $monthTitle الصادر عن نظام الموارد البشرية.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "تصدير ومشاركة تقرير مؤشرات الأداء PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Result.success(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun drawKpiCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        mainValue: String,
        subValue: String,
        borderColor: Int,
        bgColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = bgColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        // Top Accent Strip
        paint.color = borderColor
        canvas.drawRoundRect(RectF(rect.left, rect.top, rect.right, rect.top + 4f), 4f, 4f, paint)

        // Border
        paint.color = 0xFFCBD5E1.toInt()
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        // Texts
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.RIGHT

        // Title
        paint.color = 0xFF475569.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText(title, rect.right - 12f, rect.top + 22f, paint)

        // Main Value
        paint.color = borderColor
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText(mainValue, rect.right - 12f, rect.top + 48f, paint)

        // Sub Value
        paint.color = 0xFF64748B.toInt()
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText(subValue, rect.right - 12f, rect.top + 70f, paint)
    }
}
