package apap.ti._6.sikopdes_2406351024_be.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnggotaRequest {

    @NotBlank(message = "Nama wajib diisi")
    private String nama;

    @NotBlank(message = "NIK wajib diisi")
    @Pattern(regexp = "^\\d{16}$", message = "NIK harus terdiri dari tepat 16 digit angka")
    private String nik;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Nomor telepon wajib diisi")
    @Pattern(regexp = "^\\d{10,15}$", message = "Nomor telepon harus terdiri dari 10 sampai 15 digit angka")
    private String telepon;

    @NotBlank(message = "Alamat wajib diisi")
    private String alamat;
}
