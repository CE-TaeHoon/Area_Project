package pack.Helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor  // 이 줄 추가
public class AttendanceResponse {
    private boolean success;
    private String message;
    private int attendanceCount;
    private int consecutiveDays;
}
