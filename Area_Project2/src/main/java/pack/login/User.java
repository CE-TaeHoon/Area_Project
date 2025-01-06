package pack.login;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Getter
@DynamicUpdate // Entity update시, 원하는 데이터만 update하기 위함
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @Column(name = "username", nullable = false)
    private String username; // 로그인한 사용자의 이름
    
    @Column(name = "email", nullable = false)
    private String email; // 로그인한 사용자의 이메일
    
    @Column(name = "provider", nullable = true)
    private String provider; // 사용자가 로그인한 서비스(ex) google, naver..)
    
    @Column(name = "password", nullable = false)
    private String password; // 사용자의 비밀번호
    
    @Column(nullable = false)
    private String role = "ROLE_USER";  // 기본값은 일반 사용자
    
    // 사용자의 이름이나 이메일을 업데이트하는 메소드
    public User updateUser(String username) {
        this.username = username;
        return this;
    }
    
    // 비밀번호 업데이트 메소드
    public User updatePassword(String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
}
