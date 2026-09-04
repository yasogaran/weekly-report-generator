"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import Button from "@/components/ui/Button";
import Input from "@/components/ui/Input";
import { useToast } from "@/components/ui/Toast";
import { ApiError } from "@/lib/apiClient";
import { useAuth } from "@/lib/authContext";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function RegisterPage() {
  const { register } = useAuth();
  const toast = useToast();
  const router = useRouter();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    // Required-field + email-format checks only, same as login — UX only. The backend's
    // @Valid on RegisterRequest is the real gate, including any password rules it enforces.
    if (!name || !email || !password) {
      toast.error("Fill in all fields.");
      return;
    }
    if (!EMAIL_PATTERN.test(email)) {
      toast.error("Enter a valid email address.");
      return;
    }

    setSubmitting(true);
    try {
      // Self-registration is always created as TEAM_MEMBER server-side (api-doc.md — role
      // is never accepted from the client), so this redirect target will always resolve to
      // /history in practice. Branching on the returned role anyway keeps this correct if
      // that ever changes, instead of hardcoding an assumption about the response.
      const user = await register(name, email, password);
      toast.success("Account created.");
      router.replace(user.role === "MANAGER" ? "/dashboard" : "/history");
    } catch (err) {
      // Unlike login's 401, there's no deliberately-generic case to preserve here — a 400
      // from @Valid (e.g. "email must be a well-formed email address", or a duplicate email)
      // is meant to be shown to the user, so surface the backend's own message directly.
      const message =
        err instanceof ApiError
          ? err.message
          : "Couldn't create your account — check your connection and try again.";
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
        <h1 className="text-xl font-semibold text-ink">Create an account</h1>
        <div className="mt-6 flex flex-col gap-4">
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Name
            <Input
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoComplete="name"
            />
          </label>
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
              autoComplete="new-password"
            />
          </label>
        </div>
        <Button type="submit" disabled={submitting} className="mt-6 w-full">
          {submitting ? "Creating account…" : "Create account"}
        </Button>
        <p className="mt-4 text-sm text-muted">
          Already have an account?{" "}
          <Link href="/login" className="text-accent hover:underline">
            Sign in
          </Link>
        </p>
      </form>
    </main>
  );
}
