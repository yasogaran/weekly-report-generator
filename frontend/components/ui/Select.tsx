import { SelectHTMLAttributes, forwardRef } from "react";
import { fieldClasses } from "./shared";

export interface SelectOption {
  value: string;
  label: string;
}

export interface SelectProps extends Omit<SelectHTMLAttributes<HTMLSelectElement>, "children"> {
  options: SelectOption[];
  placeholder?: string;
}

// Native <select> styled to match the other form primitives. Takes a typed `options` list
// instead of raw <option> children so call sites can't pass unstyled/malformed markup.
const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { options, placeholder, className = "", ...rest },
  ref
) {
  return (
    <select ref={ref} className={`${fieldClasses} ${className}`} {...rest}>
      {placeholder && (
        <option value="" disabled hidden>
          {placeholder}
        </option>
      )}
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
});

export default Select;
