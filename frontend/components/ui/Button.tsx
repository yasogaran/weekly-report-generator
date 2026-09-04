import { ReactNode, forwardRef } from "react";
import Tooltip from "./Tooltip";

interface ButtonSharedProps {
  onClick?: () => void;
  disabled?: boolean;
  type?: "button" | "submit" | "reset";
  className?: string;
}

// Ghost is the icon-only variant. Its branch requires `ariaLabel` + `icon` instead of
// `children` — there is no way to construct a ghost Button that type-checks without an
// accessible label, and the component always wraps itself in a Tooltip using that label.
// That's what makes the design doc's "every icon-only button needs a Tooltip + aria-label"
// rule structural rather than a convention someone has to remember.
export type ButtonProps =
  | (ButtonSharedProps & {
      variant?: "primary" | "secondary" | "destructive";
      children: ReactNode;
    })
  | (ButtonSharedProps & {
      variant: "ghost";
      ariaLabel: string;
      icon: ReactNode;
    });

const baseClasses =
  "inline-flex items-center justify-center gap-2 rounded text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-accent focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50";

const labeledVariantClasses = {
  primary: "bg-accent text-paper hover:bg-accent/90",
  secondary: "border border-line bg-surface text-ink hover:bg-paper",
  destructive: "bg-feedback-error text-paper hover:bg-feedback-error/90",
} as const;

const ghostClasses = "bg-transparent text-muted hover:bg-line/40 hover:text-ink";

// Renders one of Button's four variants: primary (accent fill), secondary (outlined),
// destructive (delete/deactivate actions), or ghost (icon-only toolbar actions).
// forwardRef so callers like ConfirmDialog can focus a specific button programmatically
// (e.g. moving initial focus to Cancel when a confirmation dialog opens).
const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(props, ref) {
  const { onClick, disabled, type = "button", className = "" } = props;

  if (props.variant === "ghost") {
    return (
      <Tooltip content={props.ariaLabel}>
        <button
          ref={ref}
          type={type}
          aria-label={props.ariaLabel}
          disabled={disabled}
          onClick={onClick}
          className={`${baseClasses} h-9 w-9 p-0 ${ghostClasses} ${className}`}
        >
          {props.icon}
        </button>
      </Tooltip>
    );
  }

  const variant = props.variant ?? "primary";
  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`${baseClasses} px-4 py-2 ${labeledVariantClasses[variant]} ${className}`}
    >
      {props.children}
    </button>
  );
});

export default Button;
