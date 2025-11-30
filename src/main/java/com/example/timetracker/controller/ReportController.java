package com.example.timetracker.controller;

import com.example.timetracker.service.DailyReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final DailyReportService reportService;

    public ReportController(DailyReportService reportService) {
        this.reportService = reportService;
    }

    // ==========================
    // 1) DAILY REPORT (existing)
    // ==========================
    @GetMapping("/daily")
    public ResponseEntity<byte[]> downloadDailyReport(
            @RequestParam(required = false) String date
    ) throws IOException {

        LocalDate targetDate = (date != null)
                ? LocalDate.parse(date)
                : LocalDate.now();

        byte[] file = reportService.generateDailyReport(targetDate);
        String filename = "time-report-" + targetDate + ".xlsx";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(file);
    }

    // ==========================
    // 2) WEEKLY REPORT (new)
    // ==========================
    @GetMapping("/weekly")
    public ResponseEntity<byte[]> downloadWeeklyReport(
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) throws IOException {

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        byte[] file = reportService.generateWeeklyReport(startDate, endDate);
        String filename = "time-report-week-" + startDate + "-to-" + endDate + ".xlsx";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(file);
    }
}
