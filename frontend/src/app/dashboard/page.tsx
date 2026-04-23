"use client";
import { PieChart, BarChart3, TrendingUp, Users, CheckCircle } from "lucide-react";

export default function Dashboard() {
  return (
    <div className="max-w-6xl mx-auto">
      <div className="mb-10">
        <h1 className="text-3xl font-black text-[#1B3C53] uppercase tracking-tighter">Enterprise Analytics</h1>
        <p className="text-[#4A708B] text-sm mt-1">Performance metrics and project velocity tracking</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-10">
        <StatCard icon={<CheckCircle className="text-green-500" />} label="Sprint Completion" value="84%" sub="Target: 90%" />
        <StatCard icon={<Users className="text-[#1B3C53]" />} label="Team Velocity" value="42" sub="SP per Sprint" />
        <StatCard icon={<BarChart3 className="text-blue-500" />} label="Open Tasks" value="12" sub="In Progress" />
        <StatCard icon={<TrendingUp className="text-orange-500" />} label="Quality Score" value="9.8" sub="Customer Feedback" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white border border-[#C9BBAF]/30 rounded-[2.5rem] p-10 h-80 flex items-center justify-center shadow-sm">
           <p className="text-[#C9BBAF] font-black uppercase tracking-[0.3em] text-xs">Velocity Chart Visualization</p>
        </div>
        <div className="bg-white border border-[#C9BBAF]/30 rounded-[2.5rem] p-10 h-80 flex items-center justify-center shadow-sm">
           <p className="text-[#C9BBAF] font-black uppercase tracking-[0.3em] text-xs">Burn-down Progress</p>
        </div>
      </div>
    </div>
  );
}

function StatCard({ icon, label, value, sub }: any) {
  return (
    <div className="bg-white border border-[#C9BBAF]/30 p-8 rounded-[2.5rem] shadow-sm">
      <div className="flex items-center gap-3 mb-4">{icon} <span className="text-[10px] font-black uppercase text-[#4A708B] tracking-widest">{label}</span></div>
      <p className="text-4xl font-black text-[#1B3C53] mb-1 tracking-tighter">{value}</p>
      <p className="text-[10px] text-[#C9BBAF] font-bold uppercase tracking-tight">{sub}</p>
    </div>
  );
}