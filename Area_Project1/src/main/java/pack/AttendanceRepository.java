package pack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // 특정 사용자의 가장 최근 출석 기록 조회
    Optional<Attendance> findFirstByEmailOrderByCheckDateDesc(String email);
    // 최신순으로 출석 기록 조회
    List<Attendance> findByEmailOrderByCheckDateDesc(String email);
    // 총 출석일수 조회
    //Optional<Long> countByEmail(String email);  // Optional로 감싸기

    // 또는 이렇게 수정해볼 수 있습니다
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.email = :email")
    Optional<Long> countByEmail(@Param("email") String email);
    
    // 오늘 출석했는지 확인
    @Query("SELECT a FROM Attendance a WHERE a.email = :email " +
           "AND YEAR(a.checkDate) = YEAR(:now) " +
           "AND MONTH(a.checkDate) = MONTH(:now) " +
           "AND DAY(a.checkDate) = DAY(:now)")
    Optional<Attendance> findTodayAttendance(@Param("email") String email, @Param("now") LocalDateTime now);

    // 특정 기간 내 출석 여부 확인
    boolean existsByEmailAndCheckDateBetween(
        String email, LocalDateTime start, LocalDateTime end);

        // 특정 날짜의 출석 기록 찾기
    Optional<Attendance> findByEmailAndCheckDateBetween(
        String email, 
        LocalDateTime startOfDay, 
        LocalDateTime endOfDay
    );
    List<Attendance> findAllByEmailAndCheckDateBetween(
    String email, 
    LocalDateTime startDate, 
    LocalDateTime endDate
    );

    // 특정 사용자의 출석 기록을 날짜 내림차순으로 조회
    @Query("SELECT a FROM Attendance a WHERE a.email = :email ORDER BY a.checkDate DESC")
    List<Attendance> findConsecutiveDays(@Param("email") String email);

}
