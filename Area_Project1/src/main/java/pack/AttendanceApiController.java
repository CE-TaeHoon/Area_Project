package pack;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceApiController {

    private final AttendanceService attendanceService;

    @PostMapping("/check")
    public ResponseEntity<AttendanceResponse> checkAttendance(@RequestParam String email) {
        AttendanceResponse response = attendanceService.checkAttendance(email);
        return ResponseEntity.ok(response);
    }
    // 기존 월별 출석 조회 API
    @GetMapping("/monthly/{email}")
    public ResponseEntity<List<String>> getMonthlyAttendance(
        @PathVariable String email,
        @RequestParam int year,
        @RequestParam int month) {
    List<String> attendanceDates = attendanceService.getMonthlyAttendance(email, year, month);
    return ResponseEntity.ok(attendanceDates);
    }
    // 총 출석일수 조회
    @GetMapping("/total/{email}")
    public ResponseEntity<Integer> getTotalDays(@PathVariable String email) {
        int totalDays = attendanceService.getTotalAttendanceDays(email);
        return ResponseEntity.ok(totalDays);
    }
    // 연속 출석일수 조회 API 추가
    @GetMapping("/consecutive/{email}")
    public ResponseEntity<Integer> getConsecutiveDays(@PathVariable String email) {
        int consecutiveDays = attendanceService.getConsecutiveDays(email);
        return ResponseEntity.ok(consecutiveDays);
    }
}
