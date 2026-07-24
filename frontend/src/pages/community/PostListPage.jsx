import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyPosts, getPosts } from '../../api/postApi'
import { getApiErrorMessage } from '../../api/apiError'
import Pagination from '../../components/Pagination'

const categories = [
  { value: '', label: '전체' },
  { value: 'QUESTION', label: '질문' },
  { value: 'NOTICE', label: '공지' },
]

const categoryLabel = { QUESTION: '질문', NOTICE: '공지' }

const sortOptions = [
  { value: 'createdAt,desc', label: '최신순' },
  { value: 'viewCount,desc', label: '조회순' },
]

const formatDate = (value) => value
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value))
  : ''

function PostListPage() {
  const [category, setCategory] = useState('')
  const [keyword, setKeyword] = useState('')
  const [keywordInput, setKeywordInput] = useState('')
  const [mine, setMine] = useState(false)
  const [sort, setSort] = useState(sortOptions[0].value)
  const [page, setPage] = useState(0)
  const [posts, setPosts] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    const request = mine
      ? getMyPosts({ page, sort })
      : getPosts({ category: category || undefined, keyword: keyword || undefined, page, sort })

    request
      .then(({ data }) => {
        setPosts(data.content)
        setTotalPages(data.totalPages)
      })
      .catch((requestError) => setError(getApiErrorMessage(requestError, '게시글 목록을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [category, keyword, mine, sort, page])

  const submitSearch = (event) => {
    event.preventDefault()
    setPage(0)
    setKeyword(keywordInput.trim())
  }

  return (
    <>
      <section className="section-heading">
        <div>
          <h1>게시판</h1>
          <p>질문과 공지를 확인하고 자유롭게 글을 작성하세요.</p>
        </div>
        <Link className="button button-primary" to="/posts/new">글쓰기</Link>
      </section>

      <div className="post-toolbar">
        <div className="post-tabs">
          {categories.map((item) => (
            <button
              key={item.value}
              className={`tab ${!mine && category === item.value ? 'active' : ''}`}
              disabled={mine}
              onClick={() => { setCategory(item.value); setPage(0) }}
            >
              {item.label}
            </button>
          ))}
        </div>
        <form className="post-search" onSubmit={submitSearch}>
          <input
            placeholder="제목, 내용 검색"
            value={keywordInput}
            disabled={mine}
            onChange={(event) => setKeywordInput(event.target.value)}
          />
          <button className="button button-secondary" type="submit" disabled={mine}>검색</button>
        </form>
        <div className="post-tabs">
          {sortOptions.map((item) => (
            <button
              key={item.value}
              className={`tab ${sort === item.value ? 'active' : ''}`}
              onClick={() => { setSort(item.value); setPage(0) }}
            >
              {item.label}
            </button>
          ))}
        </div>
        <label className="post-mine-toggle">
          <input
            type="checkbox"
            checked={mine}
            onChange={(event) => { setMine(event.target.checked); setPage(0) }}
          />
          내 글만 보기
        </label>
      </div>

      {loading && <p className="notice">게시글을 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && !error && posts.length === 0 && <p className="page-card">등록된 게시글이 없습니다.</p>}

      <div className="post-list">
        {posts.map((post) => (
          <Link className="card post-row" key={post.id} to={`/posts/${post.id}`}>
            <div>
              <span className="badge">{categoryLabel[post.category] ?? post.category}</span>
              <h2>{post.title}</h2>
              <p className="meta">
                <span>{post.writerName}</span>
                <span>·</span><span>{formatDate(post.createdAt)}</span>
                <span>·</span><span>조회 {post.viewCount}</span>
                <span>·</span><span>댓글 {post.commentCount}</span>
              </p>
            </div>
          </Link>
        ))}
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </>
  )
}

export default PostListPage
