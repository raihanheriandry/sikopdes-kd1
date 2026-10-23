package apap.ti._6.sikopdes_2406351024_be.dto.request;

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
public class CreateSimpananRequest {

    @NotBlank(message = "ID Anggota wajib diisi")
    private String anggotaId;

    @NotBlank(message = "Jenis simpanan wajib diisi (WAJIB atau SUKARELA)")
    private String jenisSimpanan;

    @NotNull(message = "Nominal wajib diisi")
    private Long nominal;

    @NotNull(message = "Tanggal setor wajib diisi")
    private LocalDate tanggalSetor;

    /**
     * Format "YYYY-MM", wajib untuk WAJIB, harus null/blank untuk SUKARELA.
     */
    private String periode;
}
