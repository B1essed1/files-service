package uz.simplex.adliya.fileservice.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.service.FileUploadService;

@RequestMapping("/api/v1/upload")
@RestController
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public FileUploadResponse upload(@RequestPart MultipartFile multipartFile) {
        return fileUploadService.upload(multipartFile);
    }


}
