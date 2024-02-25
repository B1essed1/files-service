package uz.edumed.fileservice.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.edumed.fileservice.dto.FilePreviewResponse;
import uz.edumed.fileservice.dto.FileUploadResponse;
import uz.edumed.fileservice.service.FileService;

@RequestMapping("/api/file-service/v1")
@RestController
public class FileUploadController {

    private final FileService fileService;

    public FileUploadController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public FileUploadResponse upload(@RequestPart MultipartFile multipartFile,
                                     @RequestParam(value = "isQr", required = false, defaultValue = "false") Boolean isQr) {
        return fileService.upload(multipartFile, isQr);
    }

    @GetMapping("/preview")
    public FilePreviewResponse preview(@RequestParam String code) {
        return fileService.preview(code);
    }

    @GetMapping(value = "/download")
    public ResponseEntity<Resource> download(@RequestParam String code) {
        return fileService.download(code);
    }
}
