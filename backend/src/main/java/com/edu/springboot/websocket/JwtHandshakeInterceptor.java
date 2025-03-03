package com.edu.springboot.websocket;

import com.edu.springboot.CookieUtil;
import com.edu.springboot.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	@Autowired
	JwtUtil jwtutil;
	
	@Autowired
	CookieUtil cookieutil;
	
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
	    List<String> authHeaders = request.getHeaders().get("Authorization");
	    String token = null;

	    // 1. STOMP(WebSocket) 요청: `Authorization` 헤더에서 JWT 가져오기
	    if (authHeaders != null && !authHeaders.isEmpty()) {
	        token = authHeaders.get(0).replace("Bearer ", "");
	        System.out.println("🔍 WebSocket 핸드셰이크: Authorization 헤더에서 JWT 가져옴 → " + token);
	    }

	    // 2. SockJS 요청: `쿠키`에서 JWT 가져오기 (헤더 지원 안되는 경우 대비)
	    if (token == null || token.isEmpty()) {
	        if (request instanceof ServletServerHttpRequest) {  
	            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
	            token = CookieUtil.getCookieValue(servletRequest, "jwtToken");
	            System.out.println("🔍 WebSocket 핸드셰이크: 쿠키에서 JWT 가져옴 → " + token);
	        }
	    }

	    // 3. JWT 검증
	    if (token != null && jwtutil.validateToken(token)) {
	        String username = jwtutil.extractEmail(token);
	        attributes.put("jwtUser", username);
	        System.out.println("✅ WebSocket 핸드셰이크 성공: " + username);
	        return true;
	    }

	    System.out.println("❌ WebSocket 핸드셰이크 실패: JWT 없음 또는 검증 실패");
	    return false; // 인증 실패 시 WebSocket 연결 차단
	}


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
}
