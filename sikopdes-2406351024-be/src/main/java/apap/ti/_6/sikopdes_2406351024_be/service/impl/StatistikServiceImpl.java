package apap.ti._6.sikopdes_2406351024_be.service.impl;

import apap.ti._6.sikopdes_2406351024_be.dto.response.StatistikResponse;
import apap.ti._6.sikopdes_2406351024_be.dto.response.TrenSimpananDto;
import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;
import apap.ti._6.sikopdes_2406351024_be.repository.AnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.SimpananRepository;
import apap.ti._6.sikopdes_2406351024_be.service.StatistikService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class StatistikServiceImpl implements StatistikService {

    private final AnggotaRepository anggotaRepository;
    private final SimpananRepository simpananRepository;

    public StatistikServiceImpl(AnggotaRepository anggotaRepository, SimpananRepository simpananRepository) {
        this.anggotaRepository = anggotaRepository;
        this.simpananRepository = simpananRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StatistikResponse getStatistik(Integer tahunInput, String periodeTypeInput, String filterValueInput) {
        int tahun = (tahunInput != null) ? tahunInput : LocalDate.now().getYear();
        String periodeType = (periodeTypeInput != null && !periodeTypeInput.isBlank()) ? periodeTypeInput.toUpperCase() : "ALL";
        String filterValue = (filterValueInput != null && !filterValueInput.isBlank()) ? filterValueInput : "ALL";

        LocalDate startDate;
        LocalDate endDate;

        if ("MONTHLY".equals(periodeType) && !"ALL".equalsIgnoreCase(filterValue)) {
            try {
                int month = Integer.parseInt(filterValue);
                startDate = LocalDate.of(tahun, month, 1);
                endDate = startDate.plusMonths(1).minusDays(1);
            } catch (Exception e) {
                startDate = LocalDate.of(tahun, 1, 1);
                endDate = LocalDate.of(tahun, 12, 31);
            }
        } else if ("QUARTERLY".equals(periodeType) && !"ALL".equalsIgnoreCase(filterValue)) {
            int startMonth;
            int endMonth;
            switch (filterValue.toUpperCase()) {
                case "Q1":
                    startMonth = 1;
                    endMonth = 3;
                    break;
                case "Q2":
                    startMonth = 4;
                    endMonth = 6;
                    break;
                case "Q3":
                    startMonth = 7;
                    endMonth = 9;
                    break;
                case "Q4":
                    startMonth = 10;
                    endMonth = 12;
                    break;
                default:
                    startMonth = 1;
                    endMonth = 12;
                    break;
            }
            startDate = LocalDate.of(tahun, startMonth, 1);
            endDate = LocalDate.of(tahun, endMonth, Month.of(endMonth).length(LocalDate.of(tahun, endMonth, 1).isLeapYear()));
        } else {
            startDate = LocalDate.of(tahun, 1, 1);
            endDate = LocalDate.of(tahun, 12, 31);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 1. Anggota baru dihitung dari tanggalDaftar pada periode terpilih
        List<Anggota> allAnggota = anggotaRepository.findAll();
        long anggotaBaruCount = allAnggota.stream()
                .filter(a -> a.getTanggalDaftar() != null &&
                        !a.getTanggalDaftar().isBefore(startDateTime) &&
                        !a.getTanggalDaftar().isAfter(endDateTime))
                .count();

        // 2. Anggota menurut status kondisi saat ini (current snapshot)
        Map<String, Long> anggotaByStatus = new LinkedHashMap<>();
        String[] statuses = {
                "MENUNGGU_VERIFIKASI",
                "MENUNGGU_SETORAN_POKOK",
                "AKTIF",
                "DIBEKUKAN",
                "NONAKTIF",
                "DITOLAK",
                "KELUAR"
        };
        for (String s : statuses) {
            anggotaByStatus.put(s, anggotaRepository.countByStatusKeanggotaan(s));
        }

        // 3. Total simpanan per jenis dihitung dari tanggalSetor pada periode terpilih
        List<Simpanan> allSimpanan = simpananRepository.findAll();
        long totalPokok = 0L;
        long totalWajib = 0L;
        long totalSukarela = 0L;

        for (Simpanan s : allSimpanan) {
            if (s.getTanggalSetor() != null &&
                    !s.getTanggalSetor().isBefore(startDate) &&
                    !s.getTanggalSetor().isAfter(endDate)) {
                if ("POKOK".equalsIgnoreCase(s.getJenisSimpanan())) {
                    totalPokok += s.getNominal();
                } else if ("WAJIB".equalsIgnoreCase(s.getJenisSimpanan())) {
                    totalWajib += s.getNominal();
                } else if ("SUKARELA".equalsIgnoreCase(s.getJenisSimpanan())) {
                    totalSukarela += s.getNominal();
                }
            }
        }

        Map<String, Long> totalSimpananByJenis = new LinkedHashMap<>();
        totalSimpananByJenis.put("POKOK", totalPokok);
        totalSimpananByJenis.put("WAJIB", totalWajib);
        totalSimpananByJenis.put("SUKARELA", totalSukarela);
        totalSimpananByJenis.put("TOTAL", totalPokok + totalWajib + totalSukarela);

        // 4. Tren simpanan per periode (12 bulan pada tahun terpilih)
        List<TrenSimpananDto> trenList = new ArrayList<>();
        Locale idLocale = Locale.of("id", "ID");
        for (int m = 1; m <= 12; m++) {
            Month monthEnum = Month.of(m);
            LocalDate mStart = LocalDate.of(tahun, m, 1);
            LocalDate mEnd = mStart.plusMonths(1).minusDays(1);

            long mPokok = 0L;
            long mWajib = 0L;
            long mSukarela = 0L;

            for (Simpanan s : allSimpanan) {
                if (s.getTanggalSetor() != null &&
                        !s.getTanggalSetor().isBefore(mStart) &&
                        !s.getTanggalSetor().isAfter(mEnd)) {
                    if ("POKOK".equalsIgnoreCase(s.getJenisSimpanan())) {
                        mPokok += s.getNominal();
                    } else if ("WAJIB".equalsIgnoreCase(s.getJenisSimpanan())) {
                        mWajib += s.getNominal();
                    } else if ("SUKARELA".equalsIgnoreCase(s.getJenisSimpanan())) {
                        mSukarela += s.getNominal();
                    }
                }
            }

            String label = monthEnum.getDisplayName(TextStyle.SHORT, idLocale);
            trenList.add(TrenSimpananDto.builder()
                    .label(label)
                    .totalPokok(mPokok)
                    .totalWajib(mWajib)
                    .totalSukarela(mSukarela)
                    .total(mPokok + mWajib + mSukarela)
                    .build());
        }

        return StatistikResponse.builder()
                .tahun(tahun)
                .periodeType(periodeType)
                .filterValue(filterValue)
                .anggotaBaruCount(anggotaBaruCount)
                .anggotaByStatus(anggotaByStatus)
                .totalSimpananByJenis(totalSimpananByJenis)
                .trenSimpanan(trenList)
                .build();
    }
}
