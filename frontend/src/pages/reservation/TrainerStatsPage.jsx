const stats = [
  { label: '이번 달 승인 예약', value: '28건', detail: '전월 대비 +8건' },
  { label: '출석 완료 회원', value: '23명', detail: '출석률 82%' },
  { label: '인기 프로그램', value: '퇴근 후 릴랙스 요가', detail: '승인 예약 12건' },
]

function TrainerStatsPage() {
  return (
    <section>
      <div className="section-heading"><div><h1>트레이너 대시보드</h1><p>예약 현황과 프로그램 통계를 확인하세요.</p></div></div>
      <div className="grid stats-grid">
        {stats.map((stat) => <article className="card stat-card" key={stat.label}><p className="muted">{stat.label}</p><h2>{stat.value}</h2><p className="muted">{stat.detail}</p></article>)}
      </div>
      <article className="card">
        <h2>모집 인원 미달 프로그램</h2>
        <p className="muted">현재는 목업 데이터입니다. API 연결 후 실제 모집 현황을 표시합니다.</p>
        <p className="api-note">연결 API: GET /api/stats/programs/empty, /trainers/monthly, /programs/popular</p>
      </article>
    </section>
  )
}

export default TrainerStatsPage
