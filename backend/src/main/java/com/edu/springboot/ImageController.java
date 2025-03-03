package com.edu.springboot;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class ImageController {

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> handleImageUpload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            // MultipartFile → Base64 변환
            byte[] fileContent = file.getBytes();
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            // Flask 서버로 전송할 데이터 설정
            String flaskUrl = "http://127.0.0.1:5000/predict";
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("image", encodedString);

            // Flask 서버로 POST 요청 보내기
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestMap, headers);

            ResponseEntity<String> flaskResponse = restTemplate.exchange(flaskUrl, HttpMethod.POST, requestEntity, String.class);

            // Flask 서버 응답 처리
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(flaskResponse.getBody());

            responseMap.put("result", "success");
            responseMap.put("prediction", jsonNode.get("prediction").asText());
            responseMap.put("confidence", jsonNode.get("confidence").asDouble());

            return ResponseEntity.ok(responseMap);

        } catch (IOException e) {
            e.printStackTrace();
            responseMap.put("result", "error");
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }
}
