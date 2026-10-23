package apap.ti._6.sikopdes_2406351024_be.controller;

import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.KeluarAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.UpdateAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.UpdateStatusAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.VerifikasiAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.response.BaseResponse;
import apap.ti._6.sikopdes_2406351024_be.dto.response.DetailAnggotaResponse;
import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import apap.ti._6.sikopdes_2406351024_be.service.AnggotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/anggota", "/anggota"})
public class AnggotaController {

    private final AnggotaService anggotaService;

    public AnggotaController(AnggotaService anggotaService) {
        this.anggotaService = anggotaService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<Anggota>>> getAllAnggota(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status
    ) {
        List<Anggota> list = anggotaService.getAllAnggota(search, status);
        return ResponseEntity.ok(BaseResponse.success(list, "Berhasil mengambil data seluruh anggota"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<DetailAnggotaResponse>> getAnggotaById(@PathVariable("id") String id) {
        DetailAnggotaResponse detail = anggotaService.getAnggotaById(id);
        return ResponseEntity.ok(BaseResponse.success(detail, "Berhasil mengambil detail anggota"));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Anggota>> createAnggota(@Valid @RequestBody CreateAnggotaRequest request) {
        Anggota created = anggotaService.createAnggota(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created(created, "Calon anggota berhasil didaftarkan"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Anggota>> updateAnggota(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateAnggotaRequest request
    ) {
        Anggota updated = anggotaService.updateAnggota(id, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Data anggota berhasil diperbarui"));
    }

    @PostMapping("/{id}/verifikasi")
    public ResponseEntity<BaseResponse<Anggota>> verifikasiAnggota(
            @PathVariable("id") String id,
            @Valid @RequestBody VerifikasiAnggotaRequest request
    ) {
        Anggota updated = anggotaService.verifikasiAnggota(id, request);
        String actionMsg = "VERIFIKASI".equalsIgnoreCase(request.getAction())
                ? "Anggota berhasil diverifikasi"
                : "Pendaftaran anggota berhasil ditolak";
        return ResponseEntity.ok(BaseResponse.success(updated, actionMsg));
    }

    @DeleteMapping("/{id}/keluar")
    public ResponseEntity<BaseResponse<Anggota>> keluarAnggota(
            @PathVariable("id") String id,
            @Valid @RequestBody KeluarAnggotaRequest request
    ) {
        Anggota updated = anggotaService.keluarAnggota(id, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Anggota berhasil dikeluarkan"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BaseResponse<Anggota>> updateStatusKeanggotaan(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateStatusAnggotaRequest request
    ) {
        Anggota updated = anggotaService.updateStatusKeanggotaan(id, request);
        String actionMsg = "DIBEKUKAN".equalsIgnoreCase(request.getStatus())
                ? "Status anggota berhasil dibekukan"
                : "Anggota berhasil diaktifkan kembali";
        return ResponseEntity.ok(BaseResponse.success(updated, actionMsg));
    }
}
