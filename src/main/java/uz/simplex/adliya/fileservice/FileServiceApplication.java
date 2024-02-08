package uz.simplex.adliya.fileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "uz.simplex.adliya",  exclude = {
        MultipartAutoConfiguration.class } )
@EnableDiscoveryClient
@EnableAsync
// excluded so that the application uses commons-fileupload instead of Servlet 3 Multipart support

public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }

}
