package pack.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pack.domain.Rating;
import pack.dto.request.RatingRequest;
import pack.dto.response.RatingResponse;
import pack.service.RatingService;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(@RequestBody RatingRequest request) {
        Rating rating = ratingService.rateSmokingArea(
            request.getUsername(),
            request.getEmail(),
            request.getAddress(),
            request.getScore()
        );
        
        double averageRating = ratingService.getAverageRating(request.getAddress());
        RatingResponse response = RatingResponse.from(rating, averageRating);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/average")
    public ResponseEntity<Map<String, Double>> getAverageRating(@RequestParam String address) {
        double averageRating = ratingService.getAverageRating(address);
        return ResponseEntity.ok(Map.of("average", averageRating));
    }
}
