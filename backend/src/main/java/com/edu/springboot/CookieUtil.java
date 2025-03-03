package com.edu.springboot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public static void createCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(false); 
        cookie.setSecure(false); 
        cookie.setMaxAge(maxAge); 
        response.addCookie(cookie);
        
        response.addHeader("Set-Cookie", name + "=" + value + "; Path=/; HttpOnly; Secure; SameSite=None");        
    }

    // 쿠키 값 읽기
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .findFirst();
            return cookie.map(Cookie::getValue).orElse(null);
        }
        return null;
    }

    // 쿠키 삭제 (maxAge=0 설정)
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    // 쿠키에서 JWT 추출
    public String getJwtFromCookieHeader(String cookieHeader) {
        String[] cookies = cookieHeader.split("; ");
        for (String cookie : cookies) {
            if (cookie.startsWith("jwtToken=")) {  
                return cookie.substring(9); 
            }
        }
        return null;
    }
}