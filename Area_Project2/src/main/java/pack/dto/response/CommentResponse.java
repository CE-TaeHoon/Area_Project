package pack.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pack.domain.Comment;

@Getter
@Setter
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String username;
    private String email;
    private String address;
    private String content;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setUsername(comment.getUsername());
        response.setEmail(comment.getEmail());
        response.setAddress(comment.getAddress());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}