package pack.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * PasswordEncoder를 빈으로 정의합니다.
     * 여기서는 BCryptPasswordEncoder를 사용하여 비밀번호를 안전하게 암호화합니다.
     * Spring Security에서는 비밀번호를 평문으로 저장하지 않고 암호화하는 것이 권장됩니다.
     * 
     * @return BCryptPasswordEncoder 객체를 반환합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new CustomUserDetailsService(); // CustomUserDetailsService 빈 설정
//    }

    /**
     * AuthenticationManager 빈 설정을 위한 메서드입니다.
     * AuthenticationManager는 사용자 인증을 관리하는 핵심 컴포넌트로, 사용자 정보와 비밀번호를 비교하여 인증 여부를 결정합니다.
     * Spring Security 6에서는 HttpSecurity를 사용하여 AuthenticationManager를 설정할 수 있습니다.
     * 
     * @param http HttpSecurity 객체로 보안 설정을 구성합니다.
     * @return AuthenticationManager 객체를 반환합니다.
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authenticationManagerBuilder
            .userDetailsService(userDetailsService) // userDetailsService 지정
            .passwordEncoder(passwordEncoder());     // passwordEncoder 지정
        
        return authenticationManagerBuilder.build();
    }

    /**
     * SecurityFilterChain을 설정하는 메서드입니다.
     * SecurityFilterChain은 보안 필터의 체인을 정의하여 요청에 대한 보안 처리를 수행합니다.
     * 
     * @param http HttpSecurity 객체로 보안 설정을 구성합니다.
     * @return SecurityFilterChain 객체를 반환합니다.
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/index", "/upload-csv", "/api/progress").hasRole("ADMIN")  // /index는 ADMIN 권한만 접근 가능
                .requestMatchers("/", "/map/**", "/register", "/moneyinfo", "/clinic",
                		"/api/smoking-areas","/api/districts","/api/villages",
                		"/api/ratings/average", "/api/comments", "/api/favorites", "api/attendance/**",
                		"/css/**", "/image/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/map", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/map", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // CustomOAuth2UserService 주입
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true) // 세션 무효화
                .deleteCookies("JSESSIONID") // 쿠키 삭제
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> 
                        response.sendRedirect("/login") // 인증되지 않은 요청 시 리다이렉트
            )
            );

        return http.build();
    }
}
