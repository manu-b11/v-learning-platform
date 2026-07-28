import axiosClient from "./axiosClient";

export function saveVarkResult(data) {
  return axiosClient.post("/vark", data).then((res) => res.data);
}

export function getMyVarkResult() {
  return axiosClient.get("/vark/me").then((res) => res.data);
}
