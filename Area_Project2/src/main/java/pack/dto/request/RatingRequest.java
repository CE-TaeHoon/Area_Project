package pack.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RatingRequest {
    private String username;
    private String email;
    private String address;
    private int score;
}
