import axiosClient from "./axiosClient";

export function createModule(courseId, data) {
  return axiosClient
    .post(`/courses/${courseId}/modules`, data)
    .then((res) => res.data);
}

export function getModulesByCourse(courseId) {
  return axiosClient
    .get(`/courses/${courseId}/modules`)
    .then((res) => res.data);
}

export function getModuleById(id) {
  return axiosClient.get(`/modules/${id}`).then((res) => res.data);
}

export function updateModule(id, data) {
  return axiosClient.put(`/modules/${id}`, data).then((res) => res.data);
}

export function deleteModule(id) {
  return axiosClient.delete(`/modules/${id}`).then((res) => res.data);
}
