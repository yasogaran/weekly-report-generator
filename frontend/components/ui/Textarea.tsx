import { TextareaHTMLAttributes, forwardRef } from "react";
import { fieldClasses } from "./shared";

export type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement>;

// Multi-line text field (e.g. review comments). Same styling contract as Input, just taller
// and vertically resizable.
const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(
  { className = "", ...rest },
  ref
) {
  return (
    <textarea ref={ref} className={`${fieldClasses} min-h-24 resize-y ${className}`} {...rest} />
  );
});

export default Textarea;
