package cuhk.cse.cmsc5736project;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import java.util.ArrayList;
import java.util.List;

import cuhk.cse.cmsc5736project.adapters.SmartFragmentStatePagerAdapter;
import cuhk.cse.cmsc5736project.fragments.FriendsFragment;
import cuhk.cse.cmsc5736project.fragments.MapFragment;
import cuhk.cse.cmsc5736project.fragments.POIFragment;
import cuhk.cse.cmsc5736project.interfaces.OnFriendResultListener;
import cuhk.cse.cmsc5736project.interfaces.OnPOIResultListener;
import cuhk.cse.cmsc5736project.models.Friend;
import cuhk.cse.cmsc5736project.models.POI;
import cuhk.cse.cmsc5736project.models.RSSIModel;
import cuhk.cse.cmsc5736project.utils.Utility;
import cuhk.cse.cmsc5736project.views.NoSwipePager;

public class MainActivity extends AppCompatActivity {
    //Constant

    public static final int REQUEST_LOCATION_CODE = 99;

    // Variables
    private Context context;

    // Application navigation related
    private NoSwipePager viewPager;
    private AHBottomNavigation bottomNavigation;

    // ActionBar coloring
    private int last_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        setupBottomNavBar();

        LocationManager.getInstance().iniUserData(context);



        LocationManager.getInstance().initPOIDefinitions(context, new OnPOIResultListener() {
            @Override
            public void onRetrieved(List<POI> poiList) {

                LocationManager.getInstance().initCurrentUserFriendList(context, new OnFriendResultListener() {
                   @Override
                    public void onRetrieved(List<Friend> friendList) {
                       setupViewPager();

                       // Select initial page
                       int initialPage = 0;
                       selectPage(initialPage);
                       bottomNavigation.setCurrentItem(initialPage);

                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                           checkLocationPermission();
                       }
                        LocationManager.getInstance().startService(context);
                    }
               });

            }
        });

    }

    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                    System.exit(0);
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.syncdata:
                //updateModel
                //RSSIModel.getInstance().updateModel(MainActivity.this);
                LocationManager.getInstance().updatePOIDefintion(context);
                return true;
            case R.id.setup:
                intent = new Intent(this, BeaconActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                // launch settings activity
                startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void selectPage(int position) {
        // Change to page with index=position
        viewPager.setCurrentItem(position);
        tintSystemBars(bottomNavigation.getItem(position).getColor(context), true);
        last_color = bottomNavigation.getItem(position).getColor(context);
    }

    private void setupViewPager() {
        viewPager = (NoSwipePager) findViewById(R.id.viewpager);
        viewPager.setPagingEnabled(false);

        BottomBarAdapter pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

        Bundle bundle = null;
        Fragment fragment = null;

        // Map page
        bundle = new Bundle();
        bundle.putInt("color", fetchColor(R.color.color_tab_1));

        fragment = new MapFragment();
        fragment.setArguments(bundle);
        pagerAdapter.addFragments(fragment);

        // POI page
        bundle = new Bundle();
        bundle.putInt("color", fetchColor(R.color.color_tab_2));

        fragment = new POIFragment();
        fragment.setArguments(bundle);
        pagerAdapter.addFragments(fragment);

        // Friend page
        bundle = new Bundle();
        bundle.putInt("color", fetchColor(R.color.color_tab_3));

        fragment = new FriendsFragment();
        fragment.setArguments(bundle);
        pagerAdapter.addFragments(fragment);

        // Set viewpager for the fragments
        viewPager.setAdapter(pagerAdapter);

        // Setup bottom navbar on tap behaviour
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                // TEST METHOD !!! WILL REMOVE LATER
                //LocationManager.getInstance().simulateBluetoothPOIScanning();
                //LocationManager.getInstance().updateSimulatedFriendPositions();

                if (!wasSelected)
                    selectPage(position);

                return true;
            }
        });
    }

    private void setupBottomNavBar() {
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.map, R.color.color_tab_1);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.ic_maps_local_bar, R.color.color_tab_2);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.account_multiple, R.color.color_tab_3);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // Disable the translation inside the CoordinatorLayout
        bottomNavigation.setBehaviorTranslationEnabled(false);

        // Use colored navigation with circle reveal effect
        bottomNavigation.setColored(true);
        bottomNavigation.setTranslucentNavigationEnabled(true);

        // Set current item programmatically
        bottomNavigation.setCurrentItem(0);
        tintSystemBars(item1.getColor(context), false);
        last_color = item1.getColor(context);
    }

    public class BottomBarAdapter extends SmartFragmentStatePagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();

        public BottomBarAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Our custom method that populates this Adapter with Fragments
        public void addFragments(Fragment fragment) {
            fragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }

    private void tintSystemBars(int to_color_id, boolean isAnimated) {

        // Desired final colors of each bar.
        final int statusBarToColor = Utility.blendColors(to_color_id, 0, 0.2f);
        final int toolbarToColor = to_color_id;

        if (isAnimated) {
            // Initial colors of each system bar.
            final int statusBarColor = Utility.blendColors(last_color, 0, 0.2f);
            final int toolbarColor = last_color;

            ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // Use animation position to blend colors.
                    float position = animation.getAnimatedFraction();

                    // Apply blended color to the status bar.
                    int blended = Utility.blendColors(statusBarColor, statusBarToColor, position);
                    getWindow().setStatusBarColor(blended);

                    // Apply blended color to the ActionBar.
                    blended = Utility.blendColors(toolbarColor, toolbarToColor, position);
                    ColorDrawable background = new ColorDrawable(blended);
                    getSupportActionBar().setBackgroundDrawable(background);
                }
            });

            anim.setDuration(300).start();
        } else {
            getWindow().setStatusBarColor(statusBarToColor);
            ColorDrawable background = new ColorDrawable(toolbarToColor);
            getSupportActionBar().setBackgroundDrawable(background);
        }
    }

    private int fetchColor(@ColorRes int color) {
        return ContextCompat.getColor(this, color);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
