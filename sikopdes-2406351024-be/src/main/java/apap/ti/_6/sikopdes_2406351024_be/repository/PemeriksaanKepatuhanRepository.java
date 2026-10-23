package apap.ti._6.sikopdes_2406351024_be.repository;

import apap.ti._6.sikopdes_2406351024_be.model.PemeriksaanKepatuhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PemeriksaanKepatuhanRepository extends JpaRepository<PemeriksaanKepatuhan, String> {

    List<PemeriksaanKepatuhan> findTop5ByOrderByWaktuEksekusiDesc();

    @Query("SELECT p.id FROM PemeriksaanKepatuhan p WHERE p.id LIKE 'PKP%' ORDER BY p.id DESC")
    List<String> findLatestPemeriksaanIds();
}
