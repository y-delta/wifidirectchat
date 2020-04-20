package com.example.myapp

import LedgerFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.ui.directmessage.DirectMessageFragment
import com.example.myapp.ui.globalmessage.GlobalMessageFragment

import com.example.myapp.ui.main.ModalBottomSheet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MainActivity : AppCompatActivity() {

    var directMessageFragment: DirectMessageFragment = DirectMessageFragment()
    var globalMessageFragment: GlobalMessageFragment = GlobalMessageFragment()
    var ledgerFragment: LedgerFragment = LedgerFragment()
    private val modalBottomSheet = ModalBottomSheet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.nav_host_fragment, globalMessageFragment, "putGlobalMessageFragment")
                .commit()
        }
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        modalBottomSheet.show(supportFragmentManager, modalBottomSheet.tag) //implement this on WiFiDirectBroadcastReceiver when Prashant sends code
        modalBottomSheet.isCancelable = false //prevents cancelling
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_directmessage -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.nav_host_fragment,
                        directMessageFragment,
                        "putDirectMessageFragment"
                    )
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_globalmessage -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.nav_host_fragment,
                        globalMessageFragment,
                        "putGlobalMessageFragment"
                    )
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_ledger -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, ledgerFragment, "putLedgerFragment")
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }

            false
        }

}
