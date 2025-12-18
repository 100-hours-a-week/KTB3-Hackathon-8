# Backend Setup Guide

> KTB Hackathon 8 - 회식 메뉴 추천 시스템 백엔드 설정 가이드

## 빠른 시작

```bash
# 1. 저장소 클론 및 이동
cd app/backend

# 2. 환경 변수 설정
cp .env_template .env
# .env 파일 수정 (GOOGLE_API_KEY 필수)

# 3. JWT 키 생성
./scripts/generate-jwt-keys.sh  # 또는 아래 명령어 직접 실행
mkdir -p src/main/resources/keys
openssl genrsa -out src/main/resources/keys/jwtRS256.key 2048
openssl rsa -in src/main/resources/keys/jwtRS256.key -pubout -out src/main/resources/keys/jwtRS256.key.pub

# 4. 개발 서버 실행
./gradlew bootRun
```

서버 실행 후:
- Swagger UI: http://localhost:8080/swagger-ui
- H2 Console: http://localhost:8080/h2-console

---

## 목차

1. [환경 변수 설정](#환경-변수-설정)
2. [프로필별 실행 방법](#프로필별-실행-방법)
   - [Development (H2)](#development-h2-database)
   - [Production (PostgreSQL)](#production-postgresql)
   - [배포 환경별 가이드](#배포-환경별-실행-가이드)
3. [PostgreSQL 설정](#postgresql-설정)
4. [DDL Auto 모드 설정](#ddl-auto-모드-설정)
5. [JWT Keys 설정](#jwt-keys-설정)
6. [API 인증 설정](#api-인증-설정)
7. [CSRF 설정](#csrf-설정)
8. [외부 API 설정](#외부-api-설정)
9. [UTF-8 인코딩 설정](#utf-8-인코딩-설정)
10. [실행 확인](#실행-확인)
11. [Security 패턴 매칭 규칙](#security-패턴-매칭-규칙)
12. [문제 해결](#문제-해결)

---

## 환경 변수 설정

### 1. `.env` 파일 생성

`.env_template`을 복사하여 `.env` 파일을 생성하세요:

```bash
cp .env_template .env
```

### 2. 환경 변수 설정

`.env` 파일을 열어 다음 값들을 설정하세요:

```properties
# Google Map API Key (필수)
GOOGLE_API_KEY=your_google_api_key_here

# OCR Server URL (필수)
IMAGE_OCR_SERVER_URL=http://98.92.106.85:8000/receipt

# JWT Token Path (기본값 사용 가능)
PRIVATE_KEY_PATH=classpath:keys/jwtRS256.key
PUBLIC_KEY_PATH=classpath:keys/jwtRS256.key.pub

# PostgreSQL Database Configuration (prod 프로필용)
DB_URL=jdbc:postgresql://localhost:5432/ktb_hackathon
DB_USERNAME=postgres
DB_PASSWORD=your_db_password_here

# Hibernate DDL Mode
DDL_AUTO=update
```

## 프로필별 실행 방법

### Development (H2 Database)

```bash
# 프로필 지정 없이 실행 (기본값: dev)
./gradlew bootRun

# 또는 명시적으로 dev 프로필 지정
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

**개발 환경 특징:**
- H2 인메모리 데이터베이스 사용
- DDL Auto: `create-drop` (재시작 시 초기화)
- H2 Console: `http://localhost:8080/h2-console`
- Actuator: 모든 엔드포인트 공개
- Swagger UI: `http://localhost:8080/swagger-ui`

### Production (PostgreSQL)

#### Gradle로 실행

```bash
# prod 프로필로 실행
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

#### JAR 파일로 실행

**1. JAR 빌드:**
```bash
./gradlew clean build
```

**2. 프로필 지정하여 실행:**

**방법 1: 환경 변수 사용 (권장)**
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

**방법 2: 명령줄 인자 사용**
```bash
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**방법 3: 시스템 프로퍼티 사용**
```bash
java -Dspring.profiles.active=prod -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

**방법 4: 환경 변수 + DB 설정 함께 지정**
```bash
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:postgresql://your-db:5432/dbname \
  --spring.datasource.username=postgres \
  --spring.datasource.password=yourpassword
```

**방법 5: 모든 환경 변수 한번에 지정**
```bash
SPRING_PROFILES_ACTIVE=prod \
GOOGLE_API_KEY=your-api-key \
DB_URL=jdbc:postgresql://localhost:5432/ktb_hackathon \
DB_USERNAME=postgres \
DB_PASSWORD=your-password \
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

**프로덕션 환경 특징:**
- PostgreSQL 데이터베이스 사용
- DDL Auto: `update` (기본값, 환경 변수로 변경 가능)
- Actuator: `/health` 엔드포인트만 공개
- SQL 로깅: 비활성화
- JPA Open-in-View: false (N+1 방지)

### 배포 환경별 실행 가이드

#### Docker 환경

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      GOOGLE_API_KEY: ${GOOGLE_API_KEY}
      DB_URL: jdbc:postgresql://postgres:5432/ktb_hackathon
      DB_USERNAME: postgres
      DB_PASSWORD: ${DB_PASSWORD}
      PRIVATE_KEY_PATH: /app/keys/jwtRS256.key
      PUBLIC_KEY_PATH: /app/keys/jwtRS256.key.pub
    volumes:
      - ./src/main/resources/keys:/app/keys
    depends_on:
      - postgres

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ktb_hackathon
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

**실행:**
```bash
docker-compose up -d
```

#### AWS EC2 / Linux 서버

**1. systemd 서비스 생성**

**/etc/systemd/system/backend.service:**
```ini
[Unit]
Description=Backend Service
After=syslog.target network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/app

# 환경 변수
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="GOOGLE_API_KEY=your-api-key"
Environment="DB_URL=jdbc:postgresql://localhost:5432/ktb_hackathon"
Environment="DB_USERNAME=postgres"
Environment="DB_PASSWORD=your-password"
Environment="PRIVATE_KEY_PATH=/home/ubuntu/app/keys/jwtRS256.key"
Environment="PUBLIC_KEY_PATH=/home/ubuntu/app/keys/jwtRS256.key.pub"

# JVM 옵션
Environment="JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC"

# 실행
ExecStart=/usr/bin/java $JAVA_OPTS -jar /home/ubuntu/app/backend.jar

# 재시작 정책
Restart=always
RestartSec=10

# 로그
StandardOutput=journal
StandardError=journal
SyslogIdentifier=backend

# 프로세스 종료
SuccessExitStatus=143
TimeoutStopSec=10

[Install]
WantedBy=multi-user.target
```

**2. 서비스 등록 및 실행:**
```bash
# 서비스 등록
sudo systemctl daemon-reload
sudo systemctl enable backend.service

# 서비스 시작
sudo systemctl start backend

# 상태 확인
sudo systemctl status backend

# 로그 확인
sudo journalctl -u backend -f
```

**3. 서비스 관리:**
```bash
# 재시작
sudo systemctl restart backend

# 중지
sudo systemctl stop backend

# 비활성화
sudo systemctl disable backend
```

#### AWS Elastic Beanstalk

**1. 환경 변수 설정:**
```bash
eb setenv \
  SPRING_PROFILES_ACTIVE=prod \
  GOOGLE_API_KEY=your-api-key \
  DB_URL=jdbc:postgresql://your-rds-endpoint:5432/ktb_hackathon \
  DB_USERNAME=postgres \
  DB_PASSWORD=your-password
```

**2. 배포:**
```bash
./gradlew clean build
eb deploy
```

#### Heroku

**Procfile:**
```
web: java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar build/libs/*.jar
```

**환경 변수 설정:**
```bash
heroku config:set \
  SPRING_PROFILES_ACTIVE=prod \
  GOOGLE_API_KEY=your-api-key \
  DB_URL=jdbc:postgresql://your-db-url \
  DB_USERNAME=your-username \
  DB_PASSWORD=your-password
```

#### nohup으로 백그라운드 실행

```bash
# 백그라운드 실행
nohup java -jar build/libs/backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  > app.log 2>&1 &

# 프로세스 확인
ps aux | grep java

# 종료
kill -15 <PID>
```

## PostgreSQL 설정

### 1. PostgreSQL 설치

**macOS (Homebrew):**
```bash
brew install postgresql@15
brew services start postgresql@15
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### 2. 데이터베이스 생성

```bash
# PostgreSQL 접속
psql -U postgres

# 데이터베이스 생성
CREATE DATABASE ktb_hackathon;

# 유저 생성 (선택사항)
CREATE USER ktb_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ktb_hackathon TO ktb_user;

# 종료
\q
```

### 3. 환경 변수 설정

`.env` 파일에 PostgreSQL 연결 정보 입력:

```properties
DB_URL=jdbc:postgresql://localhost:5432/ktb_hackathon
DB_USERNAME=ktb_user
DB_PASSWORD=your_password
```

### 4. 원격 DB 사용 시

```properties
# AWS RDS 예시
DB_URL=jdbc:postgresql://your-db.region.rds.amazonaws.com:5432/ktb_hackathon
DB_USERNAME=admin
DB_PASSWORD=your_secure_password
```

## DDL Auto 모드 설정

환경에 따라 DDL 모드를 변경할 수 있습니다:

```properties
# 개발: 스키마 자동 업데이트
DDL_AUTO=update

# 프로덕션: 스키마 검증만 (수동 마이그레이션 권장)
DDL_AUTO=validate

# 초기 설정: 스키마 생성
DDL_AUTO=create

# 초기 설정 없음: 아무것도 안함
DDL_AUTO=none
```

## JWT Keys 설정

JWT 서명용 RSA 키 생성:

```bash
# keys 디렉토리 생성
mkdir -p src/main/resources/keys

# Private Key 생성
openssl genrsa -out src/main/resources/keys/jwtRS256.key 2048

# Public Key 추출
openssl rsa -in src/main/resources/keys/jwtRS256.key -pubout -out src/main/resources/keys/jwtRS256.key.pub
```

## API 인증 설정

### 인증이 필요한 엔드포인트

다음 4개의 엔드포인트는 **JWT 토큰 인증이 필수**입니다:

1. **그룹 생성**
   ```
   POST /api/v1/group
   ```

2. **그룹 집계 조회** (총무 전용)
   ```
   GET /api/v1/group/{groupId}/{ownerId}/aggregation
   ```

3. **초대 URL 생성** (총무 전용)
   ```
   GET /api/v1/group/{groupId}/{ownerId}/invite-url
   ```

4. **총무 통합 제출** (최종 AI 추천)
   ```
   POST /api/v1/submission/total/{groupId}
   ```

### 익명 접근 가능한 엔드포인트

다음 엔드포인트들은 **JWT 토큰 없이** 접근 가능합니다:

- **그룹 정보 조회**: `GET /api/v1/group/{groupId}`
- **그룹 멤버 제출**: `POST /api/v1/group/{groupId}/{ownerId}/submissions`
- **멤버 개별 제출**: `POST /api/v1/submission/{groupId}/user`
- **레스토랑 검색**: `GET /api/v1/restaurants/**`

### JWT 토큰 사용법

인증이 필요한 엔드포인트 호출 시 Authorization 헤더에 JWT 토큰을 포함해야 합니다:

```bash
curl -X POST http://localhost:8080/api/v1/group \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "maxCapacity": 5,
    "station": "강남",
    "budget": 50000
  }'
```

## CSRF 설정

현재 CSRF 보호가 **활성화**되어 있습니다. 프론트엔드에서 요청 시:

1. **CSRF 토큰 받기**:
   ```bash
   GET /api/v1/csrf
   ```

2. **요청 시 토큰 포함**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/group \
     -H "Content-Type: application/json" \
     -H "X-XSRF-TOKEN: YOUR_CSRF_TOKEN" \
     -H "Cookie: XSRF-TOKEN=YOUR_CSRF_TOKEN" \
     --data '...'
   ```

**개발 중 CSRF 비활성화:**
`SecurityConfig.java`에서 주석 처리를 변경:
```java
.csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
// .csrf(csrf -> csrf.csrfTokenRepository(csrfRepo()))  // CSRF 활성화
```

## 외부 API 설정

### Google Places API

**필수 설정:**
1. [Google Cloud Console](https://console.cloud.google.com)에서 프로젝트 생성
2. **Places API (New)** 활성화
3. API Key 생성 및 제한 설정:
   - API 제한사항: "Places API (New)" 선택 또는 "키 제한 안함"
   - 청구 계정 연결 필수 (무료 크레딧 $300 제공)
4. `.env` 파일에 API Key 추가:
   ```properties
   GOOGLE_API_KEY=your_actual_api_key
   ```

**에러 해결:**
- `403 Forbidden`: API Key 제한 확인 또는 청구 계정 확인
- `API_KEY_SERVICE_BLOCKED`: Cloud Console에서 Places API (New) 활성화 확인

### LLM 서버

**요구사항:**
- LLM 서버가 `http://3.236.242.98:8000`에서 실행 중이어야 함
- `/generate` 엔드포인트 제공

**요청 형식:**
```json
{
  "people": 5,
  "location": "강남역",
  "preferences": ["한식", "중식"],
  "avoid": ["매운음식"],
  "budget_per_person": 15000,
  "candidates": [
    {
      "displayName": "맛있는집",
      "primaryType": "restaurant",
      "priceRange": "₩₩",
      "rating": 4.5,
      "goodForGroups": true,
      "parkingOptions": "무료 주차 가능"
    }
  ],
  "max_new_tokens": 400
}
```

## 실행 확인

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### API 문서

브라우저에서 접속:
- Swagger UI: `http://localhost:8080/swagger-ui`
- API Docs: `http://localhost:8080/api-docs`

## Security 패턴 매칭 규칙

Spring Security는 URL 패턴을 순차적으로 매칭합니다:

### 패턴 문법

- `*` : 단일 경로 세그먼트 매칭
  - `/api/v1/group/*` → `/api/v1/group/123` ✅
  - `/api/v1/group/*` → `/api/v1/group/123/aggregation` ❌

- `**` : 모든 하위 경로 매칭 (반드시 마지막에 위치)
  - `/api/v1/restaurants/**` → `/api/v1/restaurants/search` ✅
  - `/api/v1/restaurants/**` → `/api/v1/restaurants/nearby/list` ✅

- `**/*` 형식은 **사용 불가** (에러 발생)
  - `/api/v1/group/**/aggregation` ❌ → `PatternParseException`
  - `/api/v1/group/*/aggregation` ✅ 올바른 형식

### 매칭 순서

1. **OPTIONS 요청**: 항상 허용 (CORS preflight)
2. **permitAll** 패턴 확인
3. **anonymous-only** 패턴 확인
4. **나머지**: `.anyRequest().authenticated()` → 인증 필요

### 예시

```yaml
permit-all-endpoints:
  - /api/v1/group/*/submissions     # ✅ /api/v1/group/123/submissions
  - /api/v1/group/*                 # ✅ /api/v1/group/123
  # POST /api/v1/group는 매칭 안됨 → 인증 필요
```

---

## UTF-8 인코딩 설정

본 프로젝트는 **한글 데이터 처리**를 위해 UTF-8 인코딩이 필수로 설정되어 있습니다.

### 자동 설정된 UTF-8 구성

다음 항목들은 **이미 자동으로 설정**되어 있으므로 별도 설정이 필요 없습니다:

#### 1. Gradle 빌드 설정 (build.gradle)

```gradle
// Java 컴파일 시 UTF-8 인코딩
tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

// 애플리케이션 실행 시 UTF-8 인코딩
tasks.named('bootRun') {
    systemProperty 'file.encoding', 'UTF-8'
    systemProperty 'sun.jnu.encoding', 'UTF-8'
    jvmArgs '-Dfile.encoding=UTF-8'
}

// 테스트 실행 시 UTF-8 인코딩
tasks.named('test') {
    systemProperty 'file.encoding', 'UTF-8'
    systemProperty 'sun.jnu.encoding', 'UTF-8'
    jvmArgs '-Dfile.encoding=UTF-8'
}
```

#### 2. Spring Boot 설정 (application.yaml)

```yaml
spring:
  http:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
```

#### 3. WebClient 설정 (SubmissionService.java)

LLM 서버와의 통신 시 UTF-8 인코딩:

```java
private final WebClient webClient = WebClient.builder()
        .baseUrl("http://3.236.242.98:8000")
        .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
        .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> {
                    ObjectMapper mapper = new ObjectMapper();
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
                })
                .build())
        .build();
```

### 검증 방법

#### 1. 테스트 실행

```bash
./gradlew test --tests "SubmissionControllerIntegrationTest"
```

테스트 출력에서 한글이 정상적으로 표시되는지 확인:
```
추천 레스토랑:
  - 이름: Gatten Sushi Gangnam
  - 이유: 피함 조건(생선, 매운음식, 육류)을 충족하며, 초밥을 제공하고...
```

#### 2. 실행 환경 확인

```bash
./gradlew bootRun
```

애플리케이션 로그에서 한글이 정상적으로 표시되는지 확인합니다.

### JAR 실행 시 UTF-8 설정

JAR 파일로 실행할 때도 자동으로 UTF-8이 적용되지만, 명시적으로 설정하려면:

```bash
# 개발 환경 (H2)
java -Dfile.encoding=UTF-8 -jar build/libs/backend-0.0.1-SNAPSHOT.jar

# 운영 환경 (PostgreSQL)
java -Dfile.encoding=UTF-8 -jar build/libs/backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Docker 환경

Docker 컨테이너에서 실행할 때는 Dockerfile에 다음을 추가:

```dockerfile
ENV LANG=ko_KR.UTF-8
ENV LANGUAGE=ko_KR:ko
ENV LC_ALL=ko_KR.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
```

---

## 문제 해결

### Spring Security 403 에러

**원인:**
- JWT 토큰 누락 또는 만료
- CSRF 토큰 누락 (CSRF 활성화 시)
- 권한 부족

**해결방법:**
1. JWT 토큰 확인:
   ```bash
   # 토큰 없이 인증 필요 엔드포인트 호출
   curl -X POST http://localhost:8080/api/v1/group
   # 예상: 403 Forbidden

   # 토큰과 함께 호출
   curl -X POST http://localhost:8080/api/v1/group \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   # 예상: 200 OK
   ```

2. CSRF 비활성화 (개발 환경):
   ```java
   .csrf(AbstractHttpConfigurer::disable)
   ```

### Google Places API 403 에러

**원인:**
- `API_KEY_SERVICE_BLOCKED`: Places API (New) 미활성화
- API Key 제한 설정 문제
- 청구 계정 미설정

**해결방법:**
1. Google Cloud Console → API 및 서비스 → 라이브러리
2. "Places API (New)" 검색 및 활성화
3. 사용자 인증 정보 → API Key → API 제한사항:
   - "키 제한 안함" 선택 (개발 환경)
   - 또는 "Places API (New)" 선택
4. 결제 → 청구 계정 연결
5. 5-10분 대기 후 재시도

### LLM 서버 403/422 에러

**원인:**
- 필수 필드 누락 (`location`, `candidates`)
- 잘못된 데이터 형식

**해결방법:**
1. 로그 확인:
   ```
   LLM Server Error Response: {"detail": "..."}
   ```

2. 요청 데이터 검증:
   - `location`: null이 아닌 문자열
   - `candidates`: 비어있지 않은 배열 (Google Places API 결과)

3. Google Places API가 정상 동작하는지 확인

### PostgreSQL 연결 실패

```
Caused by: org.postgresql.util.PSQLException: Connection refused
```

**해결방법:**
1. PostgreSQL이 실행 중인지 확인: `pg_isready`
2. 포트 확인: `netstat -an | grep 5432`
3. 방화벽 확인
4. DB_URL이 올바른지 확인

### 환경 변수 인식 안됨

```
Error: DB_PASSWORD must be set
```

**해결방법:**
1. `.env` 파일이 프로젝트 루트에 있는지 확인
2. IDE에서 실행 시: Run Configuration에 환경 변수 추가
3. 터미널에서 실행: `export $(cat .env | xargs)`

### H2 Console 접근 불가 (prod)

프로덕션 환경에서는 H2 Console이 비활성화됩니다. 개발 환경(`dev` 프로필)에서만 사용하세요.
