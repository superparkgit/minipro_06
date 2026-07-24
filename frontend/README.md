# Frontend

React + Vite 기반 프론트엔드입니다.

## 실행

```bash
npm install
npm run dev
```

기본 API 주소는 `http://localhost:8080/api`입니다. 다른 주소를 사용하면
`.env.example`을 복사해 `.env` 파일을 만들고 `VITE_API_BASE_URL`을 변경합니다.

## API 모듈

- `src/api/authApi.js`: 회원가입·로그인
- `src/api/adminUserApi.js`: 관리자 회원 조회·역할 변경
- `src/api/programApi.js`: 프로그램·담당 트레이너
- `src/api/reservationApi.js`: 예약·출석·통계
- `src/api/postApi.js`, `commentApi.js`: 게시글·댓글
- `src/api/reviewApi.js`: 리뷰·답변·신고·평점

각 담당자는 화면에서 자기 도메인의 API 모듈만 import해서 사용합니다.

## 페이지 담당

- 창민: `src/pages/auth/`
- 서희: `src/pages/program/`, `src/pages/reservation/`
- 용재: `src/pages/community/`, `src/pages/review/`
- 관리자 담당: `src/pages/admin/`

공통 라우팅은 `src/App.jsx`, 공통 API 설정은 `src/api/apiClient.js`에서 관리합니다.

## 미병합 브랜치 반영 기준

- `feature/auth-user`: `/auth/refresh`, `/auth/logout` 계약과 401 시 Access Token 재발급 공통 처리
- `feature/admin-user-management`: `/admin/users` 조회와 `/{userId}/roles` 역할 변경 계약
- `feature/post-comment`: 게시글·댓글·리뷰 API는 각 도메인 API 모듈에 분리

담당자는 자신의 `pages/<domain>/`과 `api/<domain>Api.js`만 수정하고,
`App.jsx`, `AppLayout.jsx`, `apiClient.js`를 바꿀 때는 팀에 미리 알려 충돌을 피합니다.
