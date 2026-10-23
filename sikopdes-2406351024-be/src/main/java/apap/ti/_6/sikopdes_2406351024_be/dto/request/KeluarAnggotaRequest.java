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
public class KeluarAnggotaRequest {

    @NotBlank(message = "Alasan keluar wajib diisi")
    private String alasan;
}
