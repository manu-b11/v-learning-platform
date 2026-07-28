import { useState } from "react";
import { useNavigate } from "react-router-dom";

import Layout from "../components/Layout";
import { varkQuestions } from "../data/varkQuestions";
import { saveVarkResult } from "../api/varkService";

function VarkTest() {
  const navigate = useNavigate();
  const [answers, setAnswers] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const totalQuestions = varkQuestions.length;
  const answeredCount = Object.keys(answers).length;
  const allAnswered = answeredCount === totalQuestions;

  function selectAnswer(questionId, style) {
    setAnswers((prev) => ({ ...prev, [questionId]: style }));
  }

  async function handleSubmit() {
    const counts = {
      Visual: 0,
      Auditivo: 0,
      "Lectura/Escritura": 0,
      Kinestésico: 0,
    };

    Object.values(answers).forEach((style) => {
      counts[style] += 1;
    });

    const payload = {
      visualScore: counts.Visual,
      auditoryScore: counts.Auditivo,
      readingWritingScore: counts["Lectura/Escritura"],
      kinestheticScore: counts.Kinestésico,
    };

    setError("");
    setIsSubmitting(true);

    try {
      const result = await saveVarkResult(payload);
      navigate("/vark-test/result", { state: { result } });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "No se pudo guardar tu resultado. Intenta de nuevo.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="tracking-tight">Diagnóstico de estilo de aprendizaje</h1>
          <p className="mt-2 text-text-secondary">
            Responde estas preguntas para personalizar el contenido de tus
            cursos según cómo aprendes mejor.
          </p>
        </div>
        <span className="shrink-0 text-sm text-text-secondary">
          {answeredCount}/{totalQuestions} respondidas
        </span>
      </div>

      <div className="mt-6 space-y-4">
        {varkQuestions.map((question, index) => (
          <div key={question.id} className="card">
            <p className="text-sm text-text-secondary">
              Pregunta {index + 1} de {totalQuestions}
            </p>
            <h3 className="mt-1 text-base">{question.text}</h3>

            <div className="mt-4 space-y-2">
              {question.options.map((option) => {
                const isSelected = answers[question.id] === option.style;

                return (
                  <button
                    key={option.style}
                    onClick={() => selectAnswer(question.id, option.style)}
                    className={`flex w-full items-center gap-3 rounded-lg border px-4 py-3 text-left text-sm transition-colors duration-200 ${
                      isSelected
                        ? "border-primary bg-accent-blue-light font-medium text-navy"
                        : "border-border bg-surface text-text-secondary hover:border-navy/20"
                    }`}
                  >
                    {option.label}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {error && <p className="mt-4 text-sm text-danger">{error}</p>}

      <div className="mt-6 flex justify-end">
        <button
          onClick={handleSubmit}
          disabled={!allAnswered || isSubmitting}
          className="btn-primary disabled:cursor-not-allowed disabled:opacity-40"
        >
          {isSubmitting ? "Guardando..." : "Ver mi resultado"}
        </button>
      </div>
    </Layout>
  );
}

export default VarkTest;
