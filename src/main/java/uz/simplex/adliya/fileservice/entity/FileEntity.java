package uz.simplex.adliya.fileservice.entity;

import lombok.*;
import uz.simplex.adliya.base.entity.AbstractAuditingEntity;

import javax.persistence.*;


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

}
