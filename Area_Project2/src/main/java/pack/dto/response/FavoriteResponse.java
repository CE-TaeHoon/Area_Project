package pack.dto.response;

import lombok.Builder;
import lombok.Getter;
import pack.domain.Favorite;

@Getter
@Builder
public class FavoriteResponse {
    private boolean isFavorited;
    private Favorite favorite;

    public static FavoriteResponse of(boolean isFavorited, Favorite favorite) {
        return FavoriteResponse.builder()
            .isFavorited(isFavorited)
            .favorite(favorite)
            .build();
    }
}