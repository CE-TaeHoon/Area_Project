package pack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pack.domain.SmokingArea;
import pack.repository.SmokingAreaRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmokingAreaService {

    private static final Logger log = LoggerFactory.getLogger(SmokingAreaService.class);
    private List<String> failedAddresses = new ArrayList<>(); // 좌표 변환 실패한 주소 저장
    private int totalFiles = 0;
    private int currentFile = 0;

    @Autowired
    private SmokingAreaRepository smokingAreaRepository;

    @Autowired
    private ResourceLoader resourceLoader;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // application.yml에서 API 키 불러오기
    @Value("${kakao.rest-api-key}")
    private String apiKey;
    
    private final String kakaoGeocodeUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=";
    private final String kakaoKeywordUrl = "https://dapi.kakao.com/v2/local/search/keyword.json?query=";

    // 전체 데이터 갱신
    @Transactional
    public Map<String, Object> updateSmokingAreaDataFromDirectory(String directoryPath) throws IOException{
        int processedFiles = 0;
        failedAddresses.clear();
        currentFile = 0;
        totalFiles = 0;
        
        try {
            Resource resource = resourceLoader.getResource("classpath:" + directoryPath);
            File directory = resource.getFile();
            
            if (directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
                if (files != null) {
                    totalFiles = files.length;
                    
                    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
                    smokingAreaRepository.deleteAll();
                    entityManager.createNativeQuery("ALTER TABLE smoking_area AUTO_INCREMENT = 1").executeUpdate();
                    
                    for (File file : files) {
                        try {
                            saveSmokingAreaDataFromCSV(file.getAbsolutePath());
                            processedFiles++;
                        } catch (Exception e) {
                            log.error("파일 처리 중 오류 발생: " + file.getName(), e);
                        }
                        currentFile++;
                    }
                    
                    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
                }
            }
        } catch (Exception e) {
            log.error("데이터 갱신 중 오류 발생", e);
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            currentFile = 0;
            totalFiles = 0;
            throw e;
        }
        
        int finalProgress = getCurrentProgress();
        currentFile = 0;
        totalFiles = 0;
        
        return Map.of(
            "processedFiles", processedFiles,
            "failedAddresses", failedAddresses,
            "progress", finalProgress
        );
    }

    public int getCurrentProgress() {
        return totalFiles == 0 ? 0 : (currentFile * 100) / totalFiles;
    }

    // 기존 데이터 전체 삭제
    public void deleteAllSmokingAreaData() {
        smokingAreaRepository.deleteAll();
    }
    
    // ID 초기화
    @Transactional
    public void resetId() {
        entityManager.createNativeQuery("ALTER TABLE smoking_area AUTO_INCREMENT = 1").executeUpdate();
    }
    
    // 모든 CSV파일 읽기
    public void saveAllSmokingAreaDataFromDirectory(String directoryPath) {
        Resource resource = resourceLoader.getResource("classpath:" + directoryPath);
        try {
            File directory = resource.getFile();
            if (directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

                if (files != null) {
                    for (File file : files) {
                        saveSmokingAreaDataFromCSV(file.getAbsolutePath()); // CSV파일의 데이터 저장
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CSV 파일에서 데이터를 읽어 DB에 저장
    public void saveSmokingAreaDataFromCSV(String filePath) {
        List<SmokingArea> smokingAreas = new ArrayList<>(); // 일괄 저장을 위한 리스트
        Map<String, Double[]> addressCache = new HashMap<>(); // 중복 방지를 위한 캐시

        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String[] nextLine;
            reader.readNext(); // 헤더 건너뛰기
            while ((nextLine = reader.readNext()) != null) {
                SmokingArea smokingArea = new SmokingArea();
                smokingArea.setDistrict(nextLine[0]); 
                smokingArea.setAddress(nextLine[1].isEmpty() ? null : nextLine[1]); 
                smokingArea.setLocationDetail(nextLine[2].isEmpty() ? null : nextLine[2]); 
                smokingArea.setType(nextLine[3].isEmpty() ? "정보 없음" : nextLine[3]); 
                smokingArea.setAreaSize(nextLine[4].isEmpty() ? 0.0 : Double.parseDouble(nextLine[4])); 
                
                if (!nextLine[5].isEmpty() && !nextLine[6].isEmpty()) {
                    smokingArea.setLatitude(Double.parseDouble(nextLine[5]));
                    smokingArea.setLongitude(Double.parseDouble(nextLine[6]));
                } else {
                    String cacheKey = smokingArea.getDistrict() + "|" + smokingArea.getAddress() + "|" + smokingArea.getLocationDetail();
                    Double[] coordinates = addressCache.get(cacheKey);
                    if (coordinates == null) {
                        coordinates = getCoordinatesFromAddress(smokingArea.getDistrict(), smokingArea.getAddress(), smokingArea.getLocationDetail());
                        
                        if (coordinates != null) {
                            addressCache.put(cacheKey, coordinates);
                            Thread.sleep(200); // API 호출 간 딜레이 추가
                        } else {
                            failedAddresses.add(smokingArea.getAddress()); // 좌표 변환 실패 주소 추가
                        }
                    }
                    if (coordinates != null) {
                        smokingArea.setLatitude(coordinates[0]);
                        smokingArea.setLongitude(coordinates[1]);
                    }
                }
                smokingAreas.add(smokingArea);
            }
            smokingAreaRepository.saveAll(smokingAreas); // 일괄 저장
        } catch (IOException | CsvValidationException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 좌표 검색 메소드 수정
    private Double[] getCoordinatesFromAddress(String district, String address, String locationDetail) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            // 1. 자치구명 + 상세위치로 키워드 검색 시도
            if (locationDetail != null) {
                String cleanAddress = preprocessAddress(district + " " + locationDetail);
                Double[] coordinates = tryKeywordSearch(cleanAddress, restTemplate, entity);
                if (coordinates != null) {
                    log.info("자치구명 + 상세위치로 좌표 검색 성공: " + cleanAddress);
                    return coordinates;
                }
            }

            // 2. 주소로 키워드 검색 시도
            if (address != null) {
                String cleanAddress = address.replaceAll("\\([^)]*\\)", "").trim();
                Double[] coordinates = tryKeywordSearch(cleanAddress, restTemplate, entity);
                if (coordinates != null) {
                    log.info("주소로 좌표 검색 성공: " + cleanAddress);
                    return coordinates;
                }

                // 3. 주소 검색 시도
                coordinates = tryAddressSearch(cleanAddress, restTemplate, entity);
                if (coordinates != null) {
                    log.info("주소로 좌표 검색 성공: " + cleanAddress);
                    return coordinates;
                }
            }

            System.out.println("모든 검색 방법 실패: " + address);
            return null;
            
        } catch (Exception e) {
            System.out.println("API 호출 중 예외 발생: 주소: " + address + ", 예외 메시지: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String preprocessAddress(String address) {
        return address.replaceAll("\\([^)]*\\)", "")  // 괄호와 괄호 안의 내용 제거
                     .replaceAll("\\s*(|~|,|1층|2층|3층|4층|5층|6층|7층|동측|서측|남측|북측|출입문|전면보도|측면부|전면부|\r\n" + //
                                "지상|지하|옥상|옥외의 일부|건물|뒷편|테라스|주차장|우측|좌측|횡단보도|상가지역\r\n" + //
                                "|도로변|가게|모퉁이|전신주|코너|건너편|본관과 신관 사이|정문|후문|본관|신관|앞|뒤|뒤편|옆|내|측|좌|우|상|하)\\s*", " ")  // 불필요한 위치 표현 제거
                     .replaceAll("\\s+", " ")  // 연속된 공백을 하나로
                     .trim();  // 앞뒤 공백 제거
    }

    // 주소 검색 시도
    private Double[] tryAddressSearch(String address, RestTemplate restTemplate, HttpEntity<String> entity) {
        try {
            String url = kakaoGeocodeUrl + address;
            ResponseEntity<String> response = 
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return extractCoordinates(response.getBody());
            }
        } catch (Exception e) {
            log.warn("주소 검색 실패: " + address, e);
        }
        return null;
    }

    // 키워드 검색 시도
    private Double[] tryKeywordSearch(String address, RestTemplate restTemplate, HttpEntity<String> entity) {
        try {
            String url = kakaoKeywordUrl + address;
            ResponseEntity<String> response = 
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return extractCoordinates(response.getBody());
            }
        } catch (Exception e) {
            log.warn("키워드 검색 실패: " + address, e);
        }
        return null;
    }

    // JSON 응답에서 좌표 추출
    private Double[] extractCoordinates(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode documents = root.get("documents");
            
            if (documents != null && documents.size() > 0) {
                JsonNode location = documents.get(0);
                Double latitude = location.get("y").asDouble();
                Double longitude = location.get("x").asDouble();
                return new Double[]{latitude, longitude};
            }
        } catch (Exception e) {
            log.warn("좌표 추출 실패", e);
        }
        return null;
    }
}