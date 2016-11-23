package hk.hku.cs.videoalbum.VIdeoCapture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hk.hku.cs.videoalbum.R;

/**
 * Created by a on 11/20/2016.
 */

public class VideoCaptureBrowserActivity extends Activity {

    private static final String TAG = VideoCaptureBrowserActivity.class.getSimpleName();

    private static final int VIDEO_CAPTURE_REQUEST = 1111;
    private static final int VIDEO_CAPTURE_PERMISSION = 2222;
    private VideoView mVideoView;

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

        if(permissions.size() > 0) {
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

            MediaController mediaController= new MediaController(this);
            mediaController.setAnchorView(mVideoView);

            mVideoView.setMediaController(mediaController);
            mVideoView.setVideoURI(videoUri);
            mVideoView.requestFocus();

            mVideoView.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VIDEO_CAPTURE_PERMISSION) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                StartVideoCapture();
            }
            else {
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
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);        //TODO: change from 10 to 5
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (4 * 1024 * 1024));

        startActivityForResult(intent, VIDEO_CAPTURE_REQUEST);
    }

    private Uri getOutputMediaFileUri() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        if (isExternalStorageAvailable()) {
            // get the Uri

            //1. Get the external storage directory
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath());

            //2. Create our subdirectory
            if (! mediaStorageDir.exists()) {
                if(! mediaStorageDir.mkdirs()){
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

            mediaFile = new File(path + "VID_" + timestamp + ".mp4");

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            //5. Return the file's URI
            return Uri.fromFile(mediaFile);
        } else {
            return null;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)){
            Log.d(TAG, "external storage available");
            return true;
        } else {
            Log.d(TAG, "not external storage");
            return false;
        }
    }
}
