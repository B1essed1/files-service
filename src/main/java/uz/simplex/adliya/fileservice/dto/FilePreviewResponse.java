package uz.simplex.adliya.fileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilePreviewResponse implements Serializable {
    private byte[] response;
}
