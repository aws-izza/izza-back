-- 토지대장 테이블 생성 (PostGIS 사용)
CREATE TABLE land (
    -- 기본 식별자
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      shape_id BIGINT,
                      unique_no VARCHAR(20) NOT NULL UNIQUE,
                      beopjung_dong_code VARCHAR(10),

    -- 주소 정보 (법정동명 + 지번 합성)
                      address VARCHAR(128),

    -- 대장 구분
                      ledger_division_code SMALLINT,
                      ledger_division_name VARCHAR(10),

    -- 기준 연월
                      base_year SMALLINT,
                      base_month SMALLINT,

    -- 지목 정보
                      land_category_code SMALLINT,
                      land_category_name VARCHAR(20),

    -- 토지 면적 (㎡)
                      land_area DECIMAL(12,2),

    -- 용도지역 정보
                      use_district_code1 SMALLINT,
                      use_district_name1 VARCHAR(50),
                      use_district_code2 SMALLINT,
                      use_district_name2 VARCHAR(50),

    -- 토지 이용 상황
                      land_use_code SMALLINT,
                      land_use_name VARCHAR(20),

    -- 지형 정보
                      terrain_height_code SMALLINT,
                      terrain_height_name VARCHAR(20),
                      terrain_shape_code SMALLINT,
                      terrain_shape_name VARCHAR(20),

    -- 도로 측면 정보
                      road_side_code SMALLINT,
                      road_side_name VARCHAR(20),

    -- 공시지가 (원/㎡)
                      official_land_price DECIMAL(10,0),

    -- 데이터 기준일자
                      data_standard_date TIMESTAMP,

    -- 경계 정보 (PostGIS Geometry)
                      boundary GEOMETRY(POLYGON, 5186) NOT NULL,

    -- 시스템 필드
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_land_parcel_unique_no ON land(unique_no);


-- 테이블 코멘트
COMMENT ON TABLE land IS '토지대장 정보';
COMMENT ON COLUMN land.shape_id IS '도형ID';
COMMENT ON COLUMN land.unique_no IS '고유번호(PNU)';
COMMENT ON COLUMN land.beopjung_dong_code IS '법정동코드';
COMMENT ON COLUMN land.address IS '주소 (법정동명 + 지번)';
COMMENT ON COLUMN land.ledger_division_code IS '대장구분코드';
COMMENT ON COLUMN land.ledger_division_name IS '대장구분명';
COMMENT ON COLUMN land.lot_number IS '지번';
COMMENT ON COLUMN land.base_year IS '기준연도';
COMMENT ON COLUMN land.base_month IS '기준월';
COMMENT ON COLUMN land.land_category_code IS '지목코드';
COMMENT ON COLUMN land.land_category_name IS '지목명';
COMMENT ON COLUMN land.land_area IS '토지면적(㎡)';
COMMENT ON COLUMN land.use_district_code1 IS '용도지역코드1';
COMMENT ON COLUMN land.use_district_name1 IS '용도지역명1';
COMMENT ON COLUMN land.use_district_code2 IS '용도지역코드2';
COMMENT ON COLUMN land.use_district_name2 IS '용도지역명2';
COMMENT ON COLUMN land.land_use_code IS '토지이용상황코드';
COMMENT ON COLUMN land.land_use_name IS '토지이용상황명';
COMMENT ON COLUMN land.terrain_height_code IS '지형높이코드';
COMMENT ON COLUMN land.terrain_height_name IS '지형높이명';
COMMENT ON COLUMN land.terrain_shape_code IS '지형형상코드';
COMMENT ON COLUMN land.terrain_shape_name IS '지형형상명';
COMMENT ON COLUMN land.road_side_code IS '도로측면코드';
COMMENT ON COLUMN land.road_side_name IS '도로측면명';
COMMENT ON COLUMN land.official_land_price IS '공시지가(원/㎡)';
COMMENT ON COLUMN land.data_standard_date IS '데이터기준일자';
COMMENT ON COLUMN land.boundary IS '경계(POLYGON)';
COMMENT ON COLUMN land.data_standard_date IS '데이터 기준일자';
COMMENT ON COLUMN land.created_at IS '생성일시';
COMMENT ON COLUMN land.updated_at IS '수정일시';


---
create table area_polygon
(
    full_code    varchar(10),
    korean_name  text,
    english_name text,
    type         varchar(1),
    boundary     geometry(Geometry, 5179)
)