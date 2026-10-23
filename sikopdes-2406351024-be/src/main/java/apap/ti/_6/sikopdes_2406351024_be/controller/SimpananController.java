package apap.ti._6.sikopdes_2406351024_be.controller;

import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateSimpananPokokRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateSimpananRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.response.BaseResponse;
import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;
import apap.ti._6.sikopdes_2406351024_be.service.SimpananService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/simpanan", "/simpanan"})
public class SimpananController {

    private final SimpananService simpananService;

    public SimpananController(SimpananService simpananService) {
        this.simpananService = simpananService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<Simpanan>>> getAllSimpanan(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "jenis", required = false) String jenis,
            @RequestParam(value = "periode", required = false) String periode,
            @RequestParam(value = "nominalMin", required = false) Long nominalMin,
            @RequestParam(value = "nominalMax", required = false) Long nominalMax
    ) {
        List<Simpanan> list = simpananService.getAllSimpanan(search, jenis, periode, nominalMin, nominalMax);
        return ResponseEntity.ok(BaseResponse.success(list, "Berhasil mengambil seluruh transaksi simpanan"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<Simpanan>> getSimpananById(@PathVariable("id") String id) {
        Simpanan simpanan = simpananService.getSimpananById(id);
        return ResponseEntity.ok(BaseResponse.success(simpanan, "Berhasil mengambil detail transaksi simpanan"));
    }

    @PostMapping("/pokok")
    public ResponseEntity<BaseResponse<Simpanan>> createSimpananPokok(@Valid @RequestBody CreateSimpananPokokRequest request) {
        Simpanan created = simpananService.createSimpananPokok(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created(created, "Simpanan pokok berhasil dicatat dan status anggota telah aktif"));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Simpanan>> createSimpanan(@Valid @RequestBody CreateSimpananRequest request) {
        Simpanan created = simpananService.createSimpanan(request);
        String jenis = request.getJenisSimpanan() != null ? request.getJenisSimpanan().toUpperCase() : "";
        String msg = "Simpanan " + jenis.toLowerCase() + " berhasil dicatat";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created(created, msg));
    }
}
