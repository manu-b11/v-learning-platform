import { Bell, Search } from "lucide-react";

function Navbar() {
  return (
    <header className="flex h-20 items-center justify-between gap-8 border-b border-border px-8">
      {/* Buscador */}
      <div className="flex-1 max-w-3xl">
        <div className="relative w-full">
          <Search
            size={20}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary"
          />

          <input
            type="text"
            placeholder="Buscar cursos..."
            className="w-full rounded-lg border border-border bg-surface py-3 pl-10 pr-4 focus:outline-none focus:ring-2 focus:ring-primary/30"
          />
        </div>
      </div>

      {/* Acciones */}
      <div className="flex items-center gap-6">
        <Bell
          size={20}
          className="cursor-pointer text-text-secondary hover:text-navy"
        />

        <div className="flex cursor-pointer items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-navy font-medium text-white">
            S
          </div>

          <span className="font-medium">Sara</span>
        </div>
      </div>
    </header>
  );
}

export default Navbar;
