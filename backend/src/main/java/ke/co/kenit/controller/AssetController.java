package ke.co.kenit.controller;

import jakarta.validation.Valid;
import ke.co.kenit.dto.AssetDTO;
import ke.co.kenit.model.Asset;
import ke.co.kenit.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public List<AssetDTO.Response> getAllAssets() {
        return assetService.getAllAssets();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetDTO.Response createAsset(@Valid @RequestBody AssetDTO.CreateRequest req) {
        return assetService.createAsset(req);
    }

    @PatchMapping("/{id}/status")
    public AssetDTO.Response updateStatus(
            @PathVariable Long id,
            @RequestParam Asset.AssetStatus status) {
        return assetService.updateStatus(id, status);
    }
}
