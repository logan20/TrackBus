package com.logan20apps.trackbus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by kwasi on 30/10/2017.
 */

class HistoryDetailAdapter(val activity : MainActivity, start: Long, end: Long, location: String) : BaseAdapter(){
    private var items = ArrayList<BusEventsDO>()
    private var toast :Toast?=null
    init{
        //run the query val queryExpression = DynamoDBQueryExpression()

        val it = BusEventsDO()
        it.eventLocation = location

        val attr1 = AttributeValue()
        attr1.n=start.toString()

        val attr2 = AttributeValue()
        attr2.n=end.toString()

        val condition = Condition()
                .withComparisonOperator(ComparisonOperator.BETWEEN)
                .withAttributeValueList(attr1,attr2)

        val queryExpression = DynamoDBQueryExpression<BusEventsDO>()
                .withHashKeyValues(it)
                .withRangeKeyCondition("event_timestamp",condition)
                .withConsistentRead(true)

        val credentialsProvider = IdentityManager.getDefaultIdentityManager().credentialsProvider
        val dynamoDBClient = AmazonDynamoDBClient(credentialsProvider)
        val dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSConfiguration(activity.applicationContext))
                .build()

        Thread{
            try{
                val results = dynamoDBMapper.query(BusEventsDO::class.java,queryExpression)
                if (results.isEmpty()){
                    activity.runOnUiThread({
                        activity.findViewById<TextView>(R.id.tv_noitems).visibility=View.VISIBLE
                        activity.findViewById<ProgressBar>(R.id.pb_hist_details).visibility=View.INVISIBLE
                    })
                }
                else{
                    for (a in results){
                        items.add(a)
                    }
                    items.reverse()
                    activity.runOnUiThread({
                        activity.findViewById<ProgressBar>(R.id.pb_hist_details).visibility=View.INVISIBLE
                        notifyDataSetChanged()
                    })
                }

            }
            catch (e:Exception){
                e.printStackTrace()
                activity.runOnUiThread({
                    makeToast("Cannot get results at this moment. Please check the internet connection and try again")
                    activity.home()
                })
            }
        }.start()




    }
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var view = p1
        if (view==null) {
            view = LayoutInflater.from(activity.applicationContext).inflate(R.layout.history_detail_item,p2,false)
        }
        val item = getItem(p0)
        view!!.findViewById<TextView>(R.id.tv_bus_event).text=item.eventType

        val millis = items[p0].eventTimestamp
        val cal = Calendar.getInstance()
        cal.timeInMillis=millis!!.toLong()

        var hour = cal.get(Calendar.HOUR_OF_DAY)
        var ampm = "AM"
        if (hour>=12){
            if (hour>12)
                hour-=12
            ampm="PM"
        }

        view.findViewById<TextView>(R.id.tv_details).text= "$hour:${cal.get(Calendar.MINUTE)} $ampm"
        view.findViewById<TextView>(R.id.tv_location).text= item.eventLocation + " --> "+item.eventDestination
        view.findViewById<ImageView>(R.id.iv_details).visibility= if (item.eventDescription!=null) View.VISIBLE else View.INVISIBLE

        view.findViewById<ImageView>(R.id.iv_details).setOnClickListener {
            makeToast(item.eventDescription!!)
        }
        view.findViewById<TextView>(R.id.tv_time_elapsed).text=getElapsed(item.eventTimestamp!!)
        return view
    }

    private fun getElapsed(eventTimestamp: Double): String {
        val cal = Calendar.getInstance()
        val l = cal.timeInMillis - eventTimestamp.toLong()

        val hr = TimeUnit.MILLISECONDS.toHours(l)
        val min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr))
        return String.format("%d hrs %02d mins ago", hr, min)

    }

    private fun makeToast(s :String){
        toast?.cancel()
        toast = Toast.makeText(activity.applicationContext,s,Toast.LENGTH_LONG)
        toast?.show()
    }

    override fun getItem(p0: Int): BusEventsDO {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return items.size
    }

}