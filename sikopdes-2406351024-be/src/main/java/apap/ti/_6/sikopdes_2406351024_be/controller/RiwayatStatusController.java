package apap.ti._6.sikopdes_2406351024_be.controller;

import apap.ti._6.sikopdes_2406351024_be.dto.response.BaseResponse;
import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import apap.ti._6.sikopdes_2406351024_be.service.RiwayatStatusService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/riwayat-status", "/riwayat-status"})
public class RiwayatStatusController {

    private final RiwayatStatusService riwayatStatusService;

    public RiwayatStatusController(RiwayatStatusService riwayatStatusService) {
        this.riwayatStatusService = riwayatStatusService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<RiwayatStatusAnggota>>> getAllRiwayat(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "statusBaru", required = false) String statusBaru,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<RiwayatStatusAnggota> list = riwayatStatusService.getAllRiwayat(search, statusBaru, startDate, endDate);
        return ResponseEntity.ok(BaseResponse.success(list, "Berhasil mengambil riwayat perubahan status"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<RiwayatStatusAnggota>> getRiwayatById(@PathVariable("id") String id) {
        RiwayatStatusAnggota riwayat = riwayatStatusService.getRiwayatById(id);
        return ResponseEntity.ok(BaseResponse.success(riwayat, "Berhasil mengambil detail riwayat status"));
    }
}
