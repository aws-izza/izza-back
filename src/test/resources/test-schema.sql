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

-- 실제 DDL 기반 beopjeong_dong 테이블
CREATE TABLE IF NOT EXISTS beopjeong_dong (
    full_code varchar(10) not null primary key,
    parent_code varchar(10),
    beopjung_dong_name text,
    sido text,
    sig text,
    emd text,
    ri text,
    dong_type VARCHAR(4),
    english_name text,
    boundary geometry(Geometry, 4326),
    center_point geometry(Point, 4326)
);

-- 테스트용 land_statistics 테이블
CREATE TABLE IF NOT EXISTS land_statistics (
    stat_type VARCHAR(50) PRIMARY KEY,
    min_value BIGINT NOT NULL,
    max_value BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
-- CREATE INDEX IF NOT EXISTS idx_land_beopjung_dong_code ON land(beopjung_dong_code);
-- CREATE INDEX IF NOT EXISTS idx_land_boundary ON land USING GIST(boundary);
-- CREATE INDEX IF NOT EXISTS idx_land_center_point ON land USING GIST(center_point);
-- CREATE INDEX IF NOT EXISTS idx_land_use_district_code1 ON land(use_district_code1);
-- CREATE INDEX IF NOT EXISTS idx_beopjeong_dong_full_code ON beopjeong_dong(full_code);
-- CREATE INDEX IF NOT EXISTS idx_beopjeong_dong_boundary ON beopjeong_dong USING GIST(boundary);
-- CREATE INDEX IF NOT EXISTS idx_beopjeong_dong_center_point ON beopjeong_dong USING GIST(center_point);
-- CREATE INDEX IF NOT EXISTS idx_beopjeong_dong_dong_type ON beopjeong_dong(dong_type);