package uz.simplex.adliya.fileservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uz.simplex.adliya.fileservice.dto.FilePreviewResponse;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.entity.FileEntity;
import uz.simplex.adliya.fileservice.repos.FileRepository;
import uz.simplex.adliya.fileservice.service.FileService;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Objects;

import static uz.simplex.adliya.fileservice.utils.CONSTANTS.BASE_URL;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file, Boolean isQr) {

        return null;
    }

    @Override
    public FilePreviewResponse preview(String code) {
        return null;
    }

    private boolean saveEntity(MultipartFile file, String fileName, String dayDirectory){
        FileEntity entity = new FileEntity();
        String sha256Hash = createSha256(file.getOriginalFilename() + System.currentTimeMillis());

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL+"/api/file-service/v1/download")
                .queryParam("code", sha256Hash);

        String previewUrl = uriBuilder.toUriString();

        String originalFilename = file.getOriginalFilename();
        entity.setExtension(Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf(".") + 1));
        entity.setName(fileName);
        entity.setFileSize(String.valueOf(file.getSize()));
        entity.setContentType(file.getContentType());
        entity.setPath(dayDirectory);
        entity.setSha256(sha256Hash);
        entity.setOriginalName(originalFilename);
        entity.setHashId(null);
        entity.setInnerUrl(previewUrl);
        fileRepository.save(entity);
        return true;
    }



    private String createSha256(String word) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest = md.digest(word.getBytes(StandardCharsets.UTF_8));

        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }


    private String makeDir(){
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = now.getMonth().name();
        String day = String.valueOf(now.getDayOfMonth());
        Path path = Paths.get(String.format("%s/%s/%s", year, month, day));
       if (Files.exists(path)){
           return path.toString();
       } else {
           try {
               Files.createDirectories(path);
               return path.toString();
           } catch (Exception e){
               log.error("FILE DIRECTORY CREATE { }", e);
           }
       }
       return "";
    }
}
