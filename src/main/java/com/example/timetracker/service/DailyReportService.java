package com.example.timetracker.service;

import com.example.timetracker.repo.TimeEntryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class DailyReportService {

    private final TimeEntryRepository repository;

    public DailyReportService(TimeEntryRepository repository) {
        this.repository = repository;
    }

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

                row.createCell(0).setCellValue(t.getWorkDate().toString());
                row.createCell(1).setCellValue(t.getEmployeeId());
                row.createCell(2).setCellValue(hours);
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
}
