import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { createPost, getPost, updatePost } from '../../api/postApi'
import { getApiErrorMessage } from '../../api/apiError'

function PostFormPage() {
  const { postId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const isEdit = Boolean(postId)

  const [category, setCategory] = useState('QUESTION')
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEdit) return

    const applyPost = (post) => {
      setCategory(post.category)
      setTitle(post.title)
      setContent(post.content)
      setLoading(false)
    }

    if (location.state?.post) {
      applyPost(location.state.post)
      return
    }

    getPost(postId)
      .then(({ data }) => applyPost(data))
      .catch((requestError) => {
        setError(getApiErrorMessage(requestError, '게시글을 불러오지 못했습니다.'))
        setLoading(false)
      })
  }, [postId, isEdit, location.state])

  const submit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      if (isEdit) {
        await updatePost(postId, { category, title, content })
        navigate(`/posts/${postId}`)
      } else {
        const { data } = await createPost({ category, title, content })
        navigate(`/posts/${data.id}`)
      }
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '게시글을 저장하지 못했습니다.'))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p className="notice">게시글을 불러오는 중입니다.</p>

  return (
    <section className="card form-card">
      <div className="section-heading"><div><h1>{isEdit ? '게시글 수정' : '게시글 작성'}</h1><p>질문 또는 공지 내용을 작성하세요.</p></div></div>
      <form className="form-grid" onSubmit={submit}>
        <label>카테고리
          <select value={category} onChange={(event) => setCategory(event.target.value)}>
            <option value="QUESTION">질문</option>
            <option value="NOTICE">공지</option>
          </select>
        </label>
        <label>제목
          <input required minLength={2} maxLength={100} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="2자 이상 100자 이하" />
        </label>
        <label>내용
          <textarea required minLength={5} maxLength={10000} rows="10" value={content} onChange={(event) => setContent(event.target.value)} placeholder="5자 이상 10000자 이하" />
        </label>
        <div className="form-actions">
          <button className="button button-primary" type="submit" disabled={submitting}>{isEdit ? '수정 저장' : '등록하기'}</button>
        </div>
      </form>
      {error && <p className="notice notice-error">{error}</p>}
    </section>
  )
}

export default PostFormPage
