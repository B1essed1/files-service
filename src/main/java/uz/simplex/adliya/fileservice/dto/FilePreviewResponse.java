package uz.simplex.adliya.fileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilePreviewResponse implements Serializable {

    private String downloadUri;

    private String ext;

    private String name;

    private String size;
}
