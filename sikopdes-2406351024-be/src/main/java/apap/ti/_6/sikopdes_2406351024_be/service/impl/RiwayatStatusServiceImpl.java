package apap.ti._6.sikopdes_2406351024_be.service.impl;

import apap.ti._6.sikopdes_2406351024_be.exception.ResourceNotFoundException;
import apap.ti._6.sikopdes_2406351024_be.model.Anggota;
import apap.ti._6.sikopdes_2406351024_be.model.RiwayatStatusAnggota;
import apap.ti._6.sikopdes_2406351024_be.repository.RiwayatStatusAnggotaRepository;
import apap.ti._6.sikopdes_2406351024_be.service.RiwayatStatusService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiwayatStatusServiceImpl implements RiwayatStatusService {

    private final RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository;

    public RiwayatStatusServiceImpl(RiwayatStatusAnggotaRepository riwayatStatusAnggotaRepository) {
        this.riwayatStatusAnggotaRepository = riwayatStatusAnggotaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiwayatStatusAnggota> getAllRiwayat(String search, String statusBaru, LocalDate startDate, LocalDate endDate) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim().toLowerCase() : null;
        String cleanStatus = (statusBaru != null && !statusBaru.trim().isEmpty() && !statusBaru.equalsIgnoreCase("ALL")) ? statusBaru.trim() : null;

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Specification<RiwayatStatusAnggota> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cleanStatus != null) {
                predicates.add(cb.equal(root.get("statusBaru"), cleanStatus));
            }

            if (startDateTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tanggalPerubahan"), startDateTime));
            }

            if (endDateTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("tanggalPerubahan"), endDateTime));
            }

            if (cleanSearch != null) {
                String pattern = "%" + cleanSearch + "%";
                Join<RiwayatStatusAnggota, Anggota> anggotaJoin = root.join("anggota", JoinType.LEFT);
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(anggotaJoin.get("nomorAnggota")), pattern),
                        cb.like(cb.lower(anggotaJoin.get("nama")), pattern)
                );
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "tanggalPerubahan");
        return riwayatStatusAnggotaRepository.findAll(spec, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public RiwayatStatusAnggota getRiwayatById(String id) {
        return riwayatStatusAnggotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Riwayat status dengan ID " + id + " tidak ditemukan"));
    }
}
