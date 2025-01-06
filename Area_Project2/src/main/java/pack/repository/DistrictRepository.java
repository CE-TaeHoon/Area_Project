package pack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.domain.District;

public interface DistrictRepository extends JpaRepository<District, Long> {
}