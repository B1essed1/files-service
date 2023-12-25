package uz.simplex.adliya.fileservice.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.fileservice.dto.FilePreviewResponse;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.service.FileService;
import uz.simplex.adliya.fileservice.service.FileUploadService;

@RequestMapping("/api/file-service/v1")
@RestController
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final FileService fileService;

    public FileUploadController(FileUploadService fileUploadService, FileService fileService) {
        this.fileUploadService = fileUploadService;
        this.fileService = fileService;
    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public FileUploadResponse upload(@RequestPart MultipartFile multipartFile,
                                     @RequestParam(value = "isQr", required = false, defaultValue = "false") Boolean isQr) {
        return fileService.upload(multipartFile, isQr);
    }

    @GetMapping("/preview")
    public FilePreviewResponse preview(@RequestParam String code) {
        return fileUploadService.preview(code);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String code) {
         return fileUploadService.download(code);
    }

}
