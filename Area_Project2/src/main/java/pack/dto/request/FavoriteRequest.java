package pack.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FavoriteRequest {
    private String username;
    private String email;
    private String address;
    private String locationDetail;
    private Double latitude;
    private Double longitude;
}