package pack.login;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pack.repository.CommentRepository;
import pack.repository.FavoriteRepository;
import pack.repository.RatingRepository;

import org.springframework.security.core.Authentication;

import java.util.Optional;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;

    public CustomUserDetailsService(UserRepository userRepository, 
                                  RatingRepository ratingRepository,
                                  CommentRepository commentRepository,
                                  FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.commentRepository = commentRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        return new CustomUserDetails(user);
    }

    // username 업데이트 메소드
    public boolean updateUsername(String email, String newUsername) {
        // 현재 로그인한 사용자의 이메일 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) auth.getPrincipal();
            String currentUserEmail = currentUser.getUser().getEmail();
            
            // 입력된 이메일과 현재 로그인한 사용자의 이메일 비교
            if (!email.equals(currentUserEmail)) {
                return false;
            }
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.updateUser(newUsername);
                userRepository.save(user);
                
                // 관련된 모든 테이블의 username 업데이트
                ratingRepository.updateUsernameByEmail(email, newUsername);
                commentRepository.updateUsernameByEmail(email, newUsername);
                favoriteRepository.updateUsernameByEmail(email, newUsername);
                
                // 현재 인증 정보 업데이트
                currentUser.getUser().updateUser(newUsername);
                return true;
            }
        }
        return false;
    }
}