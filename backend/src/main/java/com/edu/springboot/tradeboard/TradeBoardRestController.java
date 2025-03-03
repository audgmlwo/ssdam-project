package com.edu.springboot.tradeboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.edu.springboot.CookieUtil;
import com.edu.springboot.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class TradeBoardRestController {
	
	@Autowired
	ITradeBoardService dao;
	@Autowired
	CookieUtil cookieutil;
	@Autowired
	JwtUtil jwtutil;
	
	private ServletContext servletContext;

	// 게시물 업로드 경로 (상대 경로로 수정)
	private String getUploadDir() {
	    String uploadDir = System.getProperty("user.home") + "/uploads/";

	    File dir = new File(uploadDir);
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }

	    return uploadDir;
	}

	@GetMapping("/TradeBoardList")
	public Map<String, Object> TradeBoardList(ParameterDTO parameterDTO) {

	    int pageSize = 10;
	    
	    int pageNum = (parameterDTO.getPageNum() == null) ? 1 : Integer.parseInt(parameterDTO.getPageNum());

	    int start = (pageNum - 1) * pageSize + 1;
	    int end = pageNum * pageSize;

	    parameterDTO.setStart(start);
	    parameterDTO.setEnd(end);
	    
	    int totalCount = dao.totalCount();
	    int totalPages = (int) Math.ceil((double) totalCount / pageSize);


	    List<TradeBoardDTO> tboardList = dao.list(parameterDTO);

	    Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("tboardList", tboardList);
	    resultMap.put("totalPages", totalPages);
	    resultMap.put("currentPage", pageNum);
	    resultMap.put("totalCount", totalCount);

	    return resultMap;
	}


	@PostMapping("/TradeBoardSearch")
	public List<TradeBoardDTO> TradeBoardSearch(@RequestBody ParameterDTO parameterDTO) {
	     
	    if (parameterDTO.getSearchField() == null || parameterDTO.getSearchField().isEmpty()) {
	        parameterDTO.setSearchField("title");
	    }

	    return dao.search(parameterDTO);
	}

	// 게시물열람
	@GetMapping("/TradeBoardView")
	public TradeBoardDTO TradeBoardView(
	    ParameterDTO parameterDTO, 
	    HttpServletRequest request, 
	    HttpServletResponse response,
	    @RequestParam(value = "updateVisit", required = false, defaultValue = "true") boolean updateVisit
	) {
	    int tboard_idx = parameterDTO.getTboard_idx();
	    TradeBoardDTO tboardDTO = dao.view(parameterDTO);

	    if (tboardDTO == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.");
	    }

	    // updateVisit=false인 경우 조회수 증가 로직 건너뜀
	    if (updateVisit) {
	        String today = LocalDate.now().toString();
	        String cookieName = "postViewHistory";
	        String viewHistory = getCookieValue(request, cookieName);

	        if (viewHistory == null || !viewHistory.contains(tboard_idx + "_" + today)) {
	            dao.viewCount(tboard_idx);
	            String newHistory = (viewHistory == null ? "" : viewHistory + "|") + tboard_idx + "_" + today;
	            setCookie(response, cookieName, newHistory);
	        }
	    }

	    return tboardDTO;
	}

	
	//쿠키 조회 함수
	private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals(cookieName))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

	// 쿠키 생성 함수
	private void setCookie(HttpServletResponse response, String name, String value) {
	        Cookie cookie = new Cookie(name, value);
	        cookie.setHttpOnly(true);
	        cookie.setPath("/");
	        cookie.setMaxAge(60 * 60 * 24); 
	        
	        response.addCookie(cookie);
	    }

	 // 게시물 작성
	@PostMapping("/TradeBoardWrite")
	public Map<String, Object> TradeBoardWrite(
	    @RequestParam("title") String title,
	    @RequestParam("content") String content,
	    @RequestParam("email") String email,
	    @RequestParam(value = "file", required = false) MultipartFile file,
	    @RequestParam("state") String state,
	    @RequestParam("price") int price,
	    HttpServletRequest request) { 

	    Map<String, Object> resultMap = new HashMap<>();

	    // JWT 토큰 쿠키에서 가져오기
	    String jwtToken = jwtutil.resolveToken(request);

	    if (jwtToken == null) {
	        resultMap.put("result", 0);
	        resultMap.put("message", "Unauthorized: JWT 토큰이 없습니다.");
	        return resultMap;
	    }

	    // JWT 토큰 검증
	    if (!jwtutil.validateToken(jwtToken)) {
	        resultMap.put("result", 0);
	        resultMap.put("message", "Unauthorized: 유효하지 않은 JWT 토큰입니다.");
	        return resultMap;
	    }

	    // 게시글 객체 생성
	    TradeBoardDTO tboardDTO = new TradeBoardDTO();
	    tboardDTO.setTitle(title);
	    tboardDTO.setContent(content);
	    tboardDTO.setEmail(email);
	    tboardDTO.setState(state);
	    tboardDTO.setPrice(price);

	    // 파일 업로드 처리
	    if (file != null && !file.isEmpty()) {
	        try {
	            String uploadDir = getUploadDir();
	            String originalFilename = file.getOriginalFilename();
	            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
	            Path filePath = Paths.get(uploadDir + File.separator + uniqueFilename);
	            file.transferTo(filePath.toFile());

	            tboardDTO.setOfile("/uploads/" + uniqueFilename);
	        } catch (IOException e) {
	            resultMap.put("result", 0);
	            resultMap.put("message", "파일 업로드 실패: " + e.getMessage());
	            return resultMap;
	        }
	    }

	    // 데이터베이스 저장
	    int result = dao.insert(tboardDTO);
	    if (result == 1) {
	        resultMap.put("result", 1);
	        resultMap.put("message", "게시글 등록 성공");
	    } else {
	        resultMap.put("result", 0);
	        resultMap.put("message", "게시글 등록 실패");
	    }

	    return resultMap;
	}

	
	@PostMapping(value = "/TradeBoardUpdate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Map<String, Object> TradeBoardUpdate(
	        @RequestPart("boardDTO") String boardDTOString,
	        @RequestPart(value = "file", required = false) MultipartFile file) {

	    Map<String, Object> resultMap = new HashMap<>();

	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        TradeBoardDTO tboardDTO = objectMapper.readValue(boardDTOString, TradeBoardDTO.class);

	        // 기존 이미지 유지 체크를 위한 로그 추가
	        System.out.println("🔍 기존 이미지 경로 (프론트에서 전달된 값): " + tboardDTO.getOfile());

	        // DB에서 기존 데이터 조회
	        ParameterDTO paramDTO = new ParameterDTO();
	        paramDTO.setTboard_idx(tboardDTO.getTboard_idx());
	        TradeBoardDTO existingData = dao.view(paramDTO);

	        // 새 파일이 있는 경우 업로드 처리
	        if (file != null && !file.isEmpty()) {
	            String originalFilename = file.getOriginalFilename();
	            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
	            String uploadDir = getUploadDir();
	            Path filePath = Paths.get(uploadDir + File.separator + uniqueFilename);
	            file.transferTo(filePath.toFile());

	            // 새 파일 경로 저장
	            tboardDTO.setOfile("/uploads/" + uniqueFilename);
	        } else {
	            // 새 파일이 없을 경우, 기존 이미지 유지
	            if (existingData != null && existingData.getOfile() != null) {
	                System.out.println("🔍 기존 이미지 유지: " + existingData.getOfile());
	                tboardDTO.setOfile(existingData.getOfile());
	            }
	        }

	        // 최종 업데이트 실행
	        int result = dao.update(tboardDTO);
	        resultMap.put("result", result == 1 ? 1 : 0);
	    } catch (IOException e) {
	        resultMap.put("result", 0);
	        resultMap.put("message", "파일 업로드 실패: " + e.getMessage());
	    }

	    return resultMap;
	}


	@PostMapping("TradeBoardDelete")
	public Map<String, Object> TradeBoardDelete(@RequestBody Map<String, Integer> requestBody) {
	    Map<String, Object> resultMap = new HashMap<>();
	    
	    int tboard_idx = requestBody.get("tboard_idx");
	    
	    int deleteResult = dao.delete(tboard_idx);
	    
	    if (deleteResult > 0) {
	    	resultMap.put("result", 1);
	    } else {
	    	resultMap.put("result", 0);
	    }

	    return resultMap;
	}
	
	

	
}
