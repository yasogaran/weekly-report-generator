// Small "2 hours ago" formatter for the activity feed — deliberately not a new date library
// dependency (CLAUDE.md: don't silently add libraries), just Date arithmetic. Falls back to
// a plain date once something is more than a month old, since "32 days ago" stops being a
// useful unit at that point.
export function formatRelativeTime(isoTimestamp: string): string {
  const diffMs = Date.now() - new Date(isoTimestamp).getTime();
  const diffMinutes = Math.round(diffMs / (60 * 1000));
  const diffHours = Math.round(diffMinutes / 60);
  const diffDays = Math.round(diffHours / 24);

  if (diffMinutes < 1) return "just now";
  if (diffMinutes < 60) return `${diffMinutes} minute${diffMinutes === 1 ? "" : "s"} ago`;
  if (diffHours < 24) return `${diffHours} hour${diffHours === 1 ? "" : "s"} ago`;
  if (diffDays < 30) return `${diffDays} day${diffDays === 1 ? "" : "s"} ago`;
  return new Date(isoTimestamp).toLocaleDateString();
}
