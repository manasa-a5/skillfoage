import { useAuth } from '../context/AuthContext';

export default function Header() {
  const { auth, logout } = useAuth();

  return (
    <header className="max-w-6xl mx-auto pt-6 pb-4 px-4 sm:px-6 lg:px-8">
      <div className="card px-5 py-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 animate-rise">
        <div>
          <h1 className="font-heading text-2xl sm:text-3xl font-bold tracking-tight">SkillForge</h1>
          <p className="text-slate-600 text-sm sm:text-base">AI-Driven Adaptive Learning & Exam Generator</p>
        </div>
        {auth?.user && (
          <div className="flex items-center gap-3">
            <div className="text-right">
              <p className="text-sm text-slate-500">Signed in as</p>
              <p className="font-semibold">{auth.user.name} ({auth.user.role})</p>
            </div>
            <button
              onClick={logout}
              className="rounded-xl bg-primary text-white px-4 py-2 text-sm font-medium hover:opacity-90"
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
