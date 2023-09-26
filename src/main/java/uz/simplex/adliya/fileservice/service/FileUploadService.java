package uz.simplex.adliya.fileservice.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.base.exception.ExceptionWithStatusCode;
import uz.simplex.adliya.fileservice.dto.FileUploadResponse;
import uz.simplex.adliya.fileservice.entity.File;
import uz.simplex.adliya.fileservice.repos.FileRepository;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service

public class FileUploadService {


    private final FileRepository fileRepository;

    public FileUploadService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Value("file.server.url")
    private String url;
    @Value("file.server.port")
    private String port;
    @Value("file.server.username")
    private String username;
    @Value("file.server.password")
    private String password;

    @Value("file.server.directory")
    private String directoryName;

    private String createSha256(String word) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest = md.digest(word.getBytes(StandardCharsets.UTF_8));
        String sha256 = DatatypeConverter.printHexBinary(digest).toLowerCase();

        return sha256;
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

            // Create date format to extract year, month, and day
            SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
            SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");

            // Extract year, month, and day
            String year = dateFormatYear.format(currentDate);
            String month = dateFormatMonth.format(currentDate);
            String day = dateFormatDay.format(currentDate);

            File entity = new File();
            String remoteDirectory = "/" + year + "/" + month + "/" + day;


            // Specify the remote directory structure (e.g., /year/month/day)

            // Ensure the remote directory exists, create it if needed
            createRemoteDirectory(ftpClient, remoteDirectory);

            // Upload a file
            String originalName = file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean success = ftpClient.storeFile(originalName, inputStream);
            inputStream.close();
            String previewUrl = "";
            String downloadUrl;
            String sha256Hash="";
            if (success) {
                // Generate SHA-256 hash for the file
                 sha256Hash = createSha256(file.getName());

                // Construct preview and download URLs
                previewUrl = url + "/preview/" + sha256Hash;
                downloadUrl = url + "/download/" + sha256Hash;

                System.out.println("Preview URL: " + previewUrl);
                System.out.println("Download URL: " + downloadUrl);
                entity.setName(file.getName());
                entity.setFileSize(String.valueOf(file.getSize()));
                entity.setContentType(file.getContentType());
                entity.setPath(remoteDirectory);
                entity.setSha256(sha256Hash);
                entity.setOriginalName(file.getOriginalFilename());
                entity.setHashId(null);
                entity.setInnerUrl(previewUrl);
                // TODO: 26/09/23 ask this column
                fileRepository.save(entity);
            } else {
                throw new ExceptionWithStatusCode(400, "File.upload.failed");
            }

            // Disconnect from the FTP server
            ftpClient.logout();
            ftpClient.disconnect();
            return previewUrl;

        } catch (IOException e) {
            throw new ExceptionWithStatusCode(400, "File.upload.failed");
        }


    }

    private static void createRemoteDirectory(FTPClient ftpClient, String remoteDirectory) throws IOException {
        if (!directoryExists(ftpClient, remoteDirectory)) {
            boolean b = ftpClient.makeDirectory(remoteDirectory);
            if (!b) {
                throw new IOException("Failed to create remote directory: " + remoteDirectory);

            }
        }
        if (!ftpClient.changeWorkingDirectory(remoteDirectory)) {
            throw new IOException("Failed to create remote directory: " + remoteDirectory);
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
}
