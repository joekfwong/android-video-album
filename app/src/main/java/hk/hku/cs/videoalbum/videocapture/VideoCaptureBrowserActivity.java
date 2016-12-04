package hk.hku.cs.videoalbum.videocapture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hk.hku.cs.videoalbum.ListUserVideoActivity;
import hk.hku.cs.videoalbum.MainActivity;
import hk.hku.cs.videoalbum.R;
import hk.hku.cs.videoalbum.browserView.BrowserViewBrowserActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by a on 11/20/2016.
 */

public class VideoCaptureBrowserActivity extends Activity {

    private static final String TAG = VideoCaptureBrowserActivity.class.getSimpleName();

    private static final int VIDEO_CAPTURE_REQUEST = 1111;
    private static final int VIDEO_CAPTURE_PERMISSION = 2222;
    private VideoView mVideoView;

    private OkHttpClient client;

    //private String uploadServerUrl = getString(R.string.server_path) + "upload.php";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture_browser);

        Log.d(TAG, "************************************** enter create...");
        mVideoView = (VideoView) findViewById(R.id.video_image);

        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(VideoCaptureBrowserActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(VideoCaptureBrowserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(VideoCaptureBrowserActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(VideoCaptureBrowserActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }

        if (permissions.size() > 0) {
            String[] permiss = permissions.toArray(new String[0]);

            ActivityCompat.requestPermissions(VideoCaptureBrowserActivity.this, permiss,
                    VIDEO_CAPTURE_PERMISSION);
        } else {
            StartVideoCapture();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIDEO_CAPTURE_REQUEST && resultCode == RESULT_OK) {

            Uri videoUri = data.getData();

            final File myFile = new File(videoUri.getPath());

            if (myFile.isFile() && myFile.exists() && myFile != null) {
                Log.d("file checking", "ok");
            } else {
                Log.d("file checking", "failed");
            }

            new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                        }
                    });

                    uploadFileOkHttp(VideoCaptureBrowserActivity.this.getString(R.string.server_path) + "upload.php", myFile);
                }
            }).start();

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(mVideoView);

            mVideoView.setMediaController(mediaController);
            mVideoView.setVideoURI(videoUri);
            mVideoView.requestFocus();


            // added to back to video list
            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    finish();
                }
            });

            mVideoView.start();
        } else if (requestCode == VIDEO_CAPTURE_REQUEST && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VIDEO_CAPTURE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                StartVideoCapture();
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }

    private void StartVideoCapture() {
        Uri viduri = getOutputMediaFileUri();

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, viduri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120);
        //intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (4 * 1024 * 1024));

        startActivityForResult(intent, VIDEO_CAPTURE_REQUEST);
    }

//    http://stackoverflow.com/questions/23512547/how-to-use-okhttp-to-upload-a-file/30498514#30498514
    public Boolean uploadFileOkHttp(String serverURL, File myFile) {
        try {
            SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
            String username = preferences.getString("username", "");
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", myFile.getName(), RequestBody.create(MediaType.parse("video/mp4"), myFile))
                    .addFormDataPart("username", username)
                    .build();

            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(requestBody)
                    .build();

            //TODO: by OkHttp
            client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle the error
                    if (!call.isExecuted()) {
                        Log.e("Okhttp failure", "Call not executed");
                    }
                    Log.e("OkHttp failure", e.getStackTrace().toString());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (!response.isSuccessful()) {
                            // Handle the error
                            Log.e("OkHttp response", "response is not successful");
                        }
                        if (!call.isExecuted()) {
                            Log.e("Okhttp response", "Call not executed");
                        }
                        Log.d("OkHttp Response", response.toString());
                        // Upload successful
                    } catch (Exception e) {
                        Log.e("OkHttp response", e.getStackTrace().toString());
                    }
                }

            });

            return true;
        } catch (Exception ex) {
            // Handle the error
        }
        return false;
    }

    // using apache library
  /*  private void uploadVideo(Uri uri) {
        String uriAPI = "http://i.cs.hku.hk/~ltllu/php/upload.php";

//        HttpPost
    }*/
    // if use HttpClient
    // http://stackoverflow.com/questions/2975197/convert-file-uri-to-file-in-android
//    http://stackoverflow.com/questions/29058727/i-need-an-alternative-option-to-httpclient-in-android-to-send-data-to-php-as-it

    private Uri getOutputMediaFileUri() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        if (isExternalStorageAvailable()) {
            // get the Uri

            //1. Get the external storage directory
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath());

            //2. Create our subdirectory
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory.");
                    return null;
                }
            }
            //3. Create a file name
            //4. Create the file
            File mediaFile;
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getPath() + File.separator;

            SharedPreferences preferences = this.getSharedPreferences("video-album-login", 0);
            String username = preferences.getString("username", "");

            mediaFile = new File(path + username + "_" + timestamp + ".mp4");

//            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            //5. Return the file's URI
            return Uri.fromFile(mediaFile);
        } else {
            return null;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
//            Log.d(TAG, "external storage available");
            return true;
        } else {
            Log.e(TAG, "not external storage");
            return false;
        }
    }
}
