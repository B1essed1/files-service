package uz.edumed.fileservice.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.edumed.fileservice.entity.FileEntity;

import java.util.Optional;


public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findBySha256(String sha256);
}
