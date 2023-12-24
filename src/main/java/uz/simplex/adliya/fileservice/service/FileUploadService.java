package uz.simplex.adliya.fileservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uz.simplex.adliya.base.exception.ExceptionWithStatusCode;
import uz.simplex.adliya.fileservice.dto.CustomPngMultipartFile;
import uz.simplex.adliya.fileservice.dto.FilePreviewResponse;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.entity.FileEntity;
import uz.simplex.adliya.fileservice.repos.FileRepository;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class FileUploadService {

    private final FileRepository fileRepository;

    @Value("${file.server.directory}")
    private String directoryName;

    public FileUploadService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
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

    public String uploadFile(MultipartFile file) {
        try {

            FileEntity entity = new FileEntity();
            Date currentDate = new Date();


            // Create date format to extract year, month, and day
            String dayDirectory = makeDirectory(currentDate);

            // Upload a file
            InputStream inputStream = file.getInputStream();

            String fileName = String.valueOf(System.currentTimeMillis());

            boolean success = storeFile(Path.of(dayDirectory + "/" + fileName), inputStream);

            inputStream.close();

            String previewUrl;

            if (success) {
                String sha256Hash = createSha256(file.getOriginalFilename() + System.currentTimeMillis());

                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://165.232.122.8:50000/api/file-service/v1/download")
                        .queryParam("code", sha256Hash);

                previewUrl = uriBuilder.toUriString();

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
            } else {
                throw new ExceptionWithStatusCode(400, "file.upload.failed");
            }

            return previewUrl;

        } catch (IOException e) {
            throw new ExceptionWithStatusCode(400, "file.upload.failed");
        }
    }

    private boolean storeFile(Path target, InputStream inputStream) {
        try {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public FileUploadResponse uploadFileAndQr(MultipartFile file, Boolean isQr) {
        if (!isQr) {
            return new FileUploadResponse(uploadFile(file));
        } else {
            String fileName = "qr" + file.getName();
            MultipartFile qrFile = generateQr(uploadFile(file), fileName);
            return new FileUploadResponse(uploadFile(qrFile));
        }

    }

    private String makeDirectory(Date currentDate) throws IOException {
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
        SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");

        // Extract year, month, and day
        String year = dateFormatYear.format(currentDate);
        String month = dateFormatMonth.format(currentDate);
        String day = dateFormatDay.format(currentDate);

        String yearDirectory = directoryName + "/" + year;

        String monthDirectory = yearDirectory + "/" + month;


        String dayDirectory = monthDirectory + "/" + day;
        Files.createDirectories(Path.of(dayDirectory));

        return dayDirectory;
    }

    public FileUploadResponse upload(MultipartFile file) {
        return new FileUploadResponse(uploadFile(file));
    }

    public ResponseEntity<Resource> download(String code) {
        FileEntity fileEntity = fileRepository.findBySha256(code)
                .orElseThrow(() -> new ExceptionWithStatusCode(400, "file.not.found"));

        String pathOfFile = fileEntity.getPath();
        Path absolutePath = Path.of(pathOfFile + "/" + fileEntity.getName());

        try {
            byte[] fileData = Files.readAllBytes(absolutePath);
            Resource resource = new ByteArrayResource(fileData);
            String mimeType = fileEntity.getContentType();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileEntity.getOriginalName());

            headers.setContentType(MediaType.valueOf(mimeType));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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


    public MultipartFile generateQr(String url, String fileName) {
        try {
            BufferedImage image = generateQRCodeImage(url);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            return new CustomPngMultipartFile(baos.toByteArray(), fileName + ".png");
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage generateQRCodeImage(String text) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);

        // Set error correction level to H (highest)
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300, hints);

        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }
}
