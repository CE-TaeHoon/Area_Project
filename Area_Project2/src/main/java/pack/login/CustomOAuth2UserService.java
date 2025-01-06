package pack.login;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // 생성자를 
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * OAuth2 로그인 시 사용자 정보를 로드하고, 데이터베이스에 사용자 정보를 저장하거나 업데이트하는 메서드.
     * 
     * @param userRequest OAuth2UserRequest - 소셜 로그인 요청 정보
     * @return OAuth2User - 인증된 사용자 정보를 담은 객체
     */
    @Override
    @Transactional // 데이터 일관성을 위해 트랜잭션 처리, 오류 발생 시 롤백
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 기본 OAuth2UserService의 loadUser 메서드를 호출하여 OAuth2User 객체 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 소셜 로그인 제공자(Google, Kakao 등) 정보를 가져와서 소셜 타입으로 저장
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = null;
        String username = null;

        if ("kakao".equals(provider)) {
            // 카카오 계정의 사용자 정보에서 이메일과 닉네임 가져오기
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");

                // profile 객체 안의 nickname 가져오기
                @SuppressWarnings("unchecked")
				Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    username = (String) profile.get("nickname");
                }
            }
        } else if ("google".equals(provider)) {
            email = oAuth2User.getAttribute("email");
            username = oAuth2User.getAttribute("name");
        }
        // 필요에 따라 다른 제공자에 대한 조건 추가 가능

        System.out.println("로그인 시도 - 이메일: " + email + ", 이름: " + username + ", 소셜 타입: " + provider);

        User user;
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            String defaultPassword = "";
            user = User.builder()
                    .email(email)
                    .username(username)
                    .provider(provider)
                    .password(defaultPassword)
                    .role("ROLE_USER")
                    .build();
            user = userRepository.save(user);
        } else {
            user = userOptional.get();
            user.updateUser(username);
            user = userRepository.save(user);
        }

        // CustomOAuth2User 객체를 반환
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}