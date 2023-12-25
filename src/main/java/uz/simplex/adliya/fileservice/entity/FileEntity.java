package uz.simplex.adliya.fileservice.entity;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uz.simplex.adliya.base.entity.AbstractAuditingEntity;

import javax.persistence.*;
import java.util.Objects;

import static uz.simplex.adliya.fileservice.utils.CONSTANTS.BASE_URL;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "files")
public class FileEntity extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "extension",columnDefinition="VARCHAR(250)")
    private String extension;

    @Column(name = "content_type",columnDefinition="VARCHAR(250)")
    private String contentType;

    @Column(name = "file_size",columnDefinition="VARCHAR(250)")
    private String fileSize;

    @Column(name = "hash_id",columnDefinition="VARCHAR(250)")
    private String hashId;

    @Column(name = "name",columnDefinition="VARCHAR(250)")
    private String name;

    @Column(name = "original_name",columnDefinition="VARCHAR(250)")
    private String originalName;

    @Column(name = "path",columnDefinition="VARCHAR(250)")
    private String path;

    @Column(name = "inner_url",columnDefinition="VARCHAR(1000)")
    private String innerUrl;

    @Column(name = "additional",columnDefinition="VARCHAR(250)")
    private String additional;

    @Column(name = "sha256",columnDefinition="VARCHAR(250)")
    private String sha256;



    public FileEntity create(MultipartFile file, String fileName, String directory) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL + "/api/file-service/v1/download")
                .queryParam("code", fileName);

        String previewUrl = uriBuilder.toUriString();

        String originalFilename = file.getOriginalFilename();

        this.setExtension(Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf(".") + 1));
        this.setName(file.getName());
        this.setFileSize(String.valueOf(file.getSize()));
        this.setContentType(file.getContentType());
        this.setPath(directory);
        this.setSha256(fileName);
        this.setOriginalName(originalFilename);
        this.setHashId(null);
        this.setInnerUrl(previewUrl);
        return this;
    }

}
