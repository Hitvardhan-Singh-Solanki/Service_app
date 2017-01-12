package com.hitvardhan.project_app.activity;

/**
 * Created by Hitvardhan
 * Main Activity class to controll and show the Main screen UI with map and tab layout
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import com.hitvardhan.project_app.ImageCache.ImageLoader;
import com.hitvardhan.project_app.fragment.ServiceEngineer;
import com.hitvardhan.project_app.fragment.TaskAdminFragment;
import com.hitvardhan.project_app.interfaces.ReloadButtonHandler;
import com.hitvardhan.project_app.response_classes.ResponseForAdmin;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.security.PasscodeManager;
import com.salesforce.androidsdk.util.EventsObservable;


import com.hitvardhan.project_app.R;
import com.hitvardhan.project_app.response_classes.Response;
import com.hitvardhan.project_app.utils.CommanUtils;

import butterknife.ButterKnife;

import com.hitvardhan.project_app.fragment.AdminFragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener {


    //Variable Declaration
    private Gson gson = new Gson();
    public static Response res;
    private PasscodeManager passcodeManager;
    public TextView userNameView;
    public TextView userEmailId;
    public ImageView imgProfile;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static LatLng latLngMyLoc;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private LinearLayout logoutButtonNavigation, secondButtonNavigation, thirdButtonNavigation;
    private ImageView reloadButtonOnNavHeader;
    private Fragment currentFragment;
    public static RestClient client;
    private String salesForceImageUrl;
    private ImageLoader imgLoader;
    private View navHeader;
    private ResponseForAdmin mResponseForAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //implemented butterKnife
        ButterKnife.bind(this);
        // Setup view
        setContentView(R.layout.main_activity_with_navigation);


        //Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primary));


        //Setup Navigation Drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Toggle button
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toggle.syncState();
        //Setup the navigation view
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        //Get the navigation header
        navHeader = navigationView.getHeaderView(0);


        // Gets an instance of the passcode manager.
        passcodeManager = SalesforceSDKManager.getInstance().getPasscodeManager();

        //Set the username and otherInfo from the client to header
        userNameView = (TextView) navHeader.findViewById(R.id.Client_name_from_request);
        userEmailId = (TextView) navHeader.findViewById(R.id.Client_email_id_from_request);
        imgProfile = (ImageView) navHeader.findViewById(R.id.imageView_profile);


        secondButtonNavigation = (LinearLayout) navigationView
                .findViewById(R.id.navigation_tasks_admin);
        thirdButtonNavigation = (LinearLayout) navigationView
                .findViewById(R.id.navigation_users_button);

        secondButtonNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                     if (currentFragment.getClass() != TaskAdminFragment.class) {
                         currentFragment = new TaskAdminFragment();
                         FragmentTransaction transaction =
                                 getSupportFragmentManager().beginTransaction();
                         transaction.replace(R.id.frm_container, currentFragment, "");
                         //transaction.addToBackStack("");
                         transaction.commitAllowingStateLoss();
                     }
                     drawer.closeDrawer(GravityCompat.START);
            }
        });


        thirdButtonNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentFragment.getClass() != AdminFragment.class) {
                currentFragment = new AdminFragment();
                FragmentTransaction transaction =
                            getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frm_container, currentFragment, "");
                    //transaction.addToBackStack("");
                    transaction.commitAllowingStateLoss();
                }
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        //Setup the navigation logout button.
        logoutButtonNavigation = (LinearLayout) navigationView
                .findViewById(R.id.navigation_logout_main);
        logoutButtonNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Logout manager
                //Show an alert dialog box before logging out
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("Want to Logout?")
                        .setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Logout
                                SalesforceSDKManager.getInstance().logout(MainActivity.this);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.ic_alert_exclamation_mark)
                        .show();
                //close the drawer after click
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        currentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        // Hide everything until we are logged in
        // findViewById(R.id.root).setVisibility(View.INVISIBLE);
        super.onResume();
        passcodeChallenge();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Close the navigation slider
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            //Move to home screen instead of the previous activity
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        return true;
    }


    @Override
    public void onPause() {
        passcodeManager.onPause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        passcodeManager.nolongerFrontActivity(this);
        super.onDestroy();
    }


    /**
     * passcode chanllenge to handle the login/logout function using salesforce SDK
     */
    private void passcodeChallenge() {
        // Brings up the passcode screen if needed.
        if (passcodeManager.onResume(this)) {

            // work around to avoid a memory leak when we call getRestClient with the activity

            try {
                ClientManager clientManager = new ClientManager(getApplicationContext(),
                        SalesforceSDKManager.getInstance().getAccountType(),
                        SalesforceSDKManager.getInstance().getLoginOptions(),
                        SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

                client = clientManager.peekRestClient();
                if (client != null) {
                    //onResumeClient(client);
                    //showTheAppSelectionDialog(client);
                    onResumeClient(client);
                    return;
                }
            } catch (Exception e) {
                // continue with the below flow if we get any exceptions or if the client is null
                e.printStackTrace();
            }

            // Gets login options.
            final String accountType = SalesforceSDKManager.getInstance().getAccountType();
            final ClientManager.LoginOptions loginOptions = SalesforceSDKManager.getInstance()
                    .getLoginOptions();

            // Gets a rest client.
            new ClientManager(getApplicationContext(), accountType, loginOptions,
                    SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked())
                    .getRestClient(this, new ClientManager.RestClientCallback() {

                        @Override
                        public void authenticatedRestClient(RestClient client) {
                            if (client == null) {
                                SalesforceSDKManager.getInstance().logout(MainActivity.this);
                                return;
                            }

                            onResumeClient(client);


                            // Lets observers know that rendition is complete.
                            EventsObservable.get().notifyEvent(EventsObservable
                                    .EventType.RenditionComplete);
                        }
                    });
        }
    }


    /**
     * Method to call the Rest Client and make request to the server
     *
     * @param client
     */
    private void onResumeClient(final RestClient client) {

        // Keeping reference to rest client
        this.client = client;
        if (client != null) {
            Bitmap bmp = null;
            String userName = client.getClientInfo().displayName;
            String userEmail = client.getClientInfo().email;
            userNameView.setText(userName);
            userEmailId.setText(userEmail);
            salesForceImageUrl = client.getClientInfo().photoUrl;


            //caching of the image
            imgLoader = new ImageLoader(this);
            imgLoader.DisplayImage(salesForceImageUrl, imgProfile,null,this,null);


            // Show everything
            // findViewById(R.id.root).setVisibility(View.VISIBLE);

            if (CommanUtils.isNetworkAvailable(this)) {
                Snackbar.make((View) findViewById(R.id.main_root), R.string.online,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action, null).show();
                //  Log.d("ABOUT THE CLIENT",client.toString());
                if (client.getClientInfo().userId.equalsIgnoreCase(getString(R.string.adminUserId))) {
                    secondButtonNavigation.setVisibility(View.VISIBLE);
                    thirdButtonNavigation.setVisibility(View.VISIBLE);
                    //Inflate a fragment based on the ADMIN user logged in
                    if (currentFragment == null) {
                        currentFragment = new AdminFragment();
                    }
                    if (!currentFragment.isAdded()) {
                        FragmentTransaction transaction =
                                getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frm_container, currentFragment, "");
                        transaction.addToBackStack("");
                        transaction.commitAllowingStateLoss();
                    }
                } else {
                    //Inflate a fragment based on the service user logged in
                    if (currentFragment == null) {
                        currentFragment = new ServiceEngineer();
                    }

                    if (!currentFragment.isAdded()) {
                        FragmentTransaction transaction =
                                getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frm_container, currentFragment, "");
                        transaction.addToBackStack("");
                        transaction.commitAllowingStateLoss();
                    }
                }

            }
        }
    }

    /**
     * Update on UI on load of a fragment
     *
     * @param response
     */
    public void updateUi(Response response) {
        ((ServiceEngineer) currentFragment).updateDataOnUi(response);
    }


    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("PROJECT APP");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    public ResponseForAdmin getmResponseForAdmin() {
        return mResponseForAdmin;
    }

    public void setmResponseForAdmin(ResponseForAdmin mResponseForAdmin) {
        this.mResponseForAdmin = mResponseForAdmin;
    }
}