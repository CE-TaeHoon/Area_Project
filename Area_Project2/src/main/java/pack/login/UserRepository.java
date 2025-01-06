package pack.login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 소셜 제공자 정보와 이메일로 사용자 검색
    Optional<User> findUserByEmailAndProvider(String email, String provider); 

    // 이메일만을 기준으로 사용자 검색 (일반 사용자를 위한 용도)
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    void deleteByEmail(String email);
}
