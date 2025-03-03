package com.edu.springboot.member;

import java.sql.Date;

import lombok.Data;

@Data
public class MemberDTO {

	private long idx;
	private String pass;
	private String confirmPass;
	private String name;
	private String nick_name;
	private String email;
	private String role;
	private Date regi_date;
	
	
}
