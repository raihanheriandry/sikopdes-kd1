package apap.ti._6.sikopdes_2406351024_be.controller;

import apap.ti._6.sikopdes_2406351024_be.dto.response.BaseResponse;
import apap.ti._6.sikopdes_2406351024_be.dto.response.StatistikResponse;
import apap.ti._6.sikopdes_2406351024_be.service.StatistikService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/statistik", "/statistik"})
public class StatistikController {

    private final StatistikService statistikService;

    public StatistikController(StatistikService statistikService) {
        this.statistikService = statistikService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<StatistikResponse>> getStatistik(
            @RequestParam(value = "tahun", required = false) Integer tahun,
            @RequestParam(value = "periodeType", required = false) String periodeType,
            @RequestParam(value = "filterValue", required = false) String filterValue
    ) {
        StatistikResponse response = statistikService.getStatistik(tahun, periodeType, filterValue);
        return ResponseEntity.ok(BaseResponse.success(response, "Berhasil mengambil data statistik"));
    }
}
