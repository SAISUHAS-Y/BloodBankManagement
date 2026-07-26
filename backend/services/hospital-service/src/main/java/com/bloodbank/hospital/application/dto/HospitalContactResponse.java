package com.bloodbank.hospital.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalContactResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String designation;
    private String phone;
    private String email;
}
