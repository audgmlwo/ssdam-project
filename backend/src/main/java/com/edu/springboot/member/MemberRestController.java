package com.edu.springboot.member;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import com.edu.springboot.CookieUtil;
import com.edu.springboot.JwtUtil;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class MemberRestController {
	
		@Autowired
		IMemberService dao;
		
		@Autowired
	    private EmailService emailService; 		
		
		@Autowired		 
		private JwtUtil jwtUtil; 
		
		@Autowired
		private CookieUtil cookieUtil;
		
		@Autowired
	    private PasswordEncoder passwordEncoder; 
		
		private Map<String, String> emailVerificationCodes = new HashMap<>();
		
		// 회원가입
		@RequestMapping("/regist") 
		public Map<String, Object> createBoard(@RequestBody MemberDTO memberDTO) {
		    Map<String, Object> resultMap = new HashMap<>();
		    
		    // 이메일 유효성 검사
		    if (memberDTO.getEmail() == null || !memberDTO.getEmail().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
		    	resultMap.put("result", 0);
		    	resultMap.put("message", "올바른 이메일 형식을 입력하세요.");
		        return resultMap;
		    }
		    
		    // 닉네임 유효성 검사
		    if (memberDTO.getNick_name() == null || !memberDTO.getNick_name().matches("^[a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣]{1,10}$")) {
		    	resultMap.put("result", 0);
		    	resultMap.put("message", "닉네임은 10자이내로 입력하세요.");
		        return resultMap;
		    }
		    // 이름 유효성 검사
		    if (memberDTO.getName() == null || !memberDTO.getName().matches("^[a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣]{1,10}$")) {
		        resultMap.put("result", 0);
		        resultMap.put("message", "이름은 10자 이내, 특수문자 없이 입력해야 합니다.");
		        return resultMap;
		    }
		    		 
		    // 비밀번호 암호화
		    String rawPassword = memberDTO.getPass();
		    if (!rawPassword.matches("^(?=.*[a-z])(?=.*[A-Z])[A-Za-z\\d!@#$%^&*]{9,30}$")) {
		        resultMap.put("result", 0);
		        resultMap.put("message", "비밀번호는 9~30자이며, 대문자와 소문자를 포함해야 합니다.");
		        return resultMap;
		    }

		    if (!memberDTO.getPass().equals(memberDTO.getConfirmPass())) {
		        resultMap.put("result", 0);
		        resultMap.put("message", "비밀번호가 일치하지 않습니다.");
		        return resultMap;
		    }
		    
		    String encodedPassword = passwordEncoder.encode(rawPassword);
		    memberDTO.setPass(encodedPassword);

		    // 데이터베이스 저장
		    int result = dao.insert(memberDTO);

		    // 결과 응답
		    if (result == 1) {
		        System.out.println("회원가입 완료!");
		        resultMap.put("result", 1);
		        resultMap.put("message", "회원가입이 완료되었습니다.");
		    } else {
		    	resultMap.put("result", 0);
		    	resultMap.put("message", "회원가입에 실패했습니다. 다시 시도해주세요.");
		    }

		    return resultMap;
		}
		
		 @PostMapping("/sendVerificationCode")
		    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
		        String email = request.get("email");
		        Map<String, Object> resultMap = new HashMap<>();

		        // 6자리 랜덤 인증 코드 생성
		        String verificationCode = String.format("%06d", new Random().nextInt(1000000));

		        // ✅ 인증 코드 저장 (이메일 → 코드 매핑)
		        emailVerificationCodes.put(email, verificationCode);

		        // 이메일로 인증 코드 전송
		        emailService.sendEmail(email, "이메일 인증 코드", "인증 코드: " + verificationCode);

		        resultMap.put("result", 1);
		        resultMap.put("message", "인증 코드가 이메일로 발송되었습니다.");
		        return ResponseEntity.ok(resultMap);
		    }

		 @PostMapping("/verifyCode")
		    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
		        String email = request.get("email");
		        String code = request.get("code");
		        Map<String, Object> resultMap = new HashMap<>();

		        // ✅ 저장된 코드와 입력된 코드 비교
		        if (emailVerificationCodes.containsKey(email) && emailVerificationCodes.get(email).equals(code)) {
		            resultMap.put("result", 1);
		            resultMap.put("message", "인증 성공");

		            // ✅ 인증 성공 후 코드 삭제 (1회성)
		            emailVerificationCodes.remove(email);
		        } else {
		            resultMap.put("result", 0);
		            resultMap.put("message", "인증 코드가 일치하지 않습니다.");
		        }

		        return ResponseEntity.ok(resultMap);
		    }

		// 로그인 처리 ->  Spring Security + JwtLoginFilter를 통해 처리
		/*
		 * @PostMapping("/login") public Map<String, Object> login(@RequestBody
		 * MemberDTO loginDTO) { Map<String, Object> response = new HashMap<>();
		 * 
		 * Map<String, String> credentials = new HashMap<>();
		 * 
		 * credentials.put("email", loginDTO.getEmail()); credentials.put("pass",
		 * loginDTO.getPass());
		 * 
		 * int isAuthenticated = dao.login(credentials);
		 * 
		 * if (isAuthenticated == 1) {
		 * 
		 * MemberDTO user = dao.getUserByEmail(loginDTO.getEmail());
		 * 
		 * if (user != null) {
		 * 
		 * response.put("result", 1); response.put("message", "로그인 성공");
		 * response.put("idx", user.getIdx()); response.put("name", user.getName());
		 * response.put("email", user.getEmail()); response.put("nick_name",
		 * user.getNick_name()); response.put("role", user.getRole());
		 * 
		 * } else { response.put("result", 0); response.put("message",
		 * "로그인 실패: 사용자 정보가 없습니다."); } } else { response.put("result", 0);
		 * response.put("message", "로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다."); }
		 * 
		 * return response; }
		 */
		
		// 로그인 상태 확인 API 추가
		@GetMapping("/auth/check")
		public ResponseEntity<Map<String, Object>> checkAuth(HttpServletRequest request) {
		    Map<String, Object> resultMap = new HashMap<>();

		    String token = jwtUtil.resolveToken(request);
		    if (token == null || !jwtUtil.validateToken(token)) {
		    	resultMap.put("status", "unauthenticated");
		        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resultMap);
		    }
		    
		    String email = jwtUtil.extractEmail(token);
		    MemberDTO user = dao.getUserByEmail(email);

		    if (user != null) {
		    	resultMap.put("status", "authenticated");
		    	resultMap.put("email", user.getEmail());
		    	resultMap.put("name", user.getName());
		    	resultMap.put("nick_name", user.getNick_name());
		    	resultMap.put("role", user.getRole());
		    	resultMap.put("idx", user.getIdx());
		    } else {
		    	resultMap.put("status", "unauthenticated");
		    }

		    return ResponseEntity.ok(resultMap);
		}

	    @PostMapping("/logout")
	    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
	        Map<String, Object> resultMap = new HashMap<>();

	        // 쿠키 삭제 (CookieUtil 사용)
	        cookieUtil.deleteCookie(response, "jwtToken");

	        resultMap.put("message", "로그아웃 성공");
	        return ResponseEntity.ok(resultMap);
	    }
	    
	    @PatchMapping("/edit")
	    public Map<String, Object> updateMember(@RequestBody MemberDTO memberDTO) {
	        Map<String, Object> resultMap = new HashMap<>();
	        System.out.println(" API 요청 받음: /api/edit");
	        System.out.println("🔍 전달된 데이터: " + memberDTO);

	        // 비밀번호가 null이 아니고 비어있지 않을 때만 암호화 수행
	        if (memberDTO.getPass() != null && !memberDTO.getPass().isEmpty()) {
	            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	            memberDTO.setPass(passwordEncoder.encode(memberDTO.getPass()));
	        }

	        int result = dao.updateMember(memberDTO);
	        if (result == 1) {
	            resultMap.put("result", 1);
	        } else {
	            resultMap.put("result", 0);
	        }

	        return resultMap;
	    }

		@PostMapping("/findEmail")
		public Map<String, Object> findEmail(@RequestBody MemberDTO findEmailDTO) {
			Map<String, Object> resultMap = new HashMap<>();
			Map<String, String> account = new HashMap<>();

			account.put("name", findEmailDTO.getName());
			account.put("nick_name", findEmailDTO.getNick_name());

			int result = dao.findEmailCount(account); 

			if (result == 1) {

				String email = dao.getEmailByNameAndNick(account);

				resultMap.put("result", 1);
				resultMap.put("email", email);

			} else {
				resultMap.put("result", 0);
				resultMap.put("message", "이메일 찾기 실패: 사용자 정보가 없습니다.");
			}

			return resultMap;
		}

		@PostMapping("/findPwd")
		public ResponseEntity<Map<String, Object>> findPwd(@RequestBody MemberDTO findPwdDTO) {
		    Map<String, Object> resultMap = new HashMap<>();
		    Map<String, String> pwd = new HashMap<>();
		    
		    pwd.put("email", findPwdDTO.getEmail());
		    pwd.put("name", findPwdDTO.getName());

		    int result = dao.findPwd(pwd);
		    if (result == 1) {
		        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
		        String encodedPassword = passwordEncoder.encode(tempPassword);
		        
		        dao.updatePwd(findPwdDTO.getEmail(), encodedPassword);
		        

		        emailService.sendEmail(findPwdDTO.getEmail(), "임시 비밀번호 발송", "임시 비밀번호: " + tempPassword);

		        resultMap.put("result", 1);
		        resultMap.put("message", "임시 비밀번호가 이메일로 전송되었습니다.");
		        resultMap.put("tempPassword", tempPassword); // 
		    } else {
		    	resultMap.put("result", 0);
		    	resultMap.put("message", "이메일 또는 이름이 일치하지 않습니다.");
		    }

		    return ResponseEntity.ok(resultMap);
		}
		
	  
		
		// 이메일 중복 체크 API
		@GetMapping("/checkEmail")
		public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
		    Map<String, Object> resultMap = new HashMap<>();

		    int count = dao.emailExists(email);
		    if (count > 0) {
		    	resultMap.put("status", "exists");
		    	resultMap.put("message", "이미 사용 중인 이메일입니다.");
		        return ResponseEntity.status(HttpStatus.CONFLICT).body(resultMap);
		    } else {
		    	resultMap.put("status", "available");
		    	resultMap.put("message", "사용 가능한 이메일입니다.");
		        return ResponseEntity.ok(resultMap);
		    }
		}
		
		// 이메일 중복 체크 API
		@GetMapping("/checkNickName")
		public ResponseEntity<Map<String, Object>> checkNickName(@RequestParam String nick_name) {
		    Map<String, Object> resultMap = new HashMap<>();

		    int count = dao.nicknameExists(nick_name);
		    if (count > 0) {
		    	resultMap.put("status", "exists");
		    	resultMap.put("message", "이미 사용 중인 닉네임입니다.");
		        return ResponseEntity.status(HttpStatus.CONFLICT).body(resultMap);
		    } else {
		    	resultMap.put("status", "available");
		    	resultMap.put("message", "사용 가능한 닉네임입니다.");
		        return ResponseEntity.ok(resultMap);
		    }
		}
		

	}