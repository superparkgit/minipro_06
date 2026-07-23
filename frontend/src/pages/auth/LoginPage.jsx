import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { getMyProfile, login } from '../../api/authApi'
import { getApiErrorMessage } from '../../api/apiError'
import {
  AUTH_STORAGE_KEYS,
  clearStoredAuth,
  storeAuthenticatedUser,
} from '../../auth/authStorage'
import './AuthPages.css'

function LoginPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    email: location.state?.email ?? '',
    password: '',
  })
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const statusMessage = location.state?.signupComplete
    ? '회원가입이 완료되었습니다. 가입한 계정으로 로그인해 주세요.'
    : location.state?.logoutComplete
      ? '로그아웃되었습니다.'
      : ''

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
    setErrorMessage('')
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (isSubmitting) return

    setErrorMessage('')
    setIsSubmitting(true)

    let tokensStored = false

    try {
      const { data } = await login({
        email: form.email.trim(),
        password: form.password,
      })

      if (!data?.accessToken || !data?.refreshToken) {
        throw new Error('로그인 응답에서 토큰을 확인할 수 없습니다.')
      }

      localStorage.setItem(AUTH_STORAGE_KEYS.accessToken, data.accessToken)
      localStorage.setItem(AUTH_STORAGE_KEYS.refreshToken, data.refreshToken)
      tokensStored = true

      const { data: profile } = await getMyProfile()
      storeAuthenticatedUser(profile)

      window.dispatchEvent(new Event('auth:changed'))
      navigate('/programs', { replace: true })
    } catch (error) {
      if (tokensStored) clearStoredAuth()

      const message = error?.response?.status === 401
        ? '이메일 또는 비밀번호가 올바르지 않습니다.'
        : getApiErrorMessage(
          error,
          error?.message ?? '로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.',
        )

      setErrorMessage(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="auth-page" aria-labelledby="login-title">
      <div className="auth-shell">
        <aside className="auth-context" aria-label="서비스 안내">
          <span className="auth-mark" aria-hidden="true">FR</span>
          <div>
            <p className="auth-kicker">FITNESS RESERVE</p>
            <h2>운동 계획을<br />한곳에서 이어가세요.</h2>
            <p className="auth-context-copy">
              프로그램 탐색부터 예약과 커뮤니티까지, 로그인하면 나의 일정으로 연결됩니다.
            </p>
          </div>
          <ul className="auth-benefits" aria-label="로그인 후 이용 가능 기능">
            <li>내 예약 현황 확인</li>
            <li>프로그램 참여와 리뷰 작성</li>
          </ul>
        </aside>

        <div className="auth-panel">
          <header className="auth-heading">
            <p className="auth-eyebrow">계정 로그인</p>
            <h1 id="login-title">다시 만나 반가워요.</h1>
            <p>가입한 이메일과 비밀번호를 입력해 주세요.</p>
          </header>

          {statusMessage && (
            <p className="auth-message auth-message-success" role="status">
              {statusMessage}
            </p>
          )}

          {errorMessage && (
            <p className="auth-message auth-message-error" role="alert">
              {errorMessage}
            </p>
          )}

          <form className="auth-form" onSubmit={handleSubmit}>
            <label htmlFor="login-email">
              이메일
              <input
                id="login-email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="name@example.com"
                autoComplete="username"
                inputMode="email"
                required
                disabled={isSubmitting}
              />
            </label>

            <label htmlFor="login-password">
              비밀번호
              <input
                id="login-password"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                placeholder="8자 이상 입력"
                autoComplete="current-password"
                minLength={8}
                maxLength={64}
                required
                disabled={isSubmitting}
              />
            </label>

            <button
              className="button button-primary auth-submit"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? '로그인 중…' : '로그인'}
            </button>
          </form>

          <p className="auth-switch">
            아직 계정이 없나요? <Link to="/signup">회원가입</Link>
          </p>
        </div>
      </div>
    </section>
  )
}

export default LoginPage
