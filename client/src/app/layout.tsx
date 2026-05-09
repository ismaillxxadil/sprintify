import './globals.css'
import { Toaster } from 'react-hot-toast'

export const metadata = {
  title: 'Sprintify - Project Management',
  description: 'Agile Project Management Platform',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className="bg-background text-on-background">
        {children}
        <Toaster position="top-right" />
      </body>
    </html>
  )
}
