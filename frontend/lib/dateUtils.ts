// Formats a Date as "YYYY-MM-DD" using its *local* fields, not toISOString() — toISOString()
// converts to UTC first, which can silently roll the date back/forward a day depending on the
// viewer's timezone offset. Native <input type="date"> only accepts this exact plain format.
function toDateInputValue(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

// Returns the current reporting week's Monday and Saturday as "YYYY-MM-DD" strings, used to
// prefill a new report's week start/end so members don't have to pick dates for the common
// case. Sunday counts as belonging to the week that started the previous Monday (ISO-style),
// not the week ahead.
export function getDefaultReportWeek(): { weekStartDate: string; weekEndDate: string } {
  const today = new Date();
  const dayOfWeek = today.getDay(); // 0 = Sunday, 1 = Monday, ... 6 = Saturday
  const diffToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;

  const monday = new Date(today);
  monday.setDate(today.getDate() + diffToMonday);

  const saturday = new Date(monday);
  saturday.setDate(monday.getDate() + 5);

  return {
    weekStartDate: toDateInputValue(monday),
    weekEndDate: toDateInputValue(saturday),
  };
}
