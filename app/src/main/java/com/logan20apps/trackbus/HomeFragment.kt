package com.logan20apps.trackbus

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar

/**
 *
 * Created by kwasi on 29/10/2017.
 */

class HomeFragment : Fragment(){
    var layoutManager :LinearLayoutManager?=null
    var adapter : HomeAdapter?=null
    var v : View?  = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_home,container,false)
        val rv = v!!.findViewById<RecyclerView>(R.id.rv_content)
        rv.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(context)
        rv.layoutManager=layoutManager

        adapter = HomeAdapter(activity as MainActivity)
        rv.adapter=adapter


        return v
    }

    override fun onPause() {
        super.onPause()
        adapter?.cancelTimer()
    }

}