package com.edu.springboot.commboard;

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

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class BoardRestController {
	
	// JDBC 작업을 위한 자동 주입
	@Autowired
	IBoardService dao;
	
	@Autowired
	CookieUtil cookieutil;
	
	@Autowired
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

	
	@GetMapping("/commBoardList")
	public Map<String, Object> commBoardList(ParameterDTO parameterDTO) {
	    int pageSize = 10;
	    
	    // 페이지 번호를 로그로 출력
	    System.out.println("요청받은 pageNum: " + parameterDTO.getPageNum());

	    int pageNum = (parameterDTO.getPageNum() == null) ? 1 : Integer.parseInt(parameterDTO.getPageNum());

	    int start = (pageNum - 1) * pageSize + 1;
	    int end = pageNum * pageSize;

	    // start, end 값 확인
	    System.out.println("Start: " + start);
	    System.out.println("End: " + end);

	    parameterDTO.setStart(start);
	    parameterDTO.setEnd(end);

	    int totalCount = dao.totalCount();
	    int totalPages = (int) Math.ceil((double) totalCount / pageSize);

	    System.out.println("총 게시글 개수: " + totalCount);
	    System.out.println("전체 페이지 수: " + totalPages);

	    List<BoardDTO> boardList = dao.list(parameterDTO);

	    Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("boardList", boardList);
	    resultMap.put("totalPages", totalPages);
	    resultMap.put("currentPage", pageNum);
	    resultMap.put("totalCount", totalCount);

	    return resultMap;
	}

	@PostMapping("/commBoardSearch")
	public List<BoardDTO> commBoardSearch(@RequestBody ParameterDTO parameterDTO) {
	     
	    if (parameterDTO.getSearchField() == null || parameterDTO.getSearchField().isEmpty()) {
	        parameterDTO.setSearchField("title");
	    }

	    return dao.search(parameterDTO);
	}

	
	// 게시물 열람 (조회수 증가 포함)
	@GetMapping("/commBoardView")
	public BoardDTO commBoardView(
	        @RequestParam("board_idx") int board_idx,
	        @RequestParam(value = "updateVisit", defaultValue = "true") boolean updateVisit, 
	        ParameterDTO parameterDTO, HttpServletRequest request, HttpServletResponse response) {
	    
	    BoardDTO boardDTO = dao.view(parameterDTO);

	    if (boardDTO == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.");
	    }

	    // 조회수 증가 로직: updateVisit=true일 때만 증가
	    if (updateVisit) {
	        // 오늘 날짜 가져오기
	        String today = LocalDate.now().toString();
	        String cookieName = "postViewHistory";
	        String viewHistory = getCookieValue(request, cookieName);

	        if (viewHistory == null || !viewHistory.contains(board_idx + "_" + today)) {
	            dao.viewCount(board_idx);              
	            String newHistory = (viewHistory == null ? "" : viewHistory + "|") + board_idx + "_" + today;
	            setCookie(response, cookieName, newHistory);
	        }
	    }

	    return boardDTO;
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
	        cookie.setMaxAge(60 * 60 * 24); // 1일 유효
	        
	        response.addCookie(cookie);
	    }

	 // 게시물 작성
    @PostMapping("/commBoardWrite")
    public Map<String, Object> commBoardWrite(
         @RequestParam("title") String title,
         @RequestParam("content") String content,
         @RequestParam("email") String email,
         @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, Object> resultMap = new HashMap<>();
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle(title);
        boardDTO.setContent(content);
        boardDTO.setEmail(email);

        // 업로드 디렉토리 경로를 가져옴
        String uploadDir = getUploadDir();

        // 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                Path filePath = Paths.get(uploadDir + File.separator + uniqueFilename);
                file.transferTo(filePath.toFile()); 

                // 업로드된 파일 경로 설정
                boardDTO.setOfile("/uploads/" + uniqueFilename);
            } catch (IOException e) {
                resultMap.put("result", 0);
                resultMap.put("message", "파일 업로드 실패: " + e.getMessage());
                return resultMap;
            }
        }

	        // 게시글 DB 저장
	        int result = dao.insert(boardDTO);
	        
	        // 결과 반환
	        resultMap.put("result", result == 1 ? 1 : 0);
	        if (result == 0) {
	            resultMap.put("message", "게시글 등록 실패: 다시 시도해주세요.");
	        }

	        return resultMap;
	    }

	
    @PostMapping(value = "/commBoardUpdate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> commBoardUpdate(
            @RequestPart("boardDTO") BoardDTO boardDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Map<String, Object> resultMap = new HashMap<>();

        // 파일 업로드 처리 (파일이 전달된 경우)
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                String uploadDir = getUploadDir();
                Path filePath = Paths.get(uploadDir + File.separator + uniqueFilename);
                file.transferTo(filePath.toFile());

                // 업로드된 파일 경로 설정 (파일 경로 형식은 프로젝트에 맞게 수정)
                boardDTO.setOfile("/uploads/" + uniqueFilename);
            } catch (IOException e) {
                resultMap.put("result", 0);
                resultMap.put("message", "파일 업로드 실패: " + e.getMessage());
                return resultMap;
            }
        }

        // 게시글 수정 처리
        int result = dao.update(boardDTO);
        resultMap.put("result", result == 1 ? 1 : 0);
        
        return resultMap;
    }


	@PostMapping("commBoardDelete")
	public Map<String, Object> commBoardDelete(@RequestBody Map<String, Integer> requestBody) {
	    Map<String, Object> resultMap = new HashMap<>();
	    
	    int board_idx = requestBody.get("board_idx");
	    
	    int deleteResult = dao.delete(board_idx);
	    
	    if (deleteResult > 0) {
	    	resultMap.put("result", 1);
	    } else {
	    	resultMap.put("result", 0);
	    }

	    return resultMap;
	}
	
	

	
}
