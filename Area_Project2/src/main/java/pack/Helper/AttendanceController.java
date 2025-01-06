package pack.Helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check")
    public ResponseEntity<AttendanceResponse> checkAttendance(@RequestParam String email) {
        AttendanceResponse response = attendanceService.checkAttendance(email);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/monthly/{email}")
    public ResponseEntity<List<String>> getMonthlyAttendance(
        @PathVariable String email,
        @RequestParam int year,
        @RequestParam int month) {
            
            log.info("Monthly attendance request - email: {}, year: {}, month: {}", 
            email, year, month);
   
        List<String> dates = attendanceService.getMonthlyAttendance(email, year, month);
        log.info("Retrieved dates: {}", dates);
        return ResponseEntity.ok(dates);
    }
    //////////////////////계산기 
    @GetMapping("/total/{email}")
    public ResponseEntity<Integer> getTotalDays(@PathVariable String email) {
        log.info("=== AttendanceController ===");
        log.info("Endpoint: /api/attendance/total/{email}");
        log.info("Method: GET");
        log.info("Email parameter: {}", email);
        log.info("=========================");
        
        return ResponseEntity.ok(attendanceService.getTotalDays(email));
    }
    // 연속 출석일수 조회 API
    @GetMapping("/consecutive/{email}")
    public ResponseEntity<Integer> getConsecutiveDays(@PathVariable String email) {
        int consecutiveDays = attendanceService.getConsecutiveDays(email);
        return ResponseEntity.ok(consecutiveDays);
    }
}