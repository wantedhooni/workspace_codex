import { cn } from "@/lib/utils";
import { type HTMLAttributes } from "react";

const palette: Record<string, string> = {
  BUY: "bg-success/15 text-success",
  WATCH: "bg-accent/15 text-accent",
  AVOID: "bg-danger/15 text-danger",
  GROWTH: "bg-primary/10 text-primary",
  ENERGY: "bg-accent/15 text-accent",
  RISK_OFF: "bg-danger/15 text-danger",
  NEUTRAL: "bg-foreground/10 text-foreground",
  POSITIVE: "bg-success/15 text-success",
  NEGATIVE: "bg-danger/15 text-danger",
  LOW: "bg-foreground/10 text-foreground",
  MEDIUM: "bg-accent/15 text-accent",
  HIGH: "bg-danger/15 text-danger",
};

export function Badge({ className, children, ...props }: HTMLAttributes<HTMLSpanElement>) {
  const key = typeof children === "string" ? children : "";
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.12em]",
        palette[key] ?? "bg-primary/10 text-primary",
        className,
      )}
      {...props}
    >
      {children}
    </span>
  );
}
