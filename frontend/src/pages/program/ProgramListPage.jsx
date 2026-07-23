import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getPrograms } from '../../api/programApi'
import { getApiErrorMessage } from '../../api/apiError'
import { hasRole, useCurrentUser } from '../../hooks/useCurrentUser'
import { programs as demoPrograms } from './programData'

const typeIcon = { PT: '🏋️', GROUP: '🧘' }

const formatSchedule = (startAt) => startAt
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(startAt))
  : '일정 미정'

function ProgramListPage() {
  const { user } = useCurrentUser()
  const [programs, setPrograms] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getPrograms()
      .then(({ data }) => setPrograms(data))
      .catch((requestError) => {
        if (localStorage.getItem('accessToken') === 'demo-access-token') {
          setPrograms(demoPrograms.map((item) => ({ ...item, startAt: new Date(Date.now()+86400000*item.id).toISOString(), status: 'OPEN', trainers:[{id:item.trainerId,name:item.trainer,assignmentRole:'MAIN'}] })))
        } else {
          setError(getApiErrorMessage(requestError, '프로그램 목록을 불러오지 못했습니다.'))
        }
      })
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <section className="section-heading">
        <div>
          <h1>내게 맞는 프로그램</h1>
          <p>목표와 일정에 맞는 수업을 선택해 보세요.</p>
        </div>
        {hasRole(user, 'ROLE_TRAINER') && <Link className="button button-secondary" to="/programs/new">프로그램 등록</Link>}
      </section>
      {loading && <p className="notice">프로그램을 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && programs.length === 0 && <p className="page-card">등록된 프로그램이 없습니다.</p>}
      <section className="grid program-grid">
        {programs.map((program) => (
          <Link className="card program-card" key={program.id} to={`/programs/${program.id}`}>
            <div>
              <div className="program-icon">{typeIcon[program.type] ?? '🏃'}</div>
              <span className="badge">{program.type}</span>
              <h2>{program.title}</h2>
              <p>{program.description}</p>
            </div>
            <div className="meta">
              <span>{program.trainers?.find((trainer) => trainer.assignmentRole === 'MAIN')?.name ?? '담당 트레이너 미정'}</span>
              <span>·</span><span>{formatSchedule(program.startAt)}</span>
              <span>·</span><span>{program.status}</span>
            </div>
          </Link>
        ))}
      </section>
    </>
  )
}

export default ProgramListPage
