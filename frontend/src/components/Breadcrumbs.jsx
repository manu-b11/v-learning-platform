import { Link } from "react-router-dom";
import { ChevronRight } from "lucide-react";

function Breadcrumbs({ items }) {
  return (
    <nav className="flex flex-wrap items-center gap-1.5 text-sm text-text-secondary">
      {items.map((item, index) => {
        const isLast = index === items.length - 1;

        return (
          <span key={index} className="flex items-center gap-1.5">
            {index > 0 && <ChevronRight className="h-3.5 w-3.5 shrink-0" />}
            {isLast || !item.to ? (
              <span className={isLast ? "font-medium text-navy" : ""}>
                {item.label}
              </span>
            ) : (
              <Link
                to={item.to}
                className="transition-colors duration-200 hover:text-navy"
              >
                {item.label}
              </Link>
            )}
          </span>
        );
      })}
    </nav>
  );
}

export default Breadcrumbs;
