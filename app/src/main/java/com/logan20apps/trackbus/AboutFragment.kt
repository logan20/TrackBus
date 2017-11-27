package com.logan20apps.trackbus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

/**
 * Created by kwasi on 30/10/2017.
 *
 */

class AboutFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AboutPage(context)
                .isRTL(false)
                .setDescription("Logan20 apps is a website and mobile app development company founded in Trinidad. We offer our services to " +
                        "both businesses and individuals worldwide. Feel free to contact us using any of the options below.")
                .setImage(R.drawable.logo)
                .addFacebook("logan20apps")
                .addTwitter("logan20apps")
                .addInstagram("logan20apps")
                .addWebsite("http://www.logan20apps.com/")
                .addEmail("admin@logan20apps.com")
                .addItem(getPlayStoreDeveloper("logan20"))
                .create()
    }

    private fun getPlayStoreDeveloper(id: String): Element {
        val playStoreElement = Element()
        playStoreElement.title = "Check out our other apps"
        playStoreElement.iconDrawable = mehdi.sakout.aboutpage.R.drawable.about_icon_google_play
        playStoreElement.iconTint = mehdi.sakout.aboutpage.R.color.about_play_store_color
        playStoreElement.value = id
        playStoreElement.onClickListener = View.OnClickListener {
            var url = "market://developer?id=$id"
            var storeintent: Intent?
            try {
                storeintent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                storeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(storeintent)
            } catch (e: Exception) {
                url = "https://play.google.com/store/apps/developer?id=$id"
                storeintent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                storeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(storeintent)
            }
        }
        return playStoreElement
    }
}