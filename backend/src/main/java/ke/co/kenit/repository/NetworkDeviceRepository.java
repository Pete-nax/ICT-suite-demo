package ke.co.kenit.repository;

import ke.co.kenit.model.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, Long> {

    Optional<NetworkDevice> findByIpAddress(String ipAddress);

    List<NetworkDevice> findByIsOnlineTrue();

    List<NetworkDevice> findByIsOnlineFalse();

    long countByIsOnline(boolean isOnline);
}
