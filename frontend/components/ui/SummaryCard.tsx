interface SummaryCardProps {
  label: string;
  value: string;
}

// The one place `surface` background is used for elevation (frontend-design-system.md §1/§2
// — dashboard metrics only). Deliberately no box-shadow here: the design doc's shadow rule
// is "no box-shadow except Modal/Toast," so this card's elevation comes from the
// surface-vs-paper background contrast plus a border, not a shadow.
export default function SummaryCard({ label, value }: SummaryCardProps) {
  return (
    <div className="flex flex-col gap-1 rounded border border-line bg-surface px-5 py-4">
      <p className="text-xs font-medium text-muted">{label}</p>
      <p className="text-2xl font-semibold text-ink">{value}</p>
    </div>
  );
}
