package pack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pack.domain.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    // email과 address로 기존 평가 찾기
    Optional<Rating> findByEmailAndAddress(String email, String address);
    List<Rating> findByAddress(String address);
    
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.address = :address")
    Optional<Double> findAverageScoreByAddress(String address);

    @Modifying
    @Query("UPDATE Rating r SET r.username = :newUsername WHERE r.email = :email")
    void updateUsernameByEmail(String email, String newUsername);
}