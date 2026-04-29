package com.aicrm.backend.service;

import com.aicrm.backend.model.Lead;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfReportService {

    @Autowired
    private LeadService leadService;

    public ByteArrayInputStream generateLeadReport() {

        Document document =
                new Document(PageSize.A4);

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(
                    document, out);

            document.open();

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18);

            Font headFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            12);

            Paragraph title =
                    new Paragraph(
                            "AI CRM Lead Report",
                            titleFont);

            title.setAlignment(
                    Element.ALIGN_CENTER);

            title.setSpacingAfter(20);

            document.add(title);

            // ==========================
            // SUMMARY
            // ==========================
            document.add(new Paragraph(
                    "Total Leads : "
                    + leadService.getLeadCount()));

            document.add(new Paragraph(
                    "Won Deals : "
                    + leadService.getWonDealsCount()));

            document.add(new Paragraph(
                    "Lost Deals : "
                    + leadService.getLostDealsCount()));

            document.add(new Paragraph(
                    "Pending Deals : "
                    + leadService.getPendingDealsCount()));

            document.add(new Paragraph(" "));

            // ==========================
            // TABLE
            // ==========================
            PdfPTable table =
                    new PdfPTable(6);

            table.setWidthPercentage(100);

            table.setWidths(
                    new int[]{3,3,4,3,3,3});

            addHeader(table, "Name");
            addHeader(table, "Phone");
            addHeader(table, "Email");
            addHeader(table, "City");
            addHeader(table, "Deal");
            addHeader(table, "Source");

            List<Lead> leads =
                    leadService.getAllLeads();

            for (Lead lead : leads) {

                table.addCell(val(
                        lead.getName()));

                table.addCell(val(
                        lead.getPhone()));

                table.addCell(val(
                        lead.getEmail()));

                table.addCell(val(
                        lead.getCity()));

                table.addCell(val(
                        lead.getDealStatus()));

                table.addCell(val(
                        lead.getSource()));
            }

            document.add(table);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(
                out.toByteArray());
    }

    private void addHeader(
            PdfPTable table,
            String text) {

        PdfPCell cell =
                new PdfPCell();

        cell.setBackgroundColor(
                Color.LIGHT_GRAY);

        cell.setPhrase(
                new Phrase(text));

        table.addCell(cell);
    }

    private String val(String s) {
        return s == null ? "-" : s;
    }
}