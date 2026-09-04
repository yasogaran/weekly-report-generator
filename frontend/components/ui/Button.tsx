import { ReactNode, forwardRef } from "react";
import Tooltip from "./Tooltip";

interface ButtonSharedProps {
  onClick?: () => void;
  disabled?: boolean;
  type?: "button" | "submit" | "reset";
  className?: string;
}

// Ghost was originally icon-only: `ariaLabel` + `icon`, no `children`, wrapped in a Tooltip
// using that label — which is what made the design doc's "every icon-only button needs a
// Tooltip + aria-label" rule structural rather than a convention to remember. Extended here
// to also allow `icon` + visible `children` (e.g. an Eye icon + the word "View" in a table's
// action column): that case doesn't need an aria-label — the visible text already is the
// accessible name — so the two ghost shapes are mutually exclusive on ariaLabel vs children,
// not just "children optional." Passing both, or neither, is a type error, not a runtime
// guess. Every existing icon-only ghost usage is unaffected — that shape didn't change.
export type ButtonProps =
  | (ButtonSharedProps & {
      variant?: "primary" | "secondary" | "destructive";
      children: ReactNode;
    })
  | (ButtonSharedProps & {
      variant: "ghost";
      icon: ReactNode;
    } & ({ ariaLabel: string; children?: never } | { ariaLabel?: never; children: ReactNode }));

const baseClasses =
  "inline-flex items-center justify-center gap-2 rounded text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-accent focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50";

const labeledVariantClasses = {
  primary: "bg-accent text-paper hover:bg-accent/90",
  secondary: "border border-line bg-surface text-ink hover:bg-paper",
  destructive: "bg-feedback-error text-paper hover:bg-feedback-error/90",
} as const;

const ghostClasses = "bg-transparent text-muted hover:bg-line/40 hover:text-ink";

// Renders one of Button's four variants: primary (accent fill), secondary (outlined),
// destructive (delete/deactivate actions), or ghost (toolbar actions — icon-only, or icon +
// a short visible label like a table row's "View" action).
// forwardRef so callers like ConfirmDialog can focus a specific button programmatically
// (e.g. moving initial focus to Cancel when a confirmation dialog opens).
const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(props, ref) {
  const { onClick, disabled, type = "button", className = "" } = props;

  if (props.variant === "ghost") {
    // Branches on `ariaLabel` presence, not `children` presence — `children` is typed
    // ReactNode, which itself legally includes `undefined`, so `children === undefined`
    // can't reliably discriminate the union (TypeScript won't narrow on it). `ariaLabel` is
    // `string` in one branch and `never` in the other, which *is* a reliable discriminant:
    // this is what lets `props.ariaLabel` narrow to `string` (not `string | undefined`)
    // below, matching Tooltip's `content: string` prop without a cast.
    if (props.ariaLabel !== undefined) {
      // Icon-only: always wrapped in a Tooltip using the required aria-label as its content
      // — the design doc's rule for unlabeled icon buttons, made structural rather than a
      // convention someone has to remember to apply.
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

    // Icon + visible label: no Tooltip needed (the label is already visible, wrapping it
    // too would just repeat the same words in a hover bubble) and no aria-label needed
    // (the visible text already is the accessible name).
    return (
      <button ref={ref} type={type} disabled={disabled} onClick={onClick} className={`${baseClasses} px-3 py-2 ${ghostClasses} ${className}`}>
        {props.icon}
        {props.children}
      </button>
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
