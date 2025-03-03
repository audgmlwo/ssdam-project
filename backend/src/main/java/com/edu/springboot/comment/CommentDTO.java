package com.edu.springboot.comment;

import lombok.Data;

@Data
public class CommentDTO {
    private int comment_idx;
    private int board_idx;
    private String nick_name;
    private String role;
    private String content;
    private String created_date;
	
}
