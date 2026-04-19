package com.aicrm.backend.controller;

import com.aicrm.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Leads Report download பண்ணு
    @GetMapping("/leads")
    public ResponseEntity<byte[]> downloadLeadsReport() {
        try {
            byte[] excelFile = reportService.generateLeadsReport();

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=leads_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Tasks Report download பண்ணு
    @GetMapping("/tasks")
    public ResponseEntity<byte[]> downloadTasksReport() {
        try {
            byte[] excelFile = reportService.generateTasksReport();

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=tasks_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}