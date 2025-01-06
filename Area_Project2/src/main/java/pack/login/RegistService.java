package pack.login;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.annotation.PostConstruct;

@Controller
public class RegistService {

    // 사용자 정보 관리를 위한 UserRepository와 비밀번호 암호화를 위한 PasswordEncoder 선언
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자
    public RegistService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입 처리를 담당하는 메서드.
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 회원가입 완료 후 메인 페이지로 리다이렉트, 실패 시 에러 메시지와 함께 회원가입 페이지로 리다이렉트
     */
    @PostMapping("/register")
    public RedirectView register(@RequestParam String email, @RequestParam String password,RedirectAttributes redirectAttributes) {
        // 이메일 중복 확인
        if (userRepository.findByEmail(email).isPresent()) {
            // 이미 해당 이메일이 존재할 경우, 에러 메시지를 붙여 회원가입 페이지로 리다이렉트
            return new RedirectView("/register?error=exists");
        }

        String role = "ROLE_USER";
        // 관리자 이메일인 경우 ROLE_ADMIN 부여
        if (email.equals("admin@example.com")) {
            role = "ROLE_ADMIN";
        }

        // 새로운 사용자 생성 (빌더 패턴 사용으로 객체 생성)
        User user = User.builder()
                .email(email) // 사용자 이메일 설정
                .password(passwordEncoder.encode(password)) // 비밀번호 암호화 후 설정
                .provider(null) // 일반 사용자를 위해 소셜 타입은 null로 설정
                .username(email.split("@")[0]) // 기본 username 설정, 이메일의 @ 앞 부분을 username으로 설정
                .role(role)
                .build();

        // 데이터베이스에 사용자 정보 저장
        userRepository.save(user);
        
        // 회원가입 성공 시 로그인 페이지(/login)로 리다이렉트
        return new RedirectView("/login?message=success");
    }

    @PostConstruct
    public void init() {
        if (!userRepository.findByEmail("admin@example.com").isPresent()) {
            User adminUser = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin"))
                    .username("관리자")
                    .role("ROLE_ADMIN")
                    .provider(null)
                    .build();
            userRepository.save(adminUser);
        }
    }
}
