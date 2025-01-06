package pack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pack.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAddress(String address);
    
    List<Comment> findByEmailAndAddress(String email, String address);
    
    @Modifying
    @Query("UPDATE Comment c SET c.username = :newUsername WHERE c.email = :email")
    void updateUsernameByEmail(String email, String newUsername);
}