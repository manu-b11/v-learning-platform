import { Bell, Search, UserCircle } from "lucide-react";

function Navbar() {
  return (
    <header className="h-20 bg-white flex items-center justify-between px-8">
      <div className="relative w-96">
        {/* Buscador */}
        <Search
          size={20}
          className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
        />
        <input
          type="text"
          placeholder="Buscar cursos..."
          className="w-full rounded-lg border border-gray-300 py-2 pl-10 pr-4 focus:outline-none focus:ring-2 focus:ring-[#14213D]"
        />
      </div>
      {/* Acciones */}
      <div className="flex items-center gap-6">
        <Bell className="cursor-pointer text-gray-600" />

        <div className="flex items-center gap-3 cursor-pointer">
          <div className="w-9 h-9 rounded-full bg-[#14213D] text-white flex items-center justify-center font-medium">
            S
          </div>
          <span className="font-body font-medium">Sara</span>
        </div>
      </div>
    </header>
  );
}

export default Navbar;
