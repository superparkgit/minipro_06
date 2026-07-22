import { useState } from 'react'
import { useParams } from 'react-router-dom'

function ProgramFormPage() {
  const { programId } = useParams()
  const isEdit = Boolean(programId)
  const [submitted, setSubmitted] = useState(false)
  const submit = (event) => { event.preventDefault(); setSubmitted(true) }

  return (
    <section className="card form-card">
      <div className="section-heading"><div><h1>{isEdit ? '프로그램 수정' : '프로그램 등록'}</h1><p>트레이너가 프로그램 정보를 관리합니다.</p></div></div>
      <form className="form-grid" onSubmit={submit}>
        <label>프로그램명<input required placeholder="예: 퇴근 후 릴랙스 요가" /></label>
        <label>유형<select defaultValue="GROUP"><option value="GROUP">그룹 수업</option><option value="PT">1:1 PT</option></select></label>
        <label>수업 설명<textarea rows="4" placeholder="프로그램 내용을 입력하세요." /></label>
        <label>정원<input required min="1" type="number" defaultValue="8" /></label>
        <div className="form-actions"><button className="button button-primary" type="submit">{isEdit ? '수정 저장' : '등록하기'}</button></div>
      </form>
      {submitted && <p className="notice">현재는 목업입니다. 이후 {isEdit ? 'PATCH /api/programs/{id}' : 'POST /api/programs'}와 연결됩니다.</p>}
    </section>
  )
}

export default ProgramFormPage
