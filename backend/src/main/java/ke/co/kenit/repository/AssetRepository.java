package ke.co.kenit.repository;

import ke.co.kenit.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByAssetTag(String assetTag);

    List<Asset> findByAssignedToIgnoreCase(String assignedTo);

    List<Asset> findByDepartmentOrderByNameAsc(String department);

    List<Asset> findByStatus(Asset.AssetStatus status);

    long countByStatus(Asset.AssetStatus status);

    // Finance loves this one
    @Query("SELECT COALESCE(SUM(a.purchaseCostKes), 0) FROM Asset a WHERE a.status = 'ACTIVE'")
    BigDecimal sumActiveAssetValue();
}
