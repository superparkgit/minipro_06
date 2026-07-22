function PagePlaceholder({ title, owner, description, api }) {
  return (
    <section className="page-card">
      <h1>{title}</h1>
      <p><strong>담당:</strong> {owner}</p>
      <p>{description}</p>
      {api && <p><strong>연결 예정 API:</strong> <code>{api}</code></p>}
    </section>
  )
}

export default PagePlaceholder
