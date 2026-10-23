package apap.ti._6.sikopdes_2406351024_be.dto.response;

import apap.ti._6.sikopdes_2406351024_be.model.PemeriksaanKepatuhan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KepatuhanResponse {
    private String bulanAcuan;
    private List<String> periodeEvaluasi;
    private List<PemeriksaanKepatuhan> ringkasanEksekusi;
    private List<AnggotaKepatuhanDto> daftarAnggota;
}
