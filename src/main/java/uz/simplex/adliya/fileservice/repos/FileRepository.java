package uz.simplex.adliya.fileservice.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.simplex.adliya.fileservice.entity.File;


public interface FileRepository extends JpaRepository<File, Long> {

}
