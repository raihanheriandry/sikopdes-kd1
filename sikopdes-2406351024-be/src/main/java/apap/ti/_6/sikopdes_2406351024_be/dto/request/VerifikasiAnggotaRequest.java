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
public class VerifikasiAnggotaRequest {

    /**
     * Action can be "VERIFIKASI" (status becomes MENUNGGU_SETORAN_POKOK)
     * or "TOLAK" (status becomes DITOLAK).
     */
    @NotBlank(message = "Aksi verifikasi harus ditentukan (VERIFIKASI atau TOLAK)")
    private String action;

    /**
     * Mandatory if action is "TOLAK".
     */
    private String alasan;
}
