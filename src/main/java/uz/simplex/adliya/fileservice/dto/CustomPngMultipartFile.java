package uz.simplex.adliya.fileservice.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CustomPngMultipartFile implements MultipartFile {

    private final byte[] content;
    private final String filename;

    public CustomPngMultipartFile(byte[] content, String filename) {
        this.content = content;
        this.filename = filename;
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        // Set the content type based on your file type (e.g., "image/png")
        return MediaType.IMAGE_PNG_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes()  {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public Resource getResource() {
        return MultipartFile.super.getResource();
    }

    @Override
    public void transferTo(File dest) throws IllegalStateException {

    }

    @Override
    public void transferTo(java.nio.file.Path dest) throws  IllegalStateException {
        // Implement this method if needed
    }
}
