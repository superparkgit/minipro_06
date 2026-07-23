import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAdminUsers, updateUserRoles } from '../../api/adminUserApi'
import { getApiErrorMessage } from '../../api/apiError'
import { USER_ROLES } from '../../auth/authStorage'
import useAuthState from '../../auth/useAuthState'
import AdminSectionNav from './AdminSectionNav'
import './AdminUsersPage.css'

const ROLE_OPTIONS = [
  { value: USER_ROLES.user, label: '회원' },
  { value: USER_ROLES.trainer, label: '트레이너' },
  { value: USER_ROLES.admin, label: '관리자' },
]

const ROLE_ORDER = ROLE_OPTIONS.map(({ value }) => value)

const normalizeRoles = (roles = []) => (
  ROLE_ORDER.filter((role) => roles.includes(role))
)

const createRoleDrafts = (users) => Object.fromEntries(
  users.map((user) => [user.id, normalizeRoles(user.roles)]),
)

const haveSameRoles = (currentRoles = [], draftRoles = []) => (
  normalizeRoles(currentRoles).join(',') === normalizeRoles(draftRoles).join(',')
)

const formatCreatedAt = (createdAt) => {
  if (!createdAt) return '-'

  const date = new Date(createdAt)
  if (Number.isNaN(date.getTime())) return '-'

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date)
}

function AdminUsersPage() {
  const { isAuthenticated, isProfileLoaded, roles } = useAuthState()
  const isAdmin = roles.includes(USER_ROLES.admin)
  const [users, setUsers] = useState([])
  const [roleDrafts, setRoleDrafts] = useState({})
  const [isLoading, setIsLoading] = useState(true)
  const [pageError, setPageError] = useState('')
  const [updatingUserId, setUpdatingUserId] = useState(null)
  const [actionMessage, setActionMessage] = useState(null)

  const loadUsers = useCallback(async () => {
    if (!isAdmin) return

    setIsLoading(true)
    setPageError('')

    try {
      const { data } = await getAdminUsers()
      if (!Array.isArray(data)) throw new Error('회원 목록 응답 형식이 올바르지 않습니다.')

      setUsers(data)
      setRoleDrafts(createRoleDrafts(data))
    } catch (error) {
      setPageError(
        getApiErrorMessage(error, error?.message ?? '회원 목록을 불러오지 못했습니다.'),
      )
    } finally {
      setIsLoading(false)
    }
  }, [isAdmin])

  useEffect(() => {
    loadUsers()
  }, [loadUsers])

  const handleRoleToggle = (userId, role, checked) => {
    setRoleDrafts((current) => {
      const currentRoles = current[userId] ?? []
      const nextRoles = checked
        ? [...new Set([...currentRoles, role])]
        : currentRoles.filter((currentRole) => currentRole !== role)

      return { ...current, [userId]: normalizeRoles(nextRoles) }
    })
    setActionMessage(null)
  }

  const saveRoles = async (user, requestedRoles) => {
    const nextRoles = normalizeRoles(requestedRoles)

    if (nextRoles.length === 0) {
      setActionMessage({
        type: 'error',
        text: `${user.name} 회원에게 역할을 하나 이상 선택해 주세요.`,
      })
      return
    }

    setUpdatingUserId(user.id)
    setActionMessage(null)

    try {
      const { data } = await updateUserRoles(user.id, nextRoles)

      setUsers((current) => current.map((member) => (
        member.id === user.id ? data : member
      )))
      setRoleDrafts((current) => ({
        ...current,
        [user.id]: normalizeRoles(data.roles),
      }))
      setActionMessage({
        type: 'success',
        text: `${data.name} 회원의 역할을 변경했습니다.`,
      })
    } catch (error) {
      setActionMessage({
        type: 'error',
        text: getApiErrorMessage(error, '회원 역할을 변경하지 못했습니다.'),
      })
    } finally {
      setUpdatingUserId(null)
    }
  }

  const handleSave = (event, user) => {
    event.preventDefault()
    saveRoles(user, roleDrafts[user.id] ?? user.roles)
  }

  const handleTrainerApproval = (user) => {
    const currentRoles = roleDrafts[user.id] ?? user.roles
    saveRoles(user, [...currentRoles, USER_ROLES.trainer])
  }

  if (!isAuthenticated) {
    return (
      <section className="admin-access-card" aria-labelledby="admin-login-required">
        <p className="admin-eyebrow">ADMIN ONLY</p>
        <h1 id="admin-login-required">로그인이 필요합니다.</h1>
        <p>관리자 계정으로 로그인한 뒤 회원 관리 화면을 이용해 주세요.</p>
        <Link className="button button-primary" to="/login">로그인하기</Link>
      </section>
    )
  }

  if (!isProfileLoaded) {
    return (
      <section className="admin-access-card" aria-labelledby="admin-profile-loading">
        <p className="admin-eyebrow">ADMIN ONLY</p>
        <h1 id="admin-profile-loading">회원 정보를 확인하고 있습니다.</h1>
      </section>
    )
  }

  if (!isAdmin) {
    return (
      <section className="admin-access-card" aria-labelledby="admin-access-denied">
        <p className="admin-eyebrow">ADMIN ONLY</p>
        <h1 id="admin-access-denied">관리자 권한이 없습니다.</h1>
        <p>회원 조회와 역할 변경은 관리자만 수행할 수 있습니다.</p>
        <Link className="button button-secondary" to="/">홈으로 돌아가기</Link>
      </section>
    )
  }

  return (
    <section className="admin-users-page" aria-labelledby="admin-users-title">
      <AdminSectionNav />

      <header className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">MEMBER ADMINISTRATION</p>
          <h1 id="admin-users-title">회원 권한 관리</h1>
          <p>전체 회원을 조회하고 역할 변경과 트레이너 승인을 처리합니다.</p>
        </div>
        {!isLoading && !pageError && (
          <span className="admin-user-count" aria-label={`전체 회원 ${users.length}명`}>
            {users.length}명
          </span>
        )}
      </header>

      {actionMessage && (
        <p
          className={`notice ${actionMessage.type === 'error' ? 'notice-error' : ''}`}
          role={actionMessage.type === 'error' ? 'alert' : 'status'}
        >
          {actionMessage.text}
        </p>
      )}

      {isLoading && (
        <div className="admin-state-card" role="status">
          회원 목록을 불러오는 중입니다…
        </div>
      )}

      {!isLoading && pageError && (
        <div className="admin-state-card admin-state-error" role="alert">
          <strong>회원 목록을 불러오지 못했습니다.</strong>
          <span>{pageError}</span>
          <button className="button button-secondary" type="button" onClick={loadUsers}>
            다시 시도
          </button>
        </div>
      )}

      {!isLoading && !pageError && users.length === 0 && (
        <div className="admin-state-card" role="status">
          조회된 회원이 없습니다.
        </div>
      )}

      {!isLoading && !pageError && users.length > 0 && (
        <div
          className="admin-table-scroll"
          tabIndex="0"
          role="region"
          aria-label="전체 회원 역할 관리 표"
        >
          <table className="admin-users-table">
            <thead>
              <tr>
                <th scope="col">회원</th>
                <th scope="col">가입일</th>
                <th scope="col">현재 역할</th>
                <th scope="col">변경 작업</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => {
                const draftRoles = roleDrafts[user.id] ?? normalizeRoles(user.roles)
                const isUpdating = updatingUserId === user.id
                const isChanged = !haveSameRoles(user.roles, draftRoles)
                const hasTrainerRole = draftRoles.includes(USER_ROLES.trainer)

                return (
                  <tr key={user.id}>
                    <td>
                      <strong className="admin-user-name">{user.name}</strong>
                      <span className="admin-user-email">{user.email}</span>
                    </td>
                    <td>{formatCreatedAt(user.createdAt)}</td>
                    <td>
                      <div className="admin-role-badges">
                        {normalizeRoles(user.roles).map((role) => (
                          <span className="badge" key={role}>
                            {ROLE_OPTIONS.find((option) => option.value === role)?.label ?? role}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <form className="admin-role-form" onSubmit={(event) => handleSave(event, user)}>
                        <fieldset disabled={isUpdating}>
                          <legend className="sr-only">{user.email} 역할 선택</legend>
                          <div className="admin-role-options">
                            {ROLE_OPTIONS.map(({ value, label }) => (
                              <label key={value}>
                                <input
                                  type="checkbox"
                                  checked={draftRoles.includes(value)}
                                  onChange={(event) => (
                                    handleRoleToggle(user.id, value, event.target.checked)
                                  )}
                                />
                                <span>{label}</span>
                              </label>
                            ))}
                          </div>
                        </fieldset>
                        <div className="admin-row-actions">
                          {!hasTrainerRole && (
                            <button
                              className="button button-secondary"
                              type="button"
                              onClick={() => handleTrainerApproval(user)}
                              disabled={isUpdating}
                            >
                              트레이너 승인
                            </button>
                          )}
                          <button
                            className="button button-primary"
                            type="submit"
                            disabled={isUpdating || !isChanged || draftRoles.length === 0}
                          >
                            {isUpdating ? '저장 중…' : '변경 저장'}
                          </button>
                        </div>
                      </form>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

export default AdminUsersPage
