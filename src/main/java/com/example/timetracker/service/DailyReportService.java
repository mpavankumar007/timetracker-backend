package com.example.timetracker.service;

import com.example.timetracker.repo.TimeEntryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyReportService {

    private final TimeEntryRepository repository;

    public DailyReportService(TimeEntryRepository repository) {
        this.repository = repository;
    }

    // ---------- DAILY ----------
    public byte[] generateDailyReport(LocalDate date) throws IOException {
        List<TimeEntryRepository.DailyTotalProjection> totals =
                repository.findDailyTotals(date);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daily Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Employee ID");
            header.createCell(2).setCellValue("Total Hours");
            header.createCell(3).setCellValue("Shift Complete (>= 8h)");

            int rowIdx = 1;
            for (TimeEntryRepository.DailyTotalProjection t : totals) {
                Row row = sheet.createRow(rowIdx++);

                double hours = t.getTotalMs() / 1000.0 / 3600.0;
                boolean complete = hours >= 8.0;

                // Convert decimal hours → Hh Mm
                long totalMinutes = Math.round(hours * 60);
                long h = totalMinutes / 60;
                long m = totalMinutes % 60;

                row.createCell(0).setCellValue(t.getWorkDate().toString());
                row.createCell(1).setCellValue(t.getEmployeeId());
                row.createCell(2).setCellValue(h + "h " + m + "m");
                row.createCell(3).setCellValue(complete ? "Yes" : "No");
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ---------- WEEKLY ----------
    // date here is the "week end" date (e.g., Sunday)
    public byte[] generateWeeklyReport(LocalDate endDate) throws IOException {
        LocalDate startDate = endDate.minusDays(6); // 7-day window

        List<TimeEntryRepository.DailyTotalProjection> rows =
                repository.findTotalsBetween(startDate, endDate);

        // Sum totalMs per employee across the week
        Map<String, Long> totalsByEmployee = new HashMap<>();
        for (TimeEntryRepository.DailyTotalProjection r : rows) {
            totalsByEmployee.merge(
                    r.getEmployeeId(),
                    r.getTotalMs(),
                    Long::sum
            );
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Weekly Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Week Start");
            header.createCell(1).setCellValue("Week End");
            header.createCell(2).setCellValue("Employee ID");
            header.createCell(3).setCellValue("Total Hours");
            header.createCell(4).setCellValue("Shift Complete (>= 40h)");

            int rowIdx = 1;
            for (Map.Entry<String, Long> e : totalsByEmployee.entrySet()) {
                String employeeId = e.getKey();
                long totalMs = e.getValue();

                double hours = totalMs / 1000.0 / 3600.0;
                boolean complete = hours >= 40.0;

                // Convert decimal hours → Hh Mm
                long totalMinutes = Math.round(hours * 60);
                long h = totalMinutes / 60;
                long m = totalMinutes % 60;

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(startDate.toString());
                row.createCell(1).setCellValue(endDate.toString());
                row.createCell(2).setCellValue(employeeId);
                row.createCell(3).setCellValue(h + "h " + m + "m");
                row.createCell(4).setCellValue(complete ? "Yes" : "No");
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
