package apap.ti._6.sikopdes_2406351024_be.dto.response;

import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailAnggotaResponse {
    private String id;
    private String nomorAnggota;
    private String nama;
    private String nik;
    private String email;
    private String telepon;
    private String alamat;
    private LocalDateTime tanggalDaftar;
    private LocalDateTime tanggalAktivasi;
    private String statusKeanggotaan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long totalSimpananPokok;
    private Long totalSimpananWajib;
    private Long totalSimpananSukarela;
    private Long totalSimpanan;

    private List<Simpanan> listSimpanan;
    private List<RiwayatStatusAnggota> listRiwayatStatus;
}
