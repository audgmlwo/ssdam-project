package com.edu.springboot.commboard;

import java.sql.Date;

import lombok.Data;

@Data
public class BoardDTO {

	private int board_idx;
	private String board_type; //삭제예정
	private String email;
	private String role; 
	private String title;
	private String content;
	private Date created_date;
	private Date updated_date;
	private String ofile;
	private int visit_count;
}
