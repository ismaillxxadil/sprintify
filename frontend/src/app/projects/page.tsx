"use client";
import { useRouter } from "next/navigation";
import { use, useEffect, useState } from "react"; // 1. أضف use هنا
import { FolderKanban, Activity, ArrowRight } from "lucide-react";

const projects = [
  { id: "PROJ-992", name: "SPRINTIFY PLATFORM V1.0", status: "STABLE", description: "Scrum management platform" },
  { id: "PROJ-120", name: "SKILLBRIDGE", status: "ACTIVE", description: "Student services platform" },
  { id: "PROJ-333", name: "SE PROJECT", status: "PLANNING", description: "Software engineering project" },
];


export default function ProjectsPage({ params }: { params: Promise<{ projectId: string }> }) {
  const router = useRouter();
  const { projectId } = use(params);

  // هنا بنفتح المشروع في مسار عام، والـ Sidebar هو اللي هيتحكم في الأزرار
  const openProject = (projectId: string) => {
    router.push(`/projects/${projectId}/sprint-board`); // المطور يفتح بورد المهام فوراً
  };

  return (
    <div className="max-w-6xl mx-auto p-10">
      <div className="mb-10 text-center md:text-left">
        <h1 className="text-4xl font-black text-[#1B3C53] flex items-center justify-center md:justify-start gap-3 uppercase tracking-tighter">
          <FolderKanban size={40} /> Select Your Project
        </h1>
        <p className="text-[#4A708B] text-[10px] font-black uppercase tracking-[0.2em] mt-3">
          Welcome back, {localStorage.getItem("userName")}. Please select a workspace to continue.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {projects.map((project) => (
          <div
            key={project.id}
            onClick={() => openProject(project.id)}
            className="group bg-white border border-[#C9BBAF]/20 p-8 rounded-[3rem] shadow-sm hover:shadow-2xl hover:border-blue-500/30 transition-all cursor-pointer relative overflow-hidden"
          >
            <span className="text-[10px] font-mono text-[#C9BBAF] font-black tracking-widest">ID: {project.id}</span>
            <h3 className="text-xl font-black text-[#1B3C53] mt-6 mb-2 uppercase">{project.name}</h3>
            <p className="text-[11px] font-bold text-[#4A708B] uppercase mb-8">{project.description}</p>
            <div className="flex items-center justify-between pt-6 border-t border-[#FAF5F0]">
              <span className="text-[9px] px-3 py-1 rounded-full font-black uppercase bg-green-100 text-green-700">{project.status}</span>
              <div className="w-10 h-10 rounded-full bg-[#1B3C53] flex items-center justify-center text-white group-hover:scale-110 transition-transform">
                <ArrowRight size={18} />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}