package com.edu.springboot.comment;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 특정 게시글의 댓글 목록 조회
    @GetMapping("/{board_idx}")
    public List<CommentDTO> getComments(@PathVariable("board_idx") int boardIdx) {
        return commentService.getCommentsByPostIdx(boardIdx);
    }

    // 댓글 추가
    @PostMapping
    public int addComment(@RequestBody CommentDTO comment) {
        return commentService.insertComment(comment);
    }

    // 댓글 삭제
    @DeleteMapping("/{comment_idx}")
    public int deleteComment(@PathVariable("comment_idx") int commentIdx) {
        return commentService.deleteComment(commentIdx);
    }
}
