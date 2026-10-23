package apap.ti._6.sikopdes_2406351024_be.service.impl;

import apap.ti._6.sikopdes_2406351024_be.dto.response.AnggotaKepatuhanDto;
import apap.ti._6.sikopdes_2406351024_be.dto.response.KepatuhanResponse;
import apap.ti._6.sikopdes_2406351024_be.exception.BadRequestException;
import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import apap.ti._6.sikopdes_2406351024_be.model.PemeriksaanKepatuhan;
import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import apap.ti._6.sikopdes_2406351024_be.repository.AnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.PemeriksaanKepatuhanRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.RiwayatStatusAnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.SimpananRepository;
import apap.ti._6.sikopdes_2406351024_be.service.KepatuhanService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KepatuhanServiceImpl implements KepatuhanService {

    private final AnggotaRepository anggotaRepository;
    private final SimpananRepository simpananRepository;
    private final PemeriksaanKepatuhanRepository pemeriksaanKepatuhanRepository;
    private final RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository;

    public KepatuhanServiceImpl(
            AnggotaRepository anggotaRepository,
            SimpananRepository simpananRepository,
            PemeriksaanKepatuhanRepository pemeriksaanKepatuhanRepository,
            RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository
    ) {
        this.anggotaRepository = anggotaRepository;
        this.simpananRepository = simpananRepository;
        this.pemeriksaanKepatuhanRepository = pemeriksaanKepatuhanRepository;
        this.riwayatStatusAnggotaRepository = riwayatStatusAnggotaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public KepatuhanResponse getKepatuhanData(String bulanAcuanStr) {
        YearMonth acuan;
        if (bulanAcuanStr == null || bulanAcuanStr.trim().isEmpty()) {
            acuan = YearMonth.now();
        } else {
            try {
                acuan = YearMonth.parse(bulanAcuanStr.trim());
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Format bulan acuan harus YYYY-MM");
            }
        }

        // Tiga periode selesai sebelum bulan acuan (cth: jika Oktober 2026 -> Juli, Agustus, September 2026)
        String p1 = acuan.minusMonths(3).toString();
        String p2 = acuan.minusMonths(2).toString();
        String p3 = acuan.minusMonths(1).toString();
        List<String> tigaPeriode = List.of(p1, p2, p3);

        LocalDateTime startOfP1 = acuan.minusMonths(3).atDay(1).atStartOfDay();

        // Anggota berstatus Aktif dan Nonaktif
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Anggota> anggotaAktif = anggotaRepository.findByStatusKeanggotaan("AKTIF", sort);
        List<Anggota> anggotaNonaktif = anggotaRepository.findByStatusKeanggotaan("NONAKTIF", sort);

        List<Anggota> targetList = new ArrayList<>(anggotaAktif);
        targetList.addAll(anggotaNonaktif);
        targetList.sort((a, b) -> a.getId().compareTo(b.getId()));

        List<AnggotaKepatuhanDto> dtoList = new ArrayList<>();
        for (Anggota a : targetList) {
            Map<String, Boolean> statusBayar = new LinkedHashMap<>();
            boolean allPaid = true;

            for (String periode : tigaPeriode) {
                boolean paid = simpananRepository.existsByAnggota_IdAndJenisSimpananAndPeriode(a.getId(), "WAJIB", periode);
                statusBayar.put(periode, paid);
                if (!paid) {
                    allPaid = false;
                }
            }

            String hasilKepatuhan;
            if (a.getTanggalAktivasi() == null || a.getTanggalAktivasi().isAfter(startOfP1)) {
                hasilKepatuhan = "BELUM_DIEVALUASI";
            } else if (allPaid) {
                hasilKepatuhan = "PATUH";
            } else {
                hasilKepatuhan = "MENUNGGAK";
            }

            dtoList.add(AnggotaKepatuhanDto.builder()
                    .id(a.getId())
                    .nomorAnggota(a.getNomorAnggota())
                    .nama(a.getNama())
                    .statusKeanggotaan(a.getStatusKeanggotaan())
                    .tanggalAktivasi(a.getTanggalAktivasi())
                    .statusBayarPerPeriode(statusBayar)
                    .hasilKepatuhan(hasilKepatuhan)
                    .build());
        }

        List<PemeriksaanKepatuhan> ringkasanEksekusi = pemeriksaanKepatuhanRepository.findTop5ByOrderByWaktuEksekusiDesc();

        return KepatuhanResponse.builder()
                .bulanAcuan(acuan.toString())
                .periodeEvaluasi(tigaPeriode)
                .ringkasanEksekusi(ringkasanEksekusi)
                .daftarAnggota(dtoList)
                .build();
    }

    @Override
    @Transactional
    public PemeriksaanKepatuhan executeComplianceJob() {
        YearMonth nowYm = YearMonth.now();

        String p1 = nowYm.minusMonths(3).toString();
        String p2 = nowYm.minusMonths(2).toString();
        String p3 = nowYm.minusMonths(1).toString();
        List<String> tigaPeriode = List.of(p1, p2, p3);

        LocalDateTime startOfP1 = nowYm.minusMonths(3).atDay(1).atStartOfDay();

        // Hanya anggota berstatus Aktif yang dievaluasi
        List<Anggota> anggotaAktif = anggotaRepository.findByStatusKeanggotaan("AKTIF", Sort.by(Sort.Direction.ASC, "id"));

        int jumlahDiperiksa = 0;
        int jumlahDinonaktifkan = 0;
        LocalDateTime now = LocalDateTime.now();

        // Generate PKP ID
        String pkpId = generateNextPemeriksaanId();
        PemeriksaanKepatuhan pemeriksaan = PemeriksaanKepatuhan.builder()
                .id(pkpId)
                .waktuEksekusi(now)
                .periodeAwal(p1)
                .periodeAkhir(p3)
                .jumlahDiperiksa(0)
                .jumlahDinonaktifkan(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        pemeriksaan = pemeriksaanKepatuhanRepository.save(pemeriksaan);

        for (Anggota a : anggotaAktif) {
            // Hanya dievaluasi jika tanggalAktivasi jatuh sebelum atau tepat pada awal periode pertama
            if (a.getTanggalAktivasi() != null && !a.getTanggalAktivasi().isAfter(startOfP1)) {
                jumlahDiperiksa++;

                List<String> tunggakan = new ArrayList<>();
                for (String p : tigaPeriode) {
                    boolean paid = simpananRepository.existsByAnggota_IdAndJenisSimpananAndPeriode(a.getId(), "WAJIB", p);
                    if (!paid) {
                        tunggakan.add(p);
                    }
                }

                if (!tunggakan.isEmpty()) {
                    jumlahDinonaktifkan++;
                    a.setStatusKeanggotaan("NONAKTIF");
                    a.setUpdatedAt(now);
                    anggotaRepository.save(a);

                    RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                            .id(generateNextRiwayatId())
                            .anggota(a)
                            .pemeriksaan(pemeriksaan)
                            .statusSebelumnya("AKTIF")
                            .statusBaru("NONAKTIF")
                            .tanggalPerubahan(now)
                            .alasan("Menunggak simpanan wajib periode " + String.join(", ", tunggakan))
                            .diubahOleh("SISTEM")
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    riwayatStatusAnggotaRepository.save(riwayat);
                }
            }
        }

        pemeriksaan.setJumlahDiperiksa(jumlahDiperiksa);
        pemeriksaan.setJumlahDinonaktifkan(jumlahDinonaktifkan);
        return pemeriksaanKepatuhanRepository.save(pemeriksaan);
    }

    private String generateNextPemeriksaanId() {
        List<String> ids = pemeriksaanKepatuhanRepository.findLatestPemeriksaanIds();
        int maxId = 0;
        for (String id : ids) {
            if (id != null && id.startsWith("PKP")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PKP%04d", maxId + 1);
    }

    private String generateNextRiwayatId() {
        List<String> ids = riwayatStatusAnggotaRepository.findLatestRiwayatIds();
        int maxId = 0;
        for (String id : ids) {
            if (id != null && id.startsWith("RST")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("RST%04d", maxId + 1);
    }
}
