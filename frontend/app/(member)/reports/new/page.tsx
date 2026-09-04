import ReportForm from "@/components/report/ReportForm";

// Server Component wrapper — ReportForm itself is the client component that owns all the
// interactivity. Nothing here needs to fetch anything for the create case, since the form
// starts empty.
export default function NewReportPage() {
  return <ReportForm mode="create" />;
}
