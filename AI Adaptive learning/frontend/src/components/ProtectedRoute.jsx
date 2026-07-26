import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ role, children }) {
  const { auth } = useAuth();

  if (!auth?.token || !auth?.user) {
    return <Navigate to="/" replace />;
  }

  if (role && auth.user.role !== role) {
    return <Navigate to={auth.user.role === 'INSTRUCTOR' ? '/instructor' : '/student'} replace />;
  }

  return children;
}
