package apap.ti._6.sikopdes_2406351024_be.scheduler;

import apap.ti._6.sikopdes_2406351024_be.service.KepatuhanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ComplianceJob {

    private static final Logger log = LoggerFactory.getLogger(ComplianceJob.class);
    private final KepatuhanService kepatuhanService;

    public ComplianceJob(KepatuhanService kepatuhanService) {
        this.kepatuhanService = kepatuhanService;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Jakarta")
    public void runAutomaticComplianceCheck() {
        log.info("Memulai eksekusi otomatis job pemeriksaan kepatuhan simpanan wajib...");
        try {
            var result = kepatuhanService.executeComplianceJob();
            log.info("Job pemeriksaan selesai: ID={}, Diperiksa={}, Dinonaktifkan={}",
                    result.getId(), result.getJumlahDiperiksa(), result.getJumlahDinonaktifkan());
        } catch (Exception e) {
            log.error("Terjadi kesalahan saat mengeksekusi job pemeriksaan kepatuhan: {}", e.getMessage(), e);
        }
    }
}
