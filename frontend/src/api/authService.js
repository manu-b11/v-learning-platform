import axiosClient from "./axiosClient";

export function register(data) {
  return axiosClient.post("/auth/register", data).then((res) => res.data);
}

export function login(data) {
  return axiosClient.post("/auth/login", data).then((res) => res.data);
}
