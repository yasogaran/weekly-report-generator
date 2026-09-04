import type { Metadata } from "next";
import { IBM_Plex_Sans, IBM_Plex_Mono } from "next/font/google";
import { ToastProvider } from "@/components/ui/Toast";
import { ConfirmProvider } from "@/components/ui/ConfirmDialog";
import { AuthProvider } from "@/lib/authContext";
import "./globals.css";

// Default UI font, applied globally via the CSS variable below.
const plexSans = IBM_Plex_Sans({
  variable: "--font-plex-sans",
  subsets: ["latin"],
  weight: ["400", "500", "600"],
});

// Registered as a variable only — NOT applied to the body. Reachable only via
// the `font-mono` utility class, for tabular numeric table cells (see
// docs/frontend-design-system.md section 1: never used for labels/buttons).
const plexMono = IBM_Plex_Mono({
  variable: "--font-plex-mono",
  subsets: ["latin"],
  weight: ["400"],
});

export const metadata: Metadata = {
  title: "Weekly Report Generator",
  description: "Weekly report submission and team dashboard tool",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className={`${plexSans.variable} ${plexMono.variable} h-full`}>
      <body className="min-h-full flex flex-col font-sans antialiased">
        <ToastProvider>
          <AuthProvider>
            <ConfirmProvider>{children}</ConfirmProvider>
          </AuthProvider>
        </ToastProvider>
      </body>
    </html>
  );
}
