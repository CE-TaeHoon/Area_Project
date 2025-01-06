package pack.Helper;

import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class EmailRequest {
    private String email;
}