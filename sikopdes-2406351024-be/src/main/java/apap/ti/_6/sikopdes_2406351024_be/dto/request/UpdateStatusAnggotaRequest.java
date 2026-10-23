package apap.ti._6.sikopdes_2406351024_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusAnggotaRequest {

    /**
     * Target status: "DIBEKUKAN" or "AKTIF"
     */
    @NotBlank(message = "Status baru wajib ditentukan (DIBEKUKAN atau AKTIF)")
    private String status;

    /**
     * Wajib jika status baru adalah DIBEKUKAN, opsional untuk perubahan lainnya.
     */
    private String alasan;
}
