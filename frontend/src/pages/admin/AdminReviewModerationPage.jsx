import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { decideReviewReport, getAdminReviews } from '../../api/reviewApi'
import { getApiErrorMessage } from '../../api/apiError'
import { USER_ROLES } from '../../auth/authStorage'
import useAuthState from '../../auth/useAuthState'
import Pagination from '../../components/Pagination'
import AdminSectionNav from './AdminSectionNav'
import './AdminUsersPage.css'

const stars = (rating) => '★★★★★☆☆☆☆☆'.slice(5 - rating, 10 - rating)

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

function AdminReviewModerationPage() {
  const { isAuthenticated, isProfileLoaded, roles } = useAuthState()
  const isAdmin = roles.includes(USER_ROLES.admin)
  const [reviews, setReviews] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [pageError, setPageError] = useState('')
  const [decidingReviewId, setDecidingReviewId] = useState(null)
  const [actionMessage, setActionMessage] = useState(null)

  const loadReviews = useCallback(async () => {
    if (!isAdmin) return

    setIsLoading(true)
    setPageError('')

    try {
      const { data } = await getAdminReviews({ page })
      if (!Array.isArray(data?.content)) {
        throw new Error('리뷰 심사 목록 응답 형식이 올바르지 않습니다.')
      }

      setReviews(data.content)
      setTotalPages(Number(data.totalPages) || 0)
    } catch (error) {
      setPageError(
        getApiErrorMessage(error, error?.message ?? '심사 대기 목록을 불러오지 못했습니다.'),
      )
    } finally {
      setIsLoading(false)
    }
  }, [isAdmin, page])

  useEffect(() => {
    loadReviews()
  }, [loadReviews])

  const decide = async (review, decision) => {
    if (
      decision === 'APPROVED'
      && !window.confirm('이 리뷰를 삭제 상태로 확정할까요?')
    ) {
      return
    }

    setDecidingReviewId(review.id)
    setActionMessage(null)

    try {
      await decideReviewReport(review.id, { decision })

      const isLastItemOnPage = reviews.length === 1 && page > 0
      if (isLastItemOnPage) {
        setPage((current) => current - 1)
      } else {
        setReviews((items) => items.filter((item) => item.id !== review.id))
      }

      setActionMessage({
        type: 'success',
        text: decision === 'APPROVED'
          ? '리뷰 삭제를 승인했습니다.'
          : '신고를 반려하고 리뷰를 다시 공개했습니다.',
      })
    } catch (error) {
      setActionMessage({
        type: 'error',
        text: getApiErrorMessage(error, '심사 처리를 완료하지 못했습니다.'),
      })
    } finally {
      setDecidingReviewId(null)
    }
  }

  if (!isAuthenticated) {
    return (
      <section className="admin-access-card" aria-labelledby="admin-review-login-required">
        <p className="admin-eyebrow">ADMIN ONLY</p>
        <h1 id="admin-review-login-required">로그인이 필요합니다.</h1>
        <p>관리자 계정으로 로그인한 뒤 리뷰 심사 화면을 이용해 주세요.</p>
        <Link className="button button-primary" to="/login">로그인하기</Link>
      </section>
    )
  }

  if (!isProfileLoaded) {
    return (
      <section className="admin-access-card" aria-labelledby="admin-review-profile-loading">
        <p className="admin-eyebrow">ADMIN ONLY</p>
        <h1 id="admin-review-profile-loading">회원 정보를 확인하고 있습니다.</h1>
      </section>
    )
  }

  if (!isAdmin) {
    return (
      <section className="admin-access-card" aria-labelledby="admin-review-access-denied">
        <p className="admin-eyebrow">ADMIN ONLY</p>
        <h1 id="admin-review-access-denied">관리자 권한이 없습니다.</h1>
        <p>신고된 리뷰의 승인과 반려는 관리자만 수행할 수 있습니다.</p>
        <Link className="button button-secondary" to="/">홈으로 돌아가기</Link>
      </section>
    )
  }

  return (
    <section
      className="admin-review-page"
      aria-labelledby="admin-review-title"
      aria-busy={isLoading || decidingReviewId !== null}
    >
      <AdminSectionNav />

      <header className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">REVIEW MODERATION</p>
          <h1 id="admin-review-title">리뷰 심사</h1>
          <p>트레이너가 신고한 리뷰를 확인하고 삭제 승인 또는 반려를 처리합니다.</p>
        </div>
        {!isLoading && !pageError && (
          <span className="admin-user-count" aria-label={`현재 페이지 심사 대기 ${reviews.length}건`}>
            {reviews.length}건
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
          심사 대기 목록을 불러오는 중입니다…
        </div>
      )}

      {!isLoading && pageError && (
        <div className="admin-state-card admin-state-error" role="alert">
          <strong>심사 대기 목록을 불러오지 못했습니다.</strong>
          <span>{pageError}</span>
          <button className="button button-secondary" type="button" onClick={loadReviews}>
            다시 시도
          </button>
        </div>
      )}

      {!isLoading && !pageError && reviews.length === 0 && (
        <div className="admin-state-card" role="status">
          심사 대기 중인 리뷰가 없습니다.
        </div>
      )}

      {!isLoading && !pageError && reviews.length > 0 && (
        <div className="admin-review-list">
          {reviews.map((review) => {
            const isDeciding = decidingReviewId === review.id

            return (
              <article className="admin-review-card" key={review.id}>
                <div className="admin-review-card-heading">
                  <div>
                    <span className="star-rating" aria-label={`평점 ${review.rating}점`}>
                      {stars(review.rating)}
                    </span>
                    <h2>{review.programTitle}</h2>
                  </div>
                  <span className="badge pending">심사 대기</span>
                </div>

                <p className="admin-review-content">{review.content}</p>

                {review.reportReason && (
                  <p className="notice notice-error">신고 사유: {review.reportReason}</p>
                )}

                <dl className="admin-review-meta">
                  <div><dt>작성자</dt><dd>{review.userName}</dd></div>
                  <div><dt>담당 트레이너</dt><dd>{review.trainerName}</dd></div>
                  <div><dt>작성일</dt><dd>{formatCreatedAt(review.createdAt)}</dd></div>
                </dl>

                <div className="admin-row-actions">
                  <button
                    className="button button-danger"
                    type="button"
                    onClick={() => decide(review, 'APPROVED')}
                    disabled={decidingReviewId !== null}
                  >
                    {isDeciding ? '처리 중…' : '삭제 승인'}
                  </button>
                  <button
                    className="button button-secondary"
                    type="button"
                    onClick={() => decide(review, 'REJECTED')}
                    disabled={decidingReviewId !== null}
                  >
                    {isDeciding ? '처리 중…' : '신고 반려'}
                  </button>
                </div>
              </article>
            )
          })}
        </div>
      )}

      {!isLoading && !pageError && totalPages > 1 && (
        <Pagination page={page} totalPages={totalPages} onChange={setPage} />
      )}
    </section>
  )
}

export default AdminReviewModerationPage
