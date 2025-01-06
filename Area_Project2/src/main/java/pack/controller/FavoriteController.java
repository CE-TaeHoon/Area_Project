package pack.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pack.domain.Favorite;
import pack.dto.request.FavoriteRequest;
import pack.dto.response.FavoriteResponse;
import pack.service.FavoriteService;


@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public ResponseEntity<FavoriteResponse> toggleFavorite(@RequestBody FavoriteRequest request) {
        FavoriteResponse response = favoriteService.toggleFavorite(
            request.getUsername(),
            request.getEmail(),
            request.getAddress(),
            request.getLocationDetail(),
            request.getLatitude(),
            request.getLongitude()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @RequestParam String email, 
            @RequestParam String address) {
        boolean isFavorite = favoriteService.isFavorite(email, address);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Favorite>> getMyFavorites(@RequestParam String email) {
        List<Favorite> favorites = favoriteService.getFavoritesByEmail(email);
        return ResponseEntity.ok(favorites);
    }
}