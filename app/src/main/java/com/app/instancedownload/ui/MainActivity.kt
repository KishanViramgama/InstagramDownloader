package com.app.instancedownload.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.viewpager2.widget.ViewPager2
import com.app.instancedownload.BuildConfig
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivityMainBinding
import com.app.instancedownload.service.ActiveService
import com.app.instancedownload.service.DownloadService
import com.app.instancedownload.ui.setting.activity.SettingActivity
import com.app.instancedownload.util.*
import com.app.instancedownload.util.Constant.ACTION_SERVICE_START
import com.app.instancedownload.util.Constant.ACTION_SERVICE_STOP
import com.app.instancedownload.util.Constant.ACTION_START
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@BuildCompat.PrereleaseSdkCheck
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var method: Method

    @Inject
    lateinit var liveData: LiveData<LiveDataType<String>>

    lateinit var loadingDialog: LoadingDialog

    private lateinit var pageTitle: Array<String>
    private var doubleBackToExitPressedOnce = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var switchMaterial: SwitchMaterial
    private lateinit var pagerAdapter: ViewPagerAdapter
    private lateinit var inputMethodManager: InputMethodManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (BuildCompat.isAtLeastT()) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                // Back is pressed... Finishing the activity
                closeDrawer()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this /* lifecycle owner */,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Back is pressed... Finishing the activity
                        closeDrawer()
                    }
                })
        }

        loadingDialog = LoadingDialog(this)

        liveData.observe(this) {
            when (it.type) {
                Type.SERVICE -> {
                    switchMaterial.isChecked = false
                }
                else -> {}
            }
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (!isGranted) {
                    method.alertBox(this, resources.getString(R.string.msgNfNotAllow))
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val sdIconStorageDir = File(getExternalFilesDir(BuildConfig.downloadUrl).toString())

        //create storage directories, if they don't exist
        if (!sdIconStorageDir.exists()) {
            sdIconStorageDir.mkdirs()
        }
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        pageTitle =
            arrayOf(resources.getString(R.string.image), resources.getString(R.string.video))

        binding.toolbarMain.title = resources.getString(R.string.app_name)
        setSupportActionBar(binding.toolbarMain)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbarMain,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.toolbarMain.setNavigationIcon(R.drawable.ic_side_nav)
        binding.navViewMain.setNavigationItemSelectedListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.navViewMain.menu.getItem(0).isVisible = false
            binding.conMain.visible()
        } else {
            binding.navViewMain.menu.getItem(0).isVisible = true
            binding.conMain.gone()
        }

        val menu = binding.navViewMain.menu
        val menuItem = menu.findItem(R.id.drawer_switch)
        val actionView = menuItem.actionView
        switchMaterial = actionView!!.findViewById(R.id.switch_view)
        switchMaterial.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                menuItem.title = resources.getString(R.string.service_on)
                appServiceStart()
            } else {
                menuItem.title = resources.getString(R.string.service_off)
                appServiceStop()
            }
        }

        //set gravity for tab bar
        binding.tabLayoutMain.tabGravity = TabLayout.GRAVITY_FILL
        binding.viewPagerMain.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        //change ViewPager page when tab selected
        binding.tabLayoutMain.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPagerMain.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.buttonSubmitMain.setOnClickListener {

            val url = binding.editTextMain.text.toString()
            binding.editTextMain.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(binding.editTextMain.windowToken, 0)

            method.onClick(this, 0, "", url) { position, type, data ->
                if (data != "") {

                    loadingDialog.show()

                    val findData = FindData(applicationContext)
                    findData.data(data) { linkList, message, isData ->

                        if (isData) {
                            if (linkList.size != 0) {
                                Constant.downloadArray.clear()
                                Constant.downloadArray.addAll(linkList)
                                val intent = Intent(this@MainActivity, DownloadService::class.java)
                                intent.action = ACTION_START
                                startService(intent)
                            } else {
                                method.alertBox(
                                    this@MainActivity, resources.getString(R.string.no_data_found)
                                )
                            }
                        } else {
                            method.alertBox(this@MainActivity, message)
                        }

                        binding.editTextMain.setText("")
                        loadingDialog.dismiss()

                    }

                } else {
                    method.alertBox(this, resources.getString(R.string.please_enter_url))
                }

            }

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!isMyServiceRunning) {
                switchMaterial.isChecked = true
            }
        }

        binding.conAdViewMain.gone()

        //set viewpager adapter
        pagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle, pageTitle.size)
        binding.viewPagerMain.adapter = pagerAdapter
        TabLayoutMediator(
            binding.tabLayoutMain, binding.viewPagerMain, true
        ) { tab: TabLayout.Tab, position: Int ->
            // position of the current tab and that tab
            tab.text = pageTitle[position]
        }.attach()

    }

    private fun appServiceStart() {
        val intent = Intent(applicationContext, ActiveService::class.java)
        intent.action = ACTION_SERVICE_START
        startService(intent)
    }

    private fun appServiceStop() {
        val intent = Intent(applicationContext, ActiveService::class.java)
        intent.action = ACTION_SERVICE_STOP
        startService(intent)
    }

    private val isMyServiceRunning: Boolean
        get() {
            val manager = (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (ActiveService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.open_app_menu) {
            if (isAppInstalledInstagram(this@MainActivity)) {
                try {
                    val manager = this.packageManager
                    val intent = manager.getLaunchIntentForPackage("com.instagram.android")!!
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    startActivity(intent)
                } catch (e: Exception) {
                    method.alertBox(this, resources.getString(R.string.wrong))
                }
            } else {
                method.alertBox(this, resources.getString(R.string.app_not_install))
            }
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }

        // Handle navigation view item clicks here.
        //Checking if the item is in checked state or not, if not make it in checked state
        item.isChecked = !item.isChecked

        //Closing drawer on item click
        binding.drawerLayout.closeDrawers()

        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.setting) {
            unSelect(1)
            startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            return true
        }
        return true
    }

    private fun unSelect(position: Int) {
        binding.navViewMain.menu.getItem(position).isChecked = false
        binding.navViewMain.menu.getItem(position).isCheckable = false
    }

    private fun select(position: Int) {
        binding.navViewMain.menu.getItem(position).isChecked = true
    }

    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (doubleBackToExitPressedOnce) {
                finish()
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(
                this,
                resources.getString(R.string.Please_click_BACK_again_to_exit),
                Toast.LENGTH_SHORT
            ).show()
            Handler(Looper.getMainLooper()).postDelayed(
                { doubleBackToExitPressedOnce = false }, 2000
            )
        }
    }

    override fun onDestroy() {
        switchMaterial.isChecked = false
        super.onDestroy()
    }

}