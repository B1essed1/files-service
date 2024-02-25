package uz.edumed.fileservice.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uz.edumed.fileservice.dto.FilePreviewResponse;
import uz.edumed.fileservice.dto.FileUploadResponse;

public interface FileService {
    FileUploadResponse upload(MultipartFile file, Boolean isQr);
    FilePreviewResponse preview(String code);
    ResponseEntity<Resource> download(String code);

}
