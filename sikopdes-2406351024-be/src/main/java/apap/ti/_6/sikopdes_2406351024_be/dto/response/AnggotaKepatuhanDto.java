package apap.ti._6.sikopdes_2406351024_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnggotaKepatuhanDto {
    private String id;
    private String nomorAnggota;
    private String nama;
    private String statusKeanggotaan;
    private LocalDateTime tanggalAktivasi;
    private Map<String, Boolean> statusBayarPerPeriode;
    private String hasilKepatuhan;
}
