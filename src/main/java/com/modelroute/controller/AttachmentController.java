package com.modelroute.controller;

import com.modelroute.dto.AttachmentResponse;
import com.modelroute.service.AttachmentService;
import com.modelroute.service.DesktopFolderPickerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final DesktopFolderPickerService filePickerService;

    public AttachmentController(
            AttachmentService attachmentService,
            DesktopFolderPickerService filePickerService) {
        this.attachmentService = attachmentService;
        this.filePickerService = filePickerService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.store(file));
    }

    @PostMapping("/pick")
    public ResponseEntity<AttachmentResponse> pick() {
        return filePickerService.chooseFile()
                .map(java.io.File::toPath)
                .map(attachmentService::store)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
