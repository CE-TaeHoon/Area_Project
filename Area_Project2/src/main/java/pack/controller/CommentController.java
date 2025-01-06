package pack.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pack.domain.Comment;
import pack.dto.request.CommentRequest;
import pack.dto.response.CommentResponse;
import pack.service.CommentService;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@RequestBody CommentRequest request) {
        Comment comment = commentService.addComment(
            request.getUsername(),
            request.getEmail(),
            request.getAddress(),
            request.getContent()
        );
        
        return ResponseEntity.ok(CommentResponse.from(comment));
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@RequestParam String address) {
        List<Comment> comments = commentService.getCommentsByAddress(address);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}