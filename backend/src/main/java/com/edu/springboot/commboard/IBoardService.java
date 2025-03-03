package com.edu.springboot.commboard;

import java.util.ArrayList;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IBoardService {
	//게시물갯수
	public int totalCount();
	//게시물가져오기
	public ArrayList<BoardDTO>list (ParameterDTO parameterDTO);
	//게시물검색하기
	public ArrayList<BoardDTO>search (ParameterDTO parameterDTO);
	//게시물 내용보기
	public BoardDTO view (ParameterDTO parameterDTO);
	//조회수 증가
    public void viewCount(int board_idx);
	//게시물 작성하기
	public int insert(BoardDTO boardDTO);
	//게시물 수정하기
	public int update(BoardDTO boardDTO); 
	//게시물 삭제하기
	public int delete(int board_idx);
	//좋아요 조회수 증가
    public void incrementLike(Map<String, Object> paramMap);
    //좋아요 조회수 감소
    public void decrementLike(Map<String, Object> paramMap);
    //좋아요 조회수 확인
    public int getLikeCount(int board_idx);
    
}
