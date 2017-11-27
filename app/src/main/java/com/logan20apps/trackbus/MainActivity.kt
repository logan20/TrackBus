package com.logan20apps.trackbus

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.amazonaws.mobile.auth.core.IdentityManager
import com.logan20apps.trackbus.R.id.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import com.google.android.gms.maps.SupportMapFragment
import java.util.*
import kotlin.concurrent.timer


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var showSearch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            addBusEvent()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        home()
        toolbar.title = "Home"
    }

    fun home(){
        toolbar.title = "Home"
        nav_view.setCheckedItem(nav_home)
        supportFragmentManager.beginTransaction().replace(R.id.fl_content, HomeFragment()).commitAllowingStateLoss()
        fab.visibility = View.VISIBLE
        invalidateOptionsMenu()
    }

    private fun addBusEvent(){
        invalidateOptionsMenu()
        nav_view.setCheckedItem(nav_add_bus_event)
        val bef = BusEventFragment()
        bef.listener=this
        supportFragmentManager.beginTransaction().replace(R.id.fl_content, bef).commitAllowingStateLoss()
        fab.visibility = View.INVISIBLE
    }


    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            supportFragmentManager.findFragmentById(R.id.fl_content) !is HomeFragment -> home()
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_search).isVisible = showSearch
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            action_search->{
                val frag = supportFragmentManager.findFragmentById(R.id.fl_content) as HistoryFragment

                val histDetails = HistoryDetailsFragment()
                histDetails.start = frag.getStart()
                histDetails.end = frag.getEnd()
                histDetails.location = frag.getLocation()
                supportFragmentManager.beginTransaction().replace(R.id.fl_content,histDetails).commit()
                showSearch=false
                invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        fab.visibility= View.INVISIBLE
        showSearch = false

        when (item.itemId) {
            nav_add_bus_event ->{
                toolbar.title = "Add event"
                addBusEvent()
            }
            nav_home ->{
                home()
            }
            nav_history ->{
                toolbar.title = "History"
                showSearch = true
                supportFragmentManager.beginTransaction().replace(R.id.fl_content,HistoryFragment()).commit()
            }
            nav_map ->{
                toolbar.title = "Map"
                val frag = SupportMapFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fl_content, frag).commit()
                frag.getMapAsync(MapCallback(this))
            }
            /*nav_timetable->{
                supportFragmentManager.beginTransaction().replace(R.id.fl_content,TimetableFragment()).commit()
            }
            nav_settings->{

            }*/
            nav_logout ->{
                IdentityManager.getDefaultIdentityManager().signOut()
                startActivity(Intent(this@MainActivity,SplashActivity::class.java).putExtra("timer",0))
                finish()
            }
            nav_about->{
                toolbar.title = "About"
                supportFragmentManager.beginTransaction().replace(R.id.fl_content,AboutFragment()).commit()
            }
        }
        invalidateOptionsMenu()

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
