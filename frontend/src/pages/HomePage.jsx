import { Link } from 'react-router-dom'

function HomePage() {
  return (
    <section className="hero">
      <p className="eyebrow">나에게 맞는 운동을 찾는 가장 쉬운 방법</p>
      <h1>예약부터 출석과 리뷰까지, 한 곳에서.</h1>
      <p>원하는 프로그램을 찾고, 트레이너와 함께 건강한 루틴을 시작해 보세요.</p>
      <Link className="button button-primary" to="/programs">프로그램 둘러보기</Link>
    </section>
  )
}

export default HomePage
