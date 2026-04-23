"use client";

import { useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import Image from "next/image";
import { 
  ListTodo, Trophy, Users, Archive, ShieldAlert, 
  LogOut, Shield, Star, Settings, Bell, LayoutDashboard 
} from "lucide-react";
import Link from "next/link";
import "./globals.css";

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [user, setUser] = useState({ name: "", role: "", xp: "", level: "" });

  useEffect(() => {
    setMounted(true);
    setUser({
      name: localStorage.getItem("userName") || "Guest",
      role: localStorage.getItem("userRole") || "DEVELOPER",
      xp: localStorage.getItem("userXP") || "0",
      level: localStorage.getItem("userLevel") || "0",
    });
  }, [pathname]);

  if (pathname === "/") return <html lang="en"><body>{children}</body></html>;
  if (!mounted) return <html lang="en"><body className="bg-[#FAF5F0]" /></html>;

  return (
    <html lang="en">
      <body className="bg-[#FAF5F0] text-[#1B3C53] flex min-h-screen font-sans antialiased">
        <aside className="w-72 bg-[#C9BBAF]/10 border-r border-[#C9BBAF]/30 p-8 flex flex-col gap-10 shrink-0 shadow-sm">
          <div className="flex items-center gap-4">
            <div className="relative w-12 h-12 shrink-0">
              <Image src="/logo.jpeg" alt="Sprintify" fill className="object-contain" />
            </div>
            <div className="flex flex-col">
              <span className="text-2xl font-black tracking-tighter text-[#1B3C53] leading-none uppercase">Sprintify</span>
              <span className="text-[9px] font-bold text-[#4A708B] tracking-[0.2em] uppercase mt-1">for scrum</span>
            </div>
          </div>

          <div className="px-4 py-3 bg-[#1B3C53]/5 rounded-2xl border border-[#C9BBAF]/20">
            <p className="text-[8px] font-black text-[#C9BBAF] uppercase tracking-[0.2em] mb-1">Active Project</p>
            <p className="text-xs font-bold text-[#1B3C53] truncate uppercase">Sprintify Platform v1.0</p>
            <div className="mt-2 flex items-center justify-between">
              <span className="text-[9px] text-[#4A708B] font-mono">ID: PROJ-992</span>
              <span className="bg-green-100 text-green-700 text-[8px] px-1.5 py-0.5 rounded font-black">STABLE</span>
            </div>
          </div>

          <nav className="flex flex-col gap-1.5 flex-1">
            <SidebarItem href="/dashboard" icon={<LayoutDashboard size={18} />} label="Dashboard" />
            <SidebarItem href="/sprint-board" icon={<ListTodo size={18} />} label="Sprint Board" />
            <SidebarItem href="/leaderboard" icon={<Trophy size={18} />} label="Leaderboard" />
            {(user.role === "ADMIN" || user.role === "PRODUCT_OWNER" || user.role === "SCRUM_MASTER") && (
              <SidebarItem href="/backlog" icon={<Archive size={18} />} label="Product Backlog" />
            )}
            <div className="h-px bg-[#C9BBAF]/30 my-6 mx-2" />
            {user.role === "ADMIN" && (
              <>
                <p className="text-[10px] text-[#4A708B] font-black px-3 mb-3 uppercase tracking-[0.2em]">Management</p>
                <SidebarItem href="/admin/audit-logs" icon={<ShieldAlert size={18} />} label="Audit Logs" />
                <SidebarItem href="/team" icon={<Users size={18} />} label="Team Members" />
              </>
            )}
          </nav>
          
          <button onClick={() => {localStorage.clear(); router.push("/");}} className="w-full flex items-center gap-3 p-3.5 rounded-xl text-red-600 hover:bg-red-50 font-bold text-xs transition-all">
            <LogOut size={16} /> Sign Out
          </button>
        </aside>

        <main className="flex-1 p-10 overflow-y-auto">
          <header className="flex items-center justify-between mb-10 pb-6 border-b border-[#C9BBAF]/20">
            <div className="flex items-center gap-2 bg-white px-4 py-2 rounded-full border border-[#C9BBAF]/20 shadow-sm">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
              <span className="text-[10px] font-black uppercase text-[#4A708B]">System Connectivity: Secured</span>
            </div>
            
            <div className="flex items-center gap-6">
              <button className="relative p-2.5 bg-white border border-[#C9BBAF]/20 rounded-xl text-[#4A708B] hover:text-[#1B3C53] shadow-sm"><Bell size={20} /><span className="absolute top-2 right-2 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-white" /></button>
              <div className="flex items-center gap-4 pl-6 border-l border-[#C9BBAF]/30">
                <div className="text-right">
                  <p className="text-sm font-black text-[#1B3C53] uppercase leading-none">{user.name}</p>
                  <p className="text-[9px] text-[#4A708B] font-bold uppercase mt-1">Lv.{user.level} | {user.xp} XP</p>
                </div>
                <div className="w-11 h-11 bg-[#1B3C53] rounded-2xl flex items-center justify-center text-[#FAF5F0] font-bold shadow-lg border-2 border-white">{user.name[0]}</div>
              </div>
            </div>
          </header>
          {children}
        </main>
      </body>
    </html>
  );
}

function SidebarItem({ href, icon, label }: any) {
  const pathname = usePathname();
  const isActive = pathname === href;
  return (
    <Link href={href} className={`flex items-center gap-3 p-3.5 rounded-xl transition-all text-sm font-bold ${isActive ? 'bg-[#1B3C53] text-[#FAF5F0] shadow-xl' : 'text-[#4A708B] hover:bg-[#C9BBAF]/20 hover:text-[#1B3C53]'}`}>
      {icon} {label}
    </Link>
  );
}