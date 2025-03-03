package com.edu.springboot.like;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.commboard.IBoardService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class Like {

	@Autowired
	IBoardService dao;

	@GetMapping("/like")
	public Map<String, Object> like(@RequestParam int board_idx, 
	                                @RequestParam String email, 
	                                HttpServletRequest request, HttpServletResponse response) {
	    Map<String, Object> resultMap = new HashMap<>();

	    String today = LocalDate.now().toString();
	    String cookieName = "likeHistory";
	    String likeHistory = getCookieValue(request, cookieName);

	    // 🔍 현재 board_idx에 대한 오늘의 좋아요 상태 확인
	    String target = board_idx + "_" + today;
	    boolean isLiked = likeHistory != null && likeHistory.contains(target);

	    Map<String, Object> paramMap = new HashMap<>();
	    paramMap.put("board_idx", board_idx);
	    paramMap.put("email", email);

	    if (isLiked) {
	        // 좋아요 취소
	        dao.decrementLike(paramMap);
	        likeHistory = likeHistory.replace(target, "").replace("||", "|").replaceAll("^\\|", "").replaceAll("\\|$", "");
	    } else {
	        // 좋아요 추가
	        dao.incrementLike(paramMap);
	        likeHistory = (likeHistory == null || likeHistory.isEmpty()) ? target : likeHistory + "|" + target;
	    }

	    // 쿠키 갱신
	    setCookie(response, cookieName, likeHistory);

	    // 최신 좋아요 수 조회
	    int updatedLikeCount = dao.getLikeCount(board_idx);

	    resultMap.put("result", 1);
	    resultMap.put("message", isLiked ? "좋아요 취소" : "좋아요 추가");
	    resultMap.put("likeCount", updatedLikeCount);
	    return resultMap;
	}

	// 쿠키 조회 함수
	private String getCookieValue(HttpServletRequest request, String cookieName) {
	    if (request.getCookies() != null) {
	        return Arrays.stream(request.getCookies())
	            .filter(c -> c.getName().equals(cookieName))
	            .map(Cookie::getValue)
	            .findFirst()
	            .orElse(null);
	    }
	    return null;
	}

	// 쿠키 설정 함수
	private void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1일 유효
        
        response.addCookie(cookie);
    }
	// likeCount 조회 매핑 추가
		@GetMapping("/getLikeCount")
		public Map<String, Object> getLikeCount(@RequestParam int board_idx) {
		    Map<String, Object> resultMap = new HashMap<>();
		    int likeCount = dao.getLikeCount(board_idx);
		    resultMap.put("likeCount", likeCount);
		    return resultMap;
		}
	
}
