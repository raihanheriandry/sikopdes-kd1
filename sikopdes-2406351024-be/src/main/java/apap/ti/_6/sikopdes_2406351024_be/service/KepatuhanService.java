package apap.ti._6.sikopdes_2406351024_be.service;

import apap.ti._6.sikopdes_2406351024_be.dto.response.KepatuhanResponse;
import apap.ti._6.sikopdes_2406351024_be.model.PemeriksaanKepatuhan;

public interface KepatuhanService {

    KepatuhanResponse getKepatuhanData(String bulanAcuan);

    PemeriksaanKepatuhan executeComplianceJob();
}
