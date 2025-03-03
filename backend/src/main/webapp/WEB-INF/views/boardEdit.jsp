<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시글 수정</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">

    <script>
        $(document).ready(function(){
            const urlParams = new URLSearchParams(window.location.search);
            const num = urlParams.get("num");

            // 게시글 데이터 불러오기
            $.ajax({
                type: 'GET',
                url: './restBoardView.do',
                data: { num: num },
                dataType: "json",
                success: function(resData) {
                    $('#num').val(resData.num);
                    $('#title').val(resData.title);
                    $('#content').val(resData.content);
                    $('#id').val(resData.id).prop("readonly", true); // 아이디는 수정 불가
                },
                error: function(errData) {
                    console.error("❌ 데이터 로드 실패:", errData.status, errData.statusText);
                }
            });

            // 수정 버튼 클릭 시 API 호출
            $('#btnUpdate').click(function(){
                $.ajax({
                    type: 'POST',
                    url: './restBoardUpdate.do',
                    data: {
                        num: $('#num').val(),
                        title: $('#title').val(),
                        content: $('#content').val(),
                        id: $('#id').val()
                    },
                    dataType: "json",
                    success: function(res) {
                        if(res.result == 1) {
                            alert("게시글이 성공적으로 수정되었습니다.");
                            location.href = "/boardView.do?num=" + num;
                        } else {
                            alert("게시글 수정에 실패했습니다.");
                        }
                    },
                    error: function(err) {
                        console.error("❌ 수정 실패:", err.status, err.statusText);
                    }
                });
            });
        });
    </script>
</head>
<body>
    <div class="container">
        <h2>게시글 수정</h2>
        <form>
            <input type="hidden" id="num">
            <div class="form-group">
                <label>제목</label>
                <input type="text" class="form-control" id="title">
            </div>
            <div class="form-group">
                <label>내용</label>
                <textarea class="form-control" id="content"></textarea>
            </div>
            <div class="form-group">
                <label>아이디</label>
                <input type="text" class="form-control" id="id">
            </div>
            <button type="button" id="btnUpdate" class="btn btn-primary">수정 완료</button>
            <button type="button" onclick="history.back();" class="btn btn-secondary">취소</button>
        </form>
    </div>
</body>
</html>
