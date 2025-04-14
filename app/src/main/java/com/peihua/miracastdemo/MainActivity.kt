package com.peihua.miracastdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
//    private val mExt: WfdSinkExt by lazy { WfdSinkExt(this) }
//    private val mDialog: FullScreenDialog by lazy { FullScreenDialog(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setContent {
//            MiracastDemoTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fl_container, WifiDisplaySettings(),"WfdSinkSurfaceFragment")
        fragmentTransaction.commit()
    }

    override fun onStart() {
        super.onStart()

    }
}