"use client";

import { ShieldCheck, Calendar, Activity } from "lucide-react";

const LOGS = [
  { id: "L1", date: "2026-04-10", actor: "Qorashi", action: "LOGIN", target: "System", status: "Success" },
  { id: "L2", date: "2026-04-10", actor: "Ismail", action: "CREATE", target: "Backlog Item", status: "Success" },
  { id: "L3", date: "2026-04-10", actor: "Muhanad", action: "DELETE", target: "Old Task", status: "Failed" },
];

export default function AuditLogs() {
  return (
    <div className="max-w-6xl mx-auto">
      <h1 className="text-3xl font-black text-[#1B3C53] mb-10 flex items-center gap-3 uppercase tracking-tighter"><ShieldCheck size={32} /> Audit Logs</h1>
      <div className="bg-white border border-[#C9BBAF]/30 rounded-[2.5rem] overflow-hidden">
        <table className="w-full text-left"><thead><tr className="bg-[#C9BBAF]/10 text-[#4A708B] text-[10px] font-black uppercase tracking-widest"><th className="p-6">Date</th><th className="p-6">Actor</th><th className="p-6">Action</th><th className="p-6">Entity</th><th className="p-6 text-center">Status</th></tr></thead><tbody>{LOGS.map((log) => (
              <tr key={log.id} className="border-b border-[#C9BBAF]/10">
                <td className="p-6 text-xs font-mono text-[#4A708B] flex items-center gap-2"><Calendar size={14}/>{log.date}</td>
                <td className="p-6 font-bold text-[#1B3C53]">{log.actor}</td>
                <td className="p-6"><span className="bg-[#1B3C53]/5 text-[#1B3C53] px-2 py-1 rounded text-[10px] font-bold">{log.action}</span></td>
                <td className="p-6 text-xs text-[#4A708B] flex items-center gap-2"><Activity size={14}/>{log.target}</td>
                <td className="p-6 text-center"><span className={`text-[10px] font-black px-3 py-1 rounded-full ${log.status === 'Success' ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'}`}>{log.status}</span></td>
              </tr>
            ))}</tbody></table>
      </div>
    </div>
  );
}