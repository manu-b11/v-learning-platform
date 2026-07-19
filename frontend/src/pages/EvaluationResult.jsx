import { useParams, useLocation, Link } from "react-router-dom";
import { ChevronLeft, CircleCheck, XCircle } from "lucide-react";

import Layout from "../components/Layout";
import { getEvaluationById } from "../data/evaluations";

function EvaluationResult() {
  const { id } = useParams();
  const location = useLocation();
  const evaluation = getEvaluationById(id);

  if (!evaluation) {
    return (
      <Layout>
        <p className="text-text-secondary">Evaluación no encontrada.</p>
      </Layout>
    );
  }

  // Si venimos de "Resolver evaluación" tenemos el detalle recién calculado;
  // si no (evaluación ya completada antes), usamos solo el score guardado.
  const fromSubmit = location.state;
  const score = fromSubmit?.score ?? evaluation.score;
  const passed = score >= 70;

  return (
    <Layout>
      <Link
        to="/evaluations"
        className="inline-flex items-center gap-1 text-sm text-text-secondary transition-colors duration-200 hover:text-navy"
      >
        <ChevronLeft className="h-4 w-4" />
        Evaluaciones
      </Link>

      {/* Resumen del resultado */}
      <section className="card mt-4 flex flex-col items-center py-10 text-center">
        <span
          className={`badge ${passed ? "badge-success" : "badge-neutral"}`}
        >
          {passed ? "Aprobada" : "No aprobada"}
        </span>

        <p className="mt-4 font-heading text-[48px] font-bold text-navy">
          {score}%
        </p>

        <h1 className="mt-1 tracking-tight">{evaluation.title}</h1>
        <p className="mt-1 text-sm text-text-secondary">{evaluation.course}</p>
      </section>

      {/* Revisión de respuestas (solo disponible justo después de resolverla) */}
      {fromSubmit && (
        <section className="mt-8">
          <h2 className="tracking-tight">Revisión de respuestas</h2>

          <div className="panel mt-4 divide-y divide-border">
            {fromSubmit.questions.map((question, index) => {
              const selectedIndex = fromSubmit.answers[question.id];
              const isCorrect = selectedIndex === question.correctIndex;

              return (
                <div key={question.id} className="p-5">
                  <div className="flex items-start gap-3">
                    {isCorrect ? (
                      <CircleCheck className="h-5 w-5 shrink-0 text-success" />
                    ) : (
                      <XCircle className="h-5 w-5 shrink-0 text-danger" />
                    )}
                    <div>
                      <p className="text-sm text-text-secondary">
                        Pregunta {index + 1}
                      </p>
                      <h3 className="mt-1 text-[15px]">{question.text}</h3>

                      <p className="mt-2 text-sm text-text-secondary">
                        Tu respuesta:{" "}
                        <span className="font-medium text-navy">
                          {question.options[selectedIndex]}
                        </span>
                      </p>

                      {!isCorrect && (
                        <p className="mt-1 text-sm text-text-secondary">
                          Respuesta correcta:{" "}
                          <span className="font-medium text-success">
                            {question.options[question.correctIndex]}
                          </span>
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      )}

      <div className="mt-8 flex justify-end">
        <Link to="/evaluations" className="btn-secondary">
          Volver a evaluaciones
        </Link>
      </div>
    </Layout>
  );
}

export default EvaluationResult;
