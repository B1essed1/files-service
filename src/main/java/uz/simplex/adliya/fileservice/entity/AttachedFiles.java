package uz.simplex.adliya.fileservice.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uz.simplex.adliya.base.entity.AbstractAuditingEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

import static uz.simplex.adliya.fileservice.utils.CONSTANTS.BASE_URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attached_files")
public class AttachedFiles extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "attached_file_id", nullable = false)
    private Long attachedFileId;

    @Column(name = "file_sha256")
    private String fileSha256;

    @Column(name = "attached_file_sha256")
    private String attachedFileSha256;

    public AttachedFiles create( String fileId, String fileSha256, String attachedFileId, String attachedFileSha256) {
        this.fileId = Long.valueOf(fileId);
        this.fileSha256 = fileSha256;
        this.attachedFileId=  Long.valueOf(attachedFileId);
        this.attachedFileSha256 = attachedFileSha256;
        return this;
    }
}
