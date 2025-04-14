package com.peihua.miracastdemo

import androidx.appcompat.app.AppCompatActivity

class WifiDisplaySinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fl_container, WfdSinkSurfaceFragment(),"WfdSinkSurfaceFragment")
        fragmentTransaction.commit()
    }
}