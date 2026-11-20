package com.jairocodes.portfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue
    private Integer comentario_id;
    @NotBlank
    @Column(name = "comentario")
    private String comentario;
    @Column(name = "fecha_comentario")
    private LocalDateTime fecha = LocalDateTime.now();
    @NotBlank
    @Column(name = "nombre_usuario")
    private String nombreUser;
    @NotBlank
    @Email
    @Column(name = "email_usuario")
    private String emailUser;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_project")
    private Project project;

}

