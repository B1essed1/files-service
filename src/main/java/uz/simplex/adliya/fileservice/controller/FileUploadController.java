package uz.simplex.adliya.fileservice.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.fileservice.dto.FilePreviewResponse;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.service.FileService;

@RequestMapping("/api/file-service/v1")
@RestController
public class FileUploadController {

    private final FileService fileService;

    public FileUploadController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public FileUploadResponse upload(@RequestPart MultipartFile multipartFile,
                                     @RequestParam(value = "isQr", required = false, defaultValue = "false") Boolean isQr,
                                     @RequestParam(value = "pkcs7",required = false) String pkcs7,
                                     @RequestParam(value = "fileId" , required = false) String fileId) {
        return fileService.upload(multipartFile, isQr,fileId,pkcs7);
    }

    @GetMapping("/preview")
    public FilePreviewResponse preview(@RequestParam String code) {
        return fileService.preview(code);
    }

    @GetMapping("/download")
    public FilePreviewResponse download(@RequestParam String code) {
        return fileService.preview(code);
    }
}
