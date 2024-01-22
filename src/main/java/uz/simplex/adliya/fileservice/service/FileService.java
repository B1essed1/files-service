package uz.simplex.adliya.fileservice.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.fileservice.dto.FilePreviewResponse;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;

public interface FileService {
    FileUploadResponse upload(MultipartFile file, Boolean isQr);
    FilePreviewResponse preview(String code);
    ResponseEntity<Resource> download(String code);

    FileUploadResponse attach(MultipartFile file, String code, String pkcs7);

}
