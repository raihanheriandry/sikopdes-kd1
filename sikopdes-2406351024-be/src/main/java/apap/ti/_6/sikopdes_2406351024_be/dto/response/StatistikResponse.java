package apap.ti._6.sikopdes_2406351024_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistikResponse {
    private int tahun;
    private String periodeType; // "ALL", "MONTHLY", "QUARTERLY"
    private String filterValue; // e.g. "Q1", "09", or "ALL"
    private long anggotaBaruCount;
    private Map<String, Long> anggotaByStatus;
    private Map<String, Long> totalSimpananByJenis;
    private List<TrenSimpananDto> trenSimpanan;
}
