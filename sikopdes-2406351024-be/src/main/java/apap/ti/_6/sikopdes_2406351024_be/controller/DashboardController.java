package apap.ti._6.sikopdes_2406351024_be.controller;

import apap.ti._6.sikopdes_2406351024_be.dto.response.BaseResponse;
import apap.ti._6.sikopdes_2406351024_be.dto.response.DashboardStatsResponse;
import apap.ti._6.sikopdes_2406351024_be.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/dashboard", "/dashboard", "/api/beranda", "/beranda"})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(BaseResponse.success(stats, "Berhasil mengambil ringkasan dashboard"));
    }
}
