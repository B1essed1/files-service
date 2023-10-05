package uz.simplex.adliya.fileservice.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.dto.SimpleResponse;
import uz.simplex.adliya.fileservice.service.FileUploadService;

@RequestMapping("/api/file/v1/upload")
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

    @GetMapping("hi")
    public SimpleResponse hi() {
        return new SimpleResponse();
    }


}
