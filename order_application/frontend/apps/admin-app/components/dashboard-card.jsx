export function DashboardCard({ title, value, description }) {
  return (
    <article className="card">
      <p className="card-title">{title}</p>
      <strong className="card-value">{value}</strong>
      <p className="card-description">{description}</p>
    </article>
  );
}
