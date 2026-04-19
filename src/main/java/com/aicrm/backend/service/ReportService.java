package com.aicrm.backend.service;

import com.aicrm.backend.model.Lead;
import com.aicrm.backend.model.Task;
import com.aicrm.backend.repository.LeadRepository;
import com.aicrm.backend.repository.TaskRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private TaskRepository taskRepository;

    // Leads Excel Report generate பண்ணு
    public byte[] generateLeadsReport() throws Exception {
        List<Lead> leads = leadRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Leads");

        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Phone");
        header.createCell(3).setCellValue("Email");
        header.createCell(4).setCellValue("City");
        header.createCell(5).setCellValue("Status");
        header.createCell(6).setCellValue("Requirement");
        header.createCell(7).setCellValue("Created At");

        // Data rows
        int rowNum = 1;
        for (Lead lead : leads) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(lead.getId());
            row.createCell(1).setCellValue(lead.getName());
            row.createCell(2).setCellValue(lead.getPhone());
            row.createCell(3).setCellValue(lead.getEmail());
            row.createCell(4).setCellValue(lead.getCity());
            row.createCell(5).setCellValue(lead.getStatus());
            row.createCell(6).setCellValue(lead.getRequirement());
            row.createCell(7).setCellValue(
                lead.getCreatedAt() != null ?
                lead.getCreatedAt().toString() : "");
        }

        // Auto size columns
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    // Tasks Excel Report generate பண்ணு
    public byte[] generateTasksReport() throws Exception {
        List<Task> tasks = taskRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Tasks");

        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Due Date");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("Related Type");

        // Data rows
        int rowNum = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(task.getId());
            row.createCell(1).setCellValue(task.getTitle());
            row.createCell(2).setCellValue(
                task.getDueDate() != null ?
                task.getDueDate().toString() : "");
            row.createCell(3).setCellValue(task.getStatus());
            row.createCell(4).setCellValue(task.getRelatedType());
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}