package com.edu.springboot.comment;

import org.springframework.stereotype.Service;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final ICommentService commentMapper;

    public List<CommentDTO> getCommentsByPostIdx(int boardIdx) {
        return commentMapper.getCommentsByPostIdx(boardIdx);
    }

    public int insertComment(CommentDTO comment) {
        return commentMapper.insertComment(comment);
    }

    public int deleteComment(int commentIdx) {
        return commentMapper.deleteComment(commentIdx);
    }
}
