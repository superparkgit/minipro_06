import { useEffect, useState } from 'react'
import { decideReviewReport, getAdminReviews } from '../../api/reviewApi'
import { getApiErrorMessage } from '../../api/apiError'
import { hasRole, useCurrentUser } from '../../hooks/useCurrentUser'
import Pagination from '../../components/Pagination'

const stars = (rating) => '★★★★★☆☆☆☆☆'.slice(5 - rating, 10 - rating)

function AdminReviewModerationPage() {
  const { user, loading: userLoading } = useCurrentUser()
  const isAdmin = hasRole(user, 'ROLE_ADMIN')
  const [reviews, setReviews] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isAdmin) return
    setLoading(true)
    setError('')
    getAdminReviews({ page })
      .then(({ data }) => {
        setReviews(data.content)
        setTotalPages(data.totalPages)
      })
      .catch((requestError) => setError(getApiErrorMessage(requestError, '심사 대기 목록을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [page, isAdmin])

  const decide = async (reviewId, decision) => {
    setError('')
    try {
      await decideReviewReport(reviewId, { decision })
      setReviews((items) => items.filter((item) => item.id !== reviewId))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '심사 처리를 하지 못했습니다.'))
    }
  }

  if (userLoading) return <p className="notice">확인 중입니다.</p>
  if (!isAdmin) return <section className="page-card"><h1>관리자만 접근할 수 있습니다.</h1></section>

  return (
    <>
      <section className="section-heading">
        <div>
          <h1>리뷰 심사</h1>
          <p>트레이너가 삭제 요청한 리뷰를 승인(삭제) 또는 반려(복원)합니다.</p>
        </div>
      </section>

      <p className="api-note">신고 사유는 현재 API 응답에 포함되어 있지 않아 리뷰 내용만 표시됩니다.</p>

      {loading && <p className="notice">목록을 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && !error && reviews.length === 0 && <p className="page-card">심사 대기 중인 리뷰가 없습니다.</p>}

      <div className="review-list">
        {reviews.map((review) => (
          <article className="card review-card" key={review.id}>
            <div className="meta">
              <span className="star-rating">{stars(review.rating)}</span>
              <span>{review.programTitle}</span>
              <span>·</span><span>작성자 {review.userName}</span>
              <span>·</span><span>담당 트레이너 {review.trainerName}</span>
            </div>
            <p>{review.content}</p>
            <div className="row-actions">
              <button className="button button-danger" onClick={() => decide(review.id, 'APPROVED')}>승인(삭제 확정)</button>
              <button className="button button-secondary" onClick={() => decide(review.id, 'REJECTED')}>반려(복원)</button>
            </div>
          </article>
        ))}
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </>
  )
}

export default AdminReviewModerationPage
