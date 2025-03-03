package com.edu.springboot.security;

import com.edu.springboot.JwtUtil;
import com.edu.springboot.member.IMemberService;
import com.edu.springboot.member.MemberService;
import com.edu.springboot.oauth2.CustomOAuth2UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final HttpSession httpSession;

    public SecurityConfig(JwtUtil jwtUtil, IMemberService memberService, HttpSession httpSession) {
        this.jwtUtil = jwtUtil;
        this.httpSession = httpSession;
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(httpSession);
    }
    
    @Bean
    public JwtDecoder jwtDecoder(JwtUtil jwtUtil) {
        return NimbusJwtDecoder.withSecretKey(jwtUtil.getSecretKey()).build();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, MemberService memberService, PasswordEncoder passwordEncoder) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // ✅ CSRF 비활성화 (WebSocket 사용 시 필요)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // ✅ CORS 설정 적용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/uploads/**").permitAll() 
                .requestMatchers("/upload-image").permitAll()
                .requestMatchers("/api/board/my-posts").authenticated()
                .requestMatchers("/ws/**").permitAll()  // ✅ WebSocket 경로 허용
                .requestMatchers("/ws/chat/**").authenticated()  // ✅ WebSocket 채팅 경로 허용
                .requestMatchers("/sockjs-node/**").authenticated()  // ✅ SockJS 지원 추가
                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // ✅ 세션 사용 안함 (JWT 기반 인증)
            .addFilter(new JwtLoginFilter(authenticationManager, jwtUtil, memberService, passwordEncoder))  // ✅ 로그인 필터 추가
            .addFilterBefore(new JwtAuthorizationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);  // ✅ JWT 인증 필터 추가

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 모든 Origin 패턴 허용 (WebSocket 포함)
        configuration.addAllowedOriginPattern("http://localhost:5173");
        configuration.addAllowedOriginPattern("ws://localhost:8587");
        
        // 허용할 HTTP 메서드 추가
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 요청 헤더 추가
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "Accept", 
            "Origin", 
            "X-Requested-With",
            "Set-Cookie" 
        ));

        // JWT 쿠키를 받을 수 있도록 exposedHeaders 수정
        configuration.setExposedHeaders(List.of(
            "Authorization", 
            "Set-Cookie" // 
        ));

        // Credentials 허용 (쿠키 포함)
        configuration.setAllowCredentials(true);

        // CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
