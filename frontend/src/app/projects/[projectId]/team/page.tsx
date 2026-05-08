"use client";
import { use } from "react"; // إضافة use
import { Mail, Shield, Database, Lock, Code, Palette, UserPlus, Edit2, Trash2, Users } from "lucide-react";

const MEMBERS = [
  { id: 1, name: "Qorashi", role: "ADMIN", specialty: "Architecture", email: "qorashi@agile.com", icon: <Shield size={20}/> },
  { id: 2, name: "Ismail", role: "PRODUCT_OWNER", specialty: "Backend & Firebase", email: "ismail@agile.com", icon: <Database size={20}/> },
  { id: 3, name: "Mostafa", role: "SCRUM_MASTER", specialty: "Auth Logic", email: "mostafa@agile.com", icon: <Lock size={20}/> },
  { id: 4, name: "Muhanad", role: "DEVELOPER", specialty: "UML & Documentation", email: "muhanad@agile.com", icon: <Code size={20}/> },
];

export default function TeamPage({ params }: { params: Promise<{ projectId: string }> }) {
  // فك تغليف الـ params
  const { projectId } = use(params);

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-12">
        <div>
          <h1 className="text-4xl font-black text-[#1B3C53] mb-2 uppercase tracking-tighter flex items-center gap-4">
             <Users size={40}/> {projectId} Team
          </h1>
          <p className="text-[#4A708B] text-[10px] font-black uppercase tracking-[0.2em] ml-1">Assigned Talent to Project {projectId}</p>
        </div>
        
        <button className="bg-[#1B3C53] text-[#FAF5F0] px-6 py-3.5 rounded-2xl font-bold flex items-center gap-3 shadow-xl hover:opacity-90 transition-all text-sm uppercase">
          <UserPlus size={18} /> Add Member
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {MEMBERS.map((m) => (
          <div key={m.id} className="bg-white border border-[#C9BBAF]/30 rounded-[3rem] p-8 hover:shadow-2xl transition-all group relative">
            <div className="flex items-center gap-5 mb-8">
              <div className="w-16 h-16 bg-[#FAF5F0] rounded-[1.5rem] flex items-center justify-center text-[#1B3C53] border border-[#C9BBAF]/20">
                {m.icon}
              </div>
              <div>
                <h3 className="text-xl font-black text-[#1B3C53] tracking-tight">{m.name}</h3>
                <p className="text-[10px] font-black text-[#4A708B] uppercase tracking-[0.2em] mt-1">{m.role}</p>
              </div>
            </div>

            <div className="space-y-4 mb-8">
              <div>
                <p className="text-[9px] text-[#C9BBAF] font-black uppercase mb-2 tracking-widest">Specialty</p>
                <div className="bg-[#FAF5F0] px-4 py-2 rounded-xl border border-[#C9BBAF]/10 text-xs font-bold text-[#1B3C53] inline-block">{m.specialty}</div>
              </div>
              <div className="flex items-center gap-2 text-[#4A708B] text-xs font-medium bg-slate-50 p-3 rounded-xl border border-slate-100/50">
                <Mail size={14} className="text-[#C9BBAF]" /> {m.email}
              </div>
            </div>

            <div className="pt-6 border-t border-[#FAF5F0] flex items-center justify-between">
              <div className="flex items-center gap-3">
                <button className="p-2.5 bg-[#FAF5F0] text-[#4A708B] rounded-xl hover:bg-[#1B3C53] hover:text-white transition-all"><Edit2 size={16} /></button>
                <button className="p-2.5 bg-red-50 text-red-400 rounded-xl hover:bg-red-500 hover:text-white transition-all"><Trash2 size={16} /></button>
              </div>
              <span className="text-[9px] font-black text-[#C9BBAF] uppercase tracking-widest">UID: 00{m.id}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}