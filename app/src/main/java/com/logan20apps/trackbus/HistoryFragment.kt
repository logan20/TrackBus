package com.logan20apps.trackbus

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Spinner
import java.util.*

/**
 * Created by kwasi on 29/10/2017.
 */

class HistoryFragment : Fragment(){
    var v : View?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_history,container,false)
        return v
    }

    fun getLocation(): String {
        return v!!.findViewById<Spinner>(R.id.spnr_location_hist).selectedItem.toString()
    }

    fun getStart(): Long {
        val start = Calendar.getInstance()
        val dp = v!!.findViewById<DatePicker>(R.id.dp_history_date)
        start.set(dp.year,dp.month,dp.dayOfMonth,0,0,0)
        return start.timeInMillis
    }

    fun getEnd(): Long {
        val end = Calendar.getInstance()
        val dp = v!!.findViewById<DatePicker>(R.id.dp_history_date)
        end.set(dp.year,dp.month,dp.dayOfMonth,23,59,59)
        return end.timeInMillis
    }

}