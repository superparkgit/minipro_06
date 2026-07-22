import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getProgramById } from './programData'

function ProgramManagePage() {
  const { programId } = useParams()
  const program = getProgramById(programId)
  const [programStatus, setProgramStatus] = useState('OPEN')
  const [assistants, setAssistants] = useState([{ id: 2, name: '박보조 트레이너', role: 'ASSISTANT' }])

  if (!program) return <section className="page-card"><h1>프로그램을 찾을 수 없습니다.</h1></section>

  const addAssistant = () => setAssistants((items) => [...items, { id: Date.now(), name: '새 보조 트레이너', role: 'ASSISTANT' }])
  const removeAssistant = (assistantId) => setAssistants((items) => items.filter((assistant) => assistant.id !== assistantId))

  return (
    <section>
      <div className="section-heading">
        <div><h1>{program.title} 관리</h1><p>MAIN 트레이너가 프로그램 상태와 담당 강사를 관리합니다.</p></div>
        <Link className="button button-secondary" to={`/programs/${program.id}/edit`}>프로그램 수정</Link>
      </div>

      <div className="grid manage-grid">
        <article className="card">
          <h2>수업 상태</h2>
          <p className="muted">현재 상태: <span className={`badge ${programStatus.toLowerCase()}`}>{programStatus}</span></p>
          <div className="row-actions">
            <button className="button button-secondary" onClick={() => setProgramStatus('COMPLETED')}>수업 완료</button>
            <button className="button button-danger" onClick={() => setProgramStatus('CANCELED')}>폐강 처리</button>
          </div>
          <p className="api-note">연결 API: PATCH /api/programs/{`{id}`}/complete, /cancel</p>
        </article>

        <article className="card">
          <h2>예약 관리</h2>
          <p className="muted">신청 회원을 승인·거절하고 수업 후 출석을 처리합니다.</p>
          <Link className="button button-primary" to={`/programs/${program.id}/reservations`}>예약자 보기</Link>
        </article>
      </div>

      <article className="card assistant-card">
        <div className="section-heading"><div><h2>담당 트레이너</h2><p>대표 담당자와 보조 담당자를 확인합니다.</p></div><button className="button button-secondary" onClick={addAssistant}>보조 강사 추가</button></div>
        <div className="reservation-list">
          <div className="reservation-row"><div><h3>{program.trainer}</h3><p>대표 담당 트레이너</p></div><span className="badge">MAIN</span></div>
          {assistants.map((assistant) => (
            <div className="reservation-row" key={assistant.id}><div><h3>{assistant.name}</h3><p>보조 담당 트레이너</p></div><div className="row-actions"><span className="badge">{assistant.role}</span><button className="button button-danger" onClick={() => removeAssistant(assistant.id)}>제거</button></div></div>
          ))}
        </div>
        <p className="api-note">연결 API: POST /api/programs/{`{id}`}/trainers, DELETE /api/programs/{`{id}`}/trainers/{`{trainerId}`}</p>
      </article>
    </section>
  )
}

export default ProgramManagePage
