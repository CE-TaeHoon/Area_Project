package pack.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pack.domain.SmokingArea;
import pack.repository.SmokingAreaRepository;
import pack.service.SmokingAreaService;

@Controller
public class SmokingAreaController {

    @Autowired
    private SmokingAreaService smokingAreaService;

    @Autowired
    private SmokingAreaRepository smokingAreaRepository;

    @GetMapping("/upload-csv")
    @ResponseBody
    public ResponseEntity<?> uploadCSV() {
        try {
            Map<String, Object> result = smokingAreaService.updateSmokingAreaDataFromDirectory("static/csv");
            int processedFiles = (int) result.get("processedFiles");
            List<String> failedAddresses = (List<String>) result.get("failedAddresses");
            int progress = (int) result.get("progress");
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", String.format("총 %d개의 파일(.csv) 처리 완료", processedFiles),
                "failedAddresses", failedAddresses,
                "progress", progress
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(Map.of(
                                   "success", false,
                                   "message", "데이터 갱신 중 오류가 발생했습니다: " + e.getMessage(),
                                   "progress", 0
                               ));
        }
    }
    
    @GetMapping("/api/smoking-areas")
    @ResponseBody
    public List<SmokingArea> getSmokingAreasWithinBounds(@RequestParam double swLat, @RequestParam double swLng, @RequestParam double neLat, @RequestParam double neLng) {
        return smokingAreaRepository.findWithinBounds(swLat, swLng, neLat, neLng);
    }

    @GetMapping("/api/progress")
    @ResponseBody
    public ResponseEntity<?> getProgress() {
        int progress = smokingAreaService.getCurrentProgress();
        return ResponseEntity.ok().body(Map.of("progress", progress));
    }
}
