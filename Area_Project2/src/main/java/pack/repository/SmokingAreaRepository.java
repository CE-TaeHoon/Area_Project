package pack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pack.domain.SmokingArea;

public interface SmokingAreaRepository extends JpaRepository<SmokingArea, Long> {
	@Query(value = "SELECT * FROM smoking_area sa WHERE sa.latitude BETWEEN :swLat AND :neLat AND sa.longitude BETWEEN :swLng AND :neLng", nativeQuery = true)
    List<SmokingArea> findWithinBounds(@Param("swLat") double swLat, @Param("swLng") double swLng, @Param("neLat") double neLat, @Param("neLng") double neLng);
}
