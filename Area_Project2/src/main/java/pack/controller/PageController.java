package pack.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

import pack.login.CustomUserDetails;
import pack.login.CustomUserDetailsService;
import pack.login.User;
import pack.login.UserRepository;
import pack.login.CustomOAuth2User;


@Controller
public class PageController {
	@Value("${kakao.api-key}")  //지도에 대한 api키 받아오기
    private String apiKey;

    private final CustomUserDetailsService userService;
    private final UserRepository userRepository;

    @GetMapping({"/" , "/map"})  //   루트, map(맵 메인).html로 가는 컨트롤러
    public String mapPage(Model model, Authentication authentication) {
        System.out.println("지도 접근");
        
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                User user = userDetails.getUser();
                System.out.println("일반 로그인 사용자: " + user.getUsername() + ", " + user.getEmail());
                
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
                model.addAttribute("isLoggedIn", true);
            } else if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
                System.out.println("OAuth2 로그인 사용자: " + oauth2User.getUsername() + ", " + oauth2User.getEmail());
                
                model.addAttribute("username", oauth2User.getUsername());
                model.addAttribute("email", oauth2User.getEmail());
                model.addAttribute("isLoggedIn", true);
            }
        } else {
            System.out.println("로그인하지 않은 사용자");
            model.addAttribute("isLoggedIn", false);
        }
        
        model.addAttribute("apiKey", apiKey);
        return "map";
    }

    @GetMapping("/login") // login(로그인).html 가는 컨트롤러
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")	// register(회원가입).html 템플릿을 반환하여 회원가입 폼을 보여줌
    public String showRegisterForm() {
        return "register"; 
    }

    @GetMapping("/helper")
    public String helper(Model model, Authentication authentication) {
        if (authentication != null) {
            String email = null;
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                email = userDetails.getUser().getEmail();
                model.addAttribute("user", userDetails.getUser());
                System.out.println("일반 로그인 사용자 달력: " + email);
            } else if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
                email = oauth2User.getEmail();
                model.addAttribute("user", oauth2User.getUser());
                System.out.println("OAuth2 로그인 사용자 달력: " + email);
            }
            model.addAttribute("email", email);  // 이메일을 모델에 추가
            return "helper";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/moneyinfo") // moneyInfo(과태료 안내).html 컨트롤러
    public String moneyInfo(Model model) {
        System.out.println("과태료 안내 접근");

        return "moneyinfo";
    }

    public PageController(CustomUserDetailsService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }
    
    @GetMapping("/mypage")
    public String mypage(Model model, Authentication authentication) {
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                model.addAttribute("user", userDetails.getUser());
                System.out.println("일반 로그인 사용자 마이페이지: " + userDetails.getUser().getUsername());
            } else if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
                model.addAttribute("user", oauth2User.getUser());
                System.out.println("OAuth2 로그인 사용자 마이페이지: " + oauth2User.getUsername());
            }
            return "mypage";
        } else {
            return "redirect:/login";
        }
    }
    
    @PostMapping("/mypage/update-username")
    @ResponseBody
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> request, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "인증되지 않은 사용자입니다."));
        }

        String email = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            email = userDetails.getUser().getEmail();
        } else if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            email = oauth2User.getEmail();
        }

        if (email == null || !email.equals(request.get("email"))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이메일이 일치하지 않습니다."));
        }

        boolean updated = userService.updateUsername(email, request.get("newUsername"));
        
        if (updated) {
            return ResponseEntity.ok().body(Map.of("success", true, "message", "사용자 이름이 성공적으로 변경되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "사용자 이름 변경에 실패했습니다."));
        }
    }

    @DeleteMapping("/mypage/delete-account") // 회원탈퇴를 위한 엔드포인트
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "인증되지 않은 사용자입니다."));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUser().getEmail();

        // 사용자 삭제 로직
        userRepository.deleteByEmail(email); // 인스턴스를 통해 호출

        return ResponseEntity.ok().body(Map.of("success", true, "message", "회원탈퇴가 완료되었습니다."));
    }

    @GetMapping("/index")
    @PreAuthorize("hasRole('ADMIN')")  // 스프링 시큐리티의 어노테이션으로 관리자 권한 체크
    public String indexPage() {
        return "index";
    }

    @GetMapping("/clinic")  // 금연 클리닉 페이지를 위한 메서드 추가
    public String clinicPage(Model model) {
        return "clinic";  // clinic.html로 이동
    }
}