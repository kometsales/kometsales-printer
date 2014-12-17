package vertical.fl.kometPrinter.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.DeleteItemRequest;
import com.amazonaws.services.dynamodb.model.DeleteItemResult;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.TableStatus;

public class MemoryDBManager {

    private AmazonDynamoDBClient dynamoDB     = new AmazonDynamoDBClient(new BasicAWSCredentials(getAwsAccessKey(), getAwsSecretKey()));

    private String               tableName    = PropertiesReader.getProperties("dynamo.table.name");

    private static Logger        logger       = Logger.getLogger(MemoryDBManager.class);

    private String               awsAccessKey = PropertiesReader.getProperties("accessKey.printing");

    private String               awsSecretKey = PropertiesReader.getProperties("secretKey.printing");

    public String getAwsAccessKey() {
        awsAccessKey = PropertiesReader.getProperties("accessKey.printing");
        return awsAccessKey;
    }

    public String getAwsSecretKey() {
        awsSecretKey = PropertiesReader.getProperties("secretKey.printing");
        return awsSecretKey;
    }

    public AmazonDynamoDBClient getDynamoDB() {
        return dynamoDB;
    }

    @SuppressWarnings("unused")
	public void initDB() {
        if (!isTableCreated()) {
            // Create a table with a primary key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(getTableName())
                    .withKeySchema(new KeySchema(new KeySchemaElement().withAttributeName("name").withAttributeType("S")))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L));
            TableDescription createdTableDescription = getDynamoDB().createTable(createTableRequest).getTableDescription();
            //logger.debug("Created Table: " + createdTableDescription);
            // Wait for it to become active
            waitForTableToBecomeAvailable(getTableName());
        }
    }

    private boolean isTableCreated() {
        boolean tableExists = false;
        try {
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(getTableName());
            TableDescription tableDescription = getDynamoDB().describeTable(describeTableRequest).getTable();
            logger.debug("Table Description: " + tableDescription);
            tableExists = true;
        } catch (AmazonServiceException e) {
            logger.debug("[Table: " + tableName + "] not found.");
        }
        return tableExists;
    }

    private void waitForTableToBecomeAvailable(String tableName) {
        logger.debug("Waiting for " + tableName + " to become ACTIVE...");
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(100 * 20);
            } catch (Exception e) {
            }
            try {
                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                TableDescription tableDescription = getDynamoDB().describeTable(request).getTable();
                String tableStatus = tableDescription.getTableStatus();
                logger.debug("  - current state: " + tableStatus);
                if (tableStatus.equals(TableStatus.ACTIVE.toString()))
                    return;
            } catch (AmazonServiceException ase) {
                logger.debug("[Table: " + tableName + "] is not yet available...");
            }
        }
    }

    public void addRow(String id, String value) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("name", new AttributeValue(id));
        item.put("value", new AttributeValue(value));
        logger.debug("Adding row item: " + id);
        PutItemRequest putItemRequest = new PutItemRequest(getTableName(), item);
        PutItemResult putItemResult = getDynamoDB().putItem(putItemRequest);
        logger.debug("AddRow Result: " + putItemResult);
    }

    public boolean getRow(String valueId) {
        boolean isRowExists = false;
        GetItemRequest getItemRequest = new GetItemRequest().withTableName(tableName).withKey(new Key().withHashKeyElement(new AttributeValue().withS(valueId)))
                .withAttributesToGet(Arrays.asList("name", "value"));
        try {
            GetItemResult result = getDynamoDB().getItem(getItemRequest);
            Map<String, AttributeValue> resultMap = result.getItem();
            if (null != resultMap && resultMap.containsKey("name")) {
                isRowExists = (valueId.equals(resultMap.get("name").getS()));
            }
        } catch (AmazonServiceException e) {
            logger.debug("Item not found on table: " + tableName);
        } catch (AmazonClientException e) {
            logger.debug("Item not found on table: " + tableName);
        }
        logger.debug("Querying for messageId : " + valueId);
        logger.debug("Register previously inserted? : " + isRowExists);
        return isRowExists;
    }

    @SuppressWarnings("unused")
	public void deleteRow(String id) {
        
        DeleteItemRequest deleteItemRequest = new DeleteItemRequest().withTableName(tableName);
        Key aKey = new Key();
        aKey.setHashKeyElement(new AttributeValue(id));
        deleteItemRequest.setKey(aKey);
        DeleteItemResult aDeleteItemResult = getDynamoDB().deleteItem(deleteItemRequest);
        logger.debug(">>>>>> Message deleted from DB : " + id);
    }

    public String getTableName() {
        return tableName;
    }

}
