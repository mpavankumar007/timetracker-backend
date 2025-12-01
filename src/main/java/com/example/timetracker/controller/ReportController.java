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

    @GetMapping("/daily")
    public ResponseEntity<byte[]> downloadDailyReport(
            @RequestParam(required = false) String date
    ) throws IOException {

        LocalDate targetDate = (date != null && !date.isBlank())
                ? LocalDate.parse(date)
                : LocalDate.now();

        byte[] file = reportService.generateDailyReport(targetDate);
        String filename = "time-report-" + targetDate + ".xlsx";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(file);
    }

    @GetMapping("/weekly")
    public ResponseEntity<byte[]> downloadWeeklyReport(
            @RequestParam(required = false) String date
    ) throws IOException {

        LocalDate endDate = (date != null && !date.isBlank())
                ? LocalDate.parse(date)
                : LocalDate.now();

        byte[] file = reportService.generateWeeklyReport(endDate);

        String filename = "weekly-report-" +
                endDate.minusDays(6) + "_to_" + endDate + ".xlsx";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(file);
    }
}
