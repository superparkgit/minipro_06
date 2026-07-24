# Auth pages UX contract

## Scope

- Surface: React DOM Web application, Korean locale.
- Primary users: signed-out members creating an account or signing in.
- Jobs: submit valid account data, understand pending/success/failure state, recover without losing entered values.
- Routes: `/signup`, `/login`.
- Adjacent regression surface: shared header navigation and `/programs` destination.

## Ownership and invariants

| Capability | Owner | Invariant |
| --- | --- | --- |
| Form state and validation | `SignupPage`, `LoginPage`, browser | Required fields and backend length limits are represented with native form semantics; signup checks the confirmation password before requesting the API. |
| Signup request | `authApi.signup`, backend `/api/auth/signup` | A success response moves to login and preserves the submitted email. |
| Login request | `authApi.login`, backend `/api/auth/login` | Tokens are stored with `AUTH_STORAGE_KEYS` only after both Access and Refresh Token are present. |
| Current member request | `authApi.getMyProfile`, backend `/api/users/me` | Login completes only after the authenticated member name and roles are stored as `fitReserveUser`. |
| Pending state | Page component | Inputs and primary action are disabled to prevent duplicate submission. |
| Failure recovery | Page component and `apiError` | Entered values remain visible and the backend message is announced with `role="alert"`. |
| Header authentication state | `AppLayout`, `authStorage` | The login link and logout action are mutually exclusive; member name and role presentation come from `/api/users/me`, not JWT claims. |
| Logout request | `AppLayout`, `authApi.logout`, backend `/api/auth/logout` | The Refresh Token is submitted when present; local authentication data is always cleared so the current browser session exits safely. |
| Navigation | React Router | Login success replaces history with `/programs`; logout replaces history with `/login`; auth-page switching remains a standard link. |

## Operating states

- Initial: empty fields, enabled primary action, related auth route reachable.
- Pending: fields and submit action disabled, action label reports progress.
- Signup success: navigate to login, prefill email, announce completion.
- Login success: persist tokens, fetch and store `/api/users/me`, then navigate to programs.
- Authenticated shell: hide the login link and expose one logout button.
- Logout pending: disable the logout button and label it `로그아웃 중…` to prevent duplicate requests.
- Logout complete: clear Access Token, Refresh Token, and stored user data; navigate to login and announce completion.
- Session expiry: react to `auth:expired`, remove the authenticated header state, and expose login again.
- Failure: preserve fields, show one in-context error, allow correction and retry. A login `401` is described as an email/password mismatch rather than an authentication-required message.

## Layout and accessibility

- Desktop: contextual summary and task form remain simultaneously visible.
- Narrow layout: summary stacks above the form without changing semantic or focus order.
- Labels are programmatically associated; email and password autocomplete purposes are explicit.
- Keyboard focus remains visible; status and error messages use live semantic roles.
- Korean is the deliberate single locale for this class project.

## Verification map

| Claim | Implementation | Verification |
| --- | --- | --- |
| Forms expose correct names, labels, constraints and autocomplete | `LoginPage.jsx`, `SignupPage.jsx` | Production build plus browser DOM inspection |
| Layout reflows without horizontal overflow | `AuthPages.css` | Browser checks at 1280 px and 390 px |
| Empty submission uses native validation | Form attributes | Browser keyboard submission check |
| API failure is truthful and preserves input | Submit handlers | Browser API failure and success checks |
| Header reflects the current authentication state | `AppLayout.jsx`, `GET /api/users/me` | Browser checks before login, after profile state change, and after logout |
| Logout clears local authentication and is not duplicated | `AppLayout.jsx`, `authApi.js` | Browser action check plus production build |
