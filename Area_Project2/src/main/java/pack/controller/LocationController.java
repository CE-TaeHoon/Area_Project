package pack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pack.domain.District;
import pack.domain.Village;
import pack.service.DistrictService;
import pack.service.VillageService;

import java.util.List;

@Controller
public class LocationController {

    @Autowired
    private DistrictService districtService;

    @Autowired
    private VillageService villageService;

    @GetMapping("/api/districts")  //~구 데이터베이스 불러오기
    @ResponseBody
    public List<District> getDistricts() {
        return districtService.getAllDistricts();
    }

    @GetMapping("/api/villages") // ~동 데이터베이스 불러오기
    @ResponseBody
    public List<Village> getVillages(@RequestParam Long districtId) {
        return villageService.getVillagesByDistrictId(districtId);
    }
}
