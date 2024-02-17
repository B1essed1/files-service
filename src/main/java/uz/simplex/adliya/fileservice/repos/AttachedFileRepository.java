package uz.simplex.adliya.fileservice.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.simplex.adliya.fileservice.entity.AttachedFiles;

public interface AttachedFileRepository extends JpaRepository<AttachedFiles, Long> {
}
