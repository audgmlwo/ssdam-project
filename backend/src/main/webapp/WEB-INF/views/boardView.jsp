<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시판 내용 보기</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
    
    <script>
        $(function(){
            // URL에서 게시글 번호(num) 가져오기
            const urlParams = new URLSearchParams(window.location.search);
            const board_idx = urlParams.get("board_idx");

            if (!board_idx) {
                console.error("게시글 번호가 없습니다.");
                alert("게시글 번호가 존재하지 않습니다.");
                window.location.href = './boardList.do'; // 목록으로 이동
                return;
            }

            // 수정하기 버튼 클릭 시 수정 페이지로 이동
            $('#btnEdit').click(function(){
                location.href = "./boardEdit.do?board_idx=" + board_idx;
            });

            // 삭제하기 버튼 클릭 시 삭제 요청
            $('#btnDelete').click(function() {
                if (confirm("정말 삭제하시겠습니까?")) {
                    $.ajax({
                        type: 'POST',
                        url: './restBoardDelete.do',
                        data: { num: num },
                        success: function(response) {
                            alert("게시물이 삭제되었습니다.");
                            window.location.href = './boardList.do'; 
                        },
                        error: function(xhr, status, error) {
                            console.error("삭제 실패:", status, error);
                            alert("삭제에 실패하였습니다.");
                        }
                    });
                }
            });

            //게시글 데이터 불러오기
            $.ajax({
                type: 'GET',
                url: 'restBoardView.do',
                data: { board_idx: board_idx },
                contentType: "text/html; charset=UTF-8",
                dataType: "json",
                success: sucCallBack,
                error: errCallBack
            });

        });

        // 서버에서 받아온 데이터 화면에 적용
        function sucCallBack(resData) {
            console.log("서버 응답 데이터:", resData);

            $('#td1').html(resData.board_idx);
            $('#td2').html(resData.email);
            $('#td3').html(resData.created_date);
            $('#td4').html(resData.visit_count);
            $('#td5').html(resData.title);
            $('#td6').html(resData.content);
        }

        // 에러 발생 시 처리
        function errCallBack(errData) {
            console.error("AJAX 요청 실패:", errData.status, errData.statusText);
            alert("게시글 정보를 불러오는데 실패했습니다.");
        }

        // 목록 페이지로 이동
        function backList() {
            window.location.href = './boardList.do';
        }
    </script>
</head>
<body>
    <div class="container">
        <h2>게시판 API 활용하여 내용 출력하기</h2>
        <table class="table table-bordered">
            <tr>
                <th>번호</th> <td id="td1"></td>
                <th>아이디</th> <td id="td2"></td>
            </tr>
            <tr>
                <th>작성일</th> <td id="td3"></td>
                <th>조회수</th> <td id="td4"></td>
            </tr>
            <tr>
                <th>제목</th> <td colspan="3" id="td5"></td>
            </tr>
            <tr>
                <th>내용</th> <td colspan="3" id="td6"></td>
            </tr>
        </table>
       
        <!-- 버튼 추가 -->
        <button class="btn btn-primary" id="btnEdit">수정하기</button>
        <button class="btn btn-danger" id="btnDelete">삭제하기</button>
        <button class="btn btn-primary" onclick="backList()">목록으로</button>
    </div>
</body>
</html>
