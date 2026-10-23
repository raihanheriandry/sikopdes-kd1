package apap.ti._6.sikopdes_2406351024_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrenSimpananDto {
    private String label;
    private long totalPokok;
    private long totalWajib;
    private long totalSukarela;
    private long total;
}
