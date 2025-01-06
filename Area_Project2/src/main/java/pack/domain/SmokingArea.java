package pack.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SmokingArea {
    /// 흡연구역 위치정보 데이터베이스
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String district; // 자치구
    private String address;  // 주소
    private String locationDetail; // 상세위치
    private String type; // 구분
    private Double areaSize; // 규모(제곱미터)

    private Double latitude;  // 위도
    private Double longitude; // 경도
}
