package hk.hku.cs.videoalbum;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.hku.cs.videoalbum.helper.RemoteServerConnect;
import hk.hku.cs.videoalbum.videocapture.VideoCaptureBrowserActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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


        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... arg0) {
                MediaPlayer mediaPlayer = MediaPlayer.create(ListUserVideoActivity.this, R.raw.s01);
                mediaPlayer.start();

                return "";
            }
        }.execute("");
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = this.getSharedPreferences("view-video-type", 0);

        if (preferences.getBoolean("user-only-list", true)) {
            prepareList(false);
        } else {
            prepareList(true);
        }
    }

    private String urlForVideoList(boolean listAllUserVideo) {
        String url;
        if (listAllUserVideo) {
            url = getString(R.string.server_path) + "sharedvideolist.php";
        } else {
            SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
            String username = preferences.getString("username", "");
            url = getString(R.string.server_path) + "videolist.php?username=" + username;
        }
        return url;
    }

    private void prepareList(final boolean listAllUserVideo) {
        final String url = urlForVideoList(listAllUserVideo);

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

                        final ArrayList<String> filenamesList = new ArrayList<>();
                        List<Map<String, String>> videoListName = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            String filename = result.getString(i);
                            String[] token = filename.split("/");

                            filenamesList.add(ListUserVideoActivity.this.getString(R.string.server_path) + filename);

                            Map<String, String> map = new HashMap<>();
                            map.put("videos", token[token.length - 1]);
                            map.put("uploaduser", token[token.length - 2]);
                            videoListName.add(map);
                        }
                        SimpleAdapter listAdapter = new SimpleAdapter(ListUserVideoActivity.this, videoListName, R.layout.video_list_item, new String[]{"videos", "uploaduser"}, new int[]{R.id.video_file_name, R.id.upload_by});
                        listView.setAdapter(listAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent myIntent = new Intent(ListUserVideoActivity.this, VideoPlayer.class);
                                myIntent.putExtra("position", i);
                                myIntent.putStringArrayListExtra("filelist", filenamesList);
                                startActivityForResult(myIntent, 0);
                            }
                        });

                        if (!listAllUserVideo) {
                            listView.setLongClickable(true);
                            registerForContextMenu(listView);       // pop delete menu
                        } else {
                            listView.setLongClickable(false);
                            unregisterForContextMenu(listView);       // pop delete menu
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ListUserVideoActivity.this, "Cannot list video", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute("");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        SharedPreferences preferences = this.getSharedPreferences("view-video-type", 0);
        if (preferences.getBoolean("user-only-list", false)) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_user_video, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {

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

        if (id == R.id.view_own_video) {
            SharedPreferences preferences = this.getSharedPreferences("view-video-type", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("user-only-list", true);
            editor.apply();
            prepareList(false);
        }
        if (id == R.id.view_all_video) {
            SharedPreferences preferences = this.getSharedPreferences("view-video-type", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("user-only-list", false);
            editor.apply();
            prepareList(true);
        }
        if (id == R.id.nav_logout) {
            // logout

            SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent myIntent = new Intent(this, MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(myIntent, 0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void removeVideo(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        ListView listView = (ListView) findViewById(R.id.video_list);
        SimpleAdapter adapter = (SimpleAdapter) listView.getAdapter();
        String fileName = ((Map<String, String>) adapter.getItem(info.position)).get("videos");

        SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
        String userName = preferences.getString("username", "");

        final String deleteUrl = getString(R.string.server_path) + "deletefile.php?user_name=" + userName + "&file_name=" + fileName;

        Log.i("removeVideo video name", fileName);

        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
//                        prepareList();
                    }
                });


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    Log.i("Delete reponse", response.toString());
                } catch (IOException e) {
                    Log.d("Delete error", "ok http delete error");
                    e.printStackTrace();
                }

                // update video list
                prepareList(false);
            }
        }).start();

    }
}
