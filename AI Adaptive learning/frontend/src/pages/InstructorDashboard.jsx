import { useEffect, useState } from 'react';
import api from '../services/api';

const quizFormInitial = {
  title: '',
  topic: '',
  difficulty: 'MEDIUM',
  questionCount: 5
};

const materialFormInitial = {
  courseId: '',
  subject: '',
  title: '',
  description: '',
  youtubeUrl: '',
  pdfUrl: ''
};

export default function InstructorDashboard() {
  const [metrics, setMetrics] = useState(null);
  const [quizzes, setQuizzes] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [courses, setCourses] = useState([]);
  const [form, setForm] = useState(quizFormInitial);
  const [materialForm, setMaterialForm] = useState(materialFormInitial);
  const [status, setStatus] = useState('');
  const [materialStatus, setMaterialStatus] = useState('');

  const fetchData = async () => {
    const [m, q, mats, cs] = await Promise.all([
      api.get('/instructor/analytics'),
      api.get('/instructor/quizzes'),
      api.get('/instructor/materials'),
      api.get('/courses')
    ]);

    setMetrics(m.data);
    setQuizzes(q.data);
    setMaterials(mats.data);
    setCourses(cs.data || []);
  };

  useEffect(() => {
    fetchData().catch(() => setStatus('Failed to load instructor data'));
  }, []);

  const generateQuiz = async (e) => {
    e.preventDefault();
    setStatus('Generating quiz with AI...');
    try {
      await api.post('/instructor/quizzes/generate', {
        ...form,
        questionCount: Number(form.questionCount)
      });
      setStatus('Quiz generated successfully.');
      setForm(quizFormInitial);
      fetchData();
    } catch (error) {
      const message = error?.response?.data?.message || 'Quiz generation failed. Please check backend logs.';
      setStatus(message);
    }
  };

  const publishMaterial = async (e) => {
    e.preventDefault();
    setMaterialStatus('Publishing material...');
    try {
      const payload = {
        ...materialForm,
        courseId: materialForm.courseId ? Number(materialForm.courseId) : null
      };
      await api.post('/instructor/materials', payload);
      setMaterialStatus('Material published successfully.');
      setMaterialForm(materialFormInitial);
      fetchData();
    } catch (error) {
      setMaterialStatus(error?.response?.data?.message || 'Failed to publish material.');
    }
  };

  const removeMaterial = async (materialId) => {
    try {
      await api.delete(`/instructor/materials/${materialId}`);
      setMaterialStatus('Material deleted.');
      fetchData();
    } catch (error) {
      setMaterialStatus(error?.response?.data?.message || 'Failed to delete material.');
    }
  };

  return (
    <div className="space-y-6">
      <section className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-rise">
        <MetricCard label="Students" value={metrics?.totalStudents ?? '-'} />
        <MetricCard label="Quizzes" value={metrics?.totalQuizzes ?? '-'} />
        <MetricCard label="Attempts" value={metrics?.totalAttempts ?? '-'} />
        <MetricCard
          label="Avg Score"
          value={metrics ? `${metrics.averageScorePercent.toFixed(1)}%` : '-'}
        />
      </section>

      <section className="card p-6 animate-rise [animation-delay:120ms]">
        <h2 className="font-heading text-2xl font-bold mb-4">Generate AI Quiz</h2>
        <form onSubmit={generateQuiz} className="grid md:grid-cols-2 gap-4">
          <input
            className="rounded-xl border border-slate-300 px-4 py-3"
            placeholder="Quiz title"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
          <input
            className="rounded-xl border border-slate-300 px-4 py-3"
            placeholder="Topic"
            value={form.topic}
            onChange={(e) => setForm({ ...form, topic: e.target.value })}
            required
          />
          <select
            className="rounded-xl border border-slate-300 px-4 py-3"
            value={form.difficulty}
            onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
          >
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
          <input
            type="number"
            min="3"
            max="20"
            className="rounded-xl border border-slate-300 px-4 py-3"
            value={form.questionCount}
            onChange={(e) => setForm({ ...form, questionCount: e.target.value })}
          />
          <button className="rounded-xl bg-accent text-white py-3 px-4 font-semibold md:col-span-2">
            Generate
          </button>
        </form>
        {status && <p className="text-sm mt-3 text-slate-700">{status}</p>}
      </section>

      <section className="card p-6 animate-rise [animation-delay:200ms]">
        <h2 className="font-heading text-2xl font-bold mb-4">Generated Quizzes</h2>
        <div className="space-y-3">
          {quizzes.length === 0 && <p className="text-slate-600">No quizzes yet.</p>}
          {quizzes.map((quiz) => (
            <div key={quiz.id} className="rounded-xl border border-slate-200 p-4">
              <p className="font-semibold">{quiz.title}</p>
              <p className="text-sm text-slate-600">{quiz.topic} | {quiz.difficulty} | {quiz.questions.length} questions</p>
            </div>
          ))}
        </div>
      </section>

      <section className="card p-6 animate-rise [animation-delay:280ms]">
        <h2 className="font-heading text-2xl font-bold mb-4">Course Subjects & Materials</h2>
        <form onSubmit={publishMaterial} className="grid md:grid-cols-2 gap-4">
          <select
            className="rounded-xl border border-slate-300 px-4 py-3"
            value={materialForm.courseId}
            onChange={(e) => setMaterialForm({ ...materialForm, courseId: e.target.value })}
          >
            <option value="">Auto-select my default course</option>
            {courses.map((course) => (
              <option key={course.id} value={course.id}>{course.title}</option>
            ))}
          </select>

          <input
            className="rounded-xl border border-slate-300 px-4 py-3"
            placeholder="Subject (e.g., DBMS Joins)"
            value={materialForm.subject}
            onChange={(e) => setMaterialForm({ ...materialForm, subject: e.target.value })}
            required
          />

          <input
            className="rounded-xl border border-slate-300 px-4 py-3"
            placeholder="Material title"
            value={materialForm.title}
            onChange={(e) => setMaterialForm({ ...materialForm, title: e.target.value })}
            required
          />

          <input
            className="rounded-xl border border-slate-300 px-4 py-3"
            placeholder="YouTube URL"
            value={materialForm.youtubeUrl}
            onChange={(e) => setMaterialForm({ ...materialForm, youtubeUrl: e.target.value })}
          />

          <input
            className="rounded-xl border border-slate-300 px-4 py-3"
            placeholder="PDF Notes URL"
            value={materialForm.pdfUrl}
            onChange={(e) => setMaterialForm({ ...materialForm, pdfUrl: e.target.value })}
          />

          <textarea
            className="rounded-xl border border-slate-300 px-4 py-3 md:col-span-2"
            placeholder="Description"
            rows={3}
            value={materialForm.description}
            onChange={(e) => setMaterialForm({ ...materialForm, description: e.target.value })}
          />

          <button className="rounded-xl bg-accent text-white py-3 px-4 font-semibold md:col-span-2">
            Publish Material
          </button>
        </form>
        {materialStatus && <p className="text-sm mt-3 text-slate-700">{materialStatus}</p>}

        <div className="mt-6 space-y-3">
          {materials.length === 0 && <p className="text-slate-600">No materials published yet.</p>}
          {materials.map((material) => (
            <div key={material.id} className="rounded-xl border border-slate-200 p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-semibold">{material.title}</p>
                  <p className="text-sm text-slate-600">{material.subject} | {material.courseTitle}</p>
                  {material.description && <p className="text-sm mt-2 text-slate-700">{material.description}</p>}
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
                <button
                  onClick={() => removeMaterial(material.id)}
                  className="rounded-lg border border-red-300 text-red-700 px-3 py-1 text-sm"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
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
