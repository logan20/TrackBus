package com.logan20apps.trackbus

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by kwasi on 30/10/2017.
 */

@DynamoDBTable(tableName = "trackbus-mobilehub-185524515-bus_events")

class BusEventsDO {
    @get:DynamoDBHashKey(attributeName = "event_location")
    @get:DynamoDBAttribute(attributeName = "event_location")
    var eventLocation: String? = null
    @get:DynamoDBRangeKey(attributeName = "event_timestamp")
    @get:DynamoDBAttribute(attributeName = "event_timestamp")
    var eventTimestamp: Double? = null
    @get:DynamoDBAttribute(attributeName = "event_description")
    var eventDescription: String? = null
    @get:DynamoDBAttribute(attributeName = "event_destination")
    var eventDestination: String? = null
    @get:DynamoDBAttribute(attributeName = "event_type")
    var eventType: String? = null
    @get:DynamoDBAttribute(attributeName = "userId")
    var userId: String? = null
    @get:DynamoDBAttribute(attributeName = "userName")
    var userName: String? = null

}
