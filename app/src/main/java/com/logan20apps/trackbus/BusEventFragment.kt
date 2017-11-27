package com.logan20apps.trackbus

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import kotlinx.android.synthetic.main.fragment_bus_event.*
import java.net.URL



/**
 * Created by kwasi on 29/10/2017.
 *
 */

class BusEventFragment : Fragment(){
    var userID = ""
    var dynamoDBMapper : DynamoDBMapper?=null
    lateinit var listener: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_bus_event,container,false)
        v.findViewById<Button>(R.id.btn_submit).setOnClickListener{
            submit()
        }

        val credentialsProvider = IdentityManager.getDefaultIdentityManager().credentialsProvider
        userID = IdentityManager.getDefaultIdentityManager().cachedUserID
        val dynamoDBClient = AmazonDynamoDBClient(credentialsProvider)
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSConfiguration(context))
                .build()
        return v
    }


    private fun submit(){
        btn_submit.isEnabled=false
        val busEvent = spnr_bus_event.selectedItem.toString()
        val busLocation = spnr_location.selectedItem.toString()
        val desc = et_description.text.toString()
        val destination = spnr_destination.selectedItem.toString()
        val timestampNow = System.currentTimeMillis().toDouble()


        val dbObj = BusEventsDO()
        dbObj.eventDescription=desc
        dbObj.eventLocation = busLocation
        dbObj.eventType = busEvent
        dbObj.eventTimestamp = timestampNow
        dbObj.userName="REF"
        dbObj.userId = userID
        dbObj.eventDestination = destination

        val attr1 = AttributeValue()
        attr1.n= (System.currentTimeMillis() - (5L * 60L * 1000L)).toString()

        val attr2 = AttributeValue()
        attr2.s=busEvent

        val eav = HashMap<String, AttributeValue>()
        eav.put(":val1", attr2)
        eav.put(":val2",AttributeValue().withS(destination))

        val condition = Condition()
                .withComparisonOperator(ComparisonOperator.GT)
                .withAttributeValueList(attr1)

        val queryExpression = DynamoDBQueryExpression<BusEventsDO>()
                .withHashKeyValues(dbObj)
                .withFilterExpression("event_type = :val1 and event_destination = :val2")
                .withExpressionAttributeValues(eav)
                .withRangeKeyCondition("event_timestamp",condition)
                .withConsistentRead(true)

        Thread{
            val count = dynamoDBMapper?.count(BusEventsDO::class.java,queryExpression)
            if (count != null) {
                if (count>0){
                    activity.runOnUiThread({
                        Toast.makeText(context,"This has been stored already within the last 5 minutes!! Please try again later",Toast.LENGTH_SHORT).show()
                        btn_submit.isEnabled=true
                    })

                }
                else{
                    var busmapobj :BusmapEventsDO? = null;

                    if ((busLocation=="UWI"&& destination=="King's Wharf")||(busLocation=="King's Wharf" && destination=="UWI")){
                        if (busEvent=="Bus Departure"){
                            busmapobj = BusmapEventsDO()
                            busmapobj.locationDestination = destination
                            busmapobj.locationOrigin = busLocation
                            busmapobj.timestampOrigin = timestampNow
                            busmapobj.userId = userID
                        }
                    }
                    Thread{
                        dynamoDBMapper?.save(dbObj, DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.APPEND_SET))
                        if (busmapobj!=null){
                            Thread{
                                busmapobj?.pathData = getData(busmapobj!!)
                                dynamoDBMapper?.save(busmapobj, DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.APPEND_SET))
                            }.start()
                        }
                        try{
                            activity.runOnUiThread({
                                Toast.makeText(context,"Save Successful",Toast.LENGTH_SHORT).show()
                                btn_submit.isEnabled=true
                                listener.home()
                            })
                        }catch (e:Exception){}

                    }.start()
                }
            }
        }.start()


    }

    private fun getData(obj: BusmapEventsDO): String {
        var baseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?"
        val toUwi = resources.getStringArray(R.array.latlong_values_sando_uwi)
        val toSando = resources.getStringArray(R.array.latlong_values_uwi_sando)
        if (obj.locationOrigin=="UWI"){
            baseURL+="origins=${toSando[toSando.size-1]}&destinations="
            for (i in toSando.size -2 downTo  1){
                baseURL+=toSando[i]+"|"
            }
        }
        else{
            baseURL+="origins=${toUwi[0]}&destinations="
            for (i in 1 until toUwi.size){
                baseURL+=toUwi[i]+ "|"
            }
        }
        baseURL = baseURL.removeSuffix("|")
        baseURL+="&key=${getString(R.string.google_maps_key)}"
        Log.w("URL",baseURL)
        return URL(baseURL).readText()

    }

}