package com.edu.springboot.chat;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chat_idx;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "sender_email", nullable = false) //jpa를 사용, 채팅에서만 사용해서 fk는 db에서 직접 추가함
    private String senderEmail; // FK (Member 테이블의 email을 직접 저장)

    @Column(name = "receiver_email", nullable = false)
    private String receiverEmail; // FK (Member 테이블의 email을 직접 저장)
}

