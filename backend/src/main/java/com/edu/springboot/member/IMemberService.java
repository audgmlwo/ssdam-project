package com.edu.springboot.member;


import java.util.Map;

import org.apache.ibatis.annotations.Mapper;





@Mapper
public interface IMemberService {

	//회원가입
	public int insert(MemberDTO memberDTO);
	//이메일 중복
	public int emailExists(String email);
	//닉네임 중복
	public int nicknameExists(String nick_name);
	//로그인
	public int login(Map<String, String> credentials); 
	public MemberDTO getUserByEmail(String email);
	//회원 정보수정
	public int updateMember(MemberDTO memberDTO); 
	//이메일찾기
	int findEmailCount(Map<String, String> account);  
	String getEmailByNameAndNick(Map<String, String> account); 
	//비밀번호 찾기
	public int findPwd(Map<String, String> pwd); 
	public void updatePwd(String email, String tempPassword);
	

}
