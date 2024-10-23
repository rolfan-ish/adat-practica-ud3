package es.rolfan.dao.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ParticipacionId(
        @Column(name = "id_deportista") int idDeportista,
        @Column(name = "id_evento") int idEvento) {
}
