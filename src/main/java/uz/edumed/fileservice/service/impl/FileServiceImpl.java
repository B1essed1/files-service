package uz.edumed.fileservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uz.edumed.fileservice.dto.FilePreviewResponse;
import uz.edumed.fileservice.dto.FileUploadResponse;
import uz.edumed.fileservice.exception.ExceptionWithStatusCode;
import uz.edumed.fileservice.repos.FileRepository;
import uz.edumed.fileservice.service.FileService;
import uz.edumed.fileservice.service.QrGenerator;
import uz.edumed.fileservice.entity.FileEntity;

import java.io.IOException;
import java.nio.file.Path;

import static uz.edumed.fileservice.utils.Utils.*;


@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final QrGenerator qrGenerator;

    @Value("${file.base.url}")
    private String BASE_URL;

    public FileServiceImpl(FileRepository fileRepository, QrGenerator qrGenerator) {
        this.fileRepository = fileRepository;
        this.qrGenerator = qrGenerator;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file, Boolean isQr) {
        if (Boolean.FALSE.equals(isQr)) {
            return upload(file);
        } else {
            return uploadQr(file);
        }
    }

    @Override
    public FilePreviewResponse preview(String code) {
        FileEntity fileEntity = fileRepository.findBySha256(code)
                .orElseThrow(() -> new ExceptionWithStatusCode(400, "file.not.found"));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL + "/api/file-service/v1/download")
                .queryParam("code", fileEntity.getSha256());


        return new FilePreviewResponse(
                uriBuilder.toUriString(),
                fileEntity.getExtension(),
                fileEntity.getOriginalName(),
                fileEntity.getFileSize()
        );
    }

    @Override
    public ResponseEntity<Resource> download(String code) {
        try {
            FileEntity file = fileRepository.findBySha256(code)
                    .orElseThrow(() -> new ExceptionWithStatusCode(400, "file.not.found"));

            Path path = getPath(file, code);

            Resource resource = new UrlResource(path.toUri());


            if (resource.exists() && resource.isReadable()) {

                byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.valueOf(file.getContentType()));
                headers.setContentDispositionFormData("attachment", file.getOriginalName());

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(new ByteArrayResource(bytes));

            } else {
                throw new ExceptionWithStatusCode(400, "file.not.found.error");
            }

        } catch (IOException e) {
            throw new ExceptionWithStatusCode(400, "file.not.found.error");
        }
    }


    /**
     * this method uploads file to server and uploaded files returns url
     * with returned url it generates a Qr image with PNG format of that url
     * and saves it to server and returns its
     */
    private FileUploadResponse uploadQr(MultipartFile file) {
        String url = uploadFile(file, BASE_URL);

        MultipartFile qrImage = qrGenerator.generate(url, file.getName());
        String qrUrl = uploadFile(qrImage, BASE_URL);

        return new FileUploadResponse(qrUrl);
    }

    private FileUploadResponse upload(MultipartFile file) {
        return new FileUploadResponse(uploadFile(file, BASE_URL));
    }

    /**
     * Uploads file to exact directory in the server
     */
    private String uploadFile(MultipartFile file, String baseUrl) {

        Path path = makeDir();

        String code = createSha256(file.getOriginalFilename() + System.currentTimeMillis());

        //creating final directory with code in directory
        Path filePath = filePath(path, file, code);

        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ExceptionWithStatusCode(400, "file.upload.error");
        }
        return saveEntity(file, code, path.toString(), baseUrl);
    }


    /**
     * saves the file data to db and returns download url for saved file
     */
    private String saveEntity(MultipartFile file, String fileName, String directory, String baseUrl) {
        return fileRepository.save(new FileEntity().create(file, fileName, directory, baseUrl)).getInnerUrl();
    }

}
