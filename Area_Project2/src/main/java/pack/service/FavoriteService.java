package pack.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pack.domain.Favorite;
import pack.repository.FavoriteRepository;
import pack.dto.response.FavoriteResponse;


@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public FavoriteResponse toggleFavorite(String username, String email, String address, 
            String locationDetail, Double latitude, Double longitude) {
        return favoriteRepository.findByEmailAndAddress(email, address)
            .map(favorite -> {
                favoriteRepository.delete(favorite);
                return FavoriteResponse.of(false, favorite);
            })
            .orElseGet(() -> {
                Favorite newFavorite = favoriteRepository.save(Favorite.builder()
                    .username(username)
                    .email(email)
                    .address(address)
                    .locationDetail(locationDetail)
                    .latitude(latitude)
                    .longitude(longitude)
                    .createdAt(LocalDateTime.now())
                    .build());
                return FavoriteResponse.of(true, newFavorite);
            });
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String email, String address) {
        return favoriteRepository.existsByEmailAndAddress(email, address);
    }

    @Transactional(readOnly = true)
    public List<Favorite> getFavoritesByEmail(String email) {
        return favoriteRepository.findByEmailOrderByCreatedAtDesc(email);
    }
}