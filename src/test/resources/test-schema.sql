-- PostGIS 확장 활성화
CREATE EXTENSION IF NOT EXISTS postgis;

-- 테스트용 간소화된 land 테이블
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
);

-- 테스트용 간소화된 beopjung_dong 테이블
CREATE TABLE IF NOT EXISTS beopjung_dong (
    id BIGSERIAL PRIMARY KEY,
    full_code VARCHAR(10) UNIQUE NOT NULL,
    korean_name VARCHAR(255),
    simple_name VARCHAR(255),
    center_point GEOMETRY(POINT, 4326),
    boundary GEOMETRY(MULTIPOLYGON, 4326),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_land_full_code ON land(full_code);
CREATE INDEX IF NOT EXISTS idx_land_boundary ON land USING GIST(boundary);
CREATE INDEX IF NOT EXISTS idx_land_center_point ON land USING GIST(center_point);
CREATE INDEX IF NOT EXISTS idx_land_use_district_code1 ON land(use_district_code1);
CREATE INDEX IF NOT EXISTS idx_beopjung_dong_full_code ON beopjung_dong(full_code);
CREATE INDEX IF NOT EXISTS idx_beopjung_dong_boundary ON beopjung_dong USING GIST(boundary);