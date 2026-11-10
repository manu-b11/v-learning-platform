/** RF-049: nombre de los criterios WCAG 2.2 que revisa el verificador de accesibilidad. */
export const CRITERIOS_WCAG = {
  "1.1.1": "Contenido no textual",
  "1.2.1": "Solo audio y solo video (grabado)",
  "1.2.2": "Subtítulos (grabados)",
  "1.2.3": "Audiodescripción o alternativa multimedia",
  "1.3.1": "Información y relaciones",
  "1.4.3": "Contraste (mínimo)",
  "2.4.4": "Propósito de los enlaces (en contexto)",
};

/** Hallazgos que trae una respuesta 422 de publicación (RF-049), o null. */
export const hallazgosDeError = (error) => error?.response?.data?.hallazgos ?? null;
