package apap.ti._6.sikopdes_2406351024_be.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pemeriksaan_kepatuhan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PemeriksaanKepatuhan {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "waktu_eksekusi", nullable = false)
    private LocalDateTime waktuEksekusi;

    @Column(name = "periode_awal", length = 7, nullable = false)
    private String periodeAwal;

    @Column(name = "periode_akhir", length = 7, nullable = false)
    private String periodeAkhir;

    @Column(name = "jumlah_diperiksa", nullable = false)
    private int jumlahDiperiksa;

    @Column(name = "jumlah_dinonaktifkan", nullable = false)
    private int jumlahDinonaktifkan;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "pemeriksaan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RiwayatStatusAnggota> listRiwayatStatus = new ArrayList<>();
}
