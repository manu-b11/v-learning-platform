import { NavLink } from "react-router-dom";
import logo from "../assets/logo.svg";
import {
  LayoutDashboard,
  BookOpen,
  ClipboardCheck,
  User,
  Settings,
  LogOut,
} from "lucide-react";

const menuItems = [
  {
    name: "Dashboard",
    path: "/dashboard",
    icon: LayoutDashboard,
  },
  {
    name: "Mis cursos",
    path: "/courses",
    icon: BookOpen,
  },
  {
    name: "Evaluaciones",
    path: "/evaluations",
    icon: ClipboardCheck,
  },
  {
    name: "Perfil",
    path: "/profile",
    icon: User,
  },
  {
    name: "Configuración",
    path: "/settings",
    icon: Settings,
  },
];

function Sidebar() {
  return (
    <aside className="fixed left-0 top-0 flex h-screen w-64 flex-col justify-between bg-navy text-white shadow-lg">
      <div>
        {/* Logo */}
        <div className="border-b border-white/10 px-4 py-2">
          <img src={logo} alt="V-Learning" className="h-14" />
        </div>

        {/* Menú */}
        <nav className="mt-14 flex flex-col gap-2 px-4">
          {menuItems.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-4 py-3 transition-all duration-200 ${
                    isActive
                      ? "bg-surface text-navy font-medium shadow-sm"
                      : "text-white/70 hover:bg-white/10 hover:text-white"
                  }`
                }
              >
                <Icon size={20} />
                <span>{item.name}</span>
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Cerrar sesión */}
      <div className="border-t border-white/10 p-4">
        <button className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-white/70 transition-all duration-200 hover:bg-accent-coral hover:text-white">
          <LogOut size={20} />
          <span>Cerrar sesión</span>
        </button>
      </div>
    </aside>
  );
}

export default Sidebar;
