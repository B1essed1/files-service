package uz.simplex.adliya.fileservice.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.simplex.adliya.fileservice.entity.FileEntity;

import java.util.Optional;


public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findBySha256(String sha256);
    void deleteAllBySha256(String sha256);
}
