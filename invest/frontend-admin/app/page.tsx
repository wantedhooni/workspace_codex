import dynamic from "next/dynamic";

const AdminApp = dynamic(() => import("../src/app/AdminApp"), {
  ssr: false,
  loading: () => <p>Loading admin...</p>
});

export default function HomePage() {
  return <AdminApp />;
}
