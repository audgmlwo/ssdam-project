package com.edu.springboot.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    
    // (모든 대화 가져오기)
    List<ChatMessageEntity> findBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);

    // (특정 상대와의 메시지만 가져오기, 최신순 정렬)
    List<ChatMessageEntity> findBySenderEmailAndReceiverEmailOrderByTimestampDesc(String senderEmail, String receiverEmail);
    
    @Query("SELECT DISTINCT CASE " +
    	       "WHEN c.senderEmail = :email THEN c.receiverEmail " +
    	       "WHEN c.receiverEmail = :email THEN c.senderEmail " +
    	       "END FROM ChatMessageEntity c " +
    	       "WHERE c.senderEmail = :email OR c.receiverEmail = :email")
    	List<String> findChatContacts(@Param("email") String email);
    
    @Query("SELECT DISTINCT CASE " +
    	       "WHEN c.senderEmail = :email THEN c.receiverEmail " +
    	       "WHEN c.receiverEmail = :email THEN c.senderEmail " +
    	       "END " +
    	       "FROM ChatMessageEntity c " +
    	       "WHERE c.timestamp = (SELECT MAX(c2.timestamp) FROM ChatMessageEntity c2 " +
    	       "                     WHERE (c2.senderEmail = :email OR c2.receiverEmail = :email) " +
    	       "                       AND (c2.senderEmail = c.senderEmail OR c2.receiverEmail = c.receiverEmail)) " +
    	       "ORDER BY c.timestamp DESC")
    	List<String> findChatContacts1(@Param("email") String email);




    
}

