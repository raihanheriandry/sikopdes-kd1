package apap.ti._6.sikopdes_2406351024_be.service.impl;

import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateSimpananPokokRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateSimpananRequest;
import apap.ti._6.sikopdes_2406351024_be.exception.BadRequestException;
import apap.ti._6.sikopdes_2406351024_be.exception.ResourceNotFoundException;
import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;
import apap.ti._6.sikopdes_2406351024_be.repository.AnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.RiwayatStatusAnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.SimpananRepository;
import apap.ti._6.sikopdes_2406351024_be.service.SimpananService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimpananServiceImpl implements SimpananService {

    private final SimpananRepository simpananRepository;
    private final AnggotaRepository anggotaRepository;
    private final RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository;

    public SimpananServiceImpl(
            SimpananRepository simpananRepository,
            AnggotaRepository anggotaRepository,
            RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository
    ) {
        this.simpananRepository = simpananRepository;
        this.anggotaRepository = anggotaRepository;
        this.riwayatStatusAnggotaRepository = riwayatStatusAnggotaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Simpanan> getAllSimpanan(String search, String jenis, String periode, Long nominalMin, Long nominalMax) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim().toLowerCase() : null;
        String cleanJenis = (jenis != null && !jenis.trim().isEmpty() && !jenis.equalsIgnoreCase("ALL")) ? jenis.trim() : null;
        String cleanPeriode = (periode != null && !periode.trim().isEmpty() && !periode.equalsIgnoreCase("ALL")) ? periode.trim() : null;

        Specification<Simpanan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cleanJenis != null) {
                predicates.add(cb.equal(root.get("jenisSimpanan"), cleanJenis));
            }

            if (cleanPeriode != null) {
                predicates.add(cb.equal(root.get("periode"), cleanPeriode));
            }

            if (nominalMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("nominal"), nominalMin));
            }

            if (nominalMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("nominal"), nominalMax));
            }

            if (cleanSearch != null) {
                String pattern = "%" + cleanSearch + "%";
                Join<Simpanan, Anggota> anggotaJoin = root.join("anggota", JoinType.LEFT);
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("id")), pattern),
                        cb.like(cb.lower(anggotaJoin.get("nomorAnggota")), pattern),
                        cb.like(cb.lower(anggotaJoin.get("nama")), pattern)
                );
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "tanggalSetor");
        return simpananRepository.findAll(spec, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Simpanan getSimpananById(String id) {
        return simpananRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simpanan dengan ID " + id + " tidak ditemukan"));
    }

    @Override
    @Transactional
    public Simpanan createSimpananPokok(CreateSimpananPokokRequest request) {
        Anggota anggota = anggotaRepository.findById(request.getAnggotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan ID " + request.getAnggotaId() + " tidak ditemukan"));

        if (!"MENUNGGU_SETORAN_POKOK".equalsIgnoreCase(anggota.getStatusKeanggotaan())) {
            throw new BadRequestException("Simpanan pokok hanya dapat dicatat untuk anggota berstatus Menunggu Setoran Pokok");
        }

        if (request.getNominal() == null || request.getNominal() < 100000L) {
            throw new BadRequestException("Nominal simpanan pokok minimal Rp100.000");
        }

        if (request.getTanggalSetor() == null || request.getTanggalSetor().isAfter(LocalDate.now())) {
            throw new BadRequestException("Tanggal setor tidak boleh berada di masa depan");
        }

        if (simpananRepository.existsByAnggota_IdAndJenisSimpanan(anggota.getId(), "POKOK")) {
            throw new BadRequestException("Anggota ini sudah memiliki simpanan pokok");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. Simpan transaksi Simpanan POKOK
        String simpananId = generateNextSimpananId();
        Simpanan simpanan = Simpanan.builder()
                .id(simpananId)
                .anggota(anggota)
                .jenisSimpanan("POKOK")
                .nominal(request.getNominal())
                .tanggalSetor(request.getTanggalSetor())
                .periode(null)
                .createdAt(now)
                .updatedAt(now)
                .build();
        Simpanan savedSimpanan = simpananRepository.save(simpanan);

        // 2. Ubah status anggota menjadi AKTIF dan isi tanggalAktivasi
        anggota.setStatusKeanggotaan("AKTIF");
        if (anggota.getTanggalAktivasi() == null) {
            anggota.setTanggalAktivasi(now);
        }
        anggota.setUpdatedAt(now);
        anggotaRepository.save(anggota);

        // 3. Tambahkan RiwayatStatusAnggota (MENUNGGU_SETORAN_POKOK -> AKTIF)
        String riwayatId = generateNextRiwayatId();
        RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                .id(riwayatId)
                .anggota(anggota)
                .pemeriksaan(null)
                .statusSebelumnya("MENUNGGU_SETORAN_POKOK")
                .statusBaru("AKTIF")
                .tanggalPerubahan(now)
                .alasan("Pembayaran simpanan pokok awal")
                .diubahOleh("PENGURUS")
                .createdAt(now)
                .updatedAt(now)
                .build();
        riwayatStatusAnggotaRepository.save(riwayat);

        return savedSimpanan;
    }

    @Override
    @Transactional
    public Simpanan createSimpanan(CreateSimpananRequest request) {
        String jenis = request.getJenisSimpanan() != null ? request.getJenisSimpanan().trim().toUpperCase() : "";

        if ("POKOK".equals(jenis)) {
            throw new BadRequestException("Pencatatan simpanan pokok harus melalui fitur Simpanan Pokok");
        }

        if (!"WAJIB".equals(jenis) && !"SUKARELA".equals(jenis)) {
            throw new BadRequestException("Jenis simpanan hanya boleh WAJIB atau SUKARELA");
        }

        Anggota anggota = anggotaRepository.findById(request.getAnggotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan ID " + request.getAnggotaId() + " tidak ditemukan"));

        if (request.getTanggalSetor() == null || request.getTanggalSetor().isAfter(LocalDate.now())) {
            throw new BadRequestException("Tanggal setor tidak boleh berada di masa depan");
        }

        LocalDateTime now = LocalDateTime.now();

        if ("WAJIB".equals(jenis)) {
            String status = anggota.getStatusKeanggotaan();
            if (!"AKTIF".equalsIgnoreCase(status) && !"NONAKTIF".equalsIgnoreCase(status)) {
                throw new BadRequestException("Simpanan wajib hanya dapat dicatat untuk anggota berstatus Aktif atau Nonaktif");
            }

            if (request.getNominal() == null || request.getNominal() < 20000L) {
                throw new BadRequestException("Nominal simpanan wajib minimal Rp20.000");
            }

            if (request.getPeriode() == null || !request.getPeriode().matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
                throw new BadRequestException("Periode simpanan wajib harus dalam format YYYY-MM (misal: 2026-09)");
            }

            YearMonth periodeYm;
            try {
                periodeYm = YearMonth.parse(request.getPeriode());
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Format periode simpanan wajib tidak valid");
            }

            YearMonth currentYm = YearMonth.now();
            if (periodeYm.isAfter(currentYm)) {
                throw new BadRequestException("Periode simpanan wajib tidak boleh melebihi bulan berjalan (" + currentYm + ")");
            }

            if (anggota.getTanggalAktivasi() != null) {
                YearMonth aktivasiYm = YearMonth.from(anggota.getTanggalAktivasi());
                if (periodeYm.isBefore(aktivasiYm)) {
                    throw new BadRequestException("Periode simpanan wajib tidak boleh lebih awal dari bulan tanggal aktivasi (" + aktivasiYm + ")");
                }
            }

            if (simpananRepository.existsByAnggota_IdAndJenisSimpananAndPeriode(anggota.getId(), "WAJIB", request.getPeriode())) {
                throw new BadRequestException("Simpanan wajib untuk periode " + request.getPeriode() + " sudah pernah tercatat untuk anggota ini");
            }

            String simpananId = generateNextSimpananId();
            Simpanan simpanan = Simpanan.builder()
                    .id(simpananId)
                    .anggota(anggota)
                    .jenisSimpanan("WAJIB")
                    .nominal(request.getNominal())
                    .tanggalSetor(request.getTanggalSetor())
                    .periode(request.getPeriode())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            return simpananRepository.save(simpanan);

        } else {
            // SUKARELA
            if (!"AKTIF".equalsIgnoreCase(anggota.getStatusKeanggotaan())) {
                throw new BadRequestException("Simpanan sukarela hanya dapat dicatat untuk anggota berstatus Aktif");
            }

            if (request.getNominal() == null || request.getNominal() <= 0L) {
                throw new BadRequestException("Nominal simpanan sukarela harus lebih dari Rp0");
            }

            if (request.getPeriode() != null && !request.getPeriode().trim().isEmpty()) {
                throw new BadRequestException("Simpanan sukarela tidak boleh memiliki periode");
            }

            String simpananId = generateNextSimpananId();
            Simpanan simpanan = Simpanan.builder()
                    .id(simpananId)
                    .anggota(anggota)
                    .jenisSimpanan("SUKARELA")
                    .nominal(request.getNominal())
                    .tanggalSetor(request.getTanggalSetor())
                    .periode(null)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            return simpananRepository.save(simpanan);
        }
    }

    private String generateNextSimpananId() {
        List<String> ids = simpananRepository.findLatestSimpananIds();
        int maxId = 0;
        for (String id : ids) {
            if (id != null && id.startsWith("SMP")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("SMP%04d", maxId + 1);
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
