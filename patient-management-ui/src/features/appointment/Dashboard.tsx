import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getPatients,
  createPatient,
  updatePatient,
  deletePatient,
  getAppointments,
  createAppointment,
  updateAppointment,
  deleteAppointment,
} from "../../api/appointment.api";
import { useState } from "react";
import "../../styles/dashboard.css";

interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  address: string;
}

interface Appointment {
  id: string;
  patientId: string;
  appointmentDate: string;
  appointmentTime: string;
  reason: string;
  status: string;
}

export default function Dashboard() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<"patients" | "appointments">("patients");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");

  // Patient Queries
  const { data: patients = [], isLoading: patientsLoading } = useQuery({
    queryKey: ["patients"],
    queryFn: () => getPatients().then((res) => res.data),
  });

  // Appointment Queries
  const { data: appointments = [], isLoading: appointmentsLoading } = useQuery({
    queryKey: ["appointments"],
    queryFn: () => getAppointments().then((res) => res.data),
  });

  // Patient Mutations
  const createPatientMutation = useMutation({
    mutationFn: createPatient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["patients"] });
      setShowForm(false);
    },
  });

  const updatePatientMutation = useMutation({
    mutationFn: (data: any) => updatePatient(data.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["patients"] });
      setEditingId(null);
      setShowForm(false);
    },
  });

  const deletePatientMutation = useMutation({
    mutationFn: deletePatient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["patients"] });
    },
  });

  // Appointment Mutations
  const createAppointmentMutation = useMutation({
    mutationFn: createAppointment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["appointments"] });
      setShowForm(false);
    },
  });

  const updateAppointmentMutation = useMutation({
    mutationFn: (data: any) => updateAppointment(data.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["appointments"] });
      setEditingId(null);
      setShowForm(false);
    },
  });

  const deleteAppointmentMutation = useMutation({
    mutationFn: deleteAppointment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["appointments"] });
    },
  });

  const handlePatientSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data = Object.fromEntries(formData);

    if (editingId) {
      updatePatientMutation.mutate({ ...data, id: editingId } as any);
    } else {
      createPatientMutation.mutate(data);
    }
  };

  const handleAppointmentSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data = Object.fromEntries(formData);

    if (editingId) {
      updateAppointmentMutation.mutate({ ...data, id: editingId } as any);
    } else {
      createAppointmentMutation.mutate(data);
    }
  };

  const filteredPatients = patients.filter((p: Patient) =>
    `${p.firstName} ${p.lastName}`.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const filteredAppointments = appointments.filter((a: Appointment) =>
    a.reason.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Patient Management System</h1>
        <p>Manage patients and appointments efficiently</p>
      </header>

      <div className="dashboard-tabs">
        <button
          className={`tab-btn ${activeTab === "patients" ? "active" : ""}`}
          onClick={() => {
            setActiveTab("patients");
            setShowForm(false);
            setEditingId(null);
          }}
        >
          👥 Patients
        </button>
        <button
          className={`tab-btn ${activeTab === "appointments" ? "active" : ""}`}
          onClick={() => {
            setActiveTab("appointments");
            setShowForm(false);
            setEditingId(null);
          }}
        >
          📅 Appointments
        </button>
      </div>

      <div className="dashboard-content">
        {activeTab === "patients" && (
          <section className="section">
            <div className="section-header">
              <h2>Patient Management</h2>
              <button
                className="btn btn-primary"
                onClick={() => {
                  setShowForm(!showForm);
                  setEditingId(null);
                }}
              >
                {showForm ? "Cancel" : "+ Add Patient"}
              </button>
            </div>

            {showForm && (
              <div className="form-container">
                <h3>{editingId ? "Edit Patient" : "Add New Patient"}</h3>
                <form onSubmit={handlePatientSubmit}>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>First Name</label>
                      <input
                        type="text"
                        name="firstName"
                        placeholder="John"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Last Name</label>
                      <input
                        type="text"
                        name="lastName"
                        placeholder="Doe"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Email</label>
                      <input
                        type="email"
                        name="email"
                        placeholder="john@example.com"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Phone</label>
                      <input
                        type="tel"
                        name="phone"
                        placeholder="+1234567890"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Date of Birth</label>
                      <input type="date" name="dateOfBirth" required />
                    </div>
                    <div className="form-group">
                      <label>Address</label>
                      <input
                        type="text"
                        name="address"
                        placeholder="123 Main St"
                        required
                      />
                    </div>
                  </div>
                  <button type="submit" className="btn btn-success">
                    {editingId ? "Update Patient" : "Create Patient"}
                  </button>
                </form>
              </div>
            )}

            <div className="search-container">
              <input
                type="text"
                placeholder="Search patients by name..."
                className="search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            {patientsLoading ? (
              <div className="loading">Loading patients...</div>
            ) : filteredPatients.length === 0 ? (
              <div className="empty-state">No patients found</div>
            ) : (
              <div className="table-container">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Phone</th>
                      <th>Date of Birth</th>
                      <th>Address</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPatients.map((patient: Patient) => (
                      <tr key={patient.id}>
                        <td>{`${patient.firstName} ${patient.lastName}`}</td>
                        <td>{patient.email}</td>
                        <td>{patient.phone}</td>
                        <td>{patient.dateOfBirth}</td>
                        <td>{patient.address}</td>
                        <td>
                          <button
                            className="btn btn-sm btn-edit"
                            onClick={() => {
                              setEditingId(patient.id);
                              setShowForm(true);
                            }}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-sm btn-delete"
                            onClick={() => deletePatientMutation.mutate(patient.id)}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}

        {activeTab === "appointments" && (
          <section className="section">
            <div className="section-header">
              <h2>Appointment Management</h2>
              <button
                className="btn btn-primary"
                onClick={() => {
                  setShowForm(!showForm);
                  setEditingId(null);
                }}
              >
                {showForm ? "Cancel" : "+ Add Appointment"}
              </button>
            </div>

            {showForm && (
              <div className="form-container">
                <h3>{editingId ? "Edit Appointment" : "Add New Appointment"}</h3>
                <form onSubmit={handleAppointmentSubmit}>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Patient ID</label>
                      <input
                        type="text"
                        name="patientId"
                        placeholder="Patient ID"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Appointment Date</label>
                      <input type="date" name="appointmentDate" required />
                    </div>
                    <div className="form-group">
                      <label>Appointment Time</label>
                      <input type="time" name="appointmentTime" required />
                    </div>
                    <div className="form-group">
                      <label>Reason</label>
                      <input
                        type="text"
                        name="reason"
                        placeholder="Reason for appointment"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Status</label>
                      <select name="status" required>
                        <option value="">Select Status</option>
                        <option value="scheduled">Scheduled</option>
                        <option value="completed">Completed</option>
                        <option value="cancelled">Cancelled</option>
                      </select>
                    </div>
                  </div>
                  <button type="submit" className="btn btn-success">
                    {editingId ? "Update Appointment" : "Create Appointment"}
                  </button>
                </form>
              </div>
            )}

            <div className="search-container">
              <input
                type="text"
                placeholder="Search appointments by reason..."
                className="search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            {appointmentsLoading ? (
              <div className="loading">Loading appointments...</div>
            ) : filteredAppointments.length === 0 ? (
              <div className="empty-state">No appointments found</div>
            ) : (
              <div className="table-container">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Patient ID</th>
                      <th>Date</th>
                      <th>Time</th>
                      <th>Reason</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredAppointments.map((appointment: Appointment) => (
                      <tr key={appointment.id}>
                        <td>{appointment.patientId}</td>
                        <td>{appointment.appointmentDate}</td>
                        <td>{appointment.appointmentTime}</td>
                        <td>{appointment.reason}</td>
                        <td>
                          <span
                            className={`status status-${appointment.status.toLowerCase()}`}
                          >
                            {appointment.status}
                          </span>
                        </td>
                        <td>
                          <button
                            className="btn btn-sm btn-edit"
                            onClick={() => {
                              setEditingId(appointment.id);
                              setShowForm(true);
                            }}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-sm btn-delete"
                            onClick={() =>
                              deleteAppointmentMutation.mutate(appointment.id)
                            }
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}
      </div>
    </div>
  );
}
