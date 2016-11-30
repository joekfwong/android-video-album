package hk.hku.cs.videoalbum.videocapture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

  private String uploadServerUrl = "http://i.cs.hku.hk/~kfwong/videoalbum/upload.php";
  private String uploadServerHome = "http://i.cs.hku.hk/~ltllu/php/upload.htm";

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

      //TODO: mine on video uploading

      final File myFile = new File(videoUri.getPath());
//            final String videoPath = myFile.getAbsolutePath();

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


          //TODO: try upload by OkHttp
          uploadFileOkHttp(uploadServerUrl, myFile);

          //TODO: upload by url connection
//                    uploadFile(videoPath);
//                    uploadFile(myFile);
//                    Log.d("On Activity Result", "upload done");
        }
      }).start();

      MediaController mediaController = new MediaController(this);
      mediaController.setAnchorView(mVideoView);

      mVideoView.setMediaController(mediaController);
      mVideoView.setVideoURI(videoUri);
      mVideoView.requestFocus();

      mVideoView.start();

      //TODO: upload by BrowerView ok
//            uploadFileByBrowser();
//            Intent intent = new Intent(this, BrowserViewBrowserActivity.class);
//            startActivity(intent);
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
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);        //TODO: change from 10 to 5
    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (4 * 1024 * 1024));

    startActivityForResult(intent, VIDEO_CAPTURE_REQUEST);
  }

  //TODO: this is not from tutor's sample code
//    http://stackoverflow.com/questions/23512547/how-to-use-okhttp-to-upload-a-file/30498514#30498514
  public Boolean uploadFileOkHttp(String serverURL, File myFile) {
    try {
/*
            RequestBody requestBody = new MultipartBody()
                    .type(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("some-field", "some-value")
                    .build();*/

//            RequestBody requestBody = RequestBody.create(MediaType.parse("video/mp4"), file);
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
      OkHttpClient client = new OkHttpClient();
      client.newCall(request).enqueue(new Callback() {

        @Override
//                public void onFailure(Request request, IOException e) {
        public void onFailure(Call call, IOException e) {
          // Handle the error
          if (!call.isExecuted()) {
            Log.e("Okhttp failure", "Call not executed");
          }
          Log.e("OkHttp failure", e.getStackTrace().toString());
        }

        @Override
//                public void onResponse(Response response) throws IOException {
//                public void onResponse(Call call, Response response) throws IOException {
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

  public void uploadFileByBrowser() {
    Intent browserViewIntent = new Intent(this, BrowserViewBrowserActivity.class);

//        browserViewIntent.putExtra("url", uploadServerUri);
    browserViewIntent.putExtra("url", uploadServerHome);
//        this.startActivity(browserViewIntent);
    startActivityForResult(browserViewIntent, 0);
  }

  //    public int uploadFile(String filePath) {
  public int uploadFile(File file) {
//        String upLoadServerUri = "http://i.cs.hku.hk/~ltllu/php/upload.htm";
    int serverResponseCode = 0;


//        File sourceFile = new File(filePath);
//        final String uploadFileName = file.getName();

//        String fileName = uploadFileName;
    String fileName = file.getName();
//        String fileName = filePath;

    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    String randomeStr = "webKitFormBoundaryZb6k6WJ3mtDklowQ";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;


    if (!file.isFile()) {
/*
            dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :"
                    + uploadFilePath + "" + uploadFileName);
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :"
                            + uploadFilePath + "" + uploadFileName);
                }
            });*/
      return 0;
    } else {
      try {

        // open a URL connection to the Servlet
        FileInputStream fileInputStream = new FileInputStream(file);
        URL url = new URL(uploadServerUrl);

        // Open a HTTP  connection to  the URL
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
//                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + twoHyphens + twoHyphens + randomeStr);

//                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));
        conn.connect();
//                conn.setRequestProperty("file", fileName);

//                TODO: LOG FILENAME
        Log.i("file name", fileName);

        dos = new DataOutputStream(conn.getOutputStream());

        dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"file=\";" + lineEnd);
        dos.writeBytes("Content-Type:" + "video/mp4" + lineEnd);
        dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

        dos.writeBytes(lineEnd);

        //TODO: choose which toByte
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"" + "file" + "\"" + lineEnd);
        dos.writeBytes("Content-Type: video/mp4" + lineEnd);
        dos.writeBytes(lineEnd);
        dos.write(fileToByte(file));
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
/*
                dos.write(fileToByte(sourceFile));
                dos.write("\r\n".getBytes());
                dos.flush();
                Log.d("UploadFile . Write Byte", "written");*/

        //TODO: for getting response from php
        InputStream is = conn.getInputStream();

        // a version of uploading
                /*
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                */


        // Responses from the server (code and message)
        serverResponseCode = conn.getResponseCode();
        String serverResponseMessage = conn.getResponseMessage();

        Log.i("uploadFile", "HTTP Response is - "
                + serverResponseMessage + "- " + serverResponseCode);

        if (serverResponseCode == 200) {

          runOnUiThread(new Runnable() {
            public void run() {
/*

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    + " http://www.androidexample.com/media/uploads/"
                                    + uploadFileName;
*/

//                            messageText.setText(msg);
              Toast.makeText(VideoCaptureBrowserActivity.this, "File Upload Complete. old",
                      Toast.LENGTH_SHORT).show();
            }
          });
        } else {
          Log.e("Upload Byte", "Fail to upload file");
        }

        //TODO: debug get response from server
//                InputStream is = conn.getInputStream();
        // retrieve the response from server
        int ch;
        StringBuffer strBuffer = new StringBuffer();
        while ((ch = is.read()) != -1) {
          strBuffer.append((char) ch);
        }
        String str = strBuffer.toString();

        is.close();
        Log.i("Response", str);

        //close the streams //

//                dos.write("\r\n".getBytes());       // end with line end
        dos.flush();
        dos.close();
        fileInputStream.close();
        conn.disconnect();

      } catch (MalformedURLException ex) {

//                dialog.dismiss();
        ex.printStackTrace();
/*

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(UploadToServer.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
*/

        Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
      } catch (Exception e) {

//                dialog.dismiss();
        e.printStackTrace();
/*
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });*/
        Log.e("Upload file Exception", "Exception : "
                + e.getMessage(), e);
      }
//            dialog.dismiss();
      return serverResponseCode;

    } // End else block
  }

  // using apache library
  /*  private void uploadVideo(Uri uri) {
        String uriAPI = "http://i.cs.hku.hk/~ltllu/php/upload.php";

//        HttpPost
    }*/
  // if use HttpClient
  // http://stackoverflow.com/questions/2975197/convert-file-uri-to-file-in-android
//    http://stackoverflow.com/questions/29058727/i-need-an-alternative-option-to-httpclient-in-android-to-send-data-to-php-as-it


  private byte[] toByte(File file) {
    return new byte[1];
  }

  public byte[] fileToByte(File file) {
//        java.nio.file.Files.readAllBytes(file.getPath());
    java.io.RandomAccessFile radnomAccessFile;
    byte[] bytes = null;
    try {
      radnomAccessFile = new java.io.RandomAccessFile(file, "r");
      bytes = new byte[(int) radnomAccessFile.length()];
      radnomAccessFile.readFully(bytes);
    } catch (Exception e) {
      Log.e("Convert File", e.getStackTrace().toString());
    }
    return bytes;
  }

  // if use HttpURLConnection
  private void uploadFile0(Uri uri) {
//        https://www.javacodegeeks.com/2013/06/android-http-client-get-post-download-upload-multipart-request.html
//        http://androidexample.com/Upload_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83
    int serverResponseCode = 0;
    HttpURLConnection conn;
    DataOutputStream dos = null;
    File video;
    String url = "http://i.cs.hku.hk/~ltllu/php/upload.php";
    String boundary = "*****";
    String CHARSET = "UTF-8";
    byte[] buffer;
    int bytesRead, bytesAvailable, bufferSize;
    int maxBufferSize = 1 * 1024 * 1024;
    FileInputStream fileInputStream = null;
    try {
      // set params
      String delimiter = "--";
      String paramName = "file";
      video = new File(uri.getPath());
      String fileName = video.getName();
      buffer = fileToByte(video);

      // open connection
      fileInputStream = new FileInputStream(uri.getPath());

      conn = (HttpURLConnection) (new URL(url)).openConnection();
      conn.setRequestMethod("POST");
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setRequestProperty("Connection", "Keep-Alive");
      conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//            conn.setRequestProperty("Content-Type", "multipart/form-data");
      conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
      conn.setRequestProperty("file", fileName);
      conn.setRequestProperty("Accept-Charset", CHARSET);

//            conn.connect();
//            os = conn.getOutputStream();
      dos = new DataOutputStream(conn.getOutputStream());

//            os.write( (delimiter + boundary + "\r\n").getBytes());
      dos.writeBytes("--" + boundary + "\r\n");
      dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n");
      dos.writeBytes("\r\n");
//            dos.flush();
//            dos.write(("Content-Type: application/octet-stream\r\n").getBytes());
//            dos.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
//            dos.write("\r\n".getBytes());

/*
            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necessary after file data...
            dos.writeBytes("\r\n");
            dos.writeBytes("--" + boundary + "--" + "\r\n");
            */


      // server response
      serverResponseCode = conn.getResponseCode();
      String serverResponseMessage = conn.getResponseMessage();


      Log.i("uploadFile", "HTPP Response is : " + serverResponseCode + ":" + serverResponseMessage);

      //TODO: add confirm upload button/popup here
/*
            if(serverResponseCode == 200){

                runOnUiThread(new Runnable() {
                    public void run() {

                        String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                +" http://www.androidexample.com/media/uploads/"
                                +uploadFileName;

                        messageText.setText(msg);
                        Toast.makeText(UploadToServer.this, "File Upload Complete.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }*/

    } catch (Exception e) {
      Log.e("uploadFile", e.getStackTrace().toString());
    } finally {
      try {
        fileInputStream.close();
        dos.flush();
        dos.close();
      } catch (IOException e) {
        Log.e("uploadFile IOException", e.getStackTrace().toString());
        e.printStackTrace();
      }
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

      mediaFile = new File(path + "VID_" + timestamp + ".mp4");

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
