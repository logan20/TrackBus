package com.logan20apps.trackbus

import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.*
import kotlin.Comparator
import kotlin.concurrent.timer


/**
 * Created by kwasi on 30/10/2017.
 */
class HomeAdapter(val activity: MainActivity) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var items : ArrayList<BusmapEventsDO>? = null
    var t : Timer
    companion object {
        var error = 0
    }
    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    init{
        t = timer("updaterThread",false,0.toLong(),30000L,{
            update()
        })

    }

    fun cancelTimer(){
        t.cancel()
        t.purge()
    }
    fun update(){
        if (error>0){
            error--
            return
        }
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
                .awsConfiguration(AWSConfiguration(activity.applicationContext))
                .build()


        Thread{
            try{
                val tItems = ArrayList<BusmapEventsDO>()
                queryExpression
                        .map { dynamoDBMapper.query(BusmapEventsDO::class.java, it) }
                        .flatMap { it }
                        .forEach { tItems.add(it) }

                activity.runOnUiThread({
                    items = tItems
                    Collections.sort(items, Comparator<BusmapEventsDO> { p0, p1 ->
                        if (p0.timestampOrigin!! > p1.timestampOrigin!!){
                            return@Comparator -1
                        } else if (p0.timestampOrigin == p1.timestampOrigin){
                            return@Comparator 0
                        }
                        return@Comparator 1
                    })
                    try{
                        activity.findViewById<ProgressBar>(R.id.pb_pb).visibility=View.INVISIBLE
                        if (items!!.size==0){
                            activity.findViewById<TextView>(R.id.tv_noitems).visibility=View.VISIBLE
                        }
                        else{
                            activity.findViewById<TextView>(R.id.tv_noitems).visibility=View.INVISIBLE
                        }
                        notifyDataSetChanged()
                    }
                    catch (e:Exception){
                        error++
                    }

                })

            }
            catch (e:Exception){
                error+=2
                e.printStackTrace()
                activity.runOnUiThread({
                    Toast.makeText(activity.applicationContext,"Cannot update. Please check the internet connection and try again", Toast.LENGTH_SHORT).show()
                    (activity).home()
                })

            }

        }.start()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val card = LayoutInflater.from(parent.context).inflate(R.layout.home_card_item,parent,false)

        return ViewHolder(card)

    }

    override fun getItemCount(): Int {
        return if (items==null) 0 else items!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.view.findViewById<TextView>(R.id.tv_destination_location).text=items!![position].locationDestination
        holder.view.findViewById<TextView>(R.id.tv_departed_location).text=items!![position].locationOrigin
        holder.view.findViewById<TextView>(R.id.tv_departed_time).text=getTimeFromMillis(items!![position].timestampOrigin!!)
        holder.view.findViewById<TextView>(R.id.tv_eta).text=getETA(items!![position])
        Picasso.with(activity.applicationContext)
                .load( if (items!![position].locationDestination=="UWI") R.drawable.dest_uwi else R.drawable.dest_sando)
                .fit()
                .centerCrop()
                .into(holder.view.findViewById<ImageView>(R.id.iv_image))


    }

    private fun getETA(item: BusmapEventsDO): String {
        val jsonObj = JSONObject(item.pathData)
        if (jsonObj.has("status") && jsonObj.getString("status")=="REQUEST_DENIED"){
            return "UNKNOWN"
        }
        val rows = jsonObj.getJSONArray("rows")
        val rowarr = rows.getJSONObject(0)
        val elements = rowarr.getJSONArray("elements")
        val jobj = elements.getJSONObject(elements.length() -1)
        val time = item.timestampOrigin!! + (jobj.getJSONObject("duration").getInt("value")*1000L)

        return getTimeFromMillis(time)
    }

    private fun getTimeFromMillis(timestampOrigin: Double): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis=timestampOrigin.toLong()
        var hr = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        var ampm = "am"
        if (hr>=12){
            if (hr>12){
                hr -=12
            }
            ampm="pm"
        }
        return String.format("%02d:%02d %s",hr,min,ampm)
    }


}