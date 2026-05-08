"use client";
import { useEffect, useState } from "react";
import { usePathname, useRouter, useParams } from "next/navigation";
import Image from "next/image";
import { 
  FolderKanban, Trophy, Users, Archive, ShieldAlert, 
  LogOut, Bell, LayoutDashboard, ListTodo 
} from "lucide-react";
import Link from "next/link";
import "./globals.css";

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const params = useParams();
  const [mounted, setMounted] = useState(false);
  const [user, setUser] = useState({ name: "", role: "" });

  const projectId = params?.projectId as string;

  useEffect(() => {
    setMounted(true);
    setUser({
      name: localStorage.getItem("userName") || "Guest",
      role: localStorage.getItem("userRole") || "DEVELOPER",
    });
  }, [pathname]);

  if (pathname === "/" || pathname === "/projects") return <html lang="en"><body>{children}</body></html>;
  if (!mounted) return <html lang="en"><body className="bg-[#FAF5F0]" /></html>;

  // دالة ذكية لتوليد الروابط
  const getLink = (path: string) => `/projects/${projectId}${path}`;

  return (
    <html lang="en">
      <body className="bg-[#FAF5F0] text-[#1B3C53] flex min-h-screen font-sans antialiased">
        <aside className="w-72 bg-[#C9BBAF]/10 border-r border-[#C9BBAF]/30 p-8 flex flex-col gap-10 shrink-0">
          {/* Logo Section */}
          <div className="flex items-center gap-4">
            <Image src="/logo.jpeg" alt="Logo" width={48} height={48} className="object-contain" />
            <div className="flex flex-col uppercase font-black tracking-tighter">
              <span className="text-2xl leading-none">Sprintify</span>
              <span className="text-[9px] text-[#4A708B] tracking-widest mt-1">for scrum</span>
            </div>
          </div>

          {/* Active Project Card */}
          {projectId && (
            <div className="p-4 bg-[#1B3C53] text-white rounded-[2rem] shadow-xl">
              <p className="text-[8px] font-black opacity-50 uppercase tracking-widest mb-1">Active Context</p>
              <p className="text-xs font-bold truncate uppercase">{projectId}</p>
              <Link href="/projects" className="inline-block mt-3 text-[9px] font-black underline opacity-80 hover:opacity-100">SWITCH PROJECT</Link>
            </div>
          )}

          <nav className="flex flex-col gap-2 flex-1">
            {/* صفحات مشتركة */}
            <SidebarItem href={getLink("/sprint-board")} icon={<ListTodo size={18} />} label="Sprint Board" />
            <SidebarItem href={getLink("/leaderboard")} icon={<Trophy size={18} />} label="Leaderboard" />
            <SidebarItem href="/projects" icon={<FolderKanban size={18} />} label="My Projects" />

            {/* صلاحيات الإدارة */}
            {(user.role === "ADMIN" || user.role === "PRODUCT_OWNER") && (
              <>
                <div className="h-px bg-[#C9BBAF]/30 my-4 mx-2" />
                <p className="text-[10px] text-[#4A708B] font-black px-3 mb-2 uppercase tracking-widest">Management</p>
                <SidebarItem href={getLink("/dashboard")} icon={<LayoutDashboard size={18} />} label="Project Analytics" />
                <SidebarItem href={getLink("/backlog")} icon={<Archive size={18} />} label="Backlog" />
                <SidebarItem href={getLink("/team")} icon={<Users size={18} />} label="Team" />
              </>
            )}

            {/* الأدمن فقط */}
            {user.role === "ADMIN" && (
              <SidebarItem href="/admin/audit-logs" icon={<ShieldAlert size={18} />} label="System Audits" />
            )}
          </nav>

          <button onClick={() => {localStorage.clear(); router.push("/");}} className="flex items-center gap-3 p-4 rounded-2xl text-red-600 font-black text-xs hover:bg-red-50 transition-all">
            <LogOut size={16} /> SIGN OUT
          </button>
        </aside>

        <main className="flex-1 p-10 overflow-y-auto">{children}</main>
      </body>
    </html>
  );
}

function SidebarItem({ href, icon, label }: any) {
  const pathname = usePathname();
  const isActive = pathname === href;
  return (
    <Link href={href} className={`flex items-center gap-3 p-4 rounded-2xl transition-all text-xs font-black uppercase tracking-tight ${isActive ? 'bg-white text-[#1B3C53] shadow-md border border-[#C9BBAF]/20' : 'text-[#4A708B] hover:bg-[#C9BBAF]/10 hover:text-[#1B3C53]'}`}>
      {icon} {label}
    </Link>
  );
}