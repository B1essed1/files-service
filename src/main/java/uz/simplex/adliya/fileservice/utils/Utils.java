package uz.simplex.adliya.fileservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import uz.simplex.adliya.base.exception.ExceptionWithStatusCode;
import uz.simplex.adliya.fileservice.entity.FileEntity;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Objects;

import static uz.simplex.adliya.fileservice.utils.CONSTANTS.BASE_DIR;

@Slf4j
public class Utils {
    public static String    createSha256(String word) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("createSha256 { }", e);
            // TODO: 12/26/2023 Should we hande properly or is that good 
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

    public static Path makeDir() {
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
    public static Path filePath(Path path, MultipartFile file, String code) {

        String fileOriginalName = file.getOriginalFilename();
        String ext = Objects.requireNonNull(fileOriginalName).substring(fileOriginalName.lastIndexOf(".") + 1);
        String fileName = code + "." + ext;
        return java.nio.file.Path.of(path.toString(), fileName);
    }


    /**
     * get file path for download file with filename
     */
    public static Path getPath(FileEntity file, String code) {

        String filePath = file.getPath();
        String ext = file.getExtension();
        String fileName = code + "." + ext;

        return java.nio.file.Path.of(filePath, fileName);
    }
}
