import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import AppLayout from './components/layout/AppLayout';
import LoginPage from './features/auth/pages/LoginPage';
import UserListPage from './features/user/pages/UserListPage';
import UserFormPage from './features/user/pages/UserFormPage';
import RoleListPage from './features/role/pages/RoleListPage';
import RoleFormPage from './features/role/pages/RoleFormPage';
import RoadmapListPage from './features/roadmap/pages/RoadmapListPage';
import RoadmapFormPage from './features/roadmap/pages/RoadmapFormPage';
import FeatureListPage from './features/feature/pages/FeatureListPage';
import FeatureFormPage from './features/feature/pages/FeatureFormPage';
import TaskListPage from './features/task/pages/TaskListPage';
import TaskFormPage from './features/task/pages/TaskFormPage';
import FunctionalAreaListPage from './features/functionalArea/pages/FunctionalAreaListPage';
import FunctionalAreaFormPage from './features/functionalArea/pages/FunctionalAreaFormPage';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

const Router = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/users" replace />} />
        <Route path="users" element={<UserListPage />} />
        <Route path="users/new" element={<UserFormPage />} />
        <Route path="users/:id/edit" element={<UserFormPage />} />
        <Route path="roles" element={<RoleListPage />} />
        <Route path="roles/new" element={<RoleFormPage />} />
        <Route path="roles/:id/edit" element={<RoleFormPage />} />
        <Route path="roadmaps" element={<RoadmapListPage />} />
        <Route path="roadmaps/new" element={<RoadmapFormPage />} />
        <Route path="roadmaps/:id/edit" element={<RoadmapFormPage />} />
        <Route path="functional-areas" element={<FunctionalAreaListPage />} />
        <Route path="functional-areas/new" element={<FunctionalAreaFormPage />} />
        <Route path="functional-areas/:id/edit" element={<FunctionalAreaFormPage />} />
        <Route path="features" element={<FeatureListPage />} />
        <Route path="features/new" element={<FeatureFormPage />} />
        <Route path="features/:id/edit" element={<FeatureFormPage />} />
        <Route path="tasks" element={<TaskListPage />} />
        <Route path="tasks/new" element={<TaskFormPage />} />
        <Route path="tasks/:id/edit" element={<TaskFormPage />} />
      </Route>
    </Routes>
  );
};

export default Router;

