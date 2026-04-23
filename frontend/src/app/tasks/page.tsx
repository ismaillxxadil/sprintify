import { CheckCircle2, Circle, Clock } from "lucide-react";

// بيانات وهمية (Mock Data) - كأنها جاية من الباك إيند
const MOCK_TASKS = [
  { id: 1, title: "تصميم واجهة الدخول", status: "completed", points: 100, difficulty: "Easy" },
  { id: 2, title: "ربط قاعدة البيانات بالـ API", status: "in-progress", points: 250, difficulty: "Hard" },
  { id: 3, title: "عمل الـ Class Diagram للمشروع", status: "todo", points: 150, difficulty: "Medium" },
];

export default function TasksPage() {
  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold mb-8 flex items-center gap-3">
        <Clock className="text-blue-500" />
        قائمة المهام (Sprint Backlog)
      </h1>

      <div className="grid gap-4">
        {MOCK_TASKS.map((task) => (
          <div key={task.id} className="bg-slate-900 border border-slate-800 p-5 rounded-2xl flex items-center justify-between hover:border-blue-500/50 transition-colors">
            <div className="flex items-center gap-4">
              {task.status === 'completed' ? <CheckCircle2 className="text-green-500" /> : <Circle className="text-slate-600" />}
              <div>
                <h3 className="font-semibold text-lg">{task.title}</h3>
                <span className="text-sm text-slate-500">الصعوبة: {task.difficulty}</span>
              </div>
            </div>
            
            <div className="text-right">
              <span className="bg-blue-900/30 text-blue-400 px-3 py-1 rounded-full text-sm font-bold">
                +{task.points} XP
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}