package com.izza.search.persistent.dao;

import com.izza.search.persistent.model.LandDataRange;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class LandDataRangeDynamoDao {

    private final DynamoDbTable<LandDataRange> table;

    public LandDataRangeDynamoDao(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("land-data-ranges", 
                TableSchema.fromBean(LandDataRange.class));
    }

    public Optional<LandDataRange> findByRangeType(String rangeType) {
        Key key = Key.builder()
                .partitionValue(rangeType)
                .build();
        
        LandDataRange item = table.getItem(key);
        return Optional.ofNullable(item);
    }

    public void save(LandDataRange landDataRange) {
        table.putItem(landDataRange);
    }

    public void deleteByRangeType(String rangeType) {
        Key key = Key.builder()
                .partitionValue(rangeType)
                .build();
        table.deleteItem(key);
    }
}