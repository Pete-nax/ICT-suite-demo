package ke.co.kenit.service;

import ke.co.kenit.dto.AssetDTO;
import ke.co.kenit.model.Asset;
import ke.co.kenit.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepo;
    private final AtomicInteger assetCounter = new AtomicInteger(1);

    @Transactional
    public AssetDTO.Response createAsset(AssetDTO.CreateRequest req) {
        var asset = new Asset();
        asset.setAssetTag(generateAssetTag());
        asset.setName(req.getName());
        asset.setType(req.getType());
        asset.setBrand(req.getBrand());
        asset.setModel(req.getModel());
        asset.setSerialNumber(req.getSerialNumber());
        asset.setAssignedTo(req.getAssignedTo());
        asset.setDepartment(req.getDepartment());
        asset.setLocation(req.getLocation());
        asset.setPurchaseCostKes(req.getPurchaseCostKes());
        asset.setPurchaseDate(req.getPurchaseDate());
        asset.setWarrantyExpiryDate(req.getWarrantyExpiryDate());
        asset.setNotes(req.getNotes());
        asset.setStatus(Asset.AssetStatus.ACTIVE);

        return toResponse(assetRepo.save(asset));
    }

    @Transactional
    public AssetDTO.Response updateStatus(Long id, Asset.AssetStatus newStatus) {
        var asset = findOrThrow(id);
        asset.setStatus(newStatus);
        return toResponse(assetRepo.save(asset));
    }

    @Transactional
    public void linkNetworkInfo(String assetTag, String ip, String mac) {
        assetRepo.findByAssetTag(assetTag).ifPresent(asset -> {
            asset.setIpAddress(ip);
            asset.setMacAddress(mac);
            assetRepo.save(asset);
        });
    }

    public List<AssetDTO.Response> getAllAssets() {
        return assetRepo.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private Asset findOrThrow(Long id) {
        return assetRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Asset " + id + " not found"));
    }

    private String generateAssetTag() {
        int year = LocalDate.now().getYear();
        int seq = assetCounter.getAndIncrement();
        return String.format("KEN-%d-%03d", year, seq);
    }

    private AssetDTO.Response toResponse(Asset a) {
        var res = new AssetDTO.Response();
        res.setId(a.getId());
        res.setAssetTag(a.getAssetTag());
        res.setName(a.getName());
        res.setType(a.getType());
        res.setBrand(a.getBrand());
        res.setModel(a.getModel());
        res.setSerialNumber(a.getSerialNumber());
        res.setStatus(a.getStatus());
        res.setAssignedTo(a.getAssignedTo());
        res.setDepartment(a.getDepartment());
        res.setLocation(a.getLocation());
        res.setPurchaseCostKes(a.getPurchaseCostKes());
        res.setPurchaseDate(a.getPurchaseDate());
        res.setWarrantyExpiryDate(a.getWarrantyExpiryDate());
        res.setIpAddress(a.getIpAddress());
        res.setMacAddress(a.getMacAddress());
        res.setNotes(a.getNotes());
        res.setCreatedAt(a.getCreatedAt());
        return res;
    }
}
