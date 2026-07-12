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
    <aside className="w-64 h-screen bg-[#14213D] text-white flex flex-col justify-between shadow-lg">
      <div>
        {/* Logo */}
        <div className="px-8 py-8 border-b border-white/10">
          <img src={logo} alt="V-Learning" className="h-15" />
        </div>

        {/* Menú */}
        <nav className="flex flex-col mt-6 px-4 gap-2 font-body">
          {menuItems.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 ${
                    isActive
                      ? "bg-white text-[#14213D] font-medium"
                      : "text-gray-300 hover:bg-white/10 hover:text-white"
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
      <div className="p-4 border-t border-white/10">
        <button className="w-full flex items-center gap-3 px-4 py-3 rounded-xl font-body text-gray-300 hover:bg-red-500 hover:text-white transition-all duration-200">
          <LogOut size={20} />
          <span>Cerrar sesión</span>
        </button>
      </div>
    </aside>
  );
}

export default Sidebar;
