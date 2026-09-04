import { ReactNode } from "react";

export interface DataTableColumn<T> {
  key: string;
  header: string;
  render: (row: T) => ReactNode;
  /** Numeric columns (hours, %, counts) render in IBM Plex Mono for column alignment. */
  numeric?: boolean;
}

export interface DataTablePagination {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  rows: T[];
  rowKey: (row: T) => string | number;
  pagination?: DataTablePagination;
}

// Generic table wrapper for TaskTable/history/project/user lists. Deliberately simple —
// columns + rows + an optional controlled pagination footer, no built-in sorting/filtering
// since the design doc doesn't call for it at this layer (FilterBar owns filtering).
// Fully controlled (page state and change handling live in the parent), so no state of its
// own is needed here.
export default function DataTable<T>({ columns, rows, rowKey, pagination }: DataTableProps<T>) {
  return (
    <div className="rounded-none border border-line">
      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-line bg-paper">
            {columns.map((column) => (
              <th key={column.key} className="px-3 py-2 text-left font-medium text-muted">
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={rowKey(row)} className="border-b border-line last:border-0">
              {columns.map((column) => (
                <td
                  key={column.key}
                  className={`px-3 py-2 text-ink ${column.numeric ? "font-mono" : ""}`}
                >
                  {column.render(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      {pagination && (
        <div className="flex items-center justify-between border-t border-line px-3 py-2 text-sm text-muted">
          <button
            type="button"
            disabled={pagination.page <= 1}
            onClick={() => pagination.onPageChange(pagination.page - 1)}
            className="rounded px-2 py-1 hover:text-ink disabled:cursor-not-allowed disabled:opacity-40"
          >
            Previous
          </button>
          <span>
            Page {pagination.page} of {pagination.totalPages}
          </span>
          <button
            type="button"
            disabled={pagination.page >= pagination.totalPages}
            onClick={() => pagination.onPageChange(pagination.page + 1)}
            className="rounded px-2 py-1 hover:text-ink disabled:cursor-not-allowed disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
