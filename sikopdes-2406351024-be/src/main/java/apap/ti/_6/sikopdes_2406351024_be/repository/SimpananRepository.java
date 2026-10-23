package apap.ti._6.sikopdes_2406351024_be.repository;

import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimpananRepository extends JpaRepository<Simpanan, String>, JpaSpecificationExecutor<Simpanan> {

    List<Simpanan> findByAnggota_IdOrderByTanggalSetorDesc(String anggotaId);

    boolean existsByAnggota_IdAndJenisSimpanan(String anggotaId, String jenisSimpanan);

    boolean existsByAnggota_IdAndJenisSimpananAndPeriode(String anggotaId, String jenisSimpanan, String periode);

    Optional<Simpanan> findByAnggota_IdAndJenisSimpananAndPeriode(String anggotaId, String jenisSimpanan, String periode);

    @Query("SELECT s.id FROM Simpanan s WHERE s.id LIKE 'SMP%' ORDER BY s.id DESC")
    List<String> findLatestSimpananIds();

    @Query("SELECT COALESCE(SUM(s.nominal), 0) FROM Simpanan s WHERE s.anggota.id = :anggotaId")
    Long sumTotalSimpananByAnggotaId(@Param("anggotaId") String anggotaId);

    @Query("SELECT COALESCE(SUM(s.nominal), 0) FROM Simpanan s WHERE s.anggota.id = :anggotaId AND s.jenisSimpanan = :jenisSimpanan")
    Long sumSimpananByAnggotaIdAndJenis(@Param("anggotaId") String anggotaId, @Param("jenisSimpanan") String jenisSimpanan);

    @Query("SELECT COALESCE(SUM(s.nominal), 0) FROM Simpanan s WHERE s.tanggalSetor BETWEEN :startDate AND :endDate")
    Long sumNominalByTanggalSetorBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
