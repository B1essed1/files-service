package uz.simplex.adliya.fileservice.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.simplex.adliya.base.entity.AbstractAuditingEntity;

import javax.persistence.*;

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
