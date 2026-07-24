import PostDetailPage from '../pages/community/PostDetailPage'
import PostFormPage from '../pages/community/PostFormPage'
import PostListPage from '../pages/community/PostListPage'
import ProgramReviewsPage from '../pages/review/ProgramReviewsPage'
import TrainerReviewsPage from '../pages/review/TrainerReviewsPage'
import ReviewFormPage from '../pages/review/ReviewFormPage'

export const communityRoutes = [
  { path: '/posts', element: <PostListPage /> },
  { path: '/posts/new', element: <PostFormPage /> },
  { path: '/posts/:postId/edit', element: <PostFormPage /> },
  { path: '/posts/:postId', element: <PostDetailPage /> },
  { path: '/programs/:programId/reviews', element: <ProgramReviewsPage /> },
  { path: '/trainers/:trainerId/reviews', element: <TrainerReviewsPage /> },
  { path: '/reviews/write', element: <ReviewFormPage /> },
  { path: '/reviews/:reviewId/edit', element: <ReviewFormPage /> },
]

export const communityNavigation = [
  { to: '/posts', label: '게시판' },
]
