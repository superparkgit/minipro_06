function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 0) return null

  const pages = Array.from({ length: totalPages }, (_, index) => index)

  return (
    <nav className="pagination" aria-label="페이지 이동">
      <button className="button button-secondary" disabled={page <= 0} onClick={() => onChange(page - 1)}>이전</button>
      <div className="pagination-pages">
        {pages.map((number) => (
          <button
            key={number}
            className={`pagination-number ${number === page ? 'active' : ''}`}
            aria-current={number === page ? 'page' : undefined}
            onClick={() => onChange(number)}
          >
            {number + 1}
          </button>
        ))}
      </div>
      <button className="button button-secondary" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>다음</button>
    </nav>
  )
}

export default Pagination
