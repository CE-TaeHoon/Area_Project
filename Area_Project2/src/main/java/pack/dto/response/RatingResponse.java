package pack.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pack.domain.Rating;

@Getter
@Setter
@NoArgsConstructor
public class RatingResponse {
    private String username;
    private String email;
    private String address;
    private int score;
    private LocalDateTime createdAt;
    private double averageRating;  // 해당 장소의 평균 평점

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static RatingResponse from(Rating rating, double averageRating) {
        RatingResponse response = new RatingResponse();
        response.setUsername(rating.getUsername());
        response.setEmail(rating.getEmail());
        response.setAddress(rating.getAddress());
        response.setScore(rating.getScore());
        response.setCreatedAt(rating.getCreatedAt());
        response.setAverageRating(averageRating);
        return response;
    }
}