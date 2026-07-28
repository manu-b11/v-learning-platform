import api from "./axiosClient";

export async function getModulesByCourse(courseId) {
  const response = await api.get(`/courses/${courseId}/modules`);
  return response.data;
}

export async function getModuleById(id) {
  const response = await api.get(`/modules/${id}`);
  return response.data;
}
