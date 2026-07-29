import axiosClient from "./axiosClient";

export function createContent(moduleId, data) {
  return axiosClient
    .post(`/modules/${moduleId}/contents`, data)
    .then((res) => res.data);
}

export function getContentsByModule(moduleId) {
  return axiosClient
    .get(`/modules/${moduleId}/contents`)
    .then((res) => res.data);
}

export function getContentById(id) {
  return axiosClient.get(`/contents/${id}`).then((res) => res.data);
}

export function updateContent(id, data) {
  return axiosClient.put(`/contents/${id}`, data).then((res) => res.data);
}

export function deleteContent(id) {
  return axiosClient.delete(`/contents/${id}`).then((res) => res.data);
}
