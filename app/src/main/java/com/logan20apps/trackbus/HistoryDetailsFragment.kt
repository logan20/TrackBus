package com.logan20apps.trackbus

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

/**
 * Created by kwasi on 30/10/2017.
 */

class HistoryDetailsFragment:Fragment(){
    var start: Long=0
    var end: Long=0
    lateinit var location: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_history_details,container,false)
        v.findViewById<ListView>(R.id.lv_history).adapter = HistoryDetailAdapter(activity as MainActivity,start,end,location)
        return v
    }


}