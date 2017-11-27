package com.logan20apps.trackbus

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.amazonaws.mobile.auth.ui.SignInActivity
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration
import android.app.Activity
import com.amazonaws.mobile.auth.core.*
import com.amazonaws.mobile.auth.facebook.FacebookButton;
import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.facebook.AccessToken
import com.facebook.GraphRequest


class SplashActivity : AppCompatActivity() {
    var iManager : IdentityManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var minDelay:Long = 1500

        val t = intent?.extras?.getLong("timer",1500)
        if (t!=null){
            minDelay = t
        }

        iManager = IdentityManager.getDefaultIdentityManager()
        iManager!!.doStartupAuth(this, { authResults ->
            if (authResults.isUserSignedIn){
                if (iManager!!.currentIdentityProvider.displayName=="Facebook"){
                    val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { `object`, _ -> runOnUiThread({
                        goMain(`object`.getString("name"))
                    })}
                    val bundle = Bundle()
                    bundle.putString("fields","name")
                    request.parameters=bundle
                    request.executeAsync()
                }
                else{
                    goMain("")
                }
            } else{
                doSignIn()
            }
        }, minDelay)


    }

    private fun goMain(name:String) {
        val intent = Intent(this@SplashActivity, MainActivity::class.java).putExtra("name",name).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun doSignIn() {
        iManager!!.setUpToAuthenticate(
                this@SplashActivity, object : DefaultSignInResultHandler() {
            override fun onSuccess(activity: Activity, identityProvider: IdentityProvider?) {
                if (identityProvider != null) {
                    goMain("")
                }
            }

            override fun onCancel(activity: Activity): Boolean {
                // Return false to prevent the user from dismissing
                // the sign in screen by pressing back button.
                // Return true to allow this.

                return false
            }
        })

        val config = AuthUIConfiguration.Builder()
                .userPools(true)
                .backgroundColor(R.color.colorCards)
                .logoResId(R.drawable.logo_2)
                .signInButton(FacebookButton::class.java)
                //.signInButton(GoogleButton.class)
                .build()

        val context = this@SplashActivity
        SignInActivity.startSignInActivity(context, config)
        this@SplashActivity.finish()
    }
}
