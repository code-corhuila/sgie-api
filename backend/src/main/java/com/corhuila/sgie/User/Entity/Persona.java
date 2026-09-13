package com.corhuila.sgie.User.Entity;

import com.corhuila.sgie.Booking.Entity.Reserva;
import com.corhuila.sgie.common.Auditoria;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "persona")
public class Persona extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombres;
    private String apellidos;
    private String tipoDocumento;
    @NotBlank(message = "El número de identificación es obligatorio")
    @Column(name = "numero_identificacion", unique = true, nullable = false)
    private String numeroIdentificacion;
    private String telefonoMovil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @OneToOne(mappedBy = "persona", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    @JsonIgnore
    private Usuario usuario;

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Reserva> reservas = new HashSet<>();

    // equals/hashCode SOLO por id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Persona)) return false;
        Persona that = (Persona) o;
        return this.getId() != null && this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
