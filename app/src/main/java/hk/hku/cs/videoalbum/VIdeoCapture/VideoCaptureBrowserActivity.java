package hk.hku.cs.videoalbum.videocapture;

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
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hk.hku.cs.videoalbum.R;

import org.apache.http.*;

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

        //TODO: coding
        uploadVideo(viduri);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, viduri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);        //TODO: change from 10 to 5
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (4 * 1024 * 1024));

        startActivityForResult(intent, VIDEO_CAPTURE_REQUEST);
    }

    //TODO: this is not from tutor's sample code
    // if use HttpClient
    // http://stackoverflow.com/questions/2975197/convert-file-uri-to-file-in-android
//    http://stackoverflow.com/questions/29058727/i-need-an-alternative-option-to-httpclient-in-android-to-send-data-to-php-as-it
    private void uploadFile(Uri uri) {
//        https://www.javacodegeeks.com/2013/06/android-http-client-get-post-download-upload-multipart-request.html
        HttpURLConnection con;
        OutputStream os;
        File video;
        String url = "http://i.cs.hku.hk/~ltllu/php/upload.php";
        Byte data;
        try {
            // open connection
            con = (HttpURLConnection) (new URL(url)).openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data");
//            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.connect();
            os = con.getOutputStream();

            // upload files
            String delimiter = "--";
            String paramName = "file";
            video = new File(uri.getPath());
            String fileName = video.getName();
            data =

//            os.write( (delimiter + boundary + "\r\n").getBytes());
            os.write( ("Content-Disposition: form-data; name=\"" + paramName +  "\"; filename=\"" + fileName + "\"\r\n"  ).getBytes());
            os.write( ("Content-Type: application/octet-stream\r\n"  ).getBytes());
            os.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
            os.write("\r\n".getBytes());

            os.write(data);

            os.write("\r\n".getBytes());
        } catch (Exception e) {

        }
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
