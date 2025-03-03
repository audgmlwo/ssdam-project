<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	
	<ul>
	    <li><a href="/boardList.do">목록</a></li>
	</ul>
	
	<h2>rest api</h2>
	<style>
	fieldset{width:400px;}
	</style>
	<fieldset>
		<legend>페이지별 레코드 조회</legend>
		<form action="restBoardList.do">
			<select name="pageNum">
			<c:forEach begin="1" end="10" var="p">
				<option value="${p}">${p}</option>
			</c:forEach>
			</select>
			<input type="submit" value="요	청"/>
		</form>
	</fieldset>
	
	<fieldset>	
		<!-- 2개 이상의 단어를 스페이스로 구분해서 입력한 후 전송 -->
		<legend>2개 이상의 단어 검색</legend>
		<form action="restBoardSearch.do">
			<select name="searchField">
				<option value="title">제목</option>
				<option value="content">내용</option> 
			</select>
			<input type="text" name="searchWord"/>
			<input type="submit" value="요청"/>
		</form>
	</fieldset>
	
	<fieldset>	
		<legend>상세 내용 조회</legend>
		<form action="restBoardView.do">
			일련번호 : <input type="number" name="num"/>
			<input type="submit" value="요청"/>
		</form>
	</fieldset>
	
	<fieldset>
	<legend>[퀴즈]작성하기</legend>
	<form method="post" action="restBoardWrite.do">
		아이디:<input type="text" name="id" />
		제목:<input type="text" name="title" />
		내용: <textarea name="content" cols="30" rows="10"></textarea>
		<input type="submit" value="작성하기" />
	</form>
</fieldset>
	
	
</body>
</html>













