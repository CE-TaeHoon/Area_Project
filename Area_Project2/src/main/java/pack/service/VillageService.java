package pack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pack.domain.Village;
import pack.repository.VillageRepository;

import java.util.List;

@Service
public class VillageService {

    @Autowired
    private VillageRepository villageRepository;

    public List<Village> getVillagesByDistrictId(Long districtId) {
        return villageRepository.findByDistrictId(districtId);
    }
}