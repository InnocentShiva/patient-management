
import { httpClient } from "./httpClient";

// Patient APIs
export const getPatients = () =>
  httpClient.get("/patients");

export const getPatientById = (id: string) =>
  httpClient.get(`/patients/${id}`);

export const createPatient = (data: any) =>
  httpClient.post("/patients", data);

export const updatePatient = (id: string, data: any) =>
  httpClient.put(`/patients/${id}`, data);

export const deletePatient = (id: string) =>
  httpClient.delete(`/patients/${id}`);

// Appointment APIs
export const getAppointments = () =>
  httpClient.get("/appointments");

export const getAppointmentById = (id: string) =>
  httpClient.get(`/appointments/${id}`);

export const createAppointment = (data: any) =>
  httpClient.post("/appointments", data);

export const updateAppointment = (id: string, data: any) =>
  httpClient.put(`/appointments/${id}`, data);

export const deleteAppointment = (id: string) =>
  httpClient.delete(`/appointments/${id}`);
