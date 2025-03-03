package com.edu.springboot.member;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service  
public class EmailService {

    @Autowired 
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to); // 받는 이메일 주소
        message.setSubject(subject); // 이메일 제목
        message.setText(text); // 이메일 내용
        mailSender.send(message); // 이메일 전송
    }
}
