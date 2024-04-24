package com.feup.cpm.ticketbuy

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.feup.cpm.ticketbuy.controllers.Controller
import com.feup.cpm.ticketbuy.models.Customer
import com.feup.cpm.ticketbuy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()  {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var customer: Customer? = null
    private val controller = Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load customer data from local storage
        customer = controller.getLocalCustomer(this)
        println("Customer: $customer")
        if(customer != null) {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.appBarMain.toolbar)

            val drawerLayout: DrawerLayout = binding.drawerLayout
            val navView: NavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home, R.id.nav_cafeteria, R.id.nav_tickets, R.id.nav_register, R.id.nav_cafeteria, R.id.nav_transactions
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)

            // Get the navigation header view
            val headerView = navView.getHeaderView(0)

            // Update views in the header
            val userNameTextView: TextView = headerView.findViewById(R.id.userNameTextView)
            userNameTextView.text = customer!!.name

            val userEmailTextView: TextView = headerView.findViewById(R.id.userEmailTextView)
            userEmailTextView.text = customer!!.nif

            navView.setupWithNavController(navController)
        }
        else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            // Redirect to register activity

            setContentView(binding.root)
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_register)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}