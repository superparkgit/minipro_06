# Admin user management UX contract

## Scope and users

- Surface: authenticated React DOM administration page at `/admin/users`.
- Primary user: a signed-in member with `ROLE_ADMIN`.
- Affected non-user: signed-out and non-admin members, for whom the administrator navigation is omitted.
- Primary job: review all members, compare their current roles, approve a trainer, or replace a member's complete role set.
- Backend authority: Spring Security `hasRole("ADMIN")`; frontend role checks only control presentation and recovery.

## Journey and ownership

| Step | Owner | Visible result | Failure and recovery |
| --- | --- | --- | --- |
| Resolve authentication and role | `GET /api/users/me`, `authStorage`, `useAuthState` | Administrator navigation exists only for `ROLE_ADMIN` | Missing profile state is loaded before access is decided |
| Enter admin route | `AdminUsersPage` | Signed-out users see login recovery; non-admin users see an access-denied explanation | Backend still rejects unauthorized API requests |
| Load members | `GET /api/admin/users` | Loading, member count, table, empty, or error state | Error preserves the page and exposes `다시 시도` |
| Edit roles | Member-row checkbox group | Draft roles are distinct from persisted role badges | Empty role selection cannot be submitted |
| Save roles | `PATCH /api/admin/users/{userId}/roles` | Only the affected member row is replaced with the server response | Error is announced; draft remains available for correction |
| Approve trainer | Same PATCH endpoint | Existing draft roles are preserved and `ROLE_TRAINER` is added | Duplicate requests are disabled while pending |

## Invariants

- Hiding the tab is not an authorization boundary; the backend remains authoritative.
- A member must have at least one submitted role.
- Current persisted roles and unsaved draft roles remain visually distinct.
- Role mutation is explicit, not automatic: checkbox changes require `변경 저장`, except the explicitly labelled `트레이너 승인` action.
- A single member update does not block reading other rows, but the affected row's actions are disabled until completion.
- Dense comparison remains a semantic table; narrow windows use one labelled horizontal scroll region instead of dropping columns or actions.

## Accessibility and adaptation

- Role choices use native checkboxes grouped by a member-specific fieldset and legend.
- Loading and successful updates use status semantics; failures use alert semantics.
- The scrollable table region is keyboard focusable and visibly focused.
- Korean is the single deliberate locale; the document language is `ko`.

## Verification map

| Claim | Implementation | Evidence |
| --- | --- | --- |
| Non-admin navigation omits administrator entry | `AppLayout.jsx`, `useAuthState.js` | Real-browser DOM snapshot |
| Direct signed-out access is truthful and recoverable | `AdminUsersPage.jsx` | Real-browser DOM snapshot and screenshot |
| Current-member response supplies navigation roles | `GET /api/users/me`, `authStorage.js` | API contract trace and authenticated browser check |
| List and role mutation use the agreed APIs | `AdminUsersPage.jsx`, `adminUserApi.js` | Production build and source trace |
| Table preserves all actions at narrow widths | `AdminUsersPage.css` | CSS contract; authenticated rendered proof pending a valid admin fixture |
