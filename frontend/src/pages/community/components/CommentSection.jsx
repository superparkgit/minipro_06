import { useEffect, useState } from 'react'
import { createComment, deleteComment, getComments, updateComment } from '../../../api/commentApi'
import { getApiErrorMessage } from '../../../api/apiError'
import { useCurrentUser } from '../../../hooks/useCurrentUser'
import Pagination from '../../../components/Pagination'

const formatDateTime = (value) => value
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
  : ''

function CommentSection({ postId }) {
  const { user } = useCurrentUser()
  const [comments, setComments] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const [editingId, setEditingId] = useState(null)
  const [editContent, setEditContent] = useState('')

  const load = () => {
    setLoading(true)
    setError('')
    getComments(postId, { page })
      .then(({ data }) => {
        setComments(data.content)
        setTotalPages(data.totalPages)
      })
      .catch((requestError) => setError(getApiErrorMessage(requestError, '댓글을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [postId, page])

  const submitComment = async (event) => {
    event.preventDefault()
    if (!content.trim()) return
    setSubmitting(true)
    setError('')
    try {
      await createComment(postId, { content })
      setContent('')
      if (page === 0) load()
      else setPage(0)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '댓글을 등록하지 못했습니다.'))
    } finally {
      setSubmitting(false)
    }
  }

  const startEdit = (comment) => {
    setEditingId(comment.id)
    setEditContent(comment.content)
  }

  const saveEdit = async (commentId) => {
    if (!editContent.trim()) return
    setError('')
    try {
      const { data } = await updateComment(commentId, { content: editContent })
      setComments((items) => items.map((item) => item.id === commentId ? data : item))
      setEditingId(null)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '댓글을 수정하지 못했습니다.'))
    }
  }

  const remove = async (commentId) => {
    if (!window.confirm('댓글을 삭제할까요?')) return
    setError('')
    try {
      await deleteComment(commentId)
      setComments((items) => items.filter((item) => item.id !== commentId))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '댓글을 삭제하지 못했습니다.'))
    }
  }

  return (
    <section className="comment-section">
      <h2>댓글</h2>
      <form className="comment-form" onSubmit={submitComment}>
        <textarea
          rows="3"
          maxLength={500}
          placeholder="댓글을 입력하세요 (최대 500자)"
          value={content}
          onChange={(event) => setContent(event.target.value)}
        />
        <div className="form-actions">
          <button className="button button-primary" type="submit" disabled={submitting}>등록</button>
        </div>
      </form>

      {error && <p className="notice notice-error">{error}</p>}
      {loading && <p className="notice">댓글을 불러오는 중입니다.</p>}
      {!loading && comments.length === 0 && <p className="muted">첫 댓글을 남겨보세요.</p>}

      <ul className="comment-list">
        {comments.map((comment) => (
          <li className="comment-item" key={comment.id}>
            <div className="meta">
              <strong>{comment.writerName}</strong>
              <span>{formatDateTime(comment.createdAt)}</span>
            </div>
            {editingId === comment.id ? (
              <div className="comment-edit">
                <textarea rows="3" maxLength={500} value={editContent} onChange={(event) => setEditContent(event.target.value)} />
                <div className="row-actions">
                  <button className="button button-primary" onClick={() => saveEdit(comment.id)}>저장</button>
                  <button className="button button-secondary" onClick={() => setEditingId(null)}>취소</button>
                </div>
              </div>
            ) : (
              <>
                <p>{comment.content}</p>
                {user?.id === comment.writerId && (
                  <div className="row-actions">
                    <button className="button button-secondary" onClick={() => startEdit(comment)}>수정</button>
                    <button className="button button-danger" onClick={() => remove(comment.id)}>삭제</button>
                  </div>
                )}
              </>
            )}
          </li>
        ))}
      </ul>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </section>
  )
}

export default CommentSection
