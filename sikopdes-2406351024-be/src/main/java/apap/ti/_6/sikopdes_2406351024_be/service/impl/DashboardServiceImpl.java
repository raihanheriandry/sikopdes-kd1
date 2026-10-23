package apap.ti._6.sikopdes_2406351024_be.service.impl;

import apap.ti._6.sikopdes_2406351024_be.dto.response.DashboardStatsResponse;
import apap.ti._6.sikopdes_2406351024_be.repository.AnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.SimpananRepository;
import apap.ti._6.sikopdes_2406351024_be.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final AnggotaRepository anggotaRepository;
    private final SimpananRepository simpananRepository;

    public DashboardServiceImpl(AnggotaRepository anggotaRepository, SimpananRepository simpananRepository) {
        this.anggotaRepository = anggotaRepository;
        this.simpananRepository = simpananRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long waitingVerification = anggotaRepository.countByStatusKeanggotaan("MENUNGGU_VERIFIKASI");
        long waitingPokok = anggotaRepository.countByStatusKeanggotaan("MENUNGGU_SETORAN_POKOK");
        long active = anggotaRepository.countByStatusKeanggotaan("AKTIF");
        long frozen = anggotaRepository.countByStatusKeanggotaan("DIBEKUKAN");
        long inactive = anggotaRepository.countByStatusKeanggotaan("NONAKTIF");

        YearMonth currentYm = YearMonth.now();
        LocalDate startDate = currentYm.atDay(1);
        LocalDate endDate = currentYm.atEndOfMonth();

        Long currentMonthSimpanan = simpananRepository.sumNominalByTanggalSetorBetween(startDate, endDate);

        return DashboardStatsResponse.builder()
                .waitingVerificationCount(waitingVerification)
                .waitingPokokCount(waitingPokok)
                .activeCount(active)
                .frozenCount(frozen)
                .inactiveCount(inactive)
                .totalCurrentMonthSimpanan(currentMonthSimpanan != null ? currentMonthSimpanan : 0L)
                .build();
    }
}
