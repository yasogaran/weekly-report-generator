import { InputHTMLAttributes, forwardRef } from "react";
import { fieldClasses } from "./shared";

export type DatePickerProps = Omit<InputHTMLAttributes<HTMLInputElement>, "type">;

// Thin styled wrapper around the native date input. Deliberately not pulling in a calendar
// library for this — the native picker covers what the report form (week start/end) needs.
const DatePicker = forwardRef<HTMLInputElement, DatePickerProps>(function DatePicker(
  { className = "", ...rest },
  ref
) {
  return <input ref={ref} type="date" className={`${fieldClasses} ${className}`} {...rest} />;
});

export default DatePicker;
