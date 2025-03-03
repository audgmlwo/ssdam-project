package com.edu.springboot.board;

import com.edu.springboot.JwtUtil;
import com.edu.springboot.commboard.BoardDTO;
import com.edu.springboot.commboard.IBoardService;
import com.edu.springboot.tradeboard.TradeBoardDTO;
import com.edu.springboot.tradeboard.ITradeBoardService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    @Autowired
    private IBoardService commBoardService;  // 커뮤니티 게시판

    @Autowired
    private ITradeBoardService tradeBoardService; // 트레이드 게시판

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ 유저가 작성한 모든 글 조회 API (커뮤니티 + 트레이드 보드)
    @GetMapping("/my-posts")
    public ResponseEntity<Map<String, Object>> getUserPosts(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Token"));
        }

        String userEmail = jwtUtil.extractEmail(token);

        // ✅ 커뮤니티 게시판 검색 (com.edu.springboot.commboard.ParameterDTO 사용)
        com.edu.springboot.commboard.ParameterDTO commParam = new com.edu.springboot.commboard.ParameterDTO();
        commParam.setSearchField("email"); // 이메일 기준으로 검색
        commParam.setSearchWord(List.of(userEmail));

        List<BoardDTO> communityPosts = commBoardService.search(commParam);

        // ✅ 트레이드 게시판 검색 (com.edu.springboot.tradeboard.ParameterDTO 사용)
        com.edu.springboot.tradeboard.ParameterDTO tradeParam = new com.edu.springboot.tradeboard.ParameterDTO();
        tradeParam.setSearchField("email");
        tradeParam.setSearchWord(List.of(userEmail));

        List<TradeBoardDTO> tradePosts = tradeBoardService.search(tradeParam);

        // ✅ 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("community", communityPosts);
        result.put("trade", tradePosts);

        return ResponseEntity.ok(result);
    }
}
