import { InputHTMLAttributes, forwardRef } from "react";
import { fieldClasses } from "./shared";

export type InputProps = InputHTMLAttributes<HTMLInputElement>;

// Single-line text field. A thin styled wrapper over the native <input> so it stays
// fully controllable (value/onChange, validation attrs, refs) by whatever form uses it.
const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { className = "", ...rest },
  ref
) {
  return <input ref={ref} className={`${fieldClasses} ${className}`} {...rest} />;
});

export default Input;
