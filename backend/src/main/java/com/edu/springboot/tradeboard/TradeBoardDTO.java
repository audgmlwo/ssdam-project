package com.edu.springboot.tradeboard;

import java.sql.Date;

import lombok.Data;

@Data
public class TradeBoardDTO {

	private int tboard_idx;
	private String email;
	private String title;
	private String content;
	private Date created_date;
	private Date updated_date;
	private String ofile;
	private int visit_count;
	private String state;
	private int price;
}
