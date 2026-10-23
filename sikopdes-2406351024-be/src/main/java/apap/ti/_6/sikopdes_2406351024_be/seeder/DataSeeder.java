package apap.ti._6.sikopdes_2406351024_be.seeder;

import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import apap.ti._6.sikopdes_2406351024_be.model.PemeriksaanKepatuhan;
import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;
import apap.ti._6.sikopdes_2406351024_be.repository.AnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.PemeriksaanKepatuhanRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.RiwayatStatusAnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.repository.SimpananRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AnggotaRepository anggotaRepository;
    private final SimpananRepository simpananRepository;
    private final RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository;
    private final PemeriksaanKepatuhanRepository pemeriksaanKepatuhanRepository;

    private int anggotaSeq = 1;
    private int simpananSeq = 1;
    private int riwayatSeq = 1;

    public DataSeeder(
            AnggotaRepository anggotaRepository,
            SimpananRepository simpananRepository,
            RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository,
            PemeriksaanKepatuhanRepository pemeriksaanKepatuhanRepository
    ) {
        this.anggotaRepository = anggotaRepository;
        this.simpananRepository = simpananRepository;
        this.riwayatStatusAnggotaRepository = riwayatStatusAnggotaRepository;
        this.pemeriksaanKepatuhanRepository = pemeriksaanKepatuhanRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (anggotaRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker(new Locale("id", "ID"));

        // Seed initial PemeriksaanKepatuhan
        PemeriksaanKepatuhan initialPemeriksaan = PemeriksaanKepatuhan.builder()
                .id("PKP0001")
                .waktuEksekusi(LocalDateTime.of(2026, 10, 1, 0, 1, 0))
                .periodeAwal("2026-07")
                .periodeAkhir("2026-09")
                .jumlahDiperiksa(10)
                .jumlahDinonaktifkan(2)
                .createdAt(LocalDateTime.of(2026, 10, 1, 0, 1, 0))
                .updatedAt(LocalDateTime.of(2026, 10, 1, 0, 1, 0))
                .build();
        pemeriksaanKepatuhanRepository.save(initialPemeriksaan);

        // 1. MENUNGGU_VERIFIKASI (2 anggota)
        for (int i = 0; i < 2; i++) {
            LocalDateTime regDate = LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10));
            Anggota a = createBaseAnggota(faker, "MENUNGGU_VERIFIKASI", regDate, null);
            anggotaRepository.save(a);
            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
        }

        // 2. MENUNGGU_SETORAN_POKOK (2 anggota)
        for (int i = 0; i < 2; i++) {
            LocalDateTime regDate = LocalDateTime.now().minusDays(15);
            LocalDateTime verifDate = regDate.plusDays(2);
            Anggota a = createBaseAnggota(faker, "MENUNGGU_SETORAN_POKOK", regDate, null);
            anggotaRepository.save(a);
            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "MENUNGGU_SETORAN_POKOK", verifDate, null, "PENGURUS", null);
        }

        // 3. DITOLAK (2 anggota)
        for (int i = 0; i < 2; i++) {
            LocalDateTime regDate = LocalDateTime.now().minusDays(20);
            LocalDateTime tolakDate = regDate.plusDays(1);
            Anggota a = createBaseAnggota(faker, "DITOLAK", regDate, null);
            anggotaRepository.save(a);
            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "DITOLAK", tolakDate, "Dokumen KTP buram / tidak sesuai data Disdukcapil", "PENGURUS", null);
        }

        // 4. AKTIF - Patuh Simpanan Wajib Lengkap (3 anggota)
        for (int i = 0; i < 3; i++) {
            LocalDateTime regDate = LocalDateTime.of(2026, 5, 20, 10, 0);
            LocalDateTime aktDate = LocalDateTime.of(2026, 6, 1, 9, 30);
            Anggota a = createBaseAnggota(faker, "AKTIF", regDate, aktDate);
            anggotaRepository.save(a);

            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "MENUNGGU_SETORAN_POKOK", regDate.plusDays(2), null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_SETORAN_POKOK", "AKTIF", aktDate, null, "PENGURUS", null);

            // Simpanan Pokok
            createSimpanan(a, "POKOK", 100000L, aktDate.toLocalDate(), null);

            // Simpanan Wajib Lengkap: Juni, Juli, Agustus, September, Oktober 2026
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 6, 10), "2026-06");
            createSimpanan(a, "WAJIB", 25000L, LocalDate.of(2026, 7, 5), "2026-07");
            createSimpanan(a, "WAJIB", 25000L, LocalDate.of(2026, 8, 8), "2026-08");
            createSimpanan(a, "WAJIB", 30000L, LocalDate.of(2026, 9, 12), "2026-09");
            createSimpanan(a, "WAJIB", 30000L, LocalDate.of(2026, 10, 5), "2026-10");

            // Simpanan Sukarela
            createSimpanan(a, "SUKARELA", 50000L, LocalDate.of(2026, 9, 20), null);
        }

        // 5. AKTIF - Menunggak Simpanan Wajib (3 anggota) -> untuk demo job kepatuhan
        for (int i = 0; i < 3; i++) {
            LocalDateTime regDate = LocalDateTime.of(2026, 5, 25, 11, 0);
            LocalDateTime aktDate = LocalDateTime.of(2026, 6, 1, 10, 0);
            Anggota a = createBaseAnggota(faker, "AKTIF", regDate, aktDate);
            anggotaRepository.save(a);

            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "MENUNGGU_SETORAN_POKOK", regDate.plusDays(2), null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_SETORAN_POKOK", "AKTIF", aktDate, null, "PENGURUS", null);

            // Simpanan Pokok
            createSimpanan(a, "POKOK", 150000L, aktDate.toLocalDate(), null);

            // Hanya bayar Juni & Juli, menunggak Agustus & September 2026
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 6, 15), "2026-06");
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 7, 10), "2026-07");
        }

        // 6. AKTIF - Baru Aktif Bulan Berjalan (2 anggota) -> tanggal aktivasi setelah awal periode evaluasi
        for (int i = 0; i < 2; i++) {
            LocalDateTime regDate = LocalDateTime.of(2026, 10, 5, 8, 0);
            LocalDateTime aktDate = LocalDateTime.of(2026, 10, 8, 14, 0);
            Anggota a = createBaseAnggota(faker, "AKTIF", regDate, aktDate);
            anggotaRepository.save(a);

            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "MENUNGGU_SETORAN_POKOK", regDate.plusDays(1), null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_SETORAN_POKOK", "AKTIF", aktDate, null, "PENGURUS", null);

            createSimpanan(a, "POKOK", 100000L, aktDate.toLocalDate(), null);
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 10, 10), "2026-10");
        }

        // 7. DIBEKUKAN (2 anggota)
        for (int i = 0; i < 2; i++) {
            LocalDateTime regDate = LocalDateTime.of(2026, 6, 1, 9, 0);
            LocalDateTime aktDate = LocalDateTime.of(2026, 6, 15, 10, 0);
            LocalDateTime freezeDate = LocalDateTime.of(2026, 9, 15, 14, 0);
            Anggota a = createBaseAnggota(faker, "DIBEKUKAN", regDate, aktDate);
            anggotaRepository.save(a);

            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "MENUNGGU_SETORAN_POKOK", regDate.plusDays(3), null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_SETORAN_POKOK", "AKTIF", aktDate, null, "PENGURUS", null);
            createRiwayat(a, "AKTIF", "DIBEKUKAN", freezeDate, "Pemeriksaan berkala atas aktivitas akun yang mencurigakan", "PENGURUS", null);

            createSimpanan(a, "POKOK", 100000L, aktDate.toLocalDate(), null);
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 6, 20), "2026-06");
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 7, 10), "2026-07");
        }

        // 8. NONAKTIF (2 anggota)
        for (int i = 0; i < 2; i++) {
            LocalDateTime regDate = LocalDateTime.of(2026, 5, 10, 8, 0);
            LocalDateTime aktDate = LocalDateTime.of(2026, 5, 20, 11, 0);
            LocalDateTime nonaktifDate = LocalDateTime.of(2026, 10, 1, 0, 1, 0);
            Anggota a = createBaseAnggota(faker, "NONAKTIF", regDate, aktDate);
            anggotaRepository.save(a);

            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "MENUNGGU_SETORAN_POKOK", regDate.plusDays(2), null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_SETORAN_POKOK", "AKTIF", aktDate, null, "PENGURUS", null);
            createRiwayat(a, "AKTIF", "NONAKTIF", nonaktifDate, "Menunggak simpanan wajib periode 2026-07, 2026-08", "SISTEM", initialPemeriksaan);

            createSimpanan(a, "POKOK", 100000L, aktDate.toLocalDate(), null);
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 5, 25), "2026-05");
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 6, 10), "2026-06");
        }

        // 9. KELUAR (2 anggota)
        for (int i = 0; i < 2; i++) {
            LocalDateTime regDate = LocalDateTime.of(2026, 5, 5, 10, 0);
            LocalDateTime aktDate = LocalDateTime.of(2026, 5, 15, 13, 0);
            LocalDateTime keluarDate = LocalDateTime.of(2026, 9, 30, 15, 0);
            Anggota a = createBaseAnggota(faker, "KELUAR", regDate, aktDate);
            anggotaRepository.save(a);

            createRiwayat(a, null, "MENUNGGU_VERIFIKASI", regDate, null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_VERIFIKASI", "MENUNGGU_SETORAN_POKOK", regDate.plusDays(2), null, "PENGURUS", null);
            createRiwayat(a, "MENUNGGU_SETORAN_POKOK", "AKTIF", aktDate, null, "PENGURUS", null);
            createRiwayat(a, "AKTIF", "KELUAR", keluarDate, "Permohonan pengunduran diri karena pindah domisili ke luar desa", "PENGURUS", null);

            createSimpanan(a, "POKOK", 100000L, aktDate.toLocalDate(), null);
            createSimpanan(a, "WAJIB", 20000L, LocalDate.of(2026, 6, 5), "2026-06");
        }
    }

    private Anggota createBaseAnggota(Faker faker, String status, LocalDateTime regDate, LocalDateTime aktDate) {
        int seq = anggotaSeq++;
        String id = String.format("ANG%04d", seq);
        String nomorAnggota = String.format("KOP-2026-%04d", seq);
        String nik = String.format("%016d", 3171000000000000L + seq);
        String telepon = "08" + faker.number().digits(10);
        String email = "anggota" + seq + "@example.com";
        String nama = faker.name().fullName();
        String alamat = faker.address().fullAddress();

        return Anggota.builder()
                .id(id)
                .nomorAnggota(nomorAnggota)
                .nama(nama)
                .nik(nik)
                .email(email)
                .telepon(telepon)
                .alamat(alamat)
                .tanggalDaftar(regDate)
                .tanggalAktivasi(aktDate)
                .statusKeanggotaan(status)
                .createdAt(regDate)
                .updatedAt(regDate)
                .build();
    }

    private void createRiwayat(
            Anggota anggota,
            String statusSebelumnya,
            String statusBaru,
            LocalDateTime waktu,
            String alasan,
            String diubahOleh,
            PemeriksaanKepatuhan pemeriksaan
    ) {
        String id = String.format("RST%04d", riwayatSeq++);
        RiwayatStatusAnggota r = RiwayatStatusAnggota.builder()
                .id(id)
                .anggota(anggota)
                .pemeriksaan(pemeriksaan)
                .statusSebelumnya(statusSebelumnya)
                .statusBaru(statusBaru)
                .tanggalPerubahan(waktu)
                .alasan(alasan)
                .diubahOleh(diubahOleh)
                .createdAt(waktu)
                .updatedAt(waktu)
                .build();
        riwayatStatusAnggotaRepository.save(r);
    }

    private void createSimpanan(Anggota anggota, String jenis, Long nominal, LocalDate tanggalSetor, String periode) {
        String id = String.format("SMP%04d", simpananSeq++);
        Simpanan s = Simpanan.builder()
                .id(id)
                .anggota(anggota)
                .jenisSimpanan(jenis)
                .nominal(nominal)
                .tanggalSetor(tanggalSetor)
                .periode(periode)
                .createdAt(tanggalSetor.atStartOfDay())
                .updatedAt(tanggalSetor.atStartOfDay())
                .build();
        simpananRepository.save(s);
    }
}
