package com.izza.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 데이터베이스 테스트를 위한 베이스 클래스
 * PostGIS 확장이 포함된 PostgreSQL 컨테이너를 제공
 */
@SpringBootTest
@Testcontainers
@Transactional
public abstract class DatabaseTestSupport {

    @Container
    static GenericContainer<?> postgis = new GenericContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.3"))
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "izza_test")
            .withEnv("POSTGRES_USER", "test")
            .withEnv("POSTGRES_PASSWORD", "test")
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2));

    @Autowired
    protected JdbcTemplate jdbcTemplate;

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
        // PostGIS 확장 활성화 (PostGIS 이미지에는 이미 설치되어 있음)
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS postgis");
        } catch (Exception e) {
            // PostGIS가 이미 활성화되어 있을 수 있음
        }
        
        // 테스트 테이블 생성
        createTestTables();
        
        // 각 테스트 전에 실행할 공통 설정
        setupTestData();
    }

    /**
     * 테스트 테이블 생성
     */
    private void createTestTables() {
        // land 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS land (
                id BIGSERIAL PRIMARY KEY,
                shape_id BIGINT,
                unique_no VARCHAR(255) UNIQUE NOT NULL,
                full_code VARCHAR(10),
                address TEXT,
                ledger_division_code SMALLINT,
                ledger_division_name VARCHAR(100),
                base_year SMALLINT,
                base_month SMALLINT,
                land_category_code SMALLINT,
                land_category_name VARCHAR(100),
                land_area DECIMAL(15,2),
                use_district_code1 SMALLINT,
                use_district_name1 VARCHAR(100),
                use_district_code2 SMALLINT,
                use_district_name2 VARCHAR(100),
                land_use_code SMALLINT,
                land_use_name VARCHAR(100),
                terrain_height_code SMALLINT,
                terrain_height_name VARCHAR(100),
                terrain_shape_code SMALLINT,
                terrain_shape_name VARCHAR(100),
                road_side_code SMALLINT,
                road_side_name VARCHAR(100),
                official_land_price DECIMAL(15,0),
                data_standard_date TIMESTAMP,
                boundary GEOMETRY(POLYGON, 4326),
                center_point GEOMETRY(POINT, 4326),
                created_at TIMESTAMP DEFAULT NOW(),
                updated_at TIMESTAMP DEFAULT NOW()
            )
            """);

        // beopjung_dong 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS beopjung_dong (
                id BIGSERIAL PRIMARY KEY,
                full_code VARCHAR(10) UNIQUE NOT NULL,
                korean_name VARCHAR(255),
                simple_name VARCHAR(255),
                center_point GEOMETRY(POINT, 4326),
                boundary GEOMETRY(MULTIPOLYGON, 4326),
                created_at TIMESTAMP DEFAULT NOW(),
                updated_at TIMESTAMP DEFAULT NOW()
            )
            """);

        // 인덱스 생성
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_land_full_code ON land(full_code)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_land_boundary ON land USING GIST(boundary)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_land_center_point ON land USING GIST(center_point)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_land_use_district_code1 ON land(use_district_code1)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_beopjung_dong_full_code ON beopjung_dong(full_code)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_beopjung_dong_boundary ON beopjung_dong USING GIST(boundary)");
    }

    /**
     * 서브클래스에서 오버라이드하여 테스트 데이터 설정
     */
    protected void setupTestData() {
        // 기본 구현은 비어있음
    }

    /**
     * 테스트 데이터 정리
     */
    protected void cleanupTestData() {
        jdbcTemplate.execute("TRUNCATE TABLE land CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE beopjung_dong CASCADE");
    }

    /**
     * 테스트용 토지 데이터 삽입 헬퍼 메서드
     */
    protected void insertTestLand(String uniqueNo, String fullCode, String address, 
                                 Double landArea, Long officialPrice, 
                                 Double lng, Double lat) {
        String sql = """
            INSERT INTO land (unique_no, full_code, address, land_area, official_land_price, 
                             boundary, center_point, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, 
                   ST_GeomFromText('POLYGON((' || ? || ' ' || ? || ',' || 
                                           (? + 0.001) || ' ' || ? || ',' || 
                                           (? + 0.001) || ' ' || (? + 0.001) || ',' || 
                                           ? || ' ' || (? + 0.001) || ',' || 
                                           ? || ' ' || ? || '))', 4326),
                   ST_Point(?, ?, 4326),
                   NOW(), NOW())
            """;
        
        jdbcTemplate.update(sql, uniqueNo, fullCode, address, landArea, officialPrice,
                           lng, lat, lng, lat, lng, lat, lng, lat, lng, lat, lng, lat);
    }

    /**
     * 테스트용 행정구역 데이터 삽입 헬퍼 메서드
     */
    protected void insertTestBeopjungDong(String fullCode, String koreanName, String simpleName,
                                         Double lng, Double lat) {
        String sql = """
            INSERT INTO beopjung_dong (full_code, korean_name, simple_name, center_point, created_at, updated_at)
            VALUES (?, ?, ?, ST_Point(?, ?, 4326), NOW(), NOW())
            """;
        
        jdbcTemplate.update(sql, fullCode, koreanName, simpleName, lng, lat);
    }
}