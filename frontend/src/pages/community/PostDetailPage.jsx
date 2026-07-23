import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deletePost, getPost } from '../../api/postApi'
import { getApiErrorMessage } from '../../api/apiError'
import { useCurrentUser } from '../../hooks/useCurrentUser'
import CommentSection from './components/CommentSection'

const categoryLabel = { QUESTION: '질문', NOTICE: '공지' }

const formatDateTime = (value) => value
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'long', timeStyle: 'short' }).format(new Date(value))
  : ''

function PostDetailPage() {
  const { postId } = useParams()
  const navigate = useNavigate()
  const { user } = useCurrentUser()
  const [post, setPost] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    getPost(postId, { signal: controller.signal })
      .then(({ data }) => setPost(data))
      .catch((requestError) => {
        if (requestError.code === 'ERR_CANCELED') return
        setError(getApiErrorMessage(requestError, '게시글을 불러오지 못했습니다.'))
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [postId])

  const remove = async () => {
    if (!window.confirm('게시글을 삭제할까요?')) return
    try {
      await deletePost(postId)
      navigate('/posts')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '게시글을 삭제하지 못했습니다.'))
    }
  }

  if (loading) return <p className="notice">게시글을 불러오는 중입니다.</p>
  if (!post) return <section className="page-card"><h1>게시글을 찾을 수 없습니다.</h1><p>{error}</p><Link to="/posts">목록으로 돌아가기</Link></section>

  return (
    <>
      <article className="card detail-card post-detail">
        <span className="badge">{categoryLabel[post.category] ?? post.category}</span>
        <h1>{post.title}</h1>
        <p className="meta">
          <span>{post.writerName}</span>
          <span>·</span><span>{formatDateTime(post.createdAt)}</span>
          <span>·</span><span>조회 {post.viewCount}</span>
        </p>
        <hr />
        <p className="post-content">{post.content}</p>
        <div className="row-actions">
          {user?.id === post.writerId && (
            <>
              <button className="button button-secondary" onClick={() => navigate(`/posts/${post.id}/edit`, { state: { post } })}>수정</button>
              <button className="button button-danger" onClick={remove}>삭제</button>
            </>
          )}
          <Link className="button button-secondary" to="/posts">목록</Link>
        </div>
        {error && <p className="notice notice-error">{error}</p>}
      </article>

      <CommentSection postId={post.id} />
    </>
  )
}

export default PostDetailPage
