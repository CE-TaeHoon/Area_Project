package pack;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceResponse {
    private boolean success;      // 출석체크 성공 여부
    private String message;       // 응답 메시지
    private int totalDays;        // 총 출석일수
    private int consecutiveDays;  // 연속 출석일수
    private LocalDateTime lastCheckDate; // 마지막 출석일
    
    public AttendanceResponse(boolean success, String message, int totalDays, int consecutiveDays) {
        this.success = success;
        this.message = message;
        this.totalDays = totalDays;
        this.consecutiveDays = consecutiveDays;
    }
}