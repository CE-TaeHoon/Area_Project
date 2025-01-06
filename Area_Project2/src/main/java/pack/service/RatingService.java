package pack.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pack.domain.Rating;
import pack.repository.RatingRepository;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;

    @Transactional
    public Rating rateSmokingArea(String username, String email, String address, int score) {
        Rating rating = ratingRepository.findByEmailAndAddress(email, address)
            .orElse(Rating.builder()
                .username(username)
                .email(email)
                .address(address)
                .build());
        
        rating.setScore(score);
        rating.setCreatedAt(LocalDateTime.now());

        return ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(String address) {
        return ratingRepository.findAverageScoreByAddress(address)
            .orElse(0.0);
    }
}