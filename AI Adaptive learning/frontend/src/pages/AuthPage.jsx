import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const defaultForm = {
  name: '',
  email: '',
  password: '',
  role: 'STUDENT'
};

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [form, setForm] = useState(defaultForm);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { auth, login } = useAuth();

  useEffect(() => {
    if (auth?.user?.role === 'STUDENT') navigate('/student');
    if (auth?.user?.role === 'INSTRUCTOR') navigate('/instructor');
  }, [auth, navigate]);

  useEffect(() => {
    setForm(defaultForm);
    setError('');
    setStatus('');
  }, [isLogin]);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setStatus('');
    setLoading(true);

    try {
      const endpoint = isLogin ? '/auth/login' : '/auth/register';
      const payload = isLogin
        ? { email: form.email, password: form.password }
        : form;

      const { data } = await api.post(endpoint, payload);
      
      if (isLogin) {
        login(data);
        navigate(data.role === 'INSTRUCTOR' ? '/instructor' : '/student');
      } else {
        setStatus('Registration successful! Redirecting to login...');
        setForm(defaultForm);
        setTimeout(() => {
          setIsLogin(true);
          setForm(defaultForm);
        }, 1500);
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="grid md:grid-cols-2 gap-6">
      <div className="card p-6 sm:p-8 animate-rise">
        <h2 className="font-heading text-3xl font-bold mb-2">
          {isLogin ? 'Welcome back' : 'Create your account'}
        </h2>
        <p className="text-slate-600 mb-6">
          SkillForge adapts quiz difficulty from your performance and lets instructors generate AI-powered assessments.
        </p>

        <form onSubmit={submit} className="space-y-4">
          {!isLogin && (
            <input
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
              placeholder="Full name"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          )}

          <input
            type="email"
            className="w-full rounded-xl border border-slate-300 px-4 py-3"
            placeholder="Email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            required
          />

          <input
            type="password"
            className="w-full rounded-xl border border-slate-300 px-4 py-3"
            placeholder="Password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />

          {!isLogin && (
            <select
              className="w-full rounded-xl border border-slate-300 px-4 py-3"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
            >
              <option value="STUDENT">Student</option>
              <option value="INSTRUCTOR">Instructor</option>
            </select>
          )}

          {error && <p className="text-red-600 text-sm">{error}</p>}
          {status && <p className="text-green-600 text-sm">{status}</p>}

          <button
            disabled={loading}
            className="w-full rounded-xl bg-accent text-white py-3 font-semibold hover:opacity-90 disabled:opacity-60"
          >
            {loading ? 'Please wait...' : (isLogin ? 'Login' : 'Register')}
          </button>
        </form>

        <button
          onClick={() => {
            setIsLogin((v) => !v);
            setForm(defaultForm);
            setError('');
            setStatus('');
          }}
          className="mt-4 text-sm text-slate-700 underline"
        >
          {isLogin ? 'New user? Register here' : 'Already have an account? Login'}
        </button>
      </div>

      <div className="card p-6 sm:p-8 bg-gradient-to-br from-sky-500 to-cyan-600 text-white animate-rise [animation-delay:160ms]">
        <h3 className="font-heading text-2xl font-bold mb-4">What you can do</h3>
        <ul className="space-y-3 text-sm sm:text-base">
          <li>Adaptive recommendations: Easy, Medium, or Hard based on latest attempts.</li>
          <li>AI-generated quizzes by topic and difficulty for instructors.</li>
          <li>Student and Instructor dashboards with dedicated analytics.</li>
          <li>Performance tracking to monitor growth over time.</li>
        </ul>
      </div>
    </section>
  );
}
