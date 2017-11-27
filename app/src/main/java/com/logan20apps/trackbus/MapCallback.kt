package com.logan20apps.trackbus

import android.app.Activity
import android.widget.TextView
import android.widget.Toast
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject

/**
 *
 * Created by kwasi on 29/10/2017.
 */
class MapCallback(val cx : Activity) : OnMapReadyCallback {
    private var map : GoogleMap?=null
    private var items : ArrayList<BusmapEventsDO> = ArrayList()

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map!!.clear()

        Toast.makeText(cx,"Please wait while loading active buses",Toast.LENGTH_LONG).show()
        //find all buses that were recorded in the last 2 hours

        val it1 = BusmapEventsDO()
        it1.locationOrigin="UWI"

        val it2= BusmapEventsDO()
        it2.locationOrigin="King's Wharf"

        val attr1 = AttributeValue()
        attr1.n=System.currentTimeMillis().toString()

        val attr2 = AttributeValue()
        attr2.n=(System.currentTimeMillis() - (3L * 60L * 60L * 1000L)).toString()

        val condition = Condition()
                .withComparisonOperator(ComparisonOperator.BETWEEN)
                .withAttributeValueList(attr2,attr1)

        val queryExpression = arrayListOf<DynamoDBQueryExpression<BusmapEventsDO>>(DynamoDBQueryExpression<BusmapEventsDO>()
                .withHashKeyValues(it2)
                .withRangeKeyCondition("timestamp_origin",condition)
                .withConsistentRead(true)
        ,
                DynamoDBQueryExpression<BusmapEventsDO>()
                        .withHashKeyValues(it1)
                        .withRangeKeyCondition("timestamp_origin",condition)
                        .withConsistentRead(true))

        val credentialsProvider = IdentityManager.getDefaultIdentityManager().credentialsProvider
        val dynamoDBClient = AmazonDynamoDBClient(credentialsProvider)
        val dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSConfiguration(cx.applicationContext))
                .build()


        Thread{
            try{
                queryExpression
                        .map { dynamoDBMapper.query(BusmapEventsDO::class.java, it) }
                        .flatMap { it }
                        .forEach { items.add(it) }

                cx.runOnUiThread({
                    makeMarkers(items)
                })

            }
            catch (e:Exception){
                e.printStackTrace()
                cx.runOnUiThread({
                    Toast.makeText(cx.applicationContext,"Cannot get results at this moment. Please check the internet connection and try again",Toast.LENGTH_SHORT).show()
                    (cx as MainActivity).home()
                })

            }

        }.start()


    }

    private fun makeMarkers(items: ArrayList<BusmapEventsDO>) {
        val arrList : ArrayList<ArrayList<LatLng>> = ArrayList()
        if (items.size==0){
            Toast.makeText(cx.applicationContext,"No active buses to show",Toast.LENGTH_SHORT).show()
            return
        }
        for (item in items){
            try{
                val time = System.currentTimeMillis()
                val jsonObj = JSONObject(item.pathData)
                val rows = jsonObj.getJSONArray("rows")
                val rowarr = rows.getJSONObject(0)
                val elements = rowarr.getJSONArray("elements")
                var startTime = item.timestampOrigin!!.toLong()
                val destination = item.locationDestination
                var curr = 0
                var prev = 0
                val toUwi = cx.resources.getStringArray(R.array.latlong_values_sando_uwi)
                val toSando = cx.resources.getStringArray(R.array.latlong_values_uwi_sando)
                if (destination=="UWI"){
                    while(curr<elements.length() && startTime<= time){
                        val jobj = elements.getJSONObject(curr)
                        startTime+=jobj.getJSONObject("duration").getInt("value")*1000L
                        prev=curr
                        curr++
                    }
                    val vals = arrayListOf<String>(toUwi.get(prev),toUwi.get(curr))

                    val aList = ArrayList<LatLng>()
                    val it = vals.get(0).split(",")
                    val it2 = vals.get(1).split(",")
                    aList.add(LatLng(it2[0].toDouble(),it2[1].toDouble()))
                    aList.add(LatLng(it[0].toDouble(),it[1].toDouble()))
                    arrList.add(aList)

                }
                else{
                    toSando.reverse()
                    while(curr<elements.length() && startTime<= time){
                        val jobj = elements.getJSONObject(curr)
                        startTime+=jobj.getJSONObject("duration").getInt("value")*1000L
                        prev=curr
                        curr++
                    }
                    val vals = arrayListOf<String>(toSando.get(prev),toSando.get(curr))

                    val aList = ArrayList<LatLng>()
                    val it = vals.get(1).split(",")
                    val it2 = vals.get(0).split(",")
                    aList.add(LatLng(it[0].toDouble(),it[1].toDouble()))
                    aList.add(LatLng(it2[0].toDouble(),it2[1].toDouble()))
                    arrList.add(aList)

                }
                val builder =  LatLngBounds.builder()

                for (a in arrList){
                    val v1 = a[0]
                    val v2 = a[1]

                    builder.include(v1)
                    builder.include(v2)

                    val m2 = map!!.addMarker(MarkerOptions().position(v2).title("Bus to $destination"))

                    MarkerAnimation.animateMarkerToGB(m2,v1,LatLngInterpolator.Linear())
                }
                val bounds = builder.build()
                map!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0))
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(cx.applicationContext,"Maps can't be loaded at this time",Toast.LENGTH_SHORT).show()
                (cx as MainActivity).home()
            }

        }

    }
}