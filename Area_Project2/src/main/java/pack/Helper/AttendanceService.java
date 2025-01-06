package pack.Helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j  // 로깅을 위해 추가
public class AttendanceService {
    
    private final RestTemplate restTemplate;
    
    @Value("${attendance.api.url}")
    private String apiUrl;
    
    public AttendanceResponse checkAttendance(String email) {
        try {
            String url = apiUrl + "/api/attendance/check?email=" + email;  // URL 변경
            
            // POST 요청 본문에 이메일을 포함
            EmailRequest request = new EmailRequest(email);
            log.info("Calling attendance service with URL: {} and email: {}", url, email);
            
            ResponseEntity<AttendanceResponse> response = restTemplate.postForEntity(
                url, 
                request,   // 요청 본문 추가
                AttendanceResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling attendance service", e);
            throw e;
        }
    }
        public List<String> getMonthlyAttendance(String email, int year, int month) {
        String url = apiUrl + "/api/attendance/monthly/" + email + 
                    "?year=" + year + "&month=" + month;
        
        ResponseEntity<List<String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {}
        );
        
        return response.getBody();
    }
    // 총 출석일수 조회
    public Integer getTotalDays(String email) {
        try {
            String url = apiUrl + "/api/attendance/total/" + email;
            log.info("Calling total days API: {}", url);  // 로그 추가
            
            ResponseEntity<Integer> response = restTemplate.getForEntity(
                url, 
                Integer.class
            );
            
            log.info("Total days response: {}", response.getBody());  // 로그 추가
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting total days", e);
            return 0;  // 에러 발생 시 0 반환
        }
    }
    // 연속 출석일수 조회
    public Integer getConsecutiveDays(String email) {
        try {
            String url = apiUrl + "/api/attendance/consecutive/" + email;
            log.info("Calling microservice URL: {}", url);
            
            ResponseEntity<Integer> response = restTemplate.getForEntity(url, Integer.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling microservice: ", e);
            return 0;  // 에러 발생시 0 반환
        }
    }
}