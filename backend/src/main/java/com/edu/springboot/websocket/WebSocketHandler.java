package com.edu.springboot.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.edu.springboot.chat.ChatMessageEntity;
import com.edu.springboot.chat.ChatMessageRepository;
import com.edu.springboot.member.IMemberService;
import com.edu.springboot.member.MemberDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.edu.springboot.CookieUtil;
import com.edu.springboot.JwtUtil;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
	
	@Autowired
	private JwtUtil jwtUtil; 
	IMemberService dao;
	ChatMessageRepository chatMessageRepository;
	private WebSocketSessionManager sessionManager;
	private CookieUtil cookieUtil;

	
	// WebSocket 연결된 사용자 세션 저장 (이메일을 키로 사용)
	private static final Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();
	
	// ✅ 현재 접속 중인 사용자 목록을 관리하는 Map (이메일 기준)
	private static final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

	public Set<String> getConnectedUsers() {
	    return connectedUsers;
	}
	
	
	private String extractEmailFromSession(WebSocketSession session) {
	    // 1. 쿠키에서 JWT 추출
	    List<String> cookieHeaders = session.getHandshakeHeaders().get("Cookie");
	    if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
	        for (String cookieHeader : cookieHeaders) {
	            String jwtToken = cookieUtil.getJwtFromCookieHeader(cookieHeader);
	            if (jwtToken != null) {
	                return jwtUtil.extractEmail(jwtToken);
	            }
	        }
	    }
	    return null;
	}

    // ✅ WebSocket 연결 시
	 @Override
	    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
	        String email = extractEmailFromSession(session);
	        if (email != null) {
	            clients.put(email, session);
	            sessionManager.addUser(email);  // ✅ WebSocketSessionManager 사용
	            System.out.println("✅ WebSocket 연결 성공: " + email);
	        } else {
	            System.out.println("❌ WebSocket 연결 실패 (JWT 인증 실패)");
	            session.close();
	        }
	    }

    // ✅ WebSocket 메시지 처리
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("📩 메시지 수신: " + payload);

        // JSON 파싱 (receiverEmail을 함께 받도록 수정)
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload);
        String messageContent = jsonNode.get("message").asText();
        String receiverEmail = jsonNode.get("receiver_email").asText();

        // JWT 토큰에서 sender 이메일 추출
        String senderEmail = extractEmailFromSession(session);
        if (senderEmail == null) {
            System.out.println("❌ JWT 인증 실패 → 메시지 저장 불가");
            return;
        }

        // ✅ MyBatis를 이용하여 사용자 이메일 조회 (FK 연결을 위한 검증)
        MemberDTO sender = dao.getUserByEmail(senderEmail);
        MemberDTO receiver = dao.getUserByEmail(receiverEmail);

        if (sender == null || receiver == null) {
            System.out.println("❌ 존재하지 않는 사용자입니다. 메시지 저장 불가");
            return;
        }

        // ✅ 메시지 저장 (JPA 사용)
        ChatMessageEntity chatMessage = new ChatMessageEntity();
        chatMessage.setContent(messageContent);
        chatMessage.setSenderEmail(sender.getEmail());  // FK 연결
        chatMessage.setReceiverEmail(receiver.getEmail());  // FK 연결
        chatMessage.setTimestamp(LocalDateTime.now());

        chatMessageRepository.save(chatMessage); // DB 저장

        // ✅ 대상에게 메시지 전송
        WebSocketSession receiverSession = clients.get(receiverEmail);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(payload));
        }
    }

    // ✅ WebSocket 연결 해제 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String email = extractEmailFromSession(session);
        if (email != null) {
            clients.remove(email);
            sessionManager.removeUser(email);  // ✅ WebSocketSessionManager 사용
            System.out.println("🔌 WebSocket 연결 종료: " + email);
        }
    }
}
