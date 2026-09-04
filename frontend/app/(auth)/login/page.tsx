"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import Button from "@/components/ui/Button";
import Input from "@/components/ui/Input";
import { useToast } from "@/components/ui/Toast";
import { ApiError } from "@/lib/apiClient";
import { useAuth } from "@/lib/authContext";

// Loose enough to catch obvious typos ("a@b" missing a TLD) without rejecting valid
// addresses a stricter regex might miss — this is a UX nicety, not the real check.
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function LoginPage() {
  const { login } = useAuth();
  const toast = useToast();
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    // Required-field + email-format checks only — fast feedback for obvious mistakes.
    // This is UX only; the backend re-validates everything with @Valid on LoginRequest,
    // and that server-side check is the one that actually matters.
    if (!email || !password) {
      toast.error("Enter your email and password.");
      return;
    }
    if (!EMAIL_PATTERN.test(email)) {
      toast.error("Enter a valid email address.");
      return;
    }

    setSubmitting(true);
    try {
      const user = await login(email, password);
      toast.success("Signed in.");
      router.replace(user.role === "MANAGER" ? "/dashboard" : "/history");
    } catch (err) {
      // 401 covers both "wrong password" and "deactivated account" per the API doc, and
      // deliberately doesn't say which — a hardcoded generic message here matches that
      // rather than surfacing whatever detail the backend put in the response body.
      const message =
        err instanceof ApiError && err.status === 401
          ? "Invalid email or password."
          : "Couldn't sign in — check your connection and try again.";
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="flex flex-1 items-center justify-center p-4">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-sm rounded border border-line bg-surface p-6"
      >
        <h1 className="text-xl font-semibold text-ink">Sign in</h1>
        <div className="mt-6 flex flex-col gap-4">
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Email
            <Input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Password
            <Input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />
          </label>
        </div>
        <Button type="submit" disabled={submitting} className="mt-6 w-full">
          {submitting ? "Signing in…" : "Sign in"}
        </Button>
        <p className="mt-4 text-sm text-muted">
          Don&apos;t have an account?{" "}
          <Link href="/register" className="text-accent hover:underline">
            Register
          </Link>
        </p>
      </form>
    </main>
  );
}
