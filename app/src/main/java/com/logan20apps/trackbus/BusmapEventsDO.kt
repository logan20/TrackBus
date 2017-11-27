package com.logan20apps.trackbus


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by kwasi on 30/10/2017.
 */


@DynamoDBTable(tableName = "trackbus-mobilehub-185524515-busmap_events")


class BusmapEventsDO {
    @get:DynamoDBHashKey(attributeName = "location_origin")
    @get:DynamoDBAttribute(attributeName = "location_origin")
    var locationOrigin: String? = null
    @get:DynamoDBRangeKey(attributeName = "timestamp_origin")
    @get:DynamoDBAttribute(attributeName = "timestamp_origin")
    var timestampOrigin: Double? = null
    @get:DynamoDBAttribute(attributeName = "location_destination")
    var locationDestination: String? = null
    @get:DynamoDBAttribute(attributeName = "path_data")
    var pathData: String? = null
    @get:DynamoDBAttribute(attributeName = "timestamp_destination_estimate")
    var timestampDestinationEstimate: Double? = null
    @get:DynamoDBAttribute(attributeName = "userId")
    var userId: String? = null

}
