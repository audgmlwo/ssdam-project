package com.edu.springboot.security;

import com.edu.springboot.JwtUtil;
import com.edu.springboot.member.MemberService;
import com.edu.springboot.member.MemberDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    public JwtLoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, MemberService memberService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            MemberDTO loginDTO = objectMapper.readValue(request.getInputStream(), MemberDTO.class);

            UserDetails userDetails = memberService.loadUserByUsername(loginDTO.getEmail());

            // 비밀번호 비교 (암호화된 비밀번호와 일치하는지 확인)
            if (!passwordEncoder.matches(loginDTO.getPass(), userDetails.getPassword())) {
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }

            UsernamePasswordAuthenticationToken authenticationToken =
            	    new UsernamePasswordAuthenticationToken(userDetails.getUsername(), loginDTO.getPass(), userDetails.getAuthorities());

            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            throw new RuntimeException("❌ 로그인 요청 JSON 파싱 실패", e);
        }
    }

    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        
        // MemberService를 통해 사용자 정보 조회 (MemberDTO에 idx, name, nick_name 등이 포함되어 있다고 가정)
        MemberDTO member = memberService.getUserByEmail(userDetails.getUsername());

        if (member == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "사용자 정보를 찾을 수 없음");
            return;
        }

        // JWT 생성
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        // JWT를 쿠키에 저장
        Cookie cookie = new Cookie("jwtToken", jwtToken);
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);

        // JSON 응답 작성: 기존 방식의 응답 구조와 동일하게 구성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
            "result", 1,
            "message", "로그인 성공",
            "idx", member.getIdx(),
            "name", member.getName(),
            "email", member.getEmail(),
            "nick_name", member.getNick_name(),
            "role", member.getRole()
        )));
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
            "result", 0,
            "error", "로그인 실패",
            "message", failed.getMessage()
        )));
    }
}