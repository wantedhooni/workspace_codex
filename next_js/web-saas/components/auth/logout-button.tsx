import { logoutAction } from "@/app/actions/logout";
import { Button } from "@/components/ui/button";

export function LogoutButton() {
  return (
    <form action={logoutAction}>
      <Button type="submit" variant="outline">
        로그아웃
      </Button>
    </form>
  );
}