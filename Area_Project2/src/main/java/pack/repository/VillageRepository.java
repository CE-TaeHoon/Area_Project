package pack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pack.domain.Village;

import java.util.List;

public interface VillageRepository extends JpaRepository<Village, Long> {
    List<Village> findByDistrictId(Long districtId); // 특정 구의 동을 찾기 위한 메소드
}