package apap.ti._6.sikopdes_2406351024_be.service;

import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateSimpananPokokRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateSimpananRequest;
import apap.ti._6.sikopdes_2406351024_be.model.Simpanan;

import java.util.List;

public interface SimpananService {

    List<Simpanan> getAllSimpanan(String search, String jenis, String periode, Long nominalMin, Long nominalMax);

    Simpanan getSimpananById(String id);

    Simpanan createSimpananPokok(CreateSimpananPokokRequest request);

    Simpanan createSimpanan(CreateSimpananRequest request);
}
