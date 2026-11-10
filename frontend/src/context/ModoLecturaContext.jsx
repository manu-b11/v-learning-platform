import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

const ModoLecturaContext = createContext({ activo: false, activar: () => {}, salir: () => {} });

/**
 * RF-046: modo de lectura sin distracciones. Mientras está activo, AppShell oculta el menú, la
 * barra superior (puntos y ranking), el panel de perfil y el Tutor, y NotifContext guarda las
 * notificaciones para mostrarlas al salir. Se sale con Esc o con el botón «Salir».
 */
export function ModoLecturaProvider({ children }) {
  const [activo, setActivo] = useState(false);
  const activar = useCallback(() => setActivo(true), []);
  const salir = useCallback(() => setActivo(false), []);

  useEffect(() => {
    if (!activo) return undefined;
    document.documentElement.dataset.lectura = "on";
    const tecla = (e) => {
      // Si un diálogo abierto ya usó la tecla Esc, no se sale del modo lectura.
      if (e.key === "Escape" && !e.defaultPrevented && !document.querySelector("dialog[open]")) setActivo(false);
    };
    document.addEventListener("keydown", tecla);
    return () => {
      document.removeEventListener("keydown", tecla);
      delete document.documentElement.dataset.lectura;
    };
  }, [activo]);

  const valor = useMemo(() => ({ activo, activar, salir }), [activo, activar, salir]);
  return <ModoLecturaContext.Provider value={valor}>{children}</ModoLecturaContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useModoLectura() {
  return useContext(ModoLecturaContext);
}
