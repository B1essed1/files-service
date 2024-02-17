package uz.simplex.adliya.fileservice.service;


import org.springframework.web.multipart.MultipartFile;

public interface QrGenerator {
    MultipartFile generate(String url, String name);

}
