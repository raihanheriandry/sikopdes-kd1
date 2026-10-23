package apap.ti._6.sikopdes_2406351024_be.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "simpanan",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_simpanan_anggota_periode", columnNames = {"anggota_id", "periode"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simpanan {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "anggota_id", nullable = false)
    private Anggota anggota;

    @Column(name = "jenis_simpanan", length = 20, nullable = false)
    private String jenisSimpanan;

    @Column(name = "nominal", nullable = false)
    private Long nominal;

    @Column(name = "tanggal_setor", nullable = false)
    private LocalDate tanggalSetor;

    @Column(name = "periode", length = 7)
    private String periode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonProperty("anggotaId")
    public String getAnggotaId() {
        return anggota != null ? anggota.getId() : null;
    }

    @JsonProperty("nomorAnggota")
    public String getNomorAnggota() {
        return anggota != null ? anggota.getNomorAnggota() : null;
    }

    @JsonProperty("namaAnggota")
    public String getNamaAnggota() {
        return anggota != null ? anggota.getNama() : null;
    }
}
