package com.pm.patientservice.dto;

import java.util.List;

public class PagedPatientResponseDTO {
    private List<PatientResponseDTO> patients;
    private int page;
    private int size;
    private int totalPages;
    private int totalElements;

    public PagedPatientResponseDTO() {

    }

    public PagedPatientResponseDTO(
            List<PatientResponseDTO> patients,
            int page,
            int size,
            int totalPages,
            int totalElements) {
        this.patients = patients;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public int getPage() {
        return page;
    }

    public PagedPatientResponseDTO setPage(int page) {
        this.page = page;
        return this;
    }

    public List<PatientResponseDTO> getPatients() {
        return patients;
    }

    public PagedPatientResponseDTO setPatients(List<PatientResponseDTO> patients) {
        this.patients = patients;
        return this;
    }

    public int getSize() {
        return size;
    }

    public PagedPatientResponseDTO setSize(int size) {
        this.size = size;
        return this;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public PagedPatientResponseDTO setTotalPages(int totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public PagedPatientResponseDTO setTotalElements(int totalElements) {
        this.totalElements = totalElements;
        return this;
    }
}
