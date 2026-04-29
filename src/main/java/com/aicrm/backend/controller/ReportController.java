package com.aicrm.backend.controller;

import com.aicrm.backend.service.PdfReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private PdfReportService pdfReportService;

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource>
    downloadPdf() {

        var pdf =
                pdfReportService
                .generateLeadReport();

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "Content-Disposition",
                "attachment; filename=crm-report.pdf"
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(
                        new InputStreamResource(pdf)
                );
    }
}