import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Bot, CircleHelp, MessagesSquare } from "lucide-react";
import { useAuth } from "../../context/AuthContext.jsx";
import { useTutor } from "../../context/TutorContext.jsx";
import { ayudaDe, cursoDeRuta } from "../../utils/ayuda.js";
import { Dialogo } from "../ui/index.jsx";

const OPCION = "flex min-h-11 w-full items-center gap-3 rounded-ctl border border-line px-4 py-2 text-left font-bold text-strong hover:bg-subtle";

/**
 * RF-048 (WCAG 3.2.6, ayuda consistente): botón «Ayuda» siempre en la misma posición de la barra
 * superior. Ofrece la ayuda de la página actual (o la general), el Tutor de IA (solo estudiantes,
 * que son quienes lo tienen) y un canal para escribir al instructor del curso.
 */
export function BotonAyuda() {
  const { usuario } = useAuth();
  const { abrir: abrirTutor } = useTutor();
  const { pathname } = useLocation();
  const [abierto, setAbierto] = useState(false);
  const ayuda = ayudaDe(pathname);
  const cursoId = cursoDeRuta(pathname);
  const esEstudiante = usuario.rol === "ESTUDIANTE";

  return (
    <>
      <button type="button" onClick={() => setAbierto(true)} aria-haspopup="dialog"
        className="flex min-h-11 items-center gap-2 rounded-xl bg-subtle px-3 font-bold text-strong hover:bg-primary-100">
        <CircleHelp size={20} aria-hidden="true" />
        <span className="max-sm:sr-only">Ayuda</span>
      </button>
      <Dialogo abierto={abierto} onCerrar={() => setAbierto(false)} titulo="Ayuda">
        <div className="grid gap-5">
          <section aria-labelledby="ayuda-pagina">
            <h3 id="ayuda-pagina" className="mb-2 text-lg">
              {ayuda.propia ? `En esta página: ${ayuda.titulo}` : ayuda.titulo}
            </h3>
            <ul className="list-disc space-y-1 pl-5">
              {ayuda.puntos.map((p) => <li key={p}>{p}</li>)}
            </ul>
          </section>
          <section aria-labelledby="ayuda-mas" className="grid gap-2">
            <h3 id="ayuda-mas" className="text-lg">¿Necesitas más ayuda?</h3>
            {esEstudiante && (
              <button type="button" className={OPCION} onClick={() => { setAbierto(false); abrirTutor(); }}>
                <Bot size={20} aria-hidden="true" />Preguntar al Tutor de IA
              </button>
            )}
            {esEstudiante && (
              <Link to={cursoId ? `/comunidad/${cursoId}` : "/comunidad"} className={OPCION} onClick={() => setAbierto(false)}>
                <MessagesSquare size={20} aria-hidden="true" />
                {cursoId ? "Escribir al instructor en el chat del curso" : "Escribir al instructor en la comunidad del curso"}
              </Link>
            )}
            {!esEstudiante && (
              <p className="text-sm">
                Si algo no funciona, escribe al administrador de la plataforma o revisa la guía de uso del proyecto.
              </p>
            )}
          </section>
        </div>
      </Dialogo>
    </>
  );
}
