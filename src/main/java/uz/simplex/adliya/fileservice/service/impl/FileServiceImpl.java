package uz.simplex.adliya.fileservice.service.impl;

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
import uz.simplex.adliya.base.exception.ExceptionWithStatusCode;
import uz.simplex.adliya.fileservice.dto.FilePreviewResponse;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.entity.AttachedFiles;
import uz.simplex.adliya.fileservice.entity.FileEntity;
import uz.simplex.adliya.fileservice.repos.AttachedFileRepository;
import uz.simplex.adliya.fileservice.repos.FileRepository;
import uz.simplex.adliya.fileservice.service.FileService;
import uz.simplex.adliya.fileservice.service.QrGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static uz.simplex.adliya.fileservice.utils.Utils.*;


@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final QrGenerator qrGenerator;

    private final AttachedFileRepository attachedFileRepository;

    @Value("${file.base.url}")
    private String BASE_URL;

    public FileServiceImpl(FileRepository fileRepository, QrGenerator qrGenerator, AttachedFileRepository attachedFileRepository) {
        this.fileRepository = fileRepository;
        this.qrGenerator = qrGenerator;
        this.attachedFileRepository = attachedFileRepository;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file, Boolean isQr, String fileSha, String pkcs7) {
        if (Objects.nonNull(fileSha)){
            return attach(file, fileSha,pkcs7);
        }else {
            if (Boolean.FALSE.equals(isQr)) {
                return upload(file);
            } else {
                return uploadQr(file);
            }
        }
    }

    @Override
    public FilePreviewResponse preview(String code) {
        FileEntity fileEntity = fileRepository.findBySha256(code)
                .orElseThrow(() -> new ExceptionWithStatusCode(400, "file.not.found"));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL+"/api/file-service/v1/download")
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



            if (resource.exists()&&resource.isReadable()) {

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

    /***
     *
     * @param file
     * @param fileId
     * @param pkcs7
     * @return uploaded files Sha256 code
     */
    private FileUploadResponse attach(MultipartFile file, String fileId, String pkcs7) {
        try {
            FileEntity fileEntity = fileRepository.findBySha256(fileId)
                    .orElseThrow(() -> new ExceptionWithStatusCode(400, "file.not.found"));

            Path path = getPath(fileEntity, fileId);

            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                if (Objects.nonNull(pkcs7)) {
                    fileEntity.setPkcs7(pkcs7);
                    fileEntity =  fileRepository.save(fileEntity);
                }
                String attachedFileUrl = uploadFile(file,BASE_URL);
                AttachedFiles files = new AttachedFiles();
                attachedFileRepository.save(files.create(Long.valueOf(fileId),  fileEntity.getId()));
                return new FileUploadResponse(attachedFileUrl);
            }else {
                throw new ExceptionWithStatusCode(400, "file.not.found.error");
            }
        }catch (IOException e){
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

        MultipartFile qrImage =  qrGenerator.generate(url, file.getName());
        String qrUrl = uploadFile(qrImage, BASE_URL);

        return new FileUploadResponse(qrUrl);
    }

    private FileUploadResponse upload(MultipartFile file){
        return new FileUploadResponse(uploadFile(file,BASE_URL));
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
            throw new ExceptionWithStatusCode(400, "file.upload.error");
        }
        return saveEntity(file, code, path.toString(), baseUrl);
    }


    /**
     * saves the file data to db and returns download url for saved file
     */
    private String saveEntity(MultipartFile file, String fileName, String directory, String baseUrl) {
        return  fileRepository.save(new FileEntity().create(file, fileName, directory,baseUrl)).getInnerUrl();
    }

}
