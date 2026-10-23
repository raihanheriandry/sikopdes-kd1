package apap.ti._6.sikopdes_2406351024_be.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "riwayat_status_anggota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiwayatStatusAnggota {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "anggota_id", nullable = false)
    private Anggota anggota;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pemeriksaan_id")
    private PemeriksaanKepatuhan pemeriksaan;

    @Column(name = "status_sebelumnya", length = 30)
    private String statusSebelumnya;

    @Column(name = "status_baru", length = 30, nullable = false)
    private String statusBaru;

    @Column(name = "tanggal_perubahan", nullable = false)
    private LocalDateTime tanggalPerubahan;

    @Column(name = "alasan", columnDefinition = "TEXT")
    private String alasan;

    @Column(name = "diubah_oleh", length = 20, nullable = false)
    private String diubahOleh;

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

    @JsonProperty("pemeriksaanId")
    public String getPemeriksaanId() {
        return pemeriksaan != null ? pemeriksaan.getId() : null;
    }
}
