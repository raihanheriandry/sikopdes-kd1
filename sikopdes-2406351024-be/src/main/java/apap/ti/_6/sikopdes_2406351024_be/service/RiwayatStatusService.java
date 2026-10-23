package apap.ti._6.sikopdes_2406351024_be.service;

import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;

import java.time.LocalDate;
import java.util.List;

public interface RiwayatStatusService {

    List<RiwayatStatusAnggota> getAllRiwayat(String search, String statusBaru, LocalDate startDate, LocalDate endDate);

    RiwayatStatusAnggota getRiwayatById(String id);
}
