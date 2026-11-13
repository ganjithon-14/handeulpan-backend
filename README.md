# handeulpan-backend

🐑 **한 들판** 백엔드 — 2026 간지톤 14팀

들판·양·쓰다듬기·말풍선·목도리 REST API. 양의 상태(가운데·산책·가장자리)는 저장하지 않고,
요청이 올 때마다 마지막 쓰다듬은 시각으로 계산합니다. WebSocket·LLM·스케줄러 없이 REST API만으로 돌아갑니다.

## 스택

| 영역 | 선택 |
|---|---|
| 프레임워크 | Spring Boot, Spring Data JPA |
| DB | PostgreSQL |
| API 문서 | Swagger (springdoc-openapi) |
| 인증 | 기기 토큰(UUID) 요청 헤더 |
| 배포 | Railway 또는 EC2 |
| 알림 (P1) | FCM 웹 푸시 |

## 실행 방법

| 항목 | 값 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 |
| 빌드 | Gradle 9.7 (wrapper 포함) |

```bash
./gradlew bootRun          # local 프로필: H2 인메모리 DB로 바로 실행 (http://localhost:8080)
./gradlew build            # 컴파일 + 테스트
```

- `local`(기본): 설치 없이 H2로 뜹니다. H2 콘솔 `/h2-console`
- `prod`: `SPRING_PROFILES_ACTIVE=prod` + 환경변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (PostgreSQL)
- 시간 기준은 `Clock` 빈(Asia/Seoul) 하나로 통일합니다. `LocalDateTime.now()` 대신 `Clock`을 주입받아 써 주세요.

## 담당 영역

| 파트 | 담당 | 패키지 (제안) | 범위 |
|---|---|---|---|
| BE-A · 코어 | [@rohdaeyoung](https://github.com/rohdaeyoung) | `common.config`, `user`, `pasture`, `sheep`, `demo` | 프로젝트 세팅, 사용자(기기 토큰)·들판·양 엔티티, 들판 생성·미리보기·합류·조회, 쓰다듬기, 상태 계산, 데모 되감기 |
| BE-B · 돌봄·운영 | [@sinjuno](https://github.com/sinjuno) | `bubble`, `keyword`, `scarf`, `push`, `config`, 배포 설정 | 말풍선 API·위험 키워드 검사, 목도리 API, 서버·DB 배포, Swagger, 공통 응답·에러 형식, 시연용 시드 데이터, 프론트 연동 지원, P1(푸시·반응·낮잠) |

패키지 이름은 [용어표](https://github.com/ganjithon-14/.github/blob/main/CONTRIBUTING.md#5-용어-코드-이름-통일)를 따릅니다.

## API 명세

- 11/6까지 [`docs/api/`](docs/api)에 요청·응답 예시 JSON을 고정하고, 프론트는 그 JSON으로 먼저 개발합니다.
- `docs/api/`가 바뀌면 프론트 팀에도 리뷰 요청이 자동으로 갑니다.
- Swagger UI: 로컬 `http://localhost:8080/swagger-ui/index.html`, 배포 서버 `https://handeulpan.p-e.kr/swagger-ui/index.html` (배포 후 접속 가능). `POST /api/auth/signup`으로 가입해 받은 기기 토큰을 오른쪽 위 Authorize에 넣으면 됩니다.

## 브랜치

- `main` 배포 / `dev` 통합(기본) / `feat/be-a-{이슈}-{설명}`, `feat/be-b-{이슈}-{설명}`
- `dev`, `main`은 PR + 승인 1명이 있어야 머지됩니다. 리뷰어는 같은 파트의 다른 한 명입니다.
- 자세한 규칙: [CONTRIBUTING.md](https://github.com/ganjithon-14/.github/blob/main/CONTRIBUTING.md)

## 관련 레포

- 프론트엔드: [handeulpan-frontend](https://github.com/ganjithon-14/handeulpan-frontend)
