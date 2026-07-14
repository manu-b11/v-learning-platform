function StatCard({ title, value, icon: Icon, iconBg, iconColor }) {
  return (
    <div className="card flex gap-4">
      <div
        className={`flex h-14 w-14 items-center justify-center rounded-xl ${iconBg}`}
      >
        <Icon size={28} className={iconColor} />
      </div>

      <div>
        <p className="mt-1 text-text-secondary">{title}</p>
        <h2>{value}</h2>
      </div>
    </div>
  );
}

export default StatCard;
