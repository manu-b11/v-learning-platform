import { useParams } from "react-router-dom";
import { ArrowLeft, CircleCheck, CircleX } from "lucide-react";
import { api } from "../../api/client.js";
import { useAuth } from "../../context/AuthContext.jsx";
import { useAsync } from "../../utils/useAsync.js";
import { Badge, Banner, Button, Card, Cargando, EstadoError, PageHeader, Vacio } from "../../components/ui/index.jsx";

const fmtPct = (v) => (v === null || v === undefined ? "—" : `${String(v).replace(".", ",")} %`);

const ESTADO_CELDA = {
  PENDIENTE: "No presentada (cuenta 0 %)",
  POR_CALIFICAR: "Entregada, por calificar (no cuenta)",
  EXCLUIDA: "Excluida: sin alternativa accesible (no cuenta)",
};

function Celda({ celda }) {
  if (celda.estado === "CALIFICADA") return <>{fmtPct(celda.porcentaje)}</>;
  if (celda.estado === "PENDIENTE")
    return (
      <span>
        0 %<span className="block text-xs text-muted">No presentada</span>
      </span>
    );
  if (celda.estado === "POR_CALIFICAR") return <span className="text-sm">Por calificar</span>;
  return <span className="text-sm">Excluida</span>;
}

function EstadoFinal({ estado }) {
  if (estado === "APRUEBA")
    return <Badge tono="ok" icono={CircleCheck}>Aprueba</Badge>;
  if (estado === "NO_APRUEBA")
    return <Badge tono="err" icono={CircleX}>No aprueba</Badge>;
  return <Badge>Sin notas</Badge>;
}

/** RF-029: notas del curso con promedio general y quiénes aprueban (instructor del curso o administrador). */
export default function NotasCurso() {
  const { id } = useParams();
  const { usuario } = useAuth();
  const { datos, cargando, error, recargar } = useAsync(() => api.notas.delCurso(id), [id]);

  if (cargando && !datos) return <Cargando texto="Cargando las notas del curso…" />;
  if (error && !datos) return <EstadoError mensaje={error} onReintentar={recargar} />;

  const { actividades, estudiantes } = datos;
  const volver = usuario.rol === "INSTRUCTOR" ? `/cursos/${id}/editar` : "/cursos";

  return (
    <>
      <p className="mb-3">
        <Button variante="ghost" icono={ArrowLeft} to={volver}>
          {usuario.rol === "INSTRUCTOR" ? "Volver al editor del curso" : "Volver a cursos"}
        </Button>
      </p>
      <PageHeader
        eyebrow="Notas del curso"
        titulo={datos.curso}
        subtitulo={`Nota final = promedio de evaluaciones y tareas. Aprueba con ${fmtPct(datos.umbralAprobacion)} o más.`}
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <Card>
          <p className="text-sm font-bold text-muted">Promedio general del curso</p>
          <p className="mt-1 text-3xl font-bold text-strong">{fmtPct(datos.promedioGeneral)}</p>
        </Card>
        <Card>
          <p className="text-sm font-bold text-muted">Aprueban</p>
          <p className="mt-1 flex items-center gap-2 text-3xl font-bold text-strong">
            <CircleCheck aria-hidden="true" className="text-ok-fg" /> {datos.aprueban}
          </p>
        </Card>
        <Card>
          <p className="text-sm font-bold text-muted">No aprueban</p>
          <p className="mt-1 flex items-center gap-2 text-3xl font-bold text-strong">
            <CircleX aria-hidden="true" className="text-err-fg" /> {datos.noAprueban}
          </p>
        </Card>
      </div>

      {estudiantes.length === 0 ? (
        <Vacio titulo="Este curso aún no tiene estudiantes inscritos">
          Cuando inscribas estudiantes, aquí verás sus notas.
        </Vacio>
      ) : actividades.length === 0 ? (
        <Vacio titulo="Este curso aún no tiene actividades calificables">
          Agrega evaluaciones o tareas en los módulos para ver las notas.
        </Vacio>
      ) : (
        <>
          <Banner tono="info" className="mb-3">
            Reglas: la nota de una evaluación es el mejor intento. Una actividad no presentada cuenta 0 %. Una tarea
            entregada sin calificar no cuenta hasta que la califiques. Las evaluaciones sin alternativa accesible no
            cuentan para estudiantes con perfil motor.
          </Banner>
          <div className="overflow-x-auto rounded-opt border border-line" tabIndex={0} role="region" aria-label="Tabla de notas">
            <table className="w-full min-w-[640px] text-left">
              <caption className="sr-only">Notas por estudiante y actividad del curso {datos.curso}</caption>
              <thead className="bg-subtle text-sm">
                <tr>
                  <th scope="col" className="p-3">Estudiante</th>
                  {actividades.map((a) => (
                    <th key={a.clave} scope="col" className="p-3">
                      <span className="block text-xs font-normal uppercase text-muted">
                        {a.tipo === "EVALUACION" ? "Evaluación" : "Tarea"}{a.modulo ? ` · ${a.modulo}` : ""}
                      </span>
                      {a.titulo}
                    </th>
                  ))}
                  <th scope="col" className="p-3">Nota final</th>
                  <th scope="col" className="p-3">Estado</th>
                </tr>
              </thead>
              <tbody>
                {estudiantes.map((e) => (
                  <tr key={e.estudianteId} className="border-t border-line">
                    <th scope="row" className="p-3 font-normal">
                      <strong className="text-strong">{e.nombre}</strong>
                      {e.codigo && <span className="block text-xs text-muted">{e.codigo}</span>}
                      <span className="block text-xs text-muted">Avance {fmtPct(e.avance)}</span>
                    </th>
                    {e.notas.map((n) => (
                      <td key={n.clave} className="p-3" title={ESTADO_CELDA[n.estado]}>
                        <Celda celda={n} />
                      </td>
                    ))}
                    <td className="p-3 font-bold text-strong">{fmtPct(e.notaFinal)}</td>
                    <td className="p-3">
                      <EstadoFinal estado={e.estado} />
                      {(e.pendientes > 0 || e.porCalificar > 0) && (
                        <span className="mt-1 block text-xs text-muted">
                          {e.pendientes > 0 && `${e.pendientes} sin presentar`}
                          {e.pendientes > 0 && e.porCalificar > 0 && " · "}
                          {e.porCalificar > 0 && `${e.porCalificar} por calificar`}
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </>
  );
}
