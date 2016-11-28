package hk.hku.cs.videoalbum;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.hku.cs.videoalbum.helper.RemoteServerConnect;
import hk.hku.cs.videoalbum.videocapture.VideoCaptureBrowserActivity;

public class ListUserVideoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ListUserVideoActivity.this, VideoCaptureBrowserActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.loginNameTxt);
        textView.setText(preferences.getString("username", ""));

        prepareList();
    }

    private void prepareList() {
        final ProgressDialog pdialog = new ProgressDialog(this);

        pdialog.setCancelable(false);
        pdialog.setMessage("Logging in ...");
        pdialog.show();

        SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
        final String url = "http://i.cs.hku.hk/~kfwong/videoalbum/videolist.php?username=" + preferences.getString("username", "");

        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            boolean success;
            String jsonString;

            @Override
            protected String doInBackground(String... arg0) {
                success = true;
                RemoteServerConnect connect = new RemoteServerConnect();
                jsonString = connect.getUrlResponse(url);
                if (jsonString.equals("Fail to login"))
                    success = false;
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                if (success) {
                    try {
                        ListView listView = (ListView) findViewById(R.id.video_list);


                        JSONObject rootJSONObj = new JSONObject(jsonString);
                        JSONArray result = rootJSONObj.getJSONArray("videos");

                        final List<String> filenamesList = new ArrayList<>();
                        List<Map<String, String>> videoListName = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            String filename = result.getString(i);

                            Map<String, String> map = new HashMap<>();
                            map.put("videos", filename);
                            videoListName.add(map);
                        }
                        SimpleAdapter listAdapter = new SimpleAdapter(ListUserVideoActivity.this,videoListName, R.layout.video_list_item, new String[]{"videos"}, new int[]{R.id.video_file_name});
                        listView.setAdapter(listAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                System.out.println(adapterView.getItemAtPosition(i));
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //alert("Error", "Fail to login");
                }
                pdialog.hide();
            }
        }.execute("");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_user_video, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu (Menu menu) {

        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
        if (id == R.id.nav_logout) {
            // logout

            SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent myIntent = new Intent(this, MainActivity.class);
            startActivityForResult(myIntent, 0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
