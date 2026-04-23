"use client";

import { useEffect, useState } from "react";
import { Circle, Clock, CheckCircle2, Zap, Calendar, Plus, UserPlus, ArrowRight } from "lucide-react";

// داتا وهمية مطورة لتدعم الـ Self-assign
const INITIAL_TASKS = [
  { id: "T-101", name: "Complete Use Case Diagram", assignee: "Qorashi", xp: 150, difficulty: "High", type: "Docs", est: "4h", status: "DONE" },
  { id: "T-102", name: "Setup Firebase Database", assignee: "Unassigned", xp: 300, difficulty: "High", type: "Backend", est: "8h", status: "TO_DO" },
  { id: "T-103", name: "Implement Auth Logic", assignee: "Mostafa", xp: 250, difficulty: "Medium", type: "Security", est: "6h", status: "IN_PROGRESS" },
  { id: "T-104", name: "Create Class Diagram", assignee: "Unassigned", xp: 150, difficulty: "Medium", type: "Design", est: "3h", status: "TO_DO" },
];

export default function SprintBoard() {
  const [tasks, setTasks] = useState(INITIAL_TASKS);
  const [user, setUser] = useState({ name: "", role: "" });

  useEffect(() => {
    setUser({
      name: localStorage.getItem("userName") || "Guest",
      role: localStorage.getItem("userRole") || "DEVELOPER",
    });
  }, []);

  // Use Case: Update Task Status
  const moveTask = (taskId: string, newStatus: string) => {
    setTasks(prev => prev.map(t => t.id === taskId ? { ...t, status: newStatus as any } : t));
  };

  // Use Case: Self-assign Task
  const selfAssign = (taskId: string) => {
    setTasks(prev => prev.map(t => t.id === taskId ? { ...t, assignee: user.name } : t));
  };

  const columns = [
    { id: "TO_DO", label: "To Do", icon: <Circle size={18} className="text-[#C9BBAF]" /> },
    { id: "IN_PROGRESS", label: "In Progress", icon: <Clock size={18} className="text-[#4A708B]" /> },
    { id: "DONE", label: "Done", icon: <CheckCircle2 size={18} className="text-green-600" /> },
  ];

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex justify-between items-end mb-10">
        <div>
          <h1 className="text-3xl font-black text-[#1B3C53] flex items-center gap-3 uppercase tracking-tighter"><Zap className="fill-[#1B3C53]" /> Sprint Board</h1>
          <p className="text-[#4A708B] text-[10px] font-black uppercase tracking-[0.2em] mt-2">Role: {user.role} | Mission: Task Execution</p>
        </div>
        
        {/* الميزة الإدارية تظهر فقط لغير المطورين */}
        {user.role !== "DEVELOPER" && (
          <button className="bg-[#1B3C53] text-[#FAF5F0] px-6 py-3 rounded-2xl font-bold flex items-center gap-2 shadow-lg text-sm uppercase tracking-widest active:scale-95 transition-all">
            <Plus size={18}/> Create Sprint
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {columns.map((col) => (
          <div key={col.id} className="bg-[#C9BBAF]/5 rounded-[2.5rem] border border-[#C9BBAF]/20 p-6 min-h-[600px]">
            <div className="flex items-center gap-2 mb-8 px-2 uppercase font-black text-[#1B3C53] text-[10px] tracking-[0.2em]">{col.icon} {col.label}</div>
            
            <div className="flex flex-col gap-5">
              {tasks.filter(t => t.status === col.id).map(task => (
                <div key={task.id} className="bg-white border border-[#C9BBAF]/20 p-6 rounded-3xl shadow-sm hover:shadow-md transition-all">
                  <div className="flex justify-between items-start mb-4">
                    <span className="text-[9px] font-mono text-[#C9BBAF]">#{task.id}</span>
                    <span className={`text-[8px] px-2 py-0.5 rounded-full font-black uppercase ${task.difficulty === 'High' ? 'bg-red-50 text-red-600' : 'bg-[#1B3C53]/5 text-[#1B3C53]'}`}>
                      {task.difficulty}
                    </span>
                  </div>
                  
                  <h3 className="text-sm font-bold text-[#1B3C53] mb-1">{task.name}</h3>
                  <p className="text-[10px] font-bold text-[#4A708B] uppercase mb-4 tracking-tighter">{task.type}</p>
                  
                  {/* Actions Area */}
                  <div className="space-y-3 mb-5">
                    {/* Self Assign Button */}
                    {task.assignee === "Unassigned" && user.role === "DEVELOPER" && (
                      <button 
                        onClick={() => selfAssign(task.id)}
                        className="w-full py-2 bg-blue-50 text-blue-600 text-[10px] font-black uppercase rounded-xl border border-blue-100 flex items-center justify-center gap-2 hover:bg-blue-600 hover:text-white transition-all"
                      >
                        <UserPlus size={14} /> Self-Assign Task
                      </button>
                    )}

                    {/* Status Update Buttons - Only for Developer if assigned to them */}
                    {user.role === "DEVELOPER" && task.assignee === user.name && (
                      <div className="flex gap-2">
                        {task.status === "TO_DO" && (
                          <button onClick={() => moveTask(task.id, "IN_PROGRESS")} className="flex-1 py-2 bg-amber-50 text-amber-600 text-[10px] font-black uppercase rounded-xl border border-amber-100 flex items-center justify-center gap-1 hover:bg-amber-500 hover:text-white transition-all">
                            Start <ArrowRight size={12}/>
                          </button>
                        )}
                        {task.status === "IN_PROGRESS" && (
                          <button onClick={() => moveTask(task.id, "DONE")} className="flex-1 py-2 bg-green-50 text-green-600 text-[10px] font-black uppercase rounded-xl border border-green-100 flex items-center justify-center gap-1 hover:bg-green-600 hover:text-white transition-all">
                            Complete <CheckCircle2 size={12}/>
                          </button>
                        )}
                      </div>
                    )}
                  </div>

                  <div className="pt-4 border-t border-[#FAF5F0] flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-6 h-6 bg-[#1B3C53] rounded-full flex items-center justify-center text-[8px] text-white font-bold">
                        {task.assignee === "Unassigned" ? "?" : task.assignee[0]}
                      </div>
                      <span className="text-[10px] font-bold text-[#4A708B]">{task.assignee}</span>
                    </div>
                    <div className="text-right">
                      <div className="text-[#1B3C53] text-[10px] font-black">+{task.xp} XP</div>
                      <div className="text-[8px] text-[#C9BBAF] font-mono flex items-center gap-1 justify-end mt-1"><Clock size={10}/> Est: {task.est}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}