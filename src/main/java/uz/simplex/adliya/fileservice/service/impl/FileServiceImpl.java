package uz.simplex.adliya.fileservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uz.simplex.adliya.base.exception.ExceptionWithStatusCode;
import uz.simplex.adliya.fileservice.dto.FilePreviewResponse;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.entity.FileEntity;
import uz.simplex.adliya.fileservice.repos.FileRepository;
import uz.simplex.adliya.fileservice.service.FileService;
import uz.simplex.adliya.fileservice.service.QrGenerator;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Objects;

import static uz.simplex.adliya.fileservice.utils.CONSTANTS.BASE_DIR;


@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final QrGenerator qrGenerator;

    public FileServiceImpl(FileRepository fileRepository, QrGenerator qrGenerator) {
        this.fileRepository = fileRepository;
        this.qrGenerator = qrGenerator;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file, Boolean isQr) {
        if (!isQr) {
            return new FileUploadResponse(uploadFile(file));
        } else {
            return uploadQr(file);
        }
    }

    @Override
    public FilePreviewResponse preview(String code) {
        FileEntity fileEntity = fileRepository.findBySha256(code)
                .orElseThrow(() -> new ExceptionWithStatusCode(400, "file.not.found"));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://165.232.122.8:50000/api/file-service/v1/download")
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
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.valueOf(file.getContentType()));
                headers.setContentDispositionFormData("attachment", file.getOriginalName());

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);

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
        String url = uploadFile(file);
        return new FileUploadResponse(uploadFile(qrGenerator.generate(url, file.getName())));
    }


    /**
     * Uploads file to exact directory in the server
     */
    private String uploadFile(MultipartFile file) {

        Path path = makeDir();

        String code = createSha256(file.getOriginalFilename() + System.currentTimeMillis());

        //creating final directory with code in directory
        Path filePath = filePath(path, file, code);

        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new ExceptionWithStatusCode(400, "file.upload.error");
        }
        return saveEntity(file, code, path.toString());
    }


    /**
     * saves the file data to db and returns download url for saved file
     */
    private String saveEntity(MultipartFile file, String fileName, String directory) {
        FileEntity entity = new FileEntity();
        entity = fileRepository.save(entity.create(file, fileName, directory));
        return entity.getInnerUrl();
    }

    private String createSha256(String word) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("createSha256 { }",e);
            throw new RuntimeException(e);
        }
        byte[] digest = md.digest(word.getBytes(StandardCharsets.UTF_8));

        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }


    /**
     * Base directory da yangi path yaratadi
     * year
     * --month
     * ------day
     * korinishida, path kunda bir marta yaraladi
     * agar bolsa tekshirib shu bor path qaytariladi
     */

    private Path makeDir() {
        LocalDateTime now = LocalDateTime.now();

        String year = String.valueOf(now.getYear());
        String month = now.getMonth().name();
        String day = String.valueOf(now.getDayOfMonth());
        Path path = Path.of(BASE_DIR, year, month, day);

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            return path;
        } catch (Exception e) {
            throw new ExceptionWithStatusCode(400, "file.dir.create.error");
        }

    }


    /**
     * Final path where file goes
     * it is used after   @link{makeDir()} method
     */
    private Path filePath(Path path, MultipartFile file, String code) {

        String fileOriginalName = file.getOriginalFilename();
        String ext = Objects.requireNonNull(fileOriginalName).substring(fileOriginalName.lastIndexOf(".") + 1);
        String fileName = code + "." + ext;
        return Path.of(path.toString(), fileName);
    }


    /*
     * get file path for download file with filename
     */
    private Path getPath(FileEntity file, String code) {

        String filePath = file.getPath();
        String ext = file.getExtension();
        String fileName = code + "." + ext;

        return Path.of(filePath, fileName);
    }
}
