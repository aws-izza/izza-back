package com.izza.support;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 데이터베이스 테스트를 위한 베이스 클래스
 * PostGIS 확장이 포함된 PostgreSQL 컨테이너를 제공
 */
@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class DatabaseTestSupport {

    @Container
    static GenericContainer<?> postgis = new GenericContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.3"))
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "izza_test")
            .withEnv("POSTGRES_USER", "test")
            .withEnv("POSTGRES_PASSWORD", "test")
            .withReuse(false)
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2));

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected DataSource dataSource;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> 
            String.format("jdbc:postgresql://localhost:%d/izza_test", 
                         postgis.getMappedPort(5432)));
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }


    @BeforeEach
    void setUp() {
        // SQL 파일에서 DDL 실행
        executeSqlFile("test-schema.sql");

        // 각 테스트 전에 실행할 공통 설정
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    /**
     * SQL 파일을 읽어서 실행
     */
    private void executeSqlFile(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            String sql = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .filter(line -> !line.trim().startsWith("--") && !line.trim().isEmpty())
                    .collect(Collectors.joining("\n"));
            
            // SQL 문을 세미콜론으로 분리하여 실행
            String[] statements = sql.split(";");
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    try {
                        jdbcTemplate.execute(trimmedStatement);
                    } catch (Exception e) {
                        // PostGIS 확장이 이미 존재하는 경우 등 무시할 수 있는 에러
                        if (!e.getMessage().contains("already exists")) {
                            throw e;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL file: " + fileName, e);
        }
    }

    /**
     * 서브클래스에서 오버라이드하여 테스트 데이터 설정
     */
    protected void setupTestData() {
        // 실제 테스트 시나리오 데이터 설정
        insertTestBeopjungDongs();  // 행정구역 데이터 먼저 삽입
        insertIndustrialTestLands();  // 공업지역 토지
        insertCommercialTestLands();  // 상업지역 토지
        insertResidentialTestLands(); // 주거지역 토지
        
        // 토지 통계 데이터
        insertTestStatistics("LAND_AREA", 112L, 2037L);
        insertTestStatistics("OFFICIAL_LAND_PRICE", 293900L, 45010000L);
    }

    /**
     * 테스트 데이터 정리
     */
    protected void cleanupTestData() {
        jdbcTemplate.execute("TRUNCATE TABLE land CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE beopjeong_dong CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE land_statistics CASCADE");
    }

    /**
     * 테스트용 토지 데이터 삽입 헬퍼 메서드 (실제 데이터 기반)
     */
    protected void insertTestLand(String uniqueNo, String beopjungDongCode, String address, 
                                 Double landArea, Long officialPrice, Integer useDistrictCode,
                                 String landCategoryName, Double lng, Double lat, String boundary) {
        String sql = """
            INSERT INTO land (
                unique_no, full_code, address, land_area, official_land_price, 
                use_district_code1, land_category_name, boundary, center_point, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 4326), ST_Point(?, ?, 4326), NOW(), NOW())
            """;
        
        jdbcTemplate.update(sql, uniqueNo, beopjungDongCode, address, landArea, officialPrice,
                           useDistrictCode, landCategoryName, boundary, lng, lat);
    }

    /**
     * 실제 테스트 시나리오 데이터 삽입 (공업지역)
     */
    protected void insertIndustrialTestLands() {
        // 대구광역시 동구 봉무동 공업지역 토지 2개
        insertTestLand(
            "5216238",
            "2714010500", 
            "대구광역시 동구 봉무동 1564",
            2037.00, 914900L, 32, "공장용지",
            128.63537596600568, 35.918382496637406,
            "POLYGON((128.6347059660 35.9183574966,128.6354059660 35.9183574966,128.6354059660 35.9184074966,128.6347059660 35.9184074966,128.6347059660 35.9183574966))"
        );
        
        insertTestLand(
            "5216240",
            "2714010500",
            "대구광역시 동구 봉무동 1564-1", 
            1650.00, 888300L, 32, "공장용지",
            128.63584485840758, 35.918382084853306,
            "POLYGON((128.6351484584 35.9183320849,128.6365484584 35.9183320849,128.6365484584 35.9184320849,128.6351484584 35.9184320849,128.6351484584 35.9183320849))"
        );
    }

    /**
     * 실제 테스트 시나리오 데이터 삽입 (상업지역)
     */
    protected void insertCommercialTestLands() {
        // 서울특별시 서초구 반포동 상업지역 토지 2개
        insertTestLand(
            "5030679",
            "1165010700",
            "서울특별시 서초구 반포동 706-8",
            852.50, 43990000L, 22, "대",
            127.02085650236738, 37.51071969183249,
            "POLYGON((127.0198565024 37.5106196918,127.0218565024 37.5106196918,127.0218565024 37.5108196918,127.0198565024 37.5108196918,127.0198565024 37.5106196918))"
        );
        
        insertTestLand(
            "5030681", 
            "1165010700",
            "서울특별시 서초구 반포동 706-9",
            549.70, 45010000L, 22, "대", 
            127.02105871912474, 37.510655528139225,
            "POLYGON((127.0200587191 37.5105555281,127.0220587191 37.5105555281,127.0220587191 37.5107555281,127.0200587191 37.5107555281,127.0200587191 37.5105555281))"
        );
    }

    /**
     * 실제 테스트 시나리오 데이터 삽입 (주거지역)
     */
    protected void insertResidentialTestLands() {
        // 서울특별시 동작구 흑석동 주거지역 토지 2개
        insertTestLand(
            "5030678",
            "1159010500", 
            "서울특별시 동작구 흑석동 61-3",
            595.00, 293900L, 13, "종교용지",
            126.96263999295964, 37.49867054453847,
            "POLYGON((126.9616399930 37.4985705445,126.9636399930 37.4985705445,126.9636399930 37.4987705445,126.9616399930 37.4987705445,126.9616399930 37.4985705445))"
        );
        
        insertTestLand(
            "5030680",
            "1159010500",
            "서울특별시 동작구 흑석동 61-12",
            112.00, 5943000L, 14, "임야",
            126.96160434007456, 37.49986309035722,
            "POLYGON((126.9606043401 37.4997630904,126.9626043401 37.4997630904,126.9626043401 37.4999630904,126.9606043401 37.4999630904,126.9606043401 37.4997630904))"
        );
    }

    /**
     * 테스트용 행정구역 데이터 삽입 헬퍼 메서드 (실제 DDL 기반)
     */
    protected void insertTestBeopjungDong(String fullCode, String parentCode, String beopjungDongName, 
                                         String sido, String sig, String emd, String dongType, String englishName,
                                         Double lng, Double lat, String boundary) {
        String sql = """
            INSERT INTO beopjeong_dong (full_code, parent_code, beopjung_dong_name, sido, sig, emd, 
                                       dong_type, english_name, center_point, boundary)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_Point(?, ?, 4326), decode(?, 'hex'))
            """;
        
        jdbcTemplate.update(sql, fullCode, parentCode, beopjungDongName, sido, sig, emd, 
                           dongType, englishName, lng, lat, boundary);
    }

    /**
     * 실제 테스트 시나리오 행정구역 데이터 삽입 (실제 데이터 기반)
     */
    protected void insertTestBeopjungDongs() {
        // 서울특별시 (SIDO)
        insertTestBeopjungDong(
            "1100000000", null, "서울특별시", "서울특별시", null, null, "SIDO", "Seoul",
            126.99182089979709, 37.551917681786264,
            "0103000020E61000000100000004000000EC0AE4501BC85F400A4B56E8FBBB42405661214EEDB05F406584265816C742406E5336D330C55F40637256081BD94240EC0AE4501BC85F400A4B56E8FBBB4240"
        );
        
        // 대구광역시 (SIDO)
        insertTestBeopjungDong(
            "2700000000", null, "대구광역시", "대구광역시", null, null, "SIDO", "Daegu",
            128.5993573146082, 35.969839556179494,
            "0103000020E610000001000000090000000575340B210F60406BA6F177AEEA414062A01704610D6040037758E5ECF541406A8654B380146040CA6C8E10590142402C811F0C540D604041268E6F3C2642407BD7B94D431C604081AF400E641942402E9273FF841660409439A9E36C074240586D8519DD1560407227859E58DC4140F768187AED0B6040B06FDDA8E1CD41400575340B210F60406BA6F177AEEA4140"
        );
        
        // 대구광역시 동구 (SIG)
        insertTestBeopjungDong(
            "2714000000", "2700000000", "대구광역시 동구", "대구광역시", "동구", null, "SIG", "Dong-gu",
            128.68564987549834, 35.93444401364049,
            "0103000020E61000000100000004000000007C4EF845166040A1A8244FFE0142400D438362A217604025C51957F7EC4140EE0C5D36691360405396414F39F04140007C4EF845166040A1A8244FFE014240"
        );
        
        // 서울특별시 동작구 (SIG)
        insertTestBeopjungDong(
            "1159000000", "1100000000", "서울특별시 동작구", "서울특별시", "동작구", null, "SIG", "Dongjak-gu",
            126.95164017998083, 37.4988763555847,
            "0103000020E6100000010000000700000010A613F219BD5F409536445B0DC242409B04AA8C10BF5F40E0358E70FBBF4240A29FDB07D4BE5F40E77700F4FEBC4240068D7A6175BD5F4050F570FE32BF424072D99301CEB95F406C84569114BE4240F59663B551BB5F400ED10E5CFDC1424010A613F219BD5F409536445B0DC24240"
        );
        
        // 서울특별시 동작구 흑석동 (EMD)
        insertTestBeopjungDong(
            "1159010500", "1159000000", "서울특별시 동작구 흑석동", "서울특별시", "동작구", "흑석동", "EMD", "Heukseok-dong",
            126.96233712077975, 37.50549296490534,
            "0103000020E61000000100000004000000F9B101848FBD5F4047528D36ADC14240D191B0FA56BE5F4024480A20F5C04240AC6B346290BD5F40611A8143ACBF4240F9B101848FBD5F4047528D36ADC14240"
        );
        
        // 서울특별시 서초구 (SIG)
        insertTestBeopjungDong(
            "1165000000", "1100000000", "서울특별시 서초구", "서울특별시", "서초구", null, "SIG", "Seocho-gu",
            127.0312084042188, 37.473297931626604,
            "0103000020E6100000010000000A0000002F35E6CDD7C05F403406FA49E4C242405D3242943FC35F40F0CAAA5AD0BB4240245B768668C55F40C97C80EAE0BC42403DCBAE901FC65F40E738862F02BB4240CE0A657C8AC45F40CDC5C13811B74240C38103D746C25F40FF53494931B84240296794010BC25F40E4346B1F96BB4240030D525C39BF5F40FF585270C9BA4240DB65CDEEBEBE5F409A611F74D6C042402F35E6CDD7C05F403406FA49E4C24240"
        );
        
        // 서울특별시 서초구 반포동 (EMD)
        insertTestBeopjungDong(
            "1165010700", "1165000000", "서울특별시 서초구 반포동", "서울특별시", "서초구", "반포동", "EMD", "Banpo-dong",
            127.00076346395392, 37.503964469600106,
            "0103000020E610000001000000050000001AFE085E67BF5F40888A931EADC1424023682D2991C15F40D341C9B892C042406A34A8D204C05F4068B9D7C7F3BE4240C050F1C4BBBE5F4070A08D5569C042401AFE085E67BF5F40888A931EADC14240"
        );
    }

    /**
     * 테스트용 토지 통계 데이터 삽입 헬퍼 메서드
     */
    protected void insertTestStatistics(String statType, Long minValue, Long maxValue) {
        String sql = """
            INSERT INTO land_statistics (stat_type, min_value, max_value, updated_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (stat_type) 
            DO UPDATE SET min_value = EXCLUDED.min_value, 
                         max_value = EXCLUDED.max_value, 
                         updated_at = CURRENT_TIMESTAMP
            """;
        
        jdbcTemplate.update(sql, statType, minValue, maxValue);
    }
}