# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building and Running
```bash
# Build the project
./gradlew build

# Run the application locally
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.izza.search.persistent.dao.LandDaoTest"

# Run tests with detailed output
./gradlew test --info
```

### Code Quality
```bash
# Check for compilation errors
./gradlew compileJava

# Build without running tests
./gradlew build -x test
```

## Project Architecture

### Core Modules
This is a Spring Boot application for real estate (토지/land) search and analysis with the following main modules:

1. **Search Module** (`com.izza.search`): Geographic land search and mapping functionality
   - Map-based search with zoom level clustering (ZoomLevel.LAND vs ZoomLevel.GROUP)
   - PostGIS integration for spatial queries and polygon data
   - Administrative district (beopjung-dong) based grouping
   - Land filtering by area, price, and use zone categories

2. **Analysis Module** (`com.izza.analysis`): Land scoring and evaluation system
   - Multi-criteria land analysis with configurable weights
   - Scoring metrics: land area, official land price, use zone compliance
   - Infrastructure analysis: power facilities, population density
   - Safety analysis: disaster alerts, policy support

### Key Domain Models

#### Land Entity
Central entity representing individual land parcels with:
- Geographic data (PostGIS geometry: boundary polygons, center points)
- Administrative codes (beopjung-dong, use district codes)
- Physical attributes (area, terrain, road access)
- Economic data (official land price)
- Categorical data (land category, use zone, land use status)

#### Enum Value Objects
Comprehensive enum system in `com.izza.search.vo` for Korean land classification:
- `LandCategoryCode`: 지목 (land category) - 28 types from agricultural to industrial
- `UseZoneCode`: 용도지역 (use zone) - zoning classifications for development
- `UseDistrictCode`: 용도지구 (use district) - special district designations
- `RoadAccessCode`: 도로접면 (road access) - road frontage classifications
- `TerrainHeightCode`: 지형고저 (terrain height) - topographic classifications
- `LandUseStatusCode`: 토지이용상황 (land use status) - current land utilization

Each enum includes:
- Korean name mappings
- Industrial suitability indicators (`isLargeScaleIndustrialSuitable()`)
- Category-specific utility methods
- Code/name conversion helpers

### Data Access Layer
Uses Spring JDBC with custom DAO implementations:
- **LandDao**: Complex spatial queries, filtering, and statistics
- **BeopjungDongDao**: Administrative district queries with polygon data
- **LandStatisticsDao**: Precomputed statistics for land area/price ranges
- **ElectricityCostDao, PopulationDao, EmergencyTextDao**: Supporting data for analysis

### Database Configuration
- PostgreSQL with PostGIS extension for spatial data
- AWS Secrets Manager integration for production credentials
- Testcontainers with PostGIS for integration testing
- Connection pooling via HikariCP

### API Structure
REST endpoints in `com.izza.search.presentation`:
- `MapSearchController`: Geographic search and polygon retrieval
- `BaseInfoController`: Metadata and lookup data

Request/Response DTOs follow the pattern:
- Request DTOs for input validation and mapping
- Response DTOs for API output formatting
- Internal Query DTOs for DAO layer

## Important Development Notes

### Spatial Data Handling
- All geometric data uses SRID 4326 (WGS84)
- PostGIS functions for spatial queries: `ST_Contains`, `ST_Intersects`, etc.
- Zoom level determines query granularity (individual lands vs. grouped districts)

### Enum Code Management
- Korean land classification codes must match government standards
- Industrial suitability flags are critical for analysis features
- Always use enum factory methods (`fromCode()`, `fromName()`) for safe lookups

### Testing Strategy
- Integration tests use PostGIS-enabled Testcontainers
- `DatabaseTestSupport` base class provides test infrastructure
- Test data helpers for creating land and administrative district records
- Tests verify both business logic and spatial query correctness

### Analysis Scoring System
The analysis module implements a standardized scoring formula:
```
score = base_score + normalized_value * (1 - base_score)
```
Where `base_score = 0.5` ensures all metrics have a minimum baseline score.

### Configuration Profiles
- `local`: Development with local PostgreSQL (port 15432)
- `prod`: Production with AWS RDS and Secrets Manager
- Test profile uses Testcontainers with PostGIS

## Common Patterns

### Error Handling
- Use `Optional<T>` for DAO queries that may return no results
- Convert empty Optional to `IllegalArgumentException` in services
- Comprehensive logging with structured parameters for debugging

### Spatial Query Patterns
- Bounding box queries use SW/NE coordinate pairs
- Always specify SRID in PostGIS functions
- Use GIST indexes for geometry columns for performance

### Enum Usage
```java
// Safe enum lookup with error handling
UseZoneCode zoneCode = UseZoneCode.fromCode(code);

// Industrial suitability check
if (landCategory.isLargeScaleIndustrialSuitable()) {
    // Handle industrial land logic
}
```

### Land Statistics Management
Range statistics (area/price) are stored in `land_statistics` table for performance:
- Precomputed min/max values for land area and official land price
- Updated via batch jobs (separate from API operations)
- Used by analysis and search modules for normalization

### Lombok Usage Guidelines
When using `@Builder`, always attach it directly to the all-arguments constructor:
```java
@Data
public class MyClass {
    private String field1;
    private Integer field2;
    
    public MyClass() {}
    
    @Builder
    public MyClass(String field1, Integer field2) {
        this.field1 = field1;
        this.field2 = field2;
    }
}
```