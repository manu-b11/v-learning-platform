import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ChevronLeft } from "lucide-react";

import Layout from "../components/Layout";
import { getEvaluationById } from "../data/evaluations";

function TakeEvaluation() {
  const { id } = useParams();
  const navigate = useNavigate();
  const evaluation = getEvaluationById(id);
  const [answers, setAnswers] = useState({});

  if (!evaluation) {
    return (
      <Layout>
        <p className="text-text-secondary">Evaluación no encontrada.</p>
      </Layout>
    );
  }

  if (evaluation.status === "Completada") {
    return (
      <Layout>
        <p className="text-text-secondary">
          Ya completaste esta evaluación.{" "}
          <Link
            to={`/evaluations/${evaluation.id}/result`}
            className="font-medium text-primary"
          >
            Ver resultado
          </Link>
        </p>
      </Layout>
    );
  }

  const totalQuestions = evaluation.questions.length;
  const answeredCount = Object.keys(answers).length;
  const allAnswered = answeredCount === totalQuestions;

  function selectAnswer(questionId, optionIndex) {
    setAnswers((prev) => ({ ...prev, [questionId]: optionIndex }));
  }

  function handleSubmit() {
    const correctCount = evaluation.questions.filter(
      (q) => answers[q.id] === q.correctIndex,
    ).length;
    const score = Math.round((correctCount / totalQuestions) * 100);

    navigate(`/evaluations/${evaluation.id}/result`, {
      state: { score, answers, questions: evaluation.questions },
    });
  }

  return (
    <Layout>
      <Link
        to="/evaluations"
        className="inline-flex items-center gap-1 text-sm text-text-secondary transition-colors duration-200 hover:text-navy"
      >
        <ChevronLeft className="h-4 w-4" />
        Evaluaciones
      </Link>

      <div className="mt-2 flex items-center justify-between">
        <h1 className="tracking-tight">{evaluation.title}</h1>
        <span className="text-sm text-text-secondary">
          {answeredCount}/{totalQuestions} respondidas
        </span>
      </div>
      <p className="mt-1 text-sm text-text-secondary">{evaluation.course}</p>

      <div className="mt-6 space-y-4">
        {evaluation.questions.map((question, index) => (
          <div key={question.id} className="card">
            <p className="text-sm text-text-secondary">
              Pregunta {index + 1} de {totalQuestions}
            </p>
            <h3 className="mt-1 text-base">{question.text}</h3>

            <div className="mt-4 space-y-2">
              {question.options.map((option, optionIndex) => {
                const isSelected = answers[question.id] === optionIndex;

                return (
                  <button
                    key={optionIndex}
                    onClick={() => selectAnswer(question.id, optionIndex)}
                    className={`flex w-full items-center gap-3 rounded-lg border px-4 py-3 text-left text-sm transition-colors duration-200 ${
                      isSelected
                        ? "border-primary bg-accent-blue-light font-medium text-navy"
                        : "border-border bg-surface text-text-secondary hover:border-navy/20"
                    }`}
                  >
                    {option}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 flex justify-end">
        <button
          onClick={handleSubmit}
          disabled={!allAnswered}
          className="btn-primary disabled:cursor-not-allowed disabled:opacity-40"
        >
          Enviar evaluación
        </button>
      </div>
    </Layout>
  );
}

export default TakeEvaluation;
