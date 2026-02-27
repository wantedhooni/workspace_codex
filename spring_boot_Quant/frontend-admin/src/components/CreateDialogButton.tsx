import { Button as MuiButton, Dialog, DialogContent, DialogTitle } from "@mui/material";
import { type ReactNode, useMemo, useState } from "react";
import { CreateBase, SaveButton, SimpleForm, Toolbar, useNotify, useRefresh } from "react-admin";

type FormValidate = (values: Record<string, unknown>) => Record<string, unknown> | undefined;
type FormTransform = (values: Record<string, unknown>) => Record<string, unknown>;

type CreateDialogButtonProps = {
  resource: string;
  title: string;
  label?: string;
  disabled?: boolean;
  maxWidth?: "xs" | "sm" | "md" | "lg" | "xl";
  defaultValues?: Record<string, unknown>;
  successMessage?: string;
  formValidate?: FormValidate;
  transform?: FormTransform;
  children: ReactNode;
};

type DialogToolbarProps = {
  onClose: () => void;
};

function DialogToolbar({ onClose }: DialogToolbarProps) {
  return (
    <Toolbar sx={{ display: "flex", justifyContent: "space-between", gap: 1 }}>
      <MuiButton onClick={onClose} size="small">
        닫기
      </MuiButton>
      <SaveButton label="저장" />
    </Toolbar>
  );
}

export function CreateDialogButton({
  resource,
  title,
  label = "생성",
  disabled,
  maxWidth = "md",
  defaultValues,
  successMessage,
  formValidate,
  transform,
  children
}: CreateDialogButtonProps) {
  const [open, setOpen] = useState(false);
  const [formKey, setFormKey] = useState(1);
  const notify = useNotify();
  const refresh = useRefresh();

  const okMessage = useMemo(() => successMessage ?? `${title}이(가) 저장되었습니다.`, [successMessage, title]);

  const close = () => {
    setOpen(false);
    setFormKey((prev) => prev + 1);
  };

  return (
    <>
      <MuiButton variant="contained" size="small" onClick={() => setOpen(true)} disabled={disabled}>
        {label}
      </MuiButton>

      <Dialog open={open} onClose={close} fullWidth maxWidth={maxWidth}>
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          <CreateBase
            key={formKey}
            resource={resource}
            redirect={false}
            transform={transform}
            mutationOptions={{
              onSuccess: () => {
                notify(okMessage, { type: "info" });
                refresh();
                close();
              },
              onError: (error) => {
                notify(error instanceof Error ? error.message : `${title} 저장 실패`, { type: "warning" });
              }
            }}
          >
            <SimpleForm
              toolbar={<DialogToolbar onClose={close} />}
              defaultValues={defaultValues}
              validate={formValidate}
              sx={{ pt: 1 }}
            >
              {children}
            </SimpleForm>
          </CreateBase>
        </DialogContent>
      </Dialog>
    </>
  );
}
