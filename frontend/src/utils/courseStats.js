export function getAllContents(course) {
  return course.modules.flatMap((m) => m.contents);
}

export function getModuleCount(course) {
  return course.modules.length;
}

export function getTotalMinutes(course) {
  return getAllContents(course).reduce((sum, c) => sum + c.durationMinutes, 0);
}

export function getRemainingMinutes(course) {
  return getAllContents(course)
    .filter((c) => !c.completed)
    .reduce((sum, c) => sum + c.durationMinutes, 0);
}
