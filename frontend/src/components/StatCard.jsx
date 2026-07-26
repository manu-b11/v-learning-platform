function StatCard({ title, value, icon: Icon, iconBg, iconColor }) {
  return (
    <div className="card flex items-center gap-4">
      <div
        className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl ${iconBg}`}
      >
        <Icon size={22} className={iconColor} strokeWidth={1.75} />
      </div>

      <div>
        <p className="text-sm text-text-secondary">{title}</p>
        <p className="font-heading text-2xl font-bold text-navy">{value}</p>
      </div>
    </div>
  );
}

export default StatCard;
