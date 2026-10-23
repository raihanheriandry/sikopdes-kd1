package apap.ti._6.sikopdes_2406351024_be.service;

import apap.ti._6.sikopdes_2406351024_be.dto.response.StatistikResponse;

public interface StatistikService {

    StatistikResponse getStatistik(Integer tahun, String periodeType, String filterValue);
}
