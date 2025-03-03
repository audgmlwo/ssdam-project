package com.edu.springboot.tradeboard;

import java.util.ArrayList;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface ITradeBoardService {
	//게시물갯수
	public int totalCount();
	//게시물가져오기
	public ArrayList<TradeBoardDTO>list (ParameterDTO parameterDTO);
	//게시물검색하기
	public ArrayList<TradeBoardDTO>search (ParameterDTO parameterDTO);
	//게시물 내용보기
	public TradeBoardDTO view (ParameterDTO parameterDTO);
	//조회수 증가
    public void viewCount(int tboard_idx);
	//게시물 작성하기
	public int insert(TradeBoardDTO tboardDTO);
	//게시물 수정하기
	public int update(TradeBoardDTO tboardDTO); 
	//게시물 삭제하기
	public int delete(int tboard_idx);
	
}

