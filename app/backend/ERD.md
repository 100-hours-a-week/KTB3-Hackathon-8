# Entity Relationship Diagram (ERD)

## Mermaid ERD

```mermaid
erDiagram
    USER_IDENTIFIER ||--o{ SUBMIT_GROUP : "owns"
    USER_IDENTIFIER ||--o{ GROUP_MEMBER : "participates"
    SUBMIT_GROUP ||--o{ GROUP_MEMBER : "has"
    SUBMIT_GROUP ||--o{ SUBMISSION : "contains"
    SUBMISSION ||--o{ EVENT_DATE : "has"

    USER_IDENTIFIER {
        bigint id PK "사용자 ID"
        varchar username UK "로그인 아이디"
        varchar nickname "사용자 닉네임"
        varchar password "암호화된 비밀번호"
    }

    SUBMIT_GROUP {
        bigint id PK "그룹 ID"
        int max_capacity "최대 인원"
        int budget "총 예산"
        varchar station "모임 장소(역)"
        boolean has_scheduled_date "일정 확정 여부"
        timestamp start_date "시작 날짜"
        timestamp end_date "종료 날짜"
        bigint owner_id FK "총무 ID"
    }

    GROUP_MEMBER {
        bigint id PK "그룹 멤버 ID"
        bigint group_id FK "그룹 ID"
        bigint user_id FK "사용자 ID"
        boolean is_owner "총무 여부"
    }

    SUBMISSION {
        bigint id PK "제출 ID"
        bigint group_id FK "그룹 ID"
        varchar nickname "제출자 닉네임"
        text preferred_foods "선호 음식"
        text avoided_foods "피하고 싶은 음식"
        text excluded_foods "절대 제외 음식"
    }

    EVENT_DATE {
        bigint id PK "이벤트 날짜 ID"
        bigint submission_id FK "제출 ID"
        timestamp date_value "제외 날짜"
    }
```

## Detailed Relationship Diagram

```mermaid
graph TD
    subgraph "User Domain"
        U[USER_IDENTIFIER]
    end

    subgraph "Group Domain"
        G[SUBMIT_GROUP]
        GM[GROUP_MEMBER]
    end

    subgraph "Submission Domain"
        S[SUBMISSION]
        ED[EVENT_DATE]
    end

    U -->|1:N owns| G
    U -->|1:N participates| GM
    G -->|1:N has members| GM
    G -->|1:N contains| S
    S -->|1:N has excluded dates| ED

    style U fill:#e1f5ff
    style G fill:#fff3e0
    style GM fill:#fff3e0
    style S fill:#f3e5f5
    style ED fill:#f3e5f5
```

## Cardinality Summary

```mermaid
erDiagram
    USER_IDENTIFIER ||--o{ SUBMIT_GROUP : "1:N (owner)"
    USER_IDENTIFIER ||--o{ GROUP_MEMBER : "1:N"
    SUBMIT_GROUP ||--o{ GROUP_MEMBER : "1:N"
    SUBMIT_GROUP ||--o{ SUBMISSION : "1:N"
    SUBMISSION ||--o{ EVENT_DATE : "1:N"
```

## Database Flow Diagram

```mermaid
flowchart LR
    A[사용자 생성] --> B[그룹 생성]
    B --> C[그룹 멤버 추가]
    C --> D[메뉴 선호도 제출]
    D --> E[제외 날짜 추가]
    E --> F[AI 추천 요청]
    F --> G[레스토랑 추천 결과]

    style A fill:#e3f2fd
    style B fill:#fff3e0
    style C fill:#fff3e0
    style D fill:#f3e5f5
    style E fill:#f3e5f5
    style F fill:#e8f5e9
    style G fill:#e8f5e9
```

## Table Dependencies

```mermaid
graph TD
    A[user_identifier] -->|owner_id| B[submit_group]
    A -->|user_id| C[group_member]
    B -->|group_id| C
    B -->|group_id| D[submission]
    D -->|submission_id| E[event_date]

    style A fill:#bbdefb
    style B fill:#ffe0b2
    style C fill:#ffe0b2
    style D fill:#e1bee7
    style E fill:#e1bee7
```

## Indexes Visualization

```mermaid
graph LR
    subgraph "user_identifier"
        U1[id - PK]
        U2[username - INDEX]
        U3[nickname - INDEX]
    end

    subgraph "submit_group"
        G1[id - PK]
        G2[owner_id - INDEX]
        G3[station - INDEX]
        G4[start_date, end_date - INDEX]
    end

    subgraph "group_member"
        GM1[id - PK]
        GM2[group_id - INDEX]
        GM3[user_id - INDEX]
        GM4[group_id, user_id - UNIQUE]
    end

    subgraph "submission"
        S1[id - PK]
        S2[group_id - INDEX]
        S3[nickname - INDEX]
        S4[group_id, nickname - UNIQUE]
    end

    subgraph "event_date"
        E1[id - PK]
        E2[submission_id - INDEX]
        E3[date_value - INDEX]
    end

    style U1 fill:#1976d2,color:#fff
    style G1 fill:#f57c00,color:#fff
    style GM1 fill:#f57c00,color:#fff
    style S1 fill:#7b1fa2,color:#fff
    style E1 fill:#7b1fa2,color:#fff
```

## Foreign Key Relationships with Cascade Rules

```mermaid
erDiagram
    USER_IDENTIFIER ||--o{ SUBMIT_GROUP : "ON DELETE SET NULL"
    USER_IDENTIFIER ||--o{ GROUP_MEMBER : "ON DELETE CASCADE"
    SUBMIT_GROUP ||--o{ GROUP_MEMBER : "ON DELETE CASCADE"
    SUBMIT_GROUP ||--o{ SUBMISSION : "ON DELETE CASCADE"
    SUBMISSION ||--o{ EVENT_DATE : "ON DELETE CASCADE"
```

## Sequence Allocation Strategy

```mermaid
graph TD
    A[JPA Entity] -->|Request ID| B[Sequence]
    B -->|Allocate 100 IDs| C[Memory Cache]
    C -->|Return ID| A
    C -->|99 IDs remaining| D[Next Requests]
    D -->|Cache exhausted| B

    style A fill:#e3f2fd
    style B fill:#fff3e0
    style C fill:#f3e5f5
    style D fill:#e8f5e9
```

**Sequences:**
- `user_seq` (increment by 100)
- `group_seq` (increment by 100)
- `group_member_seq` (increment by 100)
- `userSubmission_seq` (increment by 100)

## Data Flow: 회식 추천 프로세스

```mermaid
sequenceDiagram
    participant User as 사용자
    participant Group as 그룹
    participant Member as 멤버
    participant Sub as 제출
    participant AI as AI 엔진

    User->>Group: 1. 그룹 생성 (총무)
    User->>Member: 2. 초대 링크 공유
    Member->>Sub: 3. 메뉴 선호도 제출
    Sub->>Sub: 4. 제외 날짜 추가
    Group->>AI: 5. 모든 제출 완료 → AI 요청
    AI->>Group: 6. 레스토랑 추천 반환
    Group->>User: 7. 최종 결과 제공
```

## Constraint Hierarchy

```mermaid
graph TB
    subgraph "Primary Keys"
        PK1[user_identifier.id]
        PK2[submit_group.id]
        PK3[group_member.id]
        PK4[submission.id]
        PK5[event_date.id]
    end

    subgraph "Unique Constraints"
        UK1[user_identifier.username]
        UK2[group_member: group_id + user_id]
        UK3[submission: group_id + nickname]
    end

    subgraph "Foreign Keys"
        FK1[submit_group.owner_id → user_identifier.id]
        FK2[group_member.group_id → submit_group.id]
        FK3[group_member.user_id → user_identifier.id]
        FK4[submission.group_id → submit_group.id]
        FK5[event_date.submission_id → submission.id]
    end

    PK1 --> FK1
    PK2 --> FK2
    PK2 --> FK4
    PK1 --> FK3
    PK4 --> FK5

    style PK1 fill:#1976d2,color:#fff
    style PK2 fill:#f57c00,color:#fff
    style PK3 fill:#f57c00,color:#fff
    style PK4 fill:#7b1fa2,color:#fff
    style PK5 fill:#7b1fa2,color:#fff
```
