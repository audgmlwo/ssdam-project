<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>게시글 작성</title>
    
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">

    <script>
        $(document).ready(function(){
            $('#btnSubmit').click(function(){
                let postData = {
                    title: $('#title').val(),
                    content: $('#content').val(),
                    email: $('#email').val()
                };

                $.ajax({
                    type: 'POST',
                    url: 'restBoardWrite.do',
                    data: postData,
                    dataType: "json",
                    success: function(res) {
                        if(res.result == 1) {
                            alert("게시글이 성공적으로 작성되었습니다.");
                            window.location.href = "boardList.do"; // 목록 페이지로 이동
                        } else {
                            alert("게시글 작성에 실패했습니다.");
                        }
                    },
                    error: function(err) {
                        console.error("❌ 게시글 작성 실패:", err.status, err.statusText);
                    }
                });
            });
        });
    </script>
</head>
<body>
    <div class="container">
        <h2>게시글 작성</h2>
        
        <div class="form-group">
            <label for="email">작성자 ID</label>
            <input type="text" class="form-control" id="email" placeholder="이메일 입력">
        </div>

        <div class="form-group">
            <label for="title">제목</label>
            <input type="text" class="form-control" id="title" placeholder="제목 입력">
        </div>

        <div class="form-group">
            <label for="content">내용</label>
            <textarea class="form-control" id="content" rows="5" placeholder="내용 입력"></textarea>
        </div>

        <button id="btnSubmit" class="btn btn-success">작성 완료</button>
        <a href="boardList.do" class="btn btn-secondary">취소</a>
    </div>
</body>
</html>
