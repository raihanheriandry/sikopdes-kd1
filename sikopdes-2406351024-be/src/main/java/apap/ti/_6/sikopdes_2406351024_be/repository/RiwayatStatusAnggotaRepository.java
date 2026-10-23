package apap.ti._6.sikopdes_2406351024_be.repository;

import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiwayatStatusAnggotaRepository extends JpaRepository<RiwayatStatusAnggota, String>, JpaSpecificationExecutor<RiwayatStatusAnggota> {

    List<RiwayatStatusAnggota> findByAnggota_IdOrderByTanggalPerubahanDesc(String anggotaId);

    @Query("SELECT r.id FROM RiwayatStatusAnggota r WHERE r.id LIKE 'RST%' ORDER BY r.id DESC")
    List<String> findLatestRiwayatIds();
}
