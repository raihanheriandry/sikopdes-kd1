package apap.ti._6.sikopdes_2406351024_be.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSimpananPokokRequest {

    @NotBlank(message = "ID Anggota wajib diisi")
    private String anggotaId;

    @NotNull(message = "Nominal wajib diisi")
    @Min(value = 100000, message = "Nominal simpanan pokok minimal Rp100.000")
    private Long nominal;

    @NotNull(message = "Tanggal setor wajib diisi")
    private LocalDate tanggalSetor;
}
