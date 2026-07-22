function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null

  return (
    <nav className="pagination">
      <button className="button button-secondary" disabled={page <= 0} onClick={() => onChange(page - 1)}>이전</button>
      <span className="pagination-status">{page + 1} / {totalPages}</span>
      <button className="button button-secondary" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>다음</button>
    </nav>
  )
}

export default Pagination
