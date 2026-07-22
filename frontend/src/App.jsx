import { Route, Routes } from 'react-router-dom'
import AppLayout from './layouts/AppLayout'
import { appRoutes } from './routes/appRoutes'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        {appRoutes.map(({ path, element }) => <Route key={path} path={path} element={element} />)}
      </Route>
    </Routes>
  )
}

export default App
