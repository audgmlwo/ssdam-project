#  **쓰담쓰담 프로젝트 (Ssdam Ssdam)**  
 **환경 보호를 위한 AI 기반 폐기물 관리 & 커뮤니티 플랫폼**  

## 📌 **프로젝트 개요**
 **쓰담쓰담**은 AI 기술을 활용한 **폐기물 관리 및 분리배출 안내 서비스**입니다.  
 **주요 기능:**  
- **AI 폐기물 인식:** 사진을 업로드하면 **AI가 쓰레기 종류를 판별**  
- **분리수거 가이드:** **지역별 배출 요령 및 스티커 가격** 안내  
- **중고거래 & 나눔 커뮤니티:** **불필요한 물건을 공유**하여 환경 보호 실천  
- **실시간 1:1 채팅:** 커뮤니티 내 중고거래를 위한 **실시간 채팅 기능** 제공  

 **개발 기간:** 2024.01.27 - 2024.02.27 (5주)  
 **개발 인원:** 4명 (팀 프로젝트)  
 **담당 역할: 전박적인 백엔드, 백엔드와 프론트엔드 연동, DB설계  
- **백엔드(Spring Boot):** API 및 DB 설계, 사용자 인증 구현  


---

##  **폴더 구조**
```
ssdam-project
│── backend/        # Spring Boot (백엔드)
│── frontend/       # React (웹 프론트엔드)
│── mobile/         # React Native (모바일 앱)
│── database/       # SQL 파일 (데이터베이스)
│── README.md       # 프로젝트 소개 문서
│── .gitignore      # Git에서 제외할 파일 목록
```

---

##  **기술 스택**
| 분야          | 사용 기술 |
|--------------|--------------------------------|
| **Backend**  | Spring Boot, MySQL, JPA, JWT  |
| **Frontend** | React, Vite, TailwindCSS |
| **Mobile**   | React Native, Expo |
| **AI 모델**  | TensorFlow, Keras, OpenCV |
| **Database** | MySQL, Sequelize |
| **CI/CD**    | GitHub Actions, AWS EC2 |

---

## **실행 방법**
### 1️⃣ **백엔드 실행 (Spring Boot)**
```bash
cd backend
./gradlew bootRun 
```

### 2️⃣ **프론트엔드 실행 (React)**
```bash
cd frontend
npm install  
npm install react-dom
npm run dev  
```

### 3️⃣ **모바일 앱 실행 (React Native)**
```bash
cd mobile/ssdam-native
npm install
npx expo start
```

### 4️⃣ **데이터베이스 설정**
1. `database/` 폴더에 있는 SQL 파일을 MySQL에 import  
2. `.env` 또는 `application.properties`에 DB 정보 추가  

---

##  **주요 기능 상세 설명**
###  **AI 기반 폐기물 인식**
- **사용자가 사진을 업로드하면** AI 모델이 **쓰레기 종류를 분석**  
- TensorFlow 기반의 **폐기물 이미지 분류 모델 적용**  
- **OpenCV**를 활용한 **이미지 전처리** 후 분류  

###  **분리수거 가이드**
- **지역별 폐기물 배출 방법** 안내(해당 지역의 폐기물 부서 홈페이지 링크)  

###  **중고거래 및 나눔 커뮤니티**
- 불필요한 물건을 **중고거래 또는 나눔**으로 등록  
- **게시글 관리:** 수정/삭제 가능  

###  **실시간 1:1 채팅**
- **중고거래 게시판에서 채팅 가능**  
- **채팅 상대 자동 추가 및 채팅 로그 저장**  

---

##  **라이선스**
이 프로젝트는 **MIT 라이선스**를 따릅니다.

---

##  **팀 프로젝트 기여도**
| 역할 | 기여 내용 |
|------|------------------------------------------------|
| 백엔드 | AI API 개발, DB 설계, 사용자 인증 |
| 프론트엔드 | UI 개발, API 연동, 반응형 디자인 |
| 모바일 | Expo 기반 앱 개발, 카메라 연동 |
| 데이터베이스 | MySQL 테이블 설계, 데이터 저장 |



##  **문의 및 연락처**
 **이메일:** audgnlwo@naver.com  
 **GitHub:** [프로필 링크](https://github.com/audgmlwo/ssdam-project?tab=readme-ov-file)  
