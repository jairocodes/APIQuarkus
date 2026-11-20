package com.jairocodes.portfolio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "administradores")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    @Id
    @GeneratedValue
    private Integer admin_id;
    @Column(name = "nombre")
    private String nombre;
    /** Dato privado del administrador: se guarda en la base, pero no se expone en las respuestas de la API. */
    @JsonIgnore
    @Column(name = "correo_electronico")
    private String email;

}
