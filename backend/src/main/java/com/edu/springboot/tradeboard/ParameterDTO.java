package com.edu.springboot.tradeboard;

import java.util.List;

import lombok.Data;

@Data
public class ParameterDTO {
	
	//일련번호와 페이지번호
	private int tboard_idx;
	private String pageNum;
	//검색필드와 검색어
	private String searchField = "title";
	private List<String> searchWord;
	//각 페이지의 구간
	private int start;
	private int end;
}
