-- auto-generated definition
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

-- land 테이블 공간 인덱스
create index idx_land_boundary_gist on land using gist(boundary);
create index idx_land_full_code on land(full_code);
create index idx_land_center_point_gist on land using gist(center_point);



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

-- beopjeong_dong 테이블 공간 인덱스
create index idx_beopjeong_dong_boundary_gist on beopjeong_dong using gist(boundary);

create table substation
(
    osm_id     varchar(20),
    name       varchar(64),
    voltage    int,
    center_point   geometry(Point, 4326),
    boundary   geometry(Polygon, 4326)
);

-- substation 테이블 공간 인덱스
create index idx_substation_center_point_gist on substation using gist(center_point);
create index idx_substation_boundary_gist on substation using gist(boundary);

create table transmission_line
(
    osm_id      varchar(20),
    name        varchar(128),
    voltage     int,
    cables      int,
    circuits    int,
    other_tags        text,
    geometry    geometry(LineString, 4326)
);

-- transmission_line 테이블 공간 인덱스
create index idx_transmission_line_geometry_gist on transmission_line using gist(geometry);

create table transmission_tower
(
    osm_id      varchar(20),
    other_tags  text,
    geometry    geometry(Point, 4326)
);

-- transmission_tower 테이블 공간 인덱스
create index idx_transmission_tower_geometry_gist on transmission_tower using gist(geometry);

create table land_power_infrastructure_proximity (
                                                     id bigint generated always as identity primary key,
                                                     land_id bigint references land(id),
                                                     infrastructure_type varchar(20) not null, -- 'substation', 'transmission_line', 'transmission_tower'
                                                     infrastructure_osm_id varchar(20) not null,
                                                     distance_meters numeric(10,2) not null,
                                                     voltage int,
                                                     additional_info jsonb, -- 인프라별 추가 정보 (cables, circuits, material 등)
                                                     created_at timestamp default CURRENT_TIMESTAMP,
                                                     updated_at timestamp default CURRENT_TIMESTAMP
);

-- 성능을 위한 인덱스
create index idx_land_power_proximity_land_id on land_power_infrastructure_proximity(land_id);
