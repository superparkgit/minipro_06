import PagePlaceholder from '../PagePlaceholder'

function ReviewsPage() {
  return <PagePlaceholder title="리뷰" owner="용재" description="출석 완료 회원이 리뷰를 작성하고 트레이너가 답변합니다." api="/api/reservations/{reservationId}/reviews" />
}

export default ReviewsPage
