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
@Table(name = "anggota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anggota {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "nomor_anggota", length = 50, nullable = false, unique = true)
    private String nomorAnggota;

    @Column(name = "nama", nullable = false)
    private String nama;

    @Column(name = "nik", length = 16, nullable = false)
    private String nik;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "telepon", length = 20, nullable = false)
    private String telepon;

    @Column(name = "alamat", columnDefinition = "TEXT", nullable = false)
    private String alamat;

    @Column(name = "tanggal_daftar", nullable = false)
    private LocalDateTime tanggalDaftar;

    @Column(name = "tanggal_aktivasi")
    private LocalDateTime tanggalAktivasi;

    @Column(name = "status_keanggotaan", length = 30, nullable = false)
    private String statusKeanggotaan;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "anggota", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Simpanan> listSimpanan = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "anggota", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RiwayatStatusAnggota> listRiwayatStatus = new ArrayList<>();
}
