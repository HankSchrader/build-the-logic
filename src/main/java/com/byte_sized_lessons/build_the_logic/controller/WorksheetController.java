package com.byte_sized_lessons.build_the_logic.controller;

import com.byte_sized_lessons.build_the_logic.dto.GenerateWorksheetRequest;
import com.byte_sized_lessons.build_the_logic.dto.WorksheetResponse;
import com.byte_sized_lessons.build_the_logic.service.PdfExportService;
import com.byte_sized_lessons.build_the_logic.service.WorksheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/worksheets")
@RequiredArgsConstructor
public class WorksheetController {

    private final WorksheetService worksheetService;
    private final PdfExportService pdfExportService;

    @PostMapping("/generate")
    public ResponseEntity<WorksheetResponse> generateWorksheet(@Valid @RequestBody GenerateWorksheetRequest request) {
        return ResponseEntity.ok(worksheetService.generateWorksheet(request));
    }

    @PostMapping
    public ResponseEntity<WorksheetResponse> createWorksheet(@Valid @RequestBody GenerateWorksheetRequest request) {
        return ResponseEntity.ok(worksheetService.generateAndSaveWorksheet(request));
    }

    @GetMapping("/{worksheetId}")
    public ResponseEntity<WorksheetResponse> getWorksheet(@PathVariable Long worksheetId) {
        return ResponseEntity.ok(worksheetService.getWorksheetById(worksheetId));
    }

    @GetMapping(value = "/{worksheetId}/export", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportWorksheet(@PathVariable Long worksheetId) {
        byte[] pdfBytes = pdfExportService.exportWorksheet(worksheetId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("worksheet-%d.pdf".formatted(worksheetId)).build());

        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}
