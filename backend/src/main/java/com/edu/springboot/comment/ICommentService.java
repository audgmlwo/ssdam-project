package com.edu.springboot.comment;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ICommentService {
	
    // 특정 게시글의 댓글 가져오기
    List<CommentDTO> getCommentsByPostIdx(int boardIdx);

    // 댓글 추가
    int insertComment(CommentDTO comment);

    // 댓글 삭제
    int deleteComment(int commentIdx);
}
