function StatCard({ title, value, icon: Icon, iconBg, iconColor }) {
  return (
    <div className="card flex items-center gap-4">
      <div
        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg ${iconBg}`}
      >
        <Icon size={20} className={iconColor} strokeWidth={1.75} />
      </div>

      <div>
        <p className="text-text-secondary text-[13px]">{title}</p>
        <p className="font-heading text-[20px] font-bold text-navy">{value}</p>
      </div>
    </div>
  );
}

export default StatCard;
