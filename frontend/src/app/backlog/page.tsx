"use client";

import { useEffect, useState } from "react";
import { Plus, Archive, CheckCircle2, Clock } from "lucide-react";

export default function BacklogPage() {
  const [mounted, setMounted] = useState(false);
  useEffect(() => { setMounted(true); }, []);
  if (!mounted) return null;

  const DATA = [
    { id: "PB-101", title: "User Authentication Module", priority: "HIGH", status: "READY", points: 8 },
    { id: "PB-102", title: "API Database Integration", priority: "MEDIUM", status: "IN_REFINEMENT", points: 5 },
    { id: "PB-103", title: "System Audit Logging", priority: "HIGH", status: "BACKLOG", points: 3 },
  ];

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-10">
        <div>
          <h1 className="text-3xl font-black text-[#1B3C53] flex items-center gap-3 uppercase tracking-tighter"><Archive size={32} /> Product Backlog</h1>
          <p className="text-[#4A708B] text-sm mt-1">Management of project requirements and stories</p>
        </div>
        <button className="bg-[#1B3C53] text-[#FAF5F0] px-6 py-3 rounded-2xl font-bold flex items-center gap-2 shadow-lg hover:opacity-90 transition-all"><Plus size={18} /> New Story</button>
      </div>
      <div className="bg-white border border-[#C9BBAF]/30 rounded-[2.5rem] overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse"><thead><tr className="bg-[#C9BBAF]/10 text-[#4A708B] text-[10px] font-black uppercase tracking-widest"><th className="p-6">ID</th><th className="p-6">User Story</th><th className="p-6">Priority</th><th className="p-6">Status</th><th className="p-6 text-center">Points</th></tr></thead><tbody>{DATA.map((item) => (
              <tr key={item.id} className="border-b border-[#C9BBAF]/10 hover:bg-[#C9BBAF]/5 transition-all">
                <td className="p-6 text-xs font-mono text-[#C9BBAF]">#{item.id}</td>
                <td className="p-6 font-bold text-[#1B3C53]">{item.title}</td>
                <td className="p-6"><span className={`px-2 py-1 rounded text-[9px] font-black ${item.priority === 'HIGH' ? 'bg-red-50 text-red-600' : 'bg-blue-50 text-blue-600'}`}>{item.priority}</span></td>
                <td className="p-6"><div className="flex items-center gap-2 text-xs text-[#4A708B]">{item.status === 'READY' ? <CheckCircle2 size={14} className="text-green-600" /> : <Clock size={14} className="text-orange-500" />}{item.status}</div></td>
                <td className="p-6 text-center"><span className="bg-[#1B3C53]/5 text-[#1B3C53] text-xs font-bold px-3 py-1 rounded-lg border border-[#1B3C53]/10">{item.points} SP</span></td>
              </tr>
            ))}</tbody></table>
      </div>
    </div>
  );
}