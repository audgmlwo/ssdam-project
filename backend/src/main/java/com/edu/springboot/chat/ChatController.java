package com.edu.springboot.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.edu.springboot.CookieUtil;
import com.edu.springboot.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CookieUtil cookieUtil;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate; //

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageEntity message) {
      
        // 메시지 저장
        message.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(message);

        // 특정 사용자의 구독 채널로 메시지 전송
        String destination = "/topic/chat/" + message.getReceiverEmail();
        messagingTemplate.convertAndSend(destination, message);
            
    }

    // 특정 사용자와의 채팅 내역 조회 (REST API)
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageEntity>> getChatMessages(
            @RequestParam String receiverEmail,
            HttpServletRequest request) {

        String cookieHeader = request.getHeader("Cookie"); 
        String token = (cookieHeader != null) ? cookieUtil.getJwtFromCookieHeader(cookieHeader) : null;

        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String senderEmail = jwtUtil.extractEmail(token);

        List<ChatMessageEntity> chatMessages = chatMessageRepository
                .findBySenderEmailAndReceiverEmailOrderByTimestampDesc(senderEmail, receiverEmail);

        List<ChatMessageEntity> receivedMessages = chatMessageRepository
                .findBySenderEmailAndReceiverEmailOrderByTimestampDesc(receiverEmail, senderEmail);

        chatMessages.addAll(receivedMessages);
        chatMessages.sort(Comparator.comparing(ChatMessageEntity::getTimestamp));

        return ResponseEntity.ok(chatMessages);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<String>> getChatContacts(HttpServletRequest request) {
        String cookieHeader = request.getHeader("Cookie"); 
        String token = (cookieHeader != null) ? cookieUtil.getJwtFromCookieHeader(cookieHeader) : null; 

        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String myEmail = jwtUtil.extractEmail(token);
        List<String> contacts = chatMessageRepository.findChatContacts(myEmail);

        return ResponseEntity.ok(contacts);
    }

    

}
