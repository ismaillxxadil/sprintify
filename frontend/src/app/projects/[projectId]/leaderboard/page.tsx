"use client";
import { use } from "react"; // إضافة use
import { Trophy, Medal } from "lucide-react";

const TEAM = [
  { name: "Qorashi", xp: 3500, level: 15 },
  { name: "Ismail", xp: 3100, level: 14 },
  { name: "Mostafa", xp: 2800, level: 12 },
  { name: "Muhanad", xp: 2200, level: 10 },
];

export default function Leaderboard({ params }: { params: Promise<{ projectId: string }> }) {
  // فك تغليف الـ params
  const { projectId } = use(params);

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-black text-[#1B3C53] mb-2 flex items-center gap-3 uppercase tracking-tighter">
        <Trophy size={32} /> Project Leaderboard
      </h1>
      <p className="text-[#4A708B] text-[10px] font-black uppercase tracking-[0.2em] mb-10 ml-10">Active Contributors in {projectId}</p>
      
      <div className="bg-white border border-[#C9BBAF]/30 rounded-[2.5rem] overflow-hidden shadow-sm">
        <table className="w-full text-left">
          <thead>
            <tr className="bg-[#C9BBAF]/10 text-[#4A708B] text-[10px] font-black uppercase tracking-widest">
              <th className="p-6">Rank</th>
              <th className="p-6">User</th>
              <th className="p-6">Level</th>
              <th className="p-6">Experience</th>
            </tr>
          </thead>
          <tbody>
            {TEAM.map((u, i) => (
              <tr key={u.name} className="border-b border-[#C9BBAF]/10 hover:bg-[#C9BBAF]/5 transition-all">
                <td className="p-6">{i < 3 ? <Medal className={i === 0 ? "text-yellow-500" : "text-slate-400"} /> : i+1}</td>
                <td className="p-6 font-bold text-[#1B3C53]">{u.name}</td>
                <td className="p-6 text-[#4A708B] font-mono text-xs">Lv. {u.level}</td>
                <td className="p-6 font-black text-[#1B3C53]">{u.xp} XP</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}