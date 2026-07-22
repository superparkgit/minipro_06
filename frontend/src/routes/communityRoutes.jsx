import PostsPage from '../pages/community/PostsPage'
import ReviewsPage from '../pages/review/ReviewsPage'

export const communityRoutes = [
  { path: '/posts', element: <PostsPage /> },
  { path: '/reviews', element: <ReviewsPage /> },
]

export const communityNavigation = [
  { to: '/posts', label: '게시판' },
]
