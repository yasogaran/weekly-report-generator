import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Disabled: this project keeps its own CLAUDE.md at the repo root; Next's
  // auto-generated frontend/CLAUDE.md + AGENTS.md would duplicate/conflict with it.
  agentRules: false,
};

export default nextConfig;
