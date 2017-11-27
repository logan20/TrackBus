package com.logan20apps.trackbus

import android.support.multidex.MultiDexApplication
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider;
import com.amazonaws.mobile.auth.facebook.FacebookSignInProvider;                                                                             ;


/**
 * Created by kwasi on 17/10/2017.
 */
class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        initializeApplication()
    }

    private fun initializeApplication() {
        val awsConfig = AWSConfiguration(applicationContext)
        if (IdentityManager.getDefaultIdentityManager() == null){
            val iManager = IdentityManager(applicationContext,awsConfig)
            IdentityManager.setDefaultIdentityManager(iManager)
            IdentityManager.getDefaultIdentityManager().addSignInProvider(CognitoUserPoolsSignInProvider::class.java)
            IdentityManager.getDefaultIdentityManager().addSignInProvider(FacebookSignInProvider::class.java)
            FacebookSignInProvider.setPermissions("public_profile,email,user_friends");
        }
    }

}