import { useEffect, useState } from 'react';
import api from '../services/api';

export default function StudentDashboard() {
  const [analytics, setAnalytics] = useState(null);
  const [quizzes, setQuizzes] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [activeQuiz, setActiveQuiz] = useState(null);
  const [answers, setAnswers] = useState({});
  const [attemptResult, setAttemptResult] = useState(null);
  const [attempts, setAttempts] = useState([]);

  const load = async () => {
    const [a, q, h, m] = await Promise.all([
      api.get('/student/analytics'),
      api.get('/student/quizzes'),
      api.get('/student/attempts'),
      api.get('/student/materials')
    ]);

    setAnalytics(a.data);
    setQuizzes(q.data);
    setAttempts(h.data);
    setMaterials(m.data);
  };

  useEffect(() => {
    load();
  }, []);

  const openQuiz = async (id) => {
    const { data } = await api.get(`/student/quizzes/${id}`);
    setActiveQuiz(data);
    setAnswers({});
    setAttemptResult(null);
  };

  const submitQuiz = async () => {
    if (!activeQuiz) return;

    const payload = {
      answers: Object.fromEntries(Object.entries(answers).map(([k, v]) => [Number(k), v]))
    };

    const { data } = await api.post(`/student/quizzes/${activeQuiz.id}/submit`, payload);
    setAttemptResult(data);
    load();
  };

  return (
    <div className="space-y-6">
      <section className="grid sm:grid-cols-3 gap-4 animate-rise">
        <MetricCard label="Total Attempts" value={analytics?.attemptCount ?? '-'} />
        <MetricCard
          label="Average Score"
          value={analytics ? `${analytics.averageScorePercent.toFixed(1)}%` : '-'}
        />
        <MetricCard label="Adaptive Recommendation" value={analytics?.adaptiveRecommendation ?? '-'} />
      </section>

      <section className="card p-6 animate-rise [animation-delay:100ms]">
        <h2 className="font-heading text-2xl font-bold mb-4">Available Quizzes</h2>
        <div className="grid md:grid-cols-2 gap-3">
          {quizzes.map((quiz) => (
            <button
              key={quiz.id}
              className="text-left rounded-xl border border-slate-200 p-4 hover:border-sky-400"
              onClick={() => openQuiz(quiz.id)}
            >
              <p className="font-semibold">{quiz.title}</p>
              <p className="text-sm text-slate-600">{quiz.topic} | {quiz.difficulty}</p>
            </button>
          ))}
          {quizzes.length === 0 && <p className="text-slate-600">No quizzes published yet.</p>}
        </div>
      </section>

      <section className="card p-6 animate-rise [animation-delay:140ms]">
        <h2 className="font-heading text-2xl font-bold mb-4">Learning Materials</h2>
        <div className="space-y-3">
          {materials.length === 0 && <p className="text-slate-600">No materials shared yet.</p>}
          {materials.map((material) => (
            <div key={material.id} className="rounded-xl border border-slate-200 p-4">
              <p className="font-semibold">{material.title}</p>
              <p className="text-sm text-slate-600">
                {material.subject} | {material.courseTitle} | by {material.instructorName}
              </p>
              {material.description && <p className="text-sm text-slate-700 mt-2">{material.description}</p>}
              {material.youtubeUrl && (
                <a
                  href={material.youtubeUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-sm text-sky-700 underline mt-2 inline-block"
                >
                  Open YouTube
                </a>
              )}
              {material.pdfUrl && (
                <a
                  href={material.pdfUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-sm text-sky-700 underline mt-2 inline-block ml-4"
                >
                  Open PDF Notes
                </a>
              )}
            </div>
          ))}
        </div>
      </section>

      {activeQuiz && (
        <section className="card p-6 animate-rise [animation-delay:180ms]">
          <h3 className="font-heading text-xl font-bold mb-1">{activeQuiz.title}</h3>
          <p className="text-sm text-slate-600 mb-4">{activeQuiz.topic} | {activeQuiz.difficulty}</p>

          <div className="space-y-5">
            {activeQuiz.questions.map((q, index) => (
              <div key={q.id} className="rounded-xl border border-slate-200 p-4">
                <p className="font-medium mb-3">{index + 1}. {q.prompt}</p>
                {['A', 'B', 'C', 'D'].map((letter) => {
                  const optionText = q[`option${letter}`];
                  return (
                    <label key={letter} className="flex items-center gap-2 py-1 text-sm">
                      <input
                        type="radio"
                        name={`q-${q.id}`}
                        value={letter}
                        checked={answers[q.id] === letter}
                        onChange={() => setAnswers((prev) => ({ ...prev, [q.id]: letter }))}
                      />
                      <span>{letter}. {optionText}</span>
                    </label>
                  );
                })}
              </div>
            ))}
          </div>

          <button
            onClick={submitQuiz}
            className="mt-5 rounded-xl bg-accent text-white px-5 py-3 font-semibold"
          >
            Submit Quiz
          </button>

          {attemptResult && (
            <div className="mt-4 rounded-xl bg-sky-50 border border-sky-200 p-4">
              <p className="font-semibold">Score: {attemptResult.score}/{attemptResult.total} ({attemptResult.percentage.toFixed(1)}%)</p>
              <p className="text-sm text-slate-700">Recommended next difficulty: {attemptResult.nextRecommendedDifficulty}</p>
            </div>
          )}
        </section>
      )}

      <section className="card p-6 animate-rise [animation-delay:260ms]">
        <h2 className="font-heading text-2xl font-bold mb-4">Recent Performance</h2>
        <div className="space-y-2">
          {attempts.slice(0, 6).map((a) => (
            <div key={a.attemptId} className="rounded-xl border border-slate-200 p-3 flex justify-between text-sm">
              <span>{new Date(a.attemptedAt).toLocaleString()}</span>
              <span className="font-semibold">{a.score}/{a.total} ({a.percentage.toFixed(1)}%)</span>
            </div>
          ))}
          {attempts.length === 0 && <p className="text-slate-600">No attempts yet.</p>}
        </div>
      </section>
    </div>
  );
}

function MetricCard({ label, value }) {
  return (
    <div className="card p-4">
      <p className="text-sm text-slate-600">{label}</p>
      <p className="text-2xl font-bold font-heading">{value}</p>
    </div>
  );
}
