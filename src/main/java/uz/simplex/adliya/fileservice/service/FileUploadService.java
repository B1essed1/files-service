package uz.simplex.adliya.fileservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    public FileUploadService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Value("${file.server.url}")
    private String url;

    @Value("${file.server.port}")
    private String port;

    @Value("${file.server.username}")
    private String username;

    @Value("${file.server.password}")
    private String password;

    @Value("${file.server.directory}")
    private String directoryName;

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
            // Connect to FTP server
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(url, Integer.parseInt(port));
            ftpClient.login(username, password);

            // Specify the remote directory on the FTP server
            ftpClient.changeWorkingDirectory(directoryName);
            Date currentDate = new Date();
            FileEntity entity = new FileEntity();


            // Create date format to extract year, month, and day
            String dayDirectory = makeDirectory(ftpClient, currentDate);

            // Upload a file
            InputStream inputStream = file.getInputStream();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String fileName = String.valueOf(System.currentTimeMillis());

            boolean success = ftpClient.storeFile(fileName, inputStream);

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

            // Disconnect from the FTP server
            ftpClient.logout();
            ftpClient.disconnect();
            return previewUrl;

        } catch (IOException e) {
            throw new ExceptionWithStatusCode(400, "file.upload.failed");
        }
    }


    public FileUploadResponse uploadFileAndQr(MultipartFile file, Boolean isQr){
        if (!isQr){
            return new FileUploadResponse(uploadFile(file));
        } else {
            String fileName ="qr"+ file.getName();
            MultipartFile qrFile = generateQr(uploadFile(file),fileName);
            return new FileUploadResponse(uploadFile(qrFile));
        }

    }


    private String makeDirectory(FTPClient ftpClient, Date currentDate) throws IOException {
        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
        SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");

        // Extract year, month, and day
        String year = dateFormatYear.format(currentDate);
        String month = dateFormatMonth.format(currentDate);
        String day = dateFormatDay.format(currentDate);

        String yearDirectory = directoryName + "/" + year;
        createRemoteDirectory(ftpClient, year);
        String monthDirectory = yearDirectory + "/" + month;
        createRemoteDirectory(ftpClient, month);

        String dayDirectory = monthDirectory + "/" + day;
        createRemoteDirectory(ftpClient, day);
        return dayDirectory;
    }

    private static void createRemoteDirectory(FTPClient ftpClient, String remoteDirectory) throws IOException {
        if (!directoryExists(ftpClient, remoteDirectory)) {
            boolean b = ftpClient.makeDirectory(remoteDirectory);
            if (!b) {
                throw new ExceptionWithStatusCode(400, "file.upload.directory.create.failed");

            }
        }
        if (!ftpClient.changeWorkingDirectory(remoteDirectory)) {
            throw new ExceptionWithStatusCode(400, "file.upload.directory.create.failed");
        }

    }

    private static boolean directoryExists(FTPClient ftpClient, String directoryName) throws IOException {
        String[] directories = ftpClient.listNames();
        if (directories != null) {
            for (String dir : directories) {
                if (dir.equals(directoryName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public FileUploadResponse upload(MultipartFile file) {
        return new FileUploadResponse(uploadFile(file));

    }

    public ResponseEntity<Resource> download(String code) {
        FileEntity fileEntity = fileRepository.findBySha256(code)
                .orElseThrow(() -> new ExceptionWithStatusCode(400, "file.not.found"));

        try {

            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(url);
            ftpClient.login(username, password);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean success = ftpClient.retrieveFile(fileEntity.getPath() + "/" + fileEntity.getName(), outputStream);
            ftpClient.logout();
            ftpClient.disconnect();


            if (success) {
                byte[] fileData = outputStream.toByteArray();
                Resource resource = new ByteArrayResource(fileData);
                String mimeType = fileEntity.getContentType();

                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileEntity.getOriginalName());

                headers.setContentType(MediaType.valueOf(mimeType));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);


            } else {
                return null;
            }
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


    public MultipartFile generateQr(String url, String fileName){
        try {
            BufferedImage image = generateQRCodeImage(url);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            return new CustomPngMultipartFile(baos.toByteArray(),fileName+".png");
        }catch (Exception e){
            return null;
        }
    }

    private  BufferedImage generateQRCodeImage(String text) throws WriterException {
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
