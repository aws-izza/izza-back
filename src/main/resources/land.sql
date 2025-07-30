create table land
(
    id                   bigint generated always as identity
        primary key,
    shape_id             bigint,
    unique_no            varchar(20)             not null
        unique,
    full_code            varchar(10),
    address              varchar(128),
    ledger_division_code smallint,
    ledger_division_name varchar(10),
    base_year            smallint,
    base_month           smallint,
    land_category_code   smallint,
    land_category_name   varchar(20),
    land_area            numeric(12, 2),
    use_district_code1   smallint,
    use_district_name1   varchar(50),
    use_district_code2   smallint,
    use_district_name2   varchar(50),
    land_use_code        smallint,
    land_use_name        varchar(20),
    terrain_height_code  smallint,
    terrain_height_name  varchar(20),
    terrain_shape_code   smallint,
    terrain_shape_name   varchar(20),
    road_side_code       smallint,
    road_side_name       varchar(20),
    official_land_price  numeric(10),
    data_standard_date   timestamp,
    boundary             geometry(Polygon, 4326) not null,
    center_point         geometry(Point, 4326),
    created_at           timestamp default CURRENT_TIMESTAMP,
    updated_at           timestamp default CURRENT_TIMESTAMP
);



create table beopjeong_dong (
    full_code               varchar(10) not null primary key,
    parent_code             varchar(10),
    beopjung_dong_name      text,                      -- 법정동명
    sido                    text,                      -- 시도
    sig                     text,                      -- 시군구
    emd                     text,                      -- 읍면동
    ri                      text,                      -- 리
    dong_type               char(4),                   -- SIDO(시도), SIG(시군구), EMD(읍면동)
    english_name            text,                      -- area_polygon.english_name
    boundary                geometry(Geometry, 4326)   -- area_polygon.geometry
);

