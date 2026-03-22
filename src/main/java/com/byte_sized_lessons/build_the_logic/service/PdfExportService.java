package com.byte_sized_lessons.build_the_logic.service;

import com.byte_sized_lessons.build_the_logic.dto.QuestionDto;
import com.byte_sized_lessons.build_the_logic.dto.WorksheetDto;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final WorksheetService worksheetService;

    public byte[] exportWorksheet(Long worksheetId) {
        WorksheetDto worksheet = worksheetService.getWorksheetById(worksheetId);
        return createPdf(worksheet);
    }

    private byte[] createPdf(WorksheetDto worksheet) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph(worksheet.metadata().title(), titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Topic: " + worksheet.metadata().topic(), bodyFont));
            document.add(new Paragraph("Difficulty: " + worksheet.metadata().difficulty(), bodyFont));
            document.add(new Paragraph("Instructions: " + worksheet.metadata().instructions(), bodyFont));
            document.add(new Paragraph("Estimated duration: " + worksheet.metadata().estimatedDurationMinutes() + " minutes", bodyFont));
            document.add(new Paragraph(" "));

            for (QuestionDto question : worksheet.questions()) {
                document.add(new Paragraph(question.order() + ". [" + question.type() + "] " + question.prompt(), bodyFont));
                document.add(new Paragraph(" "));
            }

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Failed to export worksheet to PDF", exception);
        }
    }
}
