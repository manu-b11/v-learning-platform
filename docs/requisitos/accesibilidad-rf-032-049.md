# V-Learning: nuevas funcionalidades de accesibilidad (RF-032 a RF-049)

Versión 1.0 (08/11/2026). Se derivan del análisis comparativo de accesibilidad de V-Learning frente a Moodle, Canvas, Blackboard Learn Ultra, D2L Brightspace, Google Classroom, Open edX, Chamilo y TalentLMS, y de los criterios de WCAG 2.2.

La numeración continúa la del documento del proyecto: RF-032 a RF-049, HU-032 a HU-049 y CU-032 a CU-049. Todos los requisitos quedan en estado **Propuesto**.

## 1. Resumen

| RF | HU | Funcionalidad | Discapacidad | Criterio WCAG | Referente | Prioridad | Sprint |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-032 | HU-032 | Descargar contenidos en formatos alternativos | Visual, cognitiva | 1.1.1 | Blackboard Learn (Anthology Ally) | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-033 | HU-033 | Consultar alternativa textual de los gráficos del dashboard | Visual | 1.1.1, 1.4.1 | Buenas prácticas WCAG | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-034 | HU-034 | Anunciar mensajes nuevos y notificaciones al lector de pantalla | Visual, cognitiva | 4.1.3, 2.2.2 | Buenas prácticas WCAG | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-035 | HU-035 | Activar fuente para dislexia y subrayado de enlaces | Visual, cognitiva | 1.4.12, 1.4.1 | Canvas LMS | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-036 | HU-036 | Descargar en MP3 la lectura en voz alta | Visual, cognitiva | — | D2L Brightspace (ReadSpeaker) | Baja | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-037 | HU-037 | Generar subtítulos automáticos para revisión del instructor | Auditiva | 1.2.2 | D2L Brightspace (Video Note) | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-038 | HU-038 | Consultar transcripción sincronizada del video | Auditiva, cognitiva | 1.2.2, 1.2.3 | Open edX | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-039 | HU-039 | Exigir transcripción para publicar podcasts | Auditiva | 1.2.1 | edx.org (política de subtítulos) | Alta | Sprint 8 - Accesibilidad en Acceso y Evaluaciones |
| RF-040 | HU-040 | Cargar video de interpretación en lengua de señas | Auditiva | 1.2.6 | Google Meet (fijar intérprete) | Baja | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-041 | HU-041 | Asignar acomodaciones de evaluación por estudiante | Motora, cognitiva, visual | 2.2.1 | Blackboard Learn Ultra, D2L Brightspace | Alta | Sprint 8 - Accesibilidad en Acceso y Evaluaciones |
| RF-042 | HU-042 | Avisar antes de que termine el tiempo y permitir ampliarlo | Motora, cognitiva | 2.2.1 | Buenas prácticas WCAG | Alta | Sprint 8 - Accesibilidad en Acceso y Evaluaciones |
| RF-043 | HU-043 | Desbloquear la cuenta mediante enlace al correo | Motora, cognitiva | 3.3.8 | Buenas prácticas de autenticación accesible | Alta | Sprint 8 - Accesibilidad en Acceso y Evaluaciones |
| RF-044 | HU-044 | Ingresar el código OTP de forma accesible | Motora, cognitiva, visual | 3.3.8, 3.3.1, 4.1.3 | Buenas prácticas de autenticación accesible | Alta | Sprint 8 - Accesibilidad en Acceso y Evaluaciones |
| RF-045 | HU-045 | Dictar respuestas por voz | Motora | — | Chamilo (respuesta por voz en exámenes) | Baja | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-046 | HU-046 | Activar el modo de lectura sin distracciones | Cognitiva | 2.2.2 | Canvas LMS (Immersive Reader) | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-047 | HU-047 | Recibir un resumen diario de notificaciones y pausar la gamificación | Cognitiva | 2.2.4 | Buenas prácticas WCAG | Media | Sprint 9 - Accesibilidad en Contenidos y Comunicación |
| RF-048 | HU-048 | Acceder a la ayuda desde una ubicación consistente | Cognitiva | 3.2.6 | WCAG 2.2 | Alta | Sprint 8 - Accesibilidad en Acceso y Evaluaciones |
| RF-049 | HU-049 | Verificar la accesibilidad del contenido antes de publicar | Visual, auditiva, cognitiva | 1.1.1, 1.3.1, 1.4.3, 2.4.4 | Moodle, Canvas, Blackboard Learn, D2L Brightspace | Alta | Sprint 8 - Accesibilidad en Acceso y Evaluaciones |

Relación con requisitos existentes: RF-035 amplía RF-017; RF-036 amplía RF-018; RF-037, RF-038 y RF-039 amplían RF-019; RF-040 complementa RF-020; RF-041 complementa el tiempo extra por perfil de RF-021; RF-042, RF-043 y RF-044 ajustan RF-002 y RF-031; RF-049 extiende a todo el contenido la regla de publicación de RF-019.

## 2. Requisitos funcionales y casos de uso

Cada tabla sigue el formato de los RF del documento del proyecto y describe el caso de uso CU-0XX correspondiente.

### RF-032: Descargar contenidos en formatos alternativos

| **RF-032** | Descargar contenidos en formatos alternativos |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe generar y ofrecer para descarga versiones alternativas de cada contenido de lectura: audio MP3, EPUB, HTML accesible y braille electrónico (BRF). |
| Precondición | El usuario debe estar inscrito en el curso y el contenido debe estar publicado (RF-025). |
| Poscondición | El estudiante obtiene el contenido en el formato elegido. |
| Rendimiento | Los formatos ya generados deben entregarse en menos de 3 segundos; la generación en segundo plano debe terminar en menos de 5 minutos para documentos de hasta 50 páginas. |
| Frecuencia | Cada vez que un estudiante solicita un formato alternativo. |
| Importancia | Media |
| Urgencia | Media |
| Estado | Propuesto |
| Estabilidad | Media |
| Comentarios | Librerías sugeridas: epublib o Pandoc (EPUB), un servicio de texto a voz (MP3) y liblouis (BRF). Entidad sugerida: FormatoAlternativo (contenido_id, tipo, estado, url_archivo, generado_en). Criterio WCAG: 1.1.1. Referente: Blackboard Learn (Anthology Ally). |

**Secuencia normal (CU-032)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Abre un contenido de lectura y selecciona «Descargar en otro formato». |
| 2 | Sistema | Muestra los formatos disponibles: MP3, EPUB, HTML accesible y BRF. |
| 3 | Estudiante | Elige un formato. |
| 4 | Sistema | Si el formato ya fue generado, entrega el archivo; si no, lo genera en segundo plano y notifica al estudiante cuando está listo. |
| 5 | Sistema | Registra la descarga en el log de auditoría. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si la conversión falla, el sistema informa el error, ofrece los demás formatos y notifica al instructor; el caso de uso continúa. |
| 2 | Sistema | Si el contenido es un video o una simulación, el sistema solo ofrece la transcripción (RF-019); el caso de uso continúa. |

### RF-033: Consultar alternativa textual de los gráficos del dashboard

| **RF-033** | Consultar alternativa textual de los gráficos del dashboard |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe ofrecer, junto a cada gráfico del dashboard de progreso, un resumen en texto y una tabla con los mismos datos, y distinguir las series sin depender solo del color. |
| Precondición | El estudiante debe estar autenticado y tener datos de progreso (RF-006). |
| Poscondición | El estudiante conoce su progreso sin necesidad de ver el gráfico. |
| Rendimiento | El resumen y la tabla deben mostrarse en menos de 1 segundo. |
| Frecuencia | Cada consulta del dashboard. |
| Importancia | Media |
| Urgencia | Media |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Endpoint sugerido: GET /api/progreso/{estudianteId}/resumen. El PDF exportado (RF-006) debe ser un PDF etiquetado con la tabla incluida. Criterio WCAG: 1.1.1, 1.4.1. Referente: Buenas prácticas WCAG. |

**Secuencia normal (CU-033)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Abre el dashboard de progreso. |
| 2 | Sistema | Muestra cada gráfico con un resumen en texto de una o dos frases (por ejemplo, tendencia y valor actual). |
| 3 | Estudiante | Selecciona «Ver como tabla» en un gráfico. |
| 4 | Sistema | Muestra una tabla con encabezados de fila y columna que contiene los mismos datos del gráfico. |
| 5 | Sistema | Diferencia las series con patrones, marcadores o etiquetas además del color. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si no hay datos suficientes para un gráfico, el sistema lo indica en texto en lugar de mostrar un gráfico vacío; el caso de uso continúa. |

### RF-034: Anunciar mensajes nuevos y notificaciones al lector de pantalla

| **RF-034** | Anunciar mensajes nuevos y notificaciones al lector de pantalla |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe anunciar al lector de pantalla los mensajes nuevos del chat y del foro (RF-010) y las notificaciones (RF-011) sin mover el foco, y permitir pausar las actualizaciones automáticas. |
| Precondición | El usuario debe estar autenticado y tener abierto el chat, el foro o el panel de notificaciones. |
| Poscondición | El usuario se entera de los mensajes nuevos sin perder su posición y controla el ritmo de las actualizaciones. |
| Rendimiento | El anuncio debe producirse en menos de 1 segundo tras recibir el mensaje. |
| Frecuencia | Cada mensaje o notificación recibida. |
| Importancia | Media |
| Urgencia | Media |
| Estado | Propuesto |
| Estabilidad | Media |
| Comentarios | Los mensajes STOMP deben incluir un campo textoPlano para el anuncio. Preferencia sugerida en el perfil: anunciarMensajes (sí/no). Criterio WCAG: 4.1.3, 2.2.2. Referente: Buenas prácticas WCAG. |

**Secuencia normal (CU-034)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Recibe un mensaje o una notificación nueva por WebSocket. |
| 2 | Sistema | Anuncia el autor y el texto del mensaje en una región aria-live «polite», sin mover el foco. |
| 3 | Usuario | Activa «Pausar actualizaciones» si las necesita detener. |
| 4 | Sistema | Deja de insertar mensajes en pantalla y muestra un contador de mensajes pendientes. |
| 5 | Usuario | Reanuda las actualizaciones y el sistema muestra los mensajes pendientes en orden. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Usuario | Si el usuario desactiva los anuncios en su perfil, el sistema solo actualiza el contador; el caso de uso continúa. |

### RF-035: Activar fuente para dislexia y subrayado de enlaces

| **RF-035** | Activar fuente para dislexia y subrayado de enlaces |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe ofrecer en el panel de ajustes visuales (RF-017) una fuente diseñada para dislexia (OpenDyslexic) y la opción de subrayar todos los enlaces. |
| Precondición | El usuario debe estar autenticado. |
| Poscondición | La interfaz se muestra con la fuente o el subrayado elegidos en todas las sesiones. |
| Rendimiento | El cambio debe aplicarse en menos de 1 segundo. |
| Frecuencia | Configuración ocasional. |
| Importancia | Media |
| Urgencia | Baja |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Amplía RF-017; puede integrarse en ese requisito en lugar de quedar como uno nuevo. OpenDyslexic es una fuente libre. Criterio WCAG: 1.4.12, 1.4.1. Referente: Canvas LMS. |

**Secuencia normal (CU-035)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Usuario | Abre el panel de ajustes visuales. |
| 2 | Usuario | Activa «Fuente para dislexia» o «Subrayar enlaces». |
| 3 | Sistema | Aplica el cambio en toda la interfaz y en los contenidos. |
| 4 | Sistema | Guarda la preferencia en el perfil de accesibilidad (RF-015). |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si un contenido externo incrustado no admite el cambio de fuente, el sistema lo muestra con su fuente original; el caso de uso continúa. |

### RF-036: Descargar en MP3 la lectura en voz alta

| **RF-036** | Descargar en MP3 la lectura en voz alta |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe permitir descargar en MP3 la lectura en voz alta de un contenido (RF-018), con la voz y la velocidad configuradas por el usuario. |
| Precondición | El contenido debe tener texto legible por el motor de texto a voz (RF-018). |
| Poscondición | El estudiante tiene el audio del contenido para escucharlo sin conexión. |
| Rendimiento | El MP3 de un contenido de hasta 5.000 palabras debe generarse en menos de 30 segundos. |
| Frecuencia | Ocasional. |
| Importancia | Baja |
| Urgencia | Baja |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Amplía RF-018. Puede reutilizar el formato MP3 de RF-032. Criterio WCAG: —. Referente: D2L Brightspace (ReadSpeaker). |

**Secuencia normal (CU-036)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Selecciona «Descargar audio» en el reproductor de lectura en voz alta. |
| 2 | Sistema | Genera el MP3 con la velocidad configurada. |
| 3 | Sistema | Entrega el archivo con el título del contenido como nombre. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si el contenido supera la longitud máxima, el sistema lo divide en varias pistas numeradas; el caso de uso continúa. |

### RF-037: Generar subtítulos automáticos para revisión del instructor

| **RF-037** | Generar subtítulos automáticos para revisión del instructor |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe generar automáticamente un borrador de subtítulos (VTT) al subir un video o un podcast, para que el instructor lo revise y apruebe antes de publicar. |
| Precondición | El actor debe tener el rol de Instructor y estar creando o editando un contenido de video o audio (RF-025). |
| Poscondición | El contenido queda con subtítulos revisados y puede publicarse. |
| Rendimiento | El borrador debe generarse en un tiempo no mayor a la duración del video. |
| Frecuencia | Cada vez que se sube un video o un podcast. |
| Importancia | Alta |
| Urgencia | Media |
| Estado | Propuesto |
| Estabilidad | Media |
| Comentarios | Los subtítulos automáticos nunca se publican sin revisión humana. Opciones: whisper.cpp (local) o un servicio de voz a texto en la nube. Criterio WCAG: 1.2.2. Referente: D2L Brightspace (Video Note). |

**Secuencia normal (CU-037)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Instructor | Sube un video o un podcast. |
| 2 | Sistema | Genera en segundo plano un borrador de subtítulos con reconocimiento de voz. |
| 3 | Sistema | Notifica al instructor que el borrador está listo. |
| 4 | Instructor | Revisa el borrador en un editor con la marca de tiempo de cada línea y corrige lo necesario. |
| 5 | Instructor | Aprueba los subtítulos. |
| 6 | Sistema | Marca el contenido como subtitulado y habilita su publicación (RF-019). |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si el reconocimiento de voz falla, el sistema lo informa y permite subir un archivo .srt o .vtt manualmente; el caso de uso continúa. |
| 2 | Instructor | Si el instructor no aprueba el borrador, el contenido sigue sin poder publicarse; el caso de uso queda sin efecto. |

### RF-038: Consultar transcripción sincronizada del video

| **RF-038** | Consultar transcripción sincronizada del video |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe mostrar junto al video su transcripción, resaltar la línea que se está reproduciendo, permitir saltar a cualquier punto del video desde una línea y descargar la transcripción en .txt o .srt. |
| Precondición | El video debe tener subtítulos aprobados (RF-019 o RF-037). |
| Poscondición | El estudiante sigue el video leyendo y navega por su contenido desde el texto. |
| Rendimiento | El salto a una línea debe ocurrir en menos de 500 ms. |
| Frecuencia | Cada reproducción de video. |
| Importancia | Media |
| Urgencia | Media |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Amplía RF-019. La transcripción se obtiene del mismo archivo VTT de los subtítulos. Criterio WCAG: 1.2.2, 1.2.3. Referente: Open edX. |

**Secuencia normal (CU-038)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Reproduce un video y activa «Mostrar transcripción». |
| 2 | Sistema | Muestra la transcripción al lado del video y resalta la línea en curso. |
| 3 | Estudiante | Selecciona una línea con clic o con teclado. |
| 4 | Sistema | Lleva el video al momento de esa línea. |
| 5 | Estudiante | Descarga la transcripción en .txt o .srt si la necesita. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si el video no tiene subtítulos aprobados, el sistema oculta la opción de transcripción; el caso de uso queda sin efecto. |

### RF-039: Exigir transcripción para publicar podcasts

| **RF-039** | Exigir transcripción para publicar podcasts |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe impedir la publicación de un podcast o audio grabado que no tenga transcripción, igual que RF-019 lo hace con los videos sin subtítulos. |
| Precondición | El actor debe tener el rol de Instructor y el curso debe contener al menos un contenido de audio. |
| Poscondición | Ningún audio publicado queda sin transcripción. |
| Rendimiento | La verificación debe completarse en menos de 2 segundos. |
| Frecuencia | Cada solicitud de publicación. |
| Importancia | Alta |
| Urgencia | Alta |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Amplía RF-019 al audio. Si RF-019 ya cubre los podcasts, basta con aclararlo en su descripción. Criterio WCAG: 1.2.1. Referente: edx.org (política de subtítulos). |

**Secuencia normal (CU-039)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Instructor | Solicita publicar un curso o un módulo. |
| 2 | Sistema | Verifica que todos los contenidos de audio tengan transcripción. |
| 3 | Sistema | Si todos la tienen, publica el curso o el módulo. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si un audio no tiene transcripción, el sistema bloquea la publicación, lista los audios afectados y ofrece generar el borrador automático (RF-037); el caso de uso queda sin efecto. |

### RF-040: Cargar video de interpretación en lengua de señas

| **RF-040** | Cargar video de interpretación en lengua de señas |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe permitir al instructor subir un video de interpretación en lengua de señas asociado a un contenido, para que se reproduzca sincronizado y al lado del original. |
| Precondición | El actor debe tener el rol de Instructor y el contenido debe ser un video o una lectura (RF-025). |
| Poscondición | El contenido tiene interpretación en lengua de señas disponible para los estudiantes. |
| Rendimiento | Ambos videos deben mantenerse sincronizados con una diferencia menor a 300 ms. |
| Frecuencia | Ocasional, según la disponibilidad de intérpretes. |
| Importancia | Media |
| Urgencia | Baja |
| Estado | Propuesto |
| Estabilidad | Media |
| Comentarios | Complementa RF-020, que solo cubre la visualización de la pista de lengua de señas. Criterio WCAG: 1.2.6. Referente: Google Meet (fijar intérprete). |

**Secuencia normal (CU-040)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Instructor | Abre el contenido y selecciona «Agregar interpretación en lengua de señas». |
| 2 | Instructor | Sube el video del intérprete. |
| 3 | Sistema | Valida el formato y asocia el video al contenido. |
| 4 | Sistema | Marca el contenido con el indicador de lengua de señas disponible. |
| 5 | Estudiante | Reproduce el contenido y activa la pista de lengua de señas (RF-020). |
| 6 | Sistema | Muestra el video del intérprete al lado del contenido, sincronizado con él. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si la duración del video del intérprete no coincide con la del original, el sistema avisa al instructor y le permite ajustar el desfase; el caso de uso continúa. |

### RF-041: Asignar acomodaciones de evaluación por estudiante

| **RF-041** | Asignar acomodaciones de evaluación por estudiante |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe permitir al instructor o al administrador asignar a un estudiante acomodaciones que se apliquen automáticamente a todas las evaluaciones del curso: multiplicador de tiempo (1,5x o 2x), tiempo ilimitado, intentos extra o fechas distintas. |
| Precondición | El actor debe tener el rol de Instructor o Administrador y el estudiante debe estar inscrito en el curso (RF-026). |
| Poscondición | El estudiante dispone de la acomodación en todas las evaluaciones del curso, incluidas las que se creen después. |
| Rendimiento | La acomodación debe guardarse en menos de 2 segundos y el cálculo del tiempo no debe agregar más de 200 ms al inicio de la evaluación. |
| Frecuencia | Al inicio del periodo y cuando se aprueba una nueva acomodación. |
| Importancia | Alta |
| Urgencia | Alta |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Entidad sugerida: Acomodacion (inscripcion_id, multiplicador_tiempo, tiempo_ilimitado, intentos_extra, vigente_hasta, creada_por). Complementa el tiempo extra por perfil de RF-021. Criterio WCAG: 2.2.1. Referente: Blackboard Learn Ultra, D2L Brightspace. |

**Secuencia normal (CU-041)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Instructor | Abre la lista de estudiantes del curso y selecciona «Acomodaciones» en un estudiante. |
| 2 | Instructor | Elige el multiplicador de tiempo, el tiempo ilimitado, los intentos extra o las fechas especiales, y la fecha de vigencia. |
| 3 | Sistema | Guarda la acomodación y la registra en el log de auditoría. |
| 4 | Sistema | Al iniciar una evaluación, calcula en el servidor el tiempo límite aplicando la acomodación. |
| 5 | Estudiante | Ve en la evaluación el tiempo total que tiene asignado. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si el estudiante también tiene tiempo extra por su perfil motor (RF-021), el sistema aplica el mayor de los dos y no los suma; el caso de uso continúa. |
| 2 | Sistema | Si la acomodación está vencida, el sistema no la aplica y avisa al instructor; el caso de uso continúa. |

### RF-042: Avisar antes de que termine el tiempo y permitir ampliarlo

| **RF-042** | Avisar antes de que termine el tiempo y permitir ampliarlo |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma; OBJ-1 Garantizar acceso controlado y trazable a la plataforma |
| Descripción | El sistema debe avisar de forma visible y accesible antes de que termine el tiempo de una evaluación o de la sesión, y permitir ampliarlo cuando corresponda. |
| Precondición | El usuario debe estar autenticado y tener una sesión o una evaluación cronometrada en curso. |
| Poscondición | El usuario no pierde su trabajo por quedarse sin tiempo de forma inesperada. |
| Rendimiento | El aviso debe mostrarse con una precisión de ±5 segundos. |
| Frecuencia | En cada sesión y evaluación cronometrada. |
| Importancia | Alta |
| Urgencia | Alta |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Endpoint sugerido: POST /api/auth/refresh con refresh token. Criterio WCAG: 2.2.1. Referente: Buenas prácticas WCAG. |

**Secuencia normal (CU-042)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Detecta que faltan 2 minutos para que termine la sesión o la evaluación. |
| 2 | Sistema | Muestra un aviso con el tiempo restante y lo anuncia al lector de pantalla. |
| 3 | Usuario | Selecciona «Ampliar tiempo». |
| 4 | Sistema | En la sesión, renueva el token; en la evaluación, aplica la ampliación permitida por la acomodación (RF-041). |
| 5 | Sistema | Confirma la nueva hora de finalización. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si la evaluación no permite ampliación, el aviso solo informa el tiempo restante y el guardado automático (RF-007) conserva las respuestas; el caso de uso continúa. |
| 2 | Sistema | Si el usuario no responde al aviso de sesión, el sistema guarda el trabajo y cierra la sesión; el caso de uso queda sin efecto. |

### RF-043: Desbloquear la cuenta mediante enlace al correo

| **RF-043** | Desbloquear la cuenta mediante enlace al correo |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-1 Garantizar acceso controlado y trazable a la plataforma; OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe enviar al correo del usuario un enlace de desbloqueo de un solo uso cuando la cuenta se bloquea por intentos fallidos (RF-002), para que pueda recuperar el acceso sin esperar ni depender del administrador. |
| Precondición | La cuenta debe haber sido bloqueada por 5 intentos fallidos (RF-002). |
| Poscondición | El usuario recupera el acceso a su cuenta. |
| Rendimiento | El enlace debe enviarse en menos de 3 segundos. |
| Frecuencia | Cada bloqueo de cuenta. |
| Importancia | Alta |
| Urgencia | Alta |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Reutiliza el mecanismo de enlaces de un solo uso de RF-003. Vigencia sugerida del enlace: 15 minutos. Criterio WCAG: 3.3.8. Referente: Buenas prácticas de autenticación accesible. |

**Secuencia normal (CU-043)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Bloquea la cuenta y envía al correo registrado un enlace de desbloqueo de un solo uso. |
| 2 | Usuario | Abre el enlace. |
| 3 | Sistema | Valida el enlace, desbloquea la cuenta y pide iniciar sesión. |
| 4 | Sistema | Registra el desbloqueo en el log de auditoría. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si el enlace expiró o ya se usó, el sistema permite pedir uno nuevo; el caso de uso continúa. |
| 2 | Sistema | Si se producen 3 bloqueos en 24 horas, el sistema exige el desbloqueo por el administrador (RF-030); el caso de uso queda sin efecto. |

### RF-044: Ingresar el código OTP de forma accesible

| **RF-044** | Ingresar el código OTP de forma accesible |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-1 Garantizar acceso controlado y trazable a la plataforma; OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe permitir pegar o autocompletar el código de verificación en dos pasos (RF-002, RF-031), anunciar los errores al lector de pantalla y dar tiempo suficiente para ingresarlo. |
| Precondición | El usuario debe haber ingresado correo y contraseña válidos y tener activa la verificación en dos pasos. |
| Poscondición | El usuario inicia sesión sin tener que memorizar ni transcribir el código. |
| Rendimiento | La validación del código debe responder en menos de 1 segundo. |
| Frecuencia | Cada inicio de sesión con verificación en dos pasos. |
| Importancia | Alta |
| Urgencia | Alta |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Usar un solo campo con autocomplete="one-time-code" e inputmode="numeric", sin bloquear el pegado. Vigencia mínima sugerida del código: 10 minutos. Criterio WCAG: 3.3.8, 3.3.1, 4.1.3. Referente: Buenas prácticas de autenticación accesible. |

**Secuencia normal (CU-044)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Envía el código OTP al correo y muestra un único campo de código. |
| 2 | Usuario | Pega el código o lo autocompleta desde el navegador o el sistema operativo. |
| 3 | Sistema | Valida el código y completa el inicio de sesión. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si el código es incorrecto, el sistema anuncia el error asociado al campo y conserva el foco en él; el caso de uso continúa. |
| 2 | Usuario | Si el código expiró, el usuario pide uno nuevo con el botón «Reenviar código»; el caso de uso continúa. |

### RF-045: Dictar respuestas por voz

| **RF-045** | Dictar respuestas por voz |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe permitir dictar por voz el texto de las tareas (RF-027), de las preguntas abiertas de las evaluaciones y de los mensajes del foro y del chat (RF-010). |
| Precondición | El usuario debe estar autenticado y haber dado permiso de uso del micrófono al navegador. |
| Poscondición | El estudiante redacta textos sin usar el teclado. |
| Rendimiento | El texto dictado debe aparecer en menos de 1 segundo tras cada frase. |
| Frecuencia | A demanda del estudiante. |
| Importancia | Media |
| Urgencia | Baja |
| Estado | Propuesto |
| Estabilidad | Media |
| Comentarios | Puede implementarse en el frontend con la Web Speech API, sin cambios en el backend. Criterio WCAG: —. Referente: Chamilo (respuesta por voz en exámenes). |

**Secuencia normal (CU-045)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Selecciona el botón de micrófono de un campo de texto. |
| 2 | Sistema | Transcribe la voz del estudiante en el campo en tiempo real. |
| 3 | Estudiante | Revisa y corrige el texto. |
| 4 | Estudiante | Envía la respuesta o el mensaje. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si el navegador no admite reconocimiento de voz o se niega el permiso, el sistema oculta el botón e informa la causa; el caso de uso queda sin efecto. |

### RF-046: Activar el modo de lectura sin distracciones

| **RF-046** | Activar el modo de lectura sin distracciones |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe ofrecer un modo de lectura que oculte menús, ranking, insignias y notificaciones, y muestre solo el contenido con los ajustes visuales del perfil. |
| Precondición | El estudiante debe estar viendo un contenido de lectura. |
| Poscondición | El estudiante lee el contenido sin elementos que lo distraigan. |
| Rendimiento | El modo lectura debe activarse en menos de 500 ms. |
| Frecuencia | A demanda del estudiante. |
| Importancia | Media |
| Urgencia | Media |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Se recomienda activarlo por defecto para el perfil cognitivo (RF-015). Criterio WCAG: 2.2.2. Referente: Canvas LMS (Immersive Reader). |

**Secuencia normal (CU-046)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Selecciona «Modo lectura». |
| 2 | Sistema | Oculta la navegación, el ranking y las notificaciones emergentes. |
| 3 | Sistema | Muestra el contenido en una sola columna con los ajustes visuales del perfil (RF-017). |
| 4 | Estudiante | Sale del modo lectura con la tecla Esc o con el botón «Salir». |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si llega una notificación urgente (por ejemplo, el cierre de una evaluación), el sistema la acumula y la muestra al salir del modo lectura; el caso de uso continúa. |

### RF-047: Recibir un resumen diario de notificaciones y pausar la gamificación

| **RF-047** | Recibir un resumen diario de notificaciones y pausar la gamificación |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe permitir agrupar las notificaciones no urgentes (RF-011) en un resumen diario y pausar los avisos de gamificación (RF-009) desde el perfil. |
| Precondición | El estudiante debe estar autenticado. |
| Poscondición | El estudiante recibe menos interrupciones sin perder información. |
| Rendimiento | El resumen debe enviarse con un margen de ±5 minutos sobre la hora elegida. |
| Frecuencia | Diaria. |
| Importancia | Media |
| Urgencia | Baja |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Se recomienda activar el resumen por defecto para el perfil cognitivo (RF-015). Criterio WCAG: 2.2.4. Referente: Buenas prácticas WCAG. |

**Secuencia normal (CU-047)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Abre las preferencias de notificaciones. |
| 2 | Estudiante | Activa «Resumen diario» y elige la hora de envío. |
| 3 | Estudiante | Activa «Pausar gamificación» si lo necesita. |
| 4 | Sistema | Acumula las notificaciones no urgentes y las envía juntas a la hora elegida. |
| 5 | Sistema | Deja de mostrar avisos de puntos, insignias y ranking mientras la gamificación está pausada, sin dejar de registrar los puntos. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Las notificaciones urgentes (fechas límite en menos de 24 horas) se envían de inmediato aunque el resumen esté activo; el caso de uso continúa. |

### RF-048: Acceder a la ayuda desde una ubicación consistente

| **RF-048** | Acceder a la ayuda desde una ubicación consistente |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe mostrar un botón de ayuda en la misma posición de todas las páginas, con ayuda de la página actual, acceso al Tutor de IA (RF-024) y un canal para contactar al instructor. |
| Precondición | El usuario debe estar autenticado. |
| Poscondición | El usuario encuentra ayuda sin buscarla en cada página. |
| Rendimiento | El panel de ayuda debe abrirse en menos de 500 ms. |
| Frecuencia | A demanda del usuario. |
| Importancia | Media |
| Urgencia | Media |
| Estado | Propuesto |
| Estabilidad | Alta |
| Comentarios | Cumple el criterio 3.2.6 (Consistent Help), nivel A de WCAG 2.2. Criterio WCAG: 3.2.6. Referente: WCAG 2.2. |

**Secuencia normal (CU-048)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Usuario | Selecciona el botón «Ayuda», siempre en la misma posición. |
| 2 | Sistema | Muestra la ayuda de la página actual, el acceso al Tutor de IA y la opción de escribir al instructor. |
| 3 | Usuario | Elige una opción. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si la página no tiene ayuda propia, el sistema muestra la ayuda general y el acceso al Tutor de IA; el caso de uso continúa. |

### RF-049: Verificar la accesibilidad del contenido antes de publicar

| **RF-049** | Verificar la accesibilidad del contenido antes de publicar |
| --- | --- |
| Versión | 1.0 (08/11/2026) |
| Autores | Manuela Bolívar Sánchez, Juan Esteban Martínez, Juan José Cardona (Tecnológico de Antioquia I.U.) |
| Objetivos asociados | OBJ-7 Garantizar la accesibilidad e inclusión de la plataforma |
| Descripción | El sistema debe revisar la accesibilidad de cada contenido mientras el instructor lo crea y al publicarlo: imágenes sin texto alternativo, saltos en los niveles de encabezado, tablas sin encabezados, enlaces sin texto descriptivo, contraste insuficiente y medios sin subtítulos o transcripción. |
| Precondición | El actor debe tener el rol de Instructor y estar creando o editando un contenido (RF-025). |
| Poscondición | El curso se publica sin problemas de accesibilidad bloqueantes y queda un informe de verificación. |
| Rendimiento | La verificación de un curso de hasta 100 contenidos debe completarse en menos de 5 segundos. |
| Frecuencia | Cada edición y cada solicitud de publicación. |
| Importancia | Alta |
| Urgencia | Alta |
| Estado | Propuesto |
| Estabilidad | Media |
| Comentarios | Backend: análisis del HTML con jsoup; entidad HallazgoAccesibilidad (contenido_id, regla, criterio_wcag, severidad, mensaje, resuelto). Frontend: axe-core en el editor. Amplía la regla de RF-019 a todo el contenido. Criterio WCAG: 1.1.1, 1.3.1, 1.4.3, 2.4.4. Referente: Moodle, Canvas, Blackboard Learn, D2L Brightspace. |

**Secuencia normal (CU-049)**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Instructor | Crea o edita un contenido en el editor. |
| 2 | Sistema | Muestra en el editor los problemas de accesibilidad que detecta mientras se escribe. |
| 3 | Instructor | Corrige los problemas siguiendo las indicaciones. |
| 4 | Instructor | Solicita publicar el curso. |
| 5 | Sistema | Ejecuta la verificación completa y genera un informe por contenido. |
| 6 | Sistema | Si no hay problemas bloqueantes, publica el curso. |

**Excepciones**

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Sistema | Si hay problemas bloqueantes (imagen sin texto alternativo, video sin subtítulos, audio sin transcripción), el sistema impide la publicación y muestra el informe; el caso de uso queda sin efecto. |
| 2 | Sistema | Si solo hay advertencias, el sistema permite publicar y conserva el informe para revisión; el caso de uso continúa. |

## 3. Elementos para los diagramas de casos de uso

Actores, casos de uso incluidos («include») y extensiones («extend») para dibujar el diagrama de cada RF, con la misma estructura de las figuras «Caso de uso RF-0XX» del documento.

| Caso de uso | Actor principal | Actores secundarios | «include» | «extend» |
| --- | --- | --- | --- | --- |
| CU-032 Descargar contenidos en formatos alternativos | Estudiante | Servicio de conversión | Generar formato alternativo | Notificar formato listo (RF-011) |
| CU-033 Consultar alternativa textual de los gráficos del dashboard | Estudiante | — | Generar resumen textual | Exportar PDF etiquetado (RF-006) |
| CU-034 Anunciar mensajes nuevos y notificaciones al lector de pantalla | Usuario | — | — | Pausar actualizaciones |
| CU-035 Activar fuente para dislexia y subrayado de enlaces | Usuario | — | — | Personalizar la apariencia visual (RF-017) |
| CU-036 Descargar en MP3 la lectura en voz alta | Estudiante | Servicio de texto a voz | — | Leer en voz alta (RF-018) |
| CU-037 Generar subtítulos automáticos para revisión del instructor | Instructor | Servicio de reconocimiento de voz | Revisar y aprobar subtítulos | Subir subtítulos manualmente |
| CU-038 Consultar transcripción sincronizada del video | Estudiante | — | — | Descargar transcripción |
| CU-039 Exigir transcripción para publicar podcasts | Instructor | — | Verificar transcripciones | Generar borrador automático (RF-037) |
| CU-040 Cargar video de interpretación en lengua de señas | Instructor | Estudiante | — | Ajustar desfase |
| CU-041 Asignar acomodaciones de evaluación por estudiante | Instructor | Administrador, Estudiante | Calcular tiempo límite | — |
| CU-042 Avisar antes de que termine el tiempo y permitir ampliarlo | Usuario | — | — | Ampliar tiempo |
| CU-043 Desbloquear la cuenta mediante enlace al correo | Usuario | Servicio de correo | — | Solicitar nuevo enlace |
| CU-044 Ingresar el código OTP de forma accesible | Usuario | Servicio de correo | — | Reenviar código |
| CU-045 Dictar respuestas por voz | Estudiante | — | — | — |
| CU-046 Activar el modo de lectura sin distracciones | Estudiante | — | — | — |
| CU-047 Recibir un resumen diario de notificaciones y pausar la gamificación | Estudiante | — | — | — |
| CU-048 Acceder a la ayuda desde una ubicación consistente | Usuario | — | — | Consultar al Tutor de IA (RF-024) |
| CU-049 Verificar la accesibilidad del contenido antes de publicar | Instructor | — | Generar informe de accesibilidad | Publicar curso (RF-025) |

## 4. Historias de usuario

Mismo formato del backlog de Jira (HU VLearning Jira.csv). Se proponen dos sprints nuevos: el Sprint 8 agrupa las de prioridad alta y el Sprint 9 el resto.

### HU-032: Descargar contenidos en formatos alternativos

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con discapacidad visual o cognitiva, quiero descargar los contenidos en audio, EPUB, HTML accesible o braille electrónico para estudiarlos con mi tecnología de apoyo y sin conexión.

Criterios de aceptación:
1. Que cada contenido de lectura ofrezca las opciones MP3, EPUB, HTML accesible y BRF.
2. Que el formato ya generado se reutilice y no se vuelva a procesar.
3. Que la generación en segundo plano notifique al estudiante cuando el archivo esté listo.
4. Que el HTML accesible conserve encabezados, listas, tablas y texto alternativo del original.
5. Que la descarga quede registrada en el log de auditoría.
6. Que un formato ya generado se entregue en menos de 3 segundos.

Requisito funcional relacionado: RF-032.

### HU-033: Consultar alternativa textual de los gráficos del dashboard

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con discapacidad visual, quiero consultar los datos de cada gráfico del dashboard como texto y como tabla para entender mi progreso con el lector de pantalla.

Criterios de aceptación:
1. Que cada gráfico tenga un resumen en texto visible y leído por el lector de pantalla.
2. Que cada gráfico tenga un botón «Ver como tabla» operable con teclado.
3. Que la tabla tenga encabezados de fila y de columna correctamente marcados.
4. Que ninguna serie se distinga solo por el color.
5. Que el PDF exportado incluya la tabla y esté etiquetado.
6. Que el resumen y la tabla se muestren en menos de 1 segundo.

Requisito funcional relacionado: RF-033.

### HU-034: Anunciar mensajes nuevos y notificaciones al lector de pantalla

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante que usa lector de pantalla, quiero que se me anuncien los mensajes nuevos del chat y las notificaciones sin perder el foco para seguir la conversación del curso en tiempo real.

Criterios de aceptación:
1. Que cada mensaje nuevo se anuncie con autor y texto mediante una región aria-live.
2. Que el foco no se mueva al llegar un mensaje.
3. Que exista un botón para pausar y reanudar las actualizaciones, operable con teclado.
4. Que con las actualizaciones pausadas se muestre un contador de mensajes pendientes.
5. Que el usuario pueda desactivar los anuncios desde su perfil.
6. Que el anuncio se produzca en menos de 1 segundo.

Requisito funcional relacionado: RF-034.

### HU-035: Activar fuente para dislexia y subrayado de enlaces

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con dislexia, quiero usar una fuente para dislexia y ver los enlaces subrayados para leer con menos esfuerzo y distinguir los enlaces sin depender del color.

Criterios de aceptación:
1. Que el panel de ajustes visuales ofrezca la fuente OpenDyslexic.
2. Que exista la opción de subrayar todos los enlaces.
3. Que los cambios se apliquen a la interfaz y a los contenidos.
4. Que la preferencia se guarde en el perfil de accesibilidad y persista entre sesiones.
5. Que el cambio se aplique en menos de 1 segundo.

Requisito funcional relacionado: RF-035.

### HU-036: Descargar en MP3 la lectura en voz alta

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con discapacidad visual o cognitiva, quiero descargar en MP3 la lectura en voz alta de un contenido para escucharlo cuando no tengo conexión.

Criterios de aceptación:
1. Que el reproductor de lectura en voz alta tenga la opción «Descargar audio».
2. Que el MP3 respete la velocidad configurada por el estudiante.
3. Que los contenidos largos se dividan en pistas numeradas.
4. Que el MP3 se genere en menos de 30 segundos para contenidos de hasta 5.000 palabras.

Requisito funcional relacionado: RF-036.

### HU-037: Generar subtítulos automáticos para revisión del instructor

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como instructor, quiero recibir un borrador de subtítulos generado automáticamente al subir un video para publicar contenido accesible sin transcribirlo a mano.

Criterios de aceptación:
1. Que al subir un video o un podcast se genere un borrador de subtítulos en segundo plano.
2. Que el instructor reciba una notificación cuando el borrador esté listo.
3. Que exista un editor para corregir el texto y las marcas de tiempo.
4. Que el contenido solo se pueda publicar después de que el instructor apruebe los subtítulos.
5. Que, si el reconocimiento falla, se pueda subir un archivo .srt o .vtt.
6. Que el borrador se genere en un tiempo no mayor a la duración del video.

Requisito funcional relacionado: RF-037.

### HU-038: Consultar transcripción sincronizada del video

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con discapacidad auditiva, quiero leer la transcripción sincronizada junto al video y saltar a cualquier línea para seguir y repasar el contenido sin depender del audio.

Criterios de aceptación:
1. Que la transcripción se muestre al lado del video y se resalte la línea en curso.
2. Que al seleccionar una línea el video salte a ese momento.
3. Que las líneas sean operables con teclado.
4. Que la transcripción se pueda descargar en .txt y .srt.
5. Que el salto ocurra en menos de 500 ms.

Requisito funcional relacionado: RF-038.

### HU-039: Exigir transcripción para publicar podcasts

**Sprint:** Sprint 8 - Accesibilidad en Acceso y Evaluaciones · **Etiqueta:** vlearning

Como estudiante con discapacidad auditiva, quiero que todos los podcasts del curso tengan transcripción para acceder a su contenido sin escucharlo.

Criterios de aceptación:
1. Que al publicar se verifique que todo audio tenga transcripción.
2. Que la publicación se bloquee si falta alguna transcripción.
3. Que el sistema liste los audios sin transcripción.
4. Que se ofrezca generar el borrador automático (HU-037).
5. Que la verificación tarde menos de 2 segundos.

Requisito funcional relacionado: RF-039.

### HU-040: Cargar video de interpretación en lengua de señas

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como instructor, quiero subir un video de interpretación en lengua de señas para un contenido para que los estudiantes sordos accedan al contenido en su lengua.

Criterios de aceptación:
1. Que el instructor pueda asociar un video de intérprete a un video o a una lectura.
2. Que el contenido muestre un indicador de lengua de señas disponible.
3. Que el video del intérprete se reproduzca al lado del original y sincronizado con él.
4. Que se pueda ajustar el desfase entre ambos videos.
5. Que la diferencia de sincronización sea menor a 300 ms.

Requisito funcional relacionado: RF-040.

### HU-041: Asignar acomodaciones de evaluación por estudiante

**Sprint:** Sprint 8 - Accesibilidad en Acceso y Evaluaciones · **Etiqueta:** vlearning

Como instructor, quiero asignar a un estudiante más tiempo o intentos en todas las evaluaciones del curso para cumplir sus acomodaciones sin configurarlas evaluación por evaluación.

Criterios de aceptación:
1. Que se pueda elegir un multiplicador de tiempo (1,5x o 2x), tiempo ilimitado, intentos extra o fechas especiales.
2. Que la acomodación se aplique a todas las evaluaciones del curso, también a las creadas después.
3. Que el tiempo límite se calcule en el servidor.
4. Que, si también hay tiempo extra por perfil (HU-022), se aplique el mayor de los dos.
5. Que solo Instructor y Administrador puedan asignarlas y quede registro en auditoría.
6. Que la acomodación se guarde en menos de 2 segundos.

Requisito funcional relacionado: RF-041.

### HU-042: Avisar antes de que termine el tiempo y permitir ampliarlo

**Sprint:** Sprint 8 - Accesibilidad en Acceso y Evaluaciones · **Etiqueta:** vlearning

Como estudiante con discapacidad motora o cognitiva, quiero recibir un aviso antes de que termine el tiempo y poder ampliarlo para no perder mi trabajo por mi ritmo de interacción.

Criterios de aceptación:
1. Que se muestre un aviso 2 minutos antes de que termine la sesión o la evaluación.
2. Que el aviso se anuncie al lector de pantalla y sea operable con teclado.
3. Que la sesión pueda ampliarse renovando el token.
4. Que la evaluación pueda ampliarse si la acomodación lo permite (HU-041).
5. Que, si no se puede ampliar, se conserven las respuestas guardadas.
6. Que el aviso aparezca con una precisión de ±5 segundos.

Requisito funcional relacionado: RF-042.

### HU-043: Desbloquear la cuenta mediante enlace al correo

**Sprint:** Sprint 8 - Accesibilidad en Acceso y Evaluaciones · **Etiqueta:** vlearning

Como usuario con discapacidad motora, quiero desbloquear mi cuenta con un enlace enviado a mi correo para no quedarme sin acceso por errores de tecleo.

Criterios de aceptación:
1. Que al bloquearse la cuenta se envíe un enlace de desbloqueo de un solo uso.
2. Que el enlace expire a los 15 minutos.
3. Que se pueda pedir un enlace nuevo si expiró.
4. Que tras 3 bloqueos en 24 horas se requiera el desbloqueo por el administrador.
5. Que el desbloqueo quede registrado en el log de auditoría.
6. Que el enlace se envíe en menos de 3 segundos.

Requisito funcional relacionado: RF-043.

### HU-044: Ingresar el código OTP de forma accesible

**Sprint:** Sprint 8 - Accesibilidad en Acceso y Evaluaciones · **Etiqueta:** vlearning

Como usuario con discapacidad motora o cognitiva, quiero pegar o autocompletar el código de verificación para iniciar sesión sin tener que memorizarlo ni teclearlo carácter por carácter.

Criterios de aceptación:
1. Que el código se ingrese en un solo campo que permita pegar.
2. Que el campo use autocomplete="one-time-code".
3. Que el código tenga al menos 10 minutos de vigencia.
4. Que los errores se anuncien al lector de pantalla y queden asociados al campo.
5. Que exista un botón para reenviar el código.
6. Que la validación responda en menos de 1 segundo.

Requisito funcional relacionado: RF-044.

### HU-045: Dictar respuestas por voz

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con discapacidad motora, quiero dictar por voz mis tareas y mensajes para participar sin depender del teclado.

Criterios de aceptación:
1. Que los campos de texto de tareas, preguntas abiertas, foro y chat tengan un botón de micrófono.
2. Que la voz se transcriba en el campo en tiempo real.
3. Que el texto se pueda corregir antes de enviarlo.
4. Que, si el navegador no lo admite, se oculte el botón y se informe la causa.
5. Que el texto aparezca en menos de 1 segundo tras cada frase.

Requisito funcional relacionado: RF-045.

### HU-046: Activar el modo de lectura sin distracciones

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con discapacidad cognitiva, quiero leer los contenidos en un modo sin menús ni notificaciones para concentrarme en lo que estoy estudiando.

Criterios de aceptación:
1. Que exista un botón «Modo lectura» en los contenidos de lectura.
2. Que el modo oculte navegación, ranking, insignias y notificaciones emergentes.
3. Que se respeten los ajustes visuales del perfil.
4. Que se pueda salir con la tecla Esc.
5. Que las notificaciones recibidas se muestren al salir.
6. Que el modo se active en menos de 500 ms.

Requisito funcional relacionado: RF-046.

### HU-047: Recibir un resumen diario de notificaciones y pausar la gamificación

**Sprint:** Sprint 9 - Accesibilidad en Contenidos y Comunicación · **Etiqueta:** vlearning

Como estudiante con discapacidad cognitiva, quiero recibir las notificaciones en un resumen diario y pausar los avisos de gamificación para evitar interrupciones constantes mientras estudio.

Criterios de aceptación:
1. Que se pueda activar el resumen diario y elegir su hora.
2. Que las notificaciones urgentes se sigan enviando de inmediato.
3. Que se pueda pausar la gamificación sin perder los puntos acumulados.
4. Que las preferencias se guarden en el perfil.
5. Que el resumen se envíe con un margen de ±5 minutos.

Requisito funcional relacionado: RF-047.

### HU-048: Acceder a la ayuda desde una ubicación consistente

**Sprint:** Sprint 8 - Accesibilidad en Acceso y Evaluaciones · **Etiqueta:** vlearning

Como estudiante con discapacidad cognitiva, quiero encontrar el botón de ayuda siempre en el mismo lugar para pedir ayuda sin perderme en la plataforma.

Criterios de aceptación:
1. Que el botón de ayuda esté en la misma posición en todas las páginas.
2. Que ofrezca la ayuda de la página actual, el Tutor de IA y el contacto con el instructor.
3. Que sea operable con teclado y tenga un nombre accesible.
4. Que el panel se abra en menos de 500 ms.

Requisito funcional relacionado: RF-048.

### HU-049: Verificar la accesibilidad del contenido antes de publicar

**Sprint:** Sprint 8 - Accesibilidad en Acceso y Evaluaciones · **Etiqueta:** vlearning

Como instructor, quiero que el sistema me indique los problemas de accesibilidad de mis contenidos antes de publicarlos para publicar cursos que todos los estudiantes puedan usar.

Criterios de aceptación:
1. Que el editor señale en tiempo real imágenes sin texto alternativo, saltos de encabezado, tablas sin encabezados, enlaces poco descriptivos y contraste insuficiente.
2. Que cada problema indique el criterio WCAG y cómo corregirlo.
3. Que al publicar se ejecute una verificación completa del curso.
4. Que los problemas bloqueantes impidan publicar y las advertencias no.
5. Que se conserve un informe de verificación por contenido.
6. Que la verificación de un curso de hasta 100 contenidos tarde menos de 5 segundos.

Requisito funcional relacionado: RF-049.

## 5. Fuentes

- [W3C – WCAG 2.2](https://www.w3.org/TR/WCAG22/)
- [Blackboard Help – Anthology Ally](https://help.blackboard.com/Learn/Administrator/SaaS/Tools_Management/Ally)
- [D2L Community – ReadSpeaker para contenido HTML](https://community.d2l.com/brightspace/kb/articles/23784-readspeaker-for-html-content)
- [CUNY – Recursos de accesibilidad de Brightspace (Video Note)](https://disabilityservices.commons.gc.cuny.edu/brightspace-navigation-accessibility-resources/)
- [UChicago – Fuente para dislexia y alto contraste en Canvas](https://academictech.uchicago.edu/2025/10/17/accessibility-features-and-updates-in-canvas)
- [Open edX – Reproductor de video y transcripciones](https://docs.openedx.org/en/ulmo/learners/SFD_video_player.html)
- [UAMS – Acomodaciones en Blackboard Ultra](https://educationaldevelopment.uams.edu/elearning/blackboard-ultra/blackboard-ultra-how-to/blackboard-ultra-accommodations-vs-exceptions)
- [Huddersfield – Acomodaciones en Brightspace](https://brightspacequicktips.hud.ac.uk/?p=1721)
- [Google for Education – Accesibilidad](https://edu.google.nl/our-values/accessibility)
- [BeezNest – Chamilo LMS](https://beeznest.com/es/node/707)
