package pack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    @Autowired
    private final AttendanceRepository attendanceRepository;

    @Transactional
    // public AttendanceResponse checkAttendance(String email) {
    //     // 오늘 이미 출석했는지 확인
    //     Optional<Attendance> todayAttendance = attendanceRepository
    //         .findTodayAttendance(email, LocalDateTime.now());
        
    //     if (todayAttendance.isPresent()) {
    //         return new AttendanceResponse(false, "이미 오늘 출석하셨습니다.", 
    //             todayAttendance.get().getAttendanceCount());
    //     }

    //     // 이전 출석 기록 확인
    //     Optional<Attendance> lastAttendance = attendanceRepository
    //         .findFirstByEmailOrderByCheckDateDesc(email);

    //     // 새로운 출석 기록 생성
    //     Attendance newAttendance = new Attendance(email);
        
    //     // 이전 출석 기록이 있다면 카운트 증가
    //     if (lastAttendance.isPresent()) {
    //         newAttendance.setAttendanceCount(lastAttendance.get().getAttendanceCount() + 1);
    //     }

    //     attendanceRepository.save(newAttendance);
        
    //     return new AttendanceResponse(true, "출석체크가 완료되었습니다.", 
    //         newAttendance.getAttendanceCount());
    // }
    ////////////////////////////////////기존 코드 ///////////////
    //     public List<String> getMonthlyAttendance(String email, int year, int month) {
    //     // year와 month에 해당하는 출석 기록을 조회
    //     LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
    //     LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
    //     return attendanceRepository.findByEmailAndCheckDateBetween(
    //             email, startOfMonth, endOfMonth)
    //         .stream()
    //         .map(attendance -> attendance.getCheckDate().toLocalDate().toString())
    //         .collect(Collectors.toList());
    // }
    // public int getTotalAttendanceDays(String email) {
    //     return attendanceRepository.countByEmail(email)
    //             .map(count -> count.intValue())
    //             .orElse(0);
    // }
    public List<String> getMonthlyAttendance(String email, int year, int month) {
    // year와 month에 해당하는 출석 기록을 조회
    LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
    LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
    
    try {
        // Repository에서 List로 받아오도록 수정
        List<Attendance> attendances = attendanceRepository.findAllByEmailAndCheckDateBetween(
            email, startOfMonth, endOfMonth);
            
        return attendances.stream()
            .map(attendance -> attendance.getCheckDate().toLocalDate().toString())
            .collect(Collectors.toList());
    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>(); // 오류 발생 시 빈 리스트 반환
        }
}
    // 연속 출석일수 계산 메서드
    public int getConsecutiveDays(String email) {
        // 해당 이메일의 모든 출석기록을 최신순으로 가져옴
        List<Attendance> recentAttendances = attendanceRepository
            .findByEmailOrderByCheckDateDesc(email);
            
        if (recentAttendances.isEmpty()) {
            return 0;  // 출석 기록이 없으면 0 반환
        }

        int consecutiveDays = 1;  // 첫 번째 출석부터 시작
        LocalDateTime lastDate = recentAttendances.get(0).getCheckDate();

        // 연속 출석일수 계산
        for (int i = 1; i < recentAttendances.size(); i++) {
            LocalDateTime currentDate = recentAttendances.get(i).getCheckDate();
            
            // 이전 출석일과 현재 출석일이 연속되는지 확인
            if (lastDate.toLocalDate().minusDays(1)
                    .equals(currentDate.toLocalDate())) {
                consecutiveDays++;
                lastDate = currentDate;
            } else {
                break;  // 연속이 끊기면 중단
            }
        }
        
        return consecutiveDays;
    }
    // 출석체크 처리 메서드
    @Transactional
    public AttendanceResponse checkAttendance(String email) {
        // 오늘 날짜 범위 설정
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(23, 59, 59);
        
        // 오늘 이미 출석했는지 확인
        if (attendanceRepository.existsByEmailAndCheckDateBetween(
                email, startOfDay, endOfDay)) {
            return new AttendanceResponse(false, "이미 오늘 출석체크를 하셨습니다.", 0, 0);
        }

        // 새로운 출석 기록 저장
        Attendance attendance = new Attendance(email);
        attendanceRepository.save(attendance);
        
        // 총 출석일수와 연속 출석일수 계산
        int consecutiveDays = getConsecutiveDays(email);
        int totalDays = attendanceRepository.countByEmail(email)
                .map(Long::intValue)
                .orElse(0);
        log.info("email: {}, totalDays: {}, consecutiveDays: {}", 
        email, totalDays, consecutiveDays);
        return new AttendanceResponse(true, "출석체크 완료!", totalDays, consecutiveDays);
    }
    // 연속 출석일수 계산 메소드
    public int calculateConsecutiveDays(String email) {
        List<Attendance> attendances = attendanceRepository.findByEmailOrderByCheckDateDesc(email);
        if (attendances.isEmpty()) {
            return 0;
        }

        int consecutiveDays = 1;
        LocalDateTime lastDate = attendances.get(0).getCheckDate();

        for (int i = 1; i < attendances.size(); i++) {
            LocalDateTime currentDate = attendances.get(i).getCheckDate();
            
            // 하루 차이인지 확인
            if (lastDate.toLocalDate().minusDays(1)
                    .equals(currentDate.toLocalDate())) {
                consecutiveDays++;
                lastDate = currentDate;
            } else {
                break; // 연속 출석이 끊기면 중단
            }
        }

        return consecutiveDays;
    }
    // 출석 정보 조회 메서드
    public AttendanceResponse getAttendanceInfo(String email) {
        AttendanceResponse info = new AttendanceResponse();
        
        info.setTotalDays(attendanceRepository.countByEmail(email).orElse(0L).intValue());
        info.setConsecutiveDays(calculateConsecutiveDays(email));
        
        attendanceRepository.findFirstByEmailOrderByCheckDateDesc(email)
            .ifPresent(attendance -> info.setLastCheckDate(attendance.getCheckDate()));
            
        return info;
    }
    public int getTotalAttendanceDays(String email) {
        return attendanceRepository.countByEmail(email)
                .map(Long::intValue)
                .orElse(0);
    }
}
