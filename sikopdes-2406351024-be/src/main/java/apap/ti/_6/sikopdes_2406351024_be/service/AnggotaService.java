package apap.ti._6.sikopdes_2406351024_be.service;

import apap.ti._6.sikopdes_2406351024_be.dto.request.CreateAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.KeluarAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.UpdateAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.UpdateStatusAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.request.VerifikasiAnggotaRequest;
import apap.ti._6.sikopdes_2406351024_be.dto.response.DetailAnggotaResponse;
import apap.ti._6.sikopdes_2406351024_be.model.Anggota;

import java.util.List;

public interface AnggotaService {

    List<Anggota> getAllAnggota(String search, String status);

    DetailAnggotaResponse getAnggotaById(String id);

    Anggota getAnggotaEntityById(String id);

    Anggota createAnggota(CreateAnggotaRequest request);

    Anggota updateAnggota(String id, UpdateAnggotaRequest request);

    Anggota verifikasiAnggota(String id, VerifikasiAnggotaRequest request);

    Anggota keluarAnggota(String id, KeluarAnggotaRequest request);

    Anggota updateStatusKeanggotaan(String id, UpdateStatusAnggotaRequest request);
}
