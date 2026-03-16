import { cn } from "@/lib/utils";
import { type LabelHTMLAttributes } from "react";

export function Label({ className, ...props }: LabelHTMLAttributes<HTMLLabelElement>) {
  return <label className={cn("mb-2 block text-sm font-semibold text-foreground", className)} {...props} />;
}
