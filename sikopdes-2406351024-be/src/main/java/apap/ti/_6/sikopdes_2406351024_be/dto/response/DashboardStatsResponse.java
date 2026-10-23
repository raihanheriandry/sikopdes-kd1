package apap.ti._6.sikopdes_2406351024_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long waitingVerificationCount;
    private long waitingPokokCount;
    private long activeCount;
    private long frozenCount;
    private long inactiveCount;
    private long totalCurrentMonthSimpanan;
}
