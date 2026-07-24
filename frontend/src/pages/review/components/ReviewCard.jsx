import { useState } from 'react'
import { Link } from 'react-router-dom'

export const formatDate = (value) => value
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value))
  : ''

export const stars = (rating) => '★★★★★☆☆☆☆☆'.slice(5 - rating, 10 - rating)

function ReviewCard({ review, isOwner, isAssignedTrainer, onDelete, onSubmitReply, onSubmitReport }) {
  const [openReply, setOpenReply] = useState(false)
  const [openReport, setOpenReport] = useState(false)
  const [replyDraft, setReplyDraft] = useState(review.reply?.content ?? '')
  const [reportDraft, setReportDraft] = useState('')

  const submitReply = async () => {
    const content = replyDraft.trim()
    if (!content) return
    await onSubmitReply(review, content)
    setOpenReply(false)
  }

  const submitReport = async () => {
    const reason = reportDraft.trim()
    if (!reason) return
    await onSubmitReport(review, reason)
    setOpenReport(false)
  }

  return (
    <article className="card review-card">
      <div className="meta">
        <span className="star-rating">{stars(review.rating)}</span>
        <span>{review.userName}</span>
        <span>·</span><span>{formatDate(review.createdAt)}</span>
      </div>
      <p>{review.content}</p>

      {(isOwner || isAssignedTrainer) && (
        <div className="row-actions">
          {isOwner && (
            <>
              <Link className="button button-secondary" to={`/reviews/${review.id}/edit`} state={{ review }}>수정</Link>
              <button className="button button-danger" onClick={() => onDelete(review.id)}>삭제</button>
            </>
          )}
          {isAssignedTrainer && (
            <>
              <button className="button button-secondary" onClick={() => setOpenReply((value) => !value)}>
                {review.reply ? '답변 수정' : '답변 작성'}
              </button>
              <button className="button button-danger" onClick={() => setOpenReport((value) => !value)}>삭제 요청</button>
            </>
          )}
        </div>
      )}

      {review.reply && (
        <div className="review-reply">
          <strong>{review.reply.trainerName} 트레이너 답변</strong>
          <p>{review.reply.content}</p>
        </div>
      )}

      {openReply && (
        <div className="comment-edit">
          <textarea
            rows="3"
            maxLength={500}
            placeholder="답변을 입력하세요"
            value={replyDraft}
            onChange={(event) => setReplyDraft(event.target.value)}
          />
          <div className="row-actions">
            <button className="button button-primary" onClick={submitReply}>저장</button>
            <button className="button button-secondary" onClick={() => setOpenReply(false)}>취소</button>
          </div>
        </div>
      )}

      {openReport && (
        <div className="comment-edit">
          <textarea
            rows="2"
            maxLength={200}
            placeholder="삭제 요청 사유를 입력하세요"
            value={reportDraft}
            onChange={(event) => setReportDraft(event.target.value)}
          />
          <div className="row-actions">
            <button className="button button-danger" onClick={submitReport}>요청 제출</button>
            <button className="button button-secondary" onClick={() => setOpenReport(false)}>취소</button>
          </div>
        </div>
      )}
    </article>
  )
}

export default ReviewCard
