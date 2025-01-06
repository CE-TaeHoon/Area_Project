package pack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pack.domain.Favorite;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByEmailAndAddress(String email, String address);
    List<Favorite> findByEmail(String email);
    
    boolean existsByEmailAndAddress(String email, String address);
    List<Favorite> findByEmailOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("UPDATE Favorite f SET f.username = :newUsername WHERE f.email = :email")
    void updateUsernameByEmail(String email, String newUsername);
}