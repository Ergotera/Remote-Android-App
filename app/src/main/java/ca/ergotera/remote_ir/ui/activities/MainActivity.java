package ca.ergotera.remote_ir.ui.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.felhr.services.UsbService;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.serial_com.CommandManager;
import ca.ergotera.remote_ir.ui.fragments.BugReportFragment;
import ca.ergotera.remote_ir.ui.fragments.ButtonListFragment;
import ca.ergotera.remote_ir.ui.fragments.HomeFragment;
import ca.ergotera.remote_ir.ui.fragments.InterfaceListFragment;

/**
 * The main activity contains the core logic of the application and
 * handles the application's fragment navigation.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String CLASS_ID = MainActivity.class.getSimpleName();

    public static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;

    private CommandManager commandManager = CommandManager.getInstance();

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Logger.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_home_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.activity_home_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        // Prevents central fragment from going back to the home fragment on screen rotation.
        if (null == savedInstanceState) {
            android.app.FragmentManager fragMgr = getFragmentManager();
            fragMgr.beginTransaction().replace(R.id.content_frame, new HomeFragment()).commit();
        }
        commandManager.setActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        commandManager.setFilters();  // Start listening notifications from UsbService
        commandManager.startService(UsbService.class, commandManager.getServiceConnection(), null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(commandManager.getBroadcastReceiver());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fragMgr = getFragmentManager();

        if (id == R.id.nav_home) {
            fragMgr.beginTransaction().replace(R.id.content_frame, new HomeFragment()).addToBackStack(null).commit();
        } else if (id == R.id.nav_button_list) {
            fragMgr.beginTransaction().replace(R.id.content_frame, new ButtonListFragment()).addToBackStack(null).commit();
        } else if (id == R.id.nav_interface_list) {
            fragMgr.beginTransaction().replace(R.id.content_frame, new InterfaceListFragment()).addToBackStack(null).commit();
        } else if (id == R.id.nav_report_bug) {
            fragMgr.beginTransaction().replace(R.id.content_frame, new BugReportFragment()).addToBackStack(null).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_home_drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_home_drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public Toolbar getToolbar(){
        return this.toolbar;
    }
}
