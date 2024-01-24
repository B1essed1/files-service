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


    public AttachedFiles create( Long fileId, Long attachedFileId) {
        this.fileId = fileId;
        this.attachedFileId = attachedFileId;
        return this;
    }
}
