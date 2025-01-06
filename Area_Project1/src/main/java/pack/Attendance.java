package pack;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkDate;

    @Column(nullable = false)
    private int attendanceCount;

    // 생성자
    public Attendance(String email) {
        this.email = email;
        this.attendanceCount = 1;
    }
    // 연속 출석일 수를 저장할 필드 추가
    private int consecutiveDays;
}
