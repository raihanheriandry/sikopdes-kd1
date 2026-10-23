package apap.ti._6.sikopdes_2406351024_be.service.impl;

import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.KeluarAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.UpdateAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.UpdateStatusAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.VerifikasiAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.response.DetailAnggotaResponse;
import apap.ti._6.sikopdes_2406351024_be.exception.BadRequestException;
import apap.ti._6.sikopdes_2406351024_be.exception.ResourceNotFoundException;
import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;
import apap.ti._6.sikopdes_2406351024_be.repository.AnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.RiwayatStatusAnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.SimpananRepository;
import apap.ti._6.sikopdes_2406351024_be.service.AnggotaService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnggotaServiceImpl implements AnggotaService {

    private final AnggotaRepository anggotaRepository;
    private final SimpananRepository simpananRepository;
    private final RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository;

    public AnggotaServiceImpl(
            AnggotaRepository anggotaRepository,
            SimpananRepository simpananRepository,
            RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository
    ) {
        this.anggotaRepository = anggotaRepository;
        this.simpananRepository = simpananRepository;
        this.riwayatStatusAnggotaRepository = riwayatStatusAnggotaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Anggota> getAllAnggota(String search, String status) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim().toLowerCase() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) ? status.trim() : null;

        Specification<Anggota> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cleanStatus != null) {
                predicates.add(cb.equal(root.get("statusKeanggotaan"), cleanStatus));
            }

            if (cleanSearch != null) {
                String pattern = "%" + cleanSearch + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("nomorAnggota")), pattern),
                        cb.like(cb.lower(root.get("nama")), pattern),
                        cb.like(cb.lower(root.get("nik")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                );
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return anggotaRepository.findAll(spec, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public DetailAnggotaResponse getAnggotaById(String id) {
        Anggota anggota = getAnggotaEntityById(id);

        List<Simpanan> listSimpanan = simpananRepository.findByAnggota_IdOrderByTanggalSetorDesc(id);
        List<RiwayatStatusAnggota> listRiwayat = riwayatStatusAnggotaRepository.findByAnggota_IdOrderByTanggalPerubahanDesc(id);

        Long totalPokok = simpananRepository.sumSimpananByAnggotaIdAndJenis(id, "POKOK");
        Long totalWajib = simpananRepository.sumSimpananByAnggotaIdAndJenis(id, "WAJIB");
        Long totalSukarela = simpananRepository.sumSimpananByAnggotaIdAndJenis(id, "SUKARELA");
        Long totalSemua = simpananRepository.sumTotalSimpananByAnggotaId(id);

        return DetailAnggotaResponse.builder()
                .id(anggota.getId())
                .nomorAnggota(anggota.getNomorAnggota())
                .nama(anggota.getNama())
                .nik(anggota.getNik())
                .email(anggota.getEmail())
                .telepon(anggota.getTelepon())
                .alamat(anggota.getAlamat())
                .tanggalDaftar(anggota.getTanggalDaftar())
                .tanggalAktivasi(anggota.getTanggalAktivasi())
                .statusKeanggotaan(anggota.getStatusKeanggotaan())
                .createdAt(anggota.getCreatedAt())
                .updatedAt(anggota.getUpdatedAt())
                .totalSimpananPokok(totalPokok)
                .totalSimpananWajib(totalWajib)
                .totalSimpananSukarela(totalSukarela)
                .totalSimpanan(totalSemua)
                .listSimpanan(listSimpanan)
                .listRiwayatStatus(listRiwayat)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Anggota getAnggotaEntityById(String id) {
        return anggotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anggota dengan ID " + id + " tidak ditemukan"));
    }

    @Override
    @Transactional
    public Anggota createAnggota(CreateAnggotaRequest request) {
        // NIK validation
        if (request.getNik() == null || !request.getNik().matches("^\\d{16}$")) {
            throw new BadRequestException("NIK harus terdiri dari tepat 16 digit angka");
        }
        if (anggotaRepository.existsByNikAndStatusKeanggotaanNot(request.getNik(), "DITOLAK")) {
            throw new BadRequestException("NIK " + request.getNik() + " sudah terdaftar pada anggota lain yang tidak berstatus Ditolak");
        }

        // Email validation
        if (anggotaRepository.existsByEmailAndStatusKeanggotaanNot(request.getEmail(), "DITOLAK")) {
            throw new BadRequestException("Email " + request.getEmail() + " sudah terdaftar pada anggota lain yang tidak berstatus Ditolak");
        }

        // Phone validation
        if (request.getTelepon() == null || !request.getTelepon().matches("^\\d{10,15}$")) {
            throw new BadRequestException("Nomor telepon harus terdiri dari 10 sampai 15 digit angka");
        }

        // Generate ID
        String nextId = generateNextAnggotaId();

        // Generate Nomor Anggota
        int currentYear = LocalDate.now().getYear();
        String nextNomorAnggota = generateNextNomorAnggota(currentYear);

        LocalDateTime now = LocalDateTime.now();

        Anggota anggota = Anggota.builder()
                .id(nextId)
                .nomorAnggota(nextNomorAnggota)
                .nama(request.getNama().trim())
                .nik(request.getNik().trim())
                .email(request.getEmail().trim())
                .telepon(request.getTelepon().trim())
                .alamat(request.getAlamat().trim())
                .tanggalDaftar(now)
                .tanggalAktivasi(null)
                .statusKeanggotaan("MENUNGGU_VERIFIKASI")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Anggota savedAnggota = anggotaRepository.save(anggota);

        // Initial RiwayatStatusAnggota
        String riwayatId = generateNextRiwayatId();
        RiwayatStatusAnggota initialRiwayat = RiwayatStatusAnggota.builder()
                .id(riwayatId)
                .anggota(savedAnggota)
                .pemeriksaan(null)
                .statusSebelumnya(null)
                .statusBaru("MENUNGGU_VERIFIKASI")
                .tanggalPerubahan(now)
                .alasan("Pendaftaran anggota baru")
                .diubahOleh("PENGURUS")
                .createdAt(now)
                .updatedAt(now)
                .build();
        riwayatStatusAnggotaRepository.save(initialRiwayat);

        return savedAnggota;
    }

    @Override
    @Transactional
    public Anggota updateAnggota(String id, UpdateAnggotaRequest request) {
        Anggota anggota = getAnggotaEntityById(id);

        if ("DITOLAK".equalsIgnoreCase(anggota.getStatusKeanggotaan()) || "KELUAR".equalsIgnoreCase(anggota.getStatusKeanggotaan())) {
            throw new BadRequestException("Data anggota berstatus Ditolak atau Keluar tidak dapat diubah");
        }

        // Email validation for update (exclude current id)
        if (anggotaRepository.existsByEmailAndStatusKeanggotaanNotAndIdNot(request.getEmail(), "DITOLAK", id)) {
            throw new BadRequestException("Email " + request.getEmail() + " sudah digunakan oleh anggota lain");
        }

        if (request.getTelepon() == null || !request.getTelepon().matches("^\\d{10,15}$")) {
            throw new BadRequestException("Nomor telepon harus terdiri dari 10 sampai 15 digit angka");
        }

        anggota.setNama(request.getNama().trim());
        anggota.setEmail(request.getEmail().trim());
        anggota.setTelepon(request.getTelepon().trim());
        anggota.setAlamat(request.getAlamat().trim());
        anggota.setUpdatedAt(LocalDateTime.now());

        return anggotaRepository.save(anggota);
    }

    @Override
    @Transactional
    public Anggota verifikasiAnggota(String id, VerifikasiAnggotaRequest request) {
        Anggota anggota = getAnggotaEntityById(id);

        if (!"MENUNGGU_VERIFIKASI".equalsIgnoreCase(anggota.getStatusKeanggotaan())) {
            throw new BadRequestException("Hanya anggota berstatus Menunggu Verifikasi yang dapat diverifikasi atau ditolak");
        }

        String action = request.getAction() != null ? request.getAction().trim().toUpperCase() : "";
        LocalDateTime now = LocalDateTime.now();

        if ("VERIFIKASI".equals(action)) {
            anggota.setStatusKeanggotaan("MENUNGGU_SETORAN_POKOK");
            anggota.setUpdatedAt(now);
            Anggota updated = anggotaRepository.save(anggota);

            RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                    .id(generateNextRiwayatId())
                    .anggota(updated)
                    .pemeriksaan(null)
                    .statusSebelumnya("MENUNGGU_VERIFIKASI")
                    .statusBaru("MENUNGGU_SETORAN_POKOK")
                    .tanggalPerubahan(now)
                    .alasan(request.getAlasan() != null && !request.getAlasan().isBlank() ? request.getAlasan().trim() : "Verifikasi data berhasil")
                    .diubahOleh("PENGURUS")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            riwayatStatusAnggotaRepository.save(riwayat);

            return updated;
        } else if ("TOLAK".equals(action)) {
            if (request.getAlasan() == null || request.getAlasan().trim().isEmpty()) {
                throw new BadRequestException("Alasan penolakan wajib diisi");
            }
            anggota.setStatusKeanggotaan("DITOLAK");
            anggota.setUpdatedAt(now);
            Anggota updated = anggotaRepository.save(anggota);

            RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                    .id(generateNextRiwayatId())
                    .anggota(updated)
                    .pemeriksaan(null)
                    .statusSebelumnya("MENUNGGU_VERIFIKASI")
                    .statusBaru("DITOLAK")
                    .tanggalPerubahan(now)
                    .alasan(request.getAlasan().trim())
                    .diubahOleh("PENGURUS")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            riwayatStatusAnggotaRepository.save(riwayat);

            return updated;
        } else {
            throw new BadRequestException("Aksi tidak dikenal: " + request.getAction() + ". Gunakan VERIFIKASI atau TOLAK");
        }
    }

    @Override
    @Transactional
    public Anggota keluarAnggota(String id, KeluarAnggotaRequest request) {
        Anggota anggota = getAnggotaEntityById(id);

        String currentStatus = anggota.getStatusKeanggotaan();
        if (!"AKTIF".equalsIgnoreCase(currentStatus) &&
            !"DIBEKUKAN".equalsIgnoreCase(currentStatus) &&
            !"NONAKTIF".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Hanya anggota berstatus Aktif, Dibekukan, atau Nonaktif yang dapat dikeluarkan");
        }

        if (request.getAlasan() == null || request.getAlasan().trim().isEmpty()) {
            throw new BadRequestException("Alasan keluar wajib diisi");
        }

        LocalDateTime now = LocalDateTime.now();
        anggota.setStatusKeanggotaan("KELUAR");
        anggota.setUpdatedAt(now);
        Anggota updated = anggotaRepository.save(anggota);

        RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                .id(generateNextRiwayatId())
                .anggota(updated)
                .pemeriksaan(null)
                .statusSebelumnya(currentStatus)
                .statusBaru("KELUAR")
                .tanggalPerubahan(now)
                .alasan(request.getAlasan().trim())
                .diubahOleh("PENGURUS")
                .createdAt(now)
                .updatedAt(now)
                .build();
        riwayatStatusAnggotaRepository.save(riwayat);

        return updated;
    }

    @Override
    @Transactional
    public Anggota updateStatusKeanggotaan(String id, UpdateStatusAnggotaRequest request) {
        Anggota anggota = getAnggotaEntityById(id);
        String currentStatus = anggota.getStatusKeanggotaan();
        String targetStatus = request.getStatus() != null ? request.getStatus().trim().toUpperCase() : "";
        LocalDateTime now = LocalDateTime.now();

        if ("DIBEKUKAN".equals(targetStatus)) {
            if (!"AKTIF".equalsIgnoreCase(currentStatus)) {
                throw new BadRequestException("Hanya anggota berstatus Aktif yang dapat dibekukan");
            }
            if (request.getAlasan() == null || request.getAlasan().trim().isEmpty()) {
                throw new BadRequestException("Alasan pembekuan wajib diisi");
            }

            anggota.setStatusKeanggotaan("DIBEKUKAN");
            anggota.setUpdatedAt(now);
            Anggota updated = anggotaRepository.save(anggota);

            RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                    .id(generateNextRiwayatId())
                    .anggota(updated)
                    .pemeriksaan(null)
                    .statusSebelumnya("AKTIF")
                    .statusBaru("DIBEKUKAN")
                    .tanggalPerubahan(now)
                    .alasan(request.getAlasan().trim())
                    .diubahOleh("PENGURUS")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            riwayatStatusAnggotaRepository.save(riwayat);

            return updated;

        } else if ("AKTIF".equals(targetStatus)) {
            if ("DIBEKUKAN".equalsIgnoreCase(currentStatus)) {
                // Aktifkan kembali dari status Dibekukan (tanpa periksa tunggakan)
                anggota.setStatusKeanggotaan("AKTIF");
                anggota.setUpdatedAt(now);
                Anggota updated = anggotaRepository.save(anggota);

                String alasan = (request.getAlasan() != null && !request.getAlasan().trim().isEmpty())
                        ? request.getAlasan().trim() : "Diaktifkan kembali oleh pengurus";

                RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                        .id(generateNextRiwayatId())
                        .anggota(updated)
                        .pemeriksaan(null)
                        .statusSebelumnya("DIBEKUKAN")
                        .statusBaru("AKTIF")
                        .tanggalPerubahan(now)
                        .alasan(alasan)
                        .diubahOleh("PENGURUS")
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                riwayatStatusAnggotaRepository.save(riwayat);

                return updated;

            } else if ("NONAKTIF".equalsIgnoreCase(currentStatus)) {
                // Aktifkan kembali dari status Nonaktif (harus periksa 3 periode kalender selesai terakhir)
                YearMonth currentYm = YearMonth.now();
                List<String> threeCompletedPeriods = List.of(
                        currentYm.minusMonths(3).toString(),
                        currentYm.minusMonths(2).toString(),
                        currentYm.minusMonths(1).toString()
                );

                List<String> unpaidPeriods = new ArrayList<>();
                for (String p : threeCompletedPeriods) {
                    boolean paid = simpananRepository.existsByAnggota_IdAndJenisSimpananAndPeriode(id, "WAJIB", p);
                    if (!paid) {
                        unpaidPeriods.add(p);
                    }
                }

                if (!unpaidPeriods.isEmpty()) {
                    throw new BadRequestException("Gagal mengaktifkan kembali anggota. Masih menunggak simpanan wajib periode: " + String.join(", ", unpaidPeriods));
                }

                anggota.setStatusKeanggotaan("AKTIF");
                anggota.setUpdatedAt(now);
                Anggota updated = anggotaRepository.save(anggota);

                String alasan = (request.getAlasan() != null && !request.getAlasan().trim().isEmpty())
                        ? request.getAlasan().trim() : "Diaktifkan kembali setelah melunasi simpanan wajib";

                RiwayatStatusAnggota riwayat = RiwayatStatusAnggota.builder()
                        .id(generateNextRiwayatId())
                        .anggota(updated)
                        .pemeriksaan(null)
                        .statusSebelumnya("NONAKTIF")
                        .statusBaru("AKTIF")
                        .tanggalPerubahan(now)
                        .alasan(alasan)
                        .diubahOleh("PENGURUS")
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                riwayatStatusAnggotaRepository.save(riwayat);

                return updated;
            } else {
                throw new BadRequestException("Hanya anggota berstatus Dibekukan atau Nonaktif yang dapat diaktifkan kembali");
            }
        } else {
            throw new BadRequestException("Status target tidak valid: " + request.getStatus() + ". Hanya DIBEKUKAN atau AKTIF yang diperbolehkan");
        }
    }

    private String generateNextAnggotaId() {
        List<String> ids = anggotaRepository.findLatestAnggotaIds();
        int maxId = 0;
        for (String id : ids) {
            if (id != null && id.startsWith("ANG")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("ANG%04d", maxId + 1);
    }

    private String generateNextNomorAnggota(int tahun) {
        List<String> listNomor = anggotaRepository.findNomorAnggotaByTahun(tahun);
        int maxNomor = 0;
        for (String no : listNomor) {
            if (no != null) {
                String[] parts = no.split("-");
                if (parts.length == 3) {
                    try {
                        int num = Integer.parseInt(parts[2]);
                        if (num > maxNomor) {
                            maxNomor = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format("KOP-%d-%04d", tahun, maxNomor + 1);
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
