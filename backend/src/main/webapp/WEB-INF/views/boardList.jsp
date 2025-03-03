<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시판 목록</title>
    
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">

    <script>
		$(document).ready(function () {
		    let currentPage = 1;
		    loadBoardList(currentPage); // 페이지 로드 시 목록 불러오기

		    function loadBoardList(pageNum) {
		        $.ajax({
		            type: 'get',
		            url: './restBoardList.do',
		            data: { pageNum: pageNum },
		            contentType: "application/json; charset=UTF-8",
		            dataType: "json",
		            success: function (response) {
		                
		                let totalPages = response.totalPages;
		                let currentPage = response.currentPage;

		                renderBoardList(response.boardList);
		                renderPagination(totalPages, currentPage);
		            },
		            error: function (xhr, status, error) {
		                console.error("목록 불러오기 실패:", status, error);
		            }
		        });
		    }

		    function renderBoardList(boardList) {
		        let tableData = "";
		        $(boardList).each(function (index, data) {
		            tableData += "<tr>"
		                + "<td>" + data.board_idx + "</td>"
		                + "<td><a href='boardView.do?board_idx=" + data.board_idx + "'>" + data.title + "</a></td>"
		                + "<td>" + data.email + "</td>"
		                + "<td>" + data.created_date + "</td>"
		                + "<td>" + data.visit_count + "</td>"
		                + "</tr>";
		        });
		        $('#show_data').html(tableData);
		    }

		    function renderPagination(totalPages, currentPage) {
		        console.log("페이지네이션 생성 - 총 페이지:", totalPages, "현재 페이지:", currentPage);

		        if (!totalPages || totalPages < 1) {
		            console.warn(" 페이지네이션 오류 - totalPages 값 없음");
		            $("#pagination").html(""); // 
		            return;
		        }

		        let paginationHTML = "";

		        if (currentPage > 1) {
		            paginationHTML += '<li class="page-item"><a class="page-link" href="#" data-page="1">« 처음</a></li>';
		            paginationHTML += '<li class="page-item"><a class="page-link" href="#" data-page="' + (currentPage - 1) + '">‹ 이전</a></li>';
		        }

		        for (let i = 1; i <= totalPages; i++) {
		            paginationHTML += '<li class="page-item ' + (i === currentPage ? 'active' : '') + '"><a class="page-link" href="#" data-page="' + i + '">' + i + '</a></li>';
		        }

		        if (currentPage < totalPages) {
		            paginationHTML += '<li class="page-item"><a class="page-link" href="#" data-page="' + (currentPage + 1) + '">다음 ›</a></li>';
		            paginationHTML += '<li class="page-item"><a class="page-link" href="#" data-page="' + totalPages + '">마지막 »</a></li>';
		        }

		        $("#pagination").html(paginationHTML);
		    }

		    // 페이지 버튼 클릭 이벤트 처리
		    $(document).on("click", ".page-link", function (e) {
		        e.preventDefault();
		        let selectedPage = $(this).data("page");
		        currentPage = selectedPage;
		        loadBoardList(currentPage);
		    });

		    $("#btnBoard").click(function () {
		        loadBoardList(currentPage);
		    });
		});

    </script>
</head>
<body>
    <div class="container">
        <h2>게시판 API 활용하여 목록 출력하기</h2>
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th>번호</th>
                    <th>제목</th>
                    <th>아이디(이메일)</th>
                    <th>작성일</th>
                    <th>조회수</th>
                </tr>
            </thead>
            <tbody id="show_data"></tbody>
        </table>

        
        <!-- 게시글 작성 버튼 -->
        <div class="mt-3">
            <a href="boardWrite.do" class="btn btn-primary">게시글 작성</a>
        </div>
		
		<!-- 페이징 기능 -->
		<div class="text-center mt-3">
		    <nav>
		        <ul class="pagination justify-content-center" id="pagination"></ul>
		    </nav>
		</div>
	   

    </div>
</body>
</html>
