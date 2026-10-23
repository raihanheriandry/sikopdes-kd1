package apap.ti._6.sikopdes_2406351024_be.controller;

import apap.ti._6.sikopdes_2406351024_be.dto.response.BaseResponse;
import apap.ti._6.sikopdes_2406351024_be.dto.response.KepatuhanResponse;
import apap.ti._6.sikopdes_2406351024_be.model.PemeriksaanKepatuhan;
import apap.ti._6.sikopdes_2406351024_be.service.KepatuhanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/simpanan/kepatuhan", "/simpanan/kepatuhan"})
public class KepatuhanController {

    private final KepatuhanService kepatuhanService;

    public KepatuhanController(KepatuhanService kepatuhanService) {
        this.kepatuhanService = kepatuhanService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<KepatuhanResponse>> getKepatuhanData(
            @RequestParam(value = "bulanAcuan", required = false) String bulanAcuan
    ) {
        KepatuhanResponse response = kepatuhanService.getKepatuhanData(bulanAcuan);
        return ResponseEntity.ok(BaseResponse.success(response, "Berhasil mengambil data kepatuhan simpanan wajib"));
    }

    @PostMapping("/trigger")
    public ResponseEntity<BaseResponse<PemeriksaanKepatuhan>> triggerJobManual() {
        PemeriksaanKepatuhan pkp = kepatuhanService.executeComplianceJob();
        return ResponseEntity.ok(BaseResponse.success(pkp, "Job pemeriksaan kepatuhan berhasil dijalankan secara manual"));
    }
}
