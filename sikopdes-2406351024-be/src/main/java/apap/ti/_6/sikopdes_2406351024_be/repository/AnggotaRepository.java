package apap.ti._6.sikopdes_2406351024_be.repository;

import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnggotaRepository extends JpaRepository<Anggota, String>, JpaSpecificationExecutor<Anggota> {

    boolean existsByNikAndStatusKeanggotaanNot(String nik, String statusKeanggotaan);

    boolean existsByNikAndStatusKeanggotaanNotAndIdNot(String nik, String statusKeanggotaan, String id);

    boolean existsByEmailAndStatusKeanggotaanNot(String email, String statusKeanggotaan);

    boolean existsByEmailAndStatusKeanggotaanNotAndIdNot(String email, String statusKeanggotaan, String id);

    List<Anggota> findByStatusKeanggotaan(String statusKeanggotaan, Sort sort);

    long countByStatusKeanggotaan(String statusKeanggotaan);

    @Query("SELECT a.nomorAnggota FROM Anggota a WHERE a.nomorAnggota LIKE CONCAT('KOP-', :tahun, '-%') ORDER BY a.nomorAnggota DESC")
    List<String> findNomorAnggotaByTahun(@Param("tahun") int tahun);

    @Query("SELECT a.id FROM Anggota a WHERE a.id LIKE 'ANG%' ORDER BY a.id DESC")
    List<String> findLatestAnggotaIds();
}
