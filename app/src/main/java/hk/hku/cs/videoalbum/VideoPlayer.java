package hk.hku.cs.videoalbum;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

//TODO: from Billy
public class VideoPlayer extends ActionBarActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);


        Button btn1 = (Button) findViewById(R.id.exit);
        btn1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }

        });
        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        //Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.v01);
        //videoView.setVideoURI(video);
        //videoView.start();
        final List listA = new ArrayList<>();
        listA.add("android.resource://" + getPackageName() + "/" + R.raw.v01);
        listA.add("android.resource://" + getPackageName() + "/" + R.raw.v02);
        listA.add("android.resource://" + getPackageName() + "/" + R.raw.v03);
        listA.add("android.resource://" + getPackageName() + "/" + R.raw.v04);
        listA.add("android.resource://" + getPackageName() + "/" + R.raw.v05);
        listA.add("http://i.cs.hku.hk/~kfwong/videoalbum/upload.php?video=ee_13mar2012_part3.mp4");





        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new OnClickListener() {


            //CheckBox chck = (CheckBox) findViewById(R.id.checkBox);
            ListIterator listIterator = listA.listIterator();

            public void onClick(View v) {
             //   Object randomItem = listA.get(new Random().nextInt(listA.size()));

            //    if (chck.isChecked()) {
            //        Uri video = Uri.parse(String.valueOf(randomItem));
            //        videoView.setVideoURI(video);
            //        videoView.start();
            //    } else {
                if(listIterator.hasNext()) {
                    Uri video = Uri.parse(String.valueOf(listIterator.next()));
                    videoView.setVideoURI(video);
                    videoView.start();
                }
            //    }


            }
        });

        Button prev = (Button) findViewById(R.id.prev);
        prev.setOnClickListener(new OnClickListener() {


            //CheckBox chck = (CheckBox) findViewById(R.id.checkBox);
            ListIterator listIterator = listA.listIterator();

            public void onClick(View v) {
                //Object randomItem = listA.get(new Random().nextInt(listA.size()));

                //if (chck.isChecked()) {
                //    Uri video = Uri.parse(String.valueOf(randomItem));
                //    videoView.setVideoURI(video);
                //    videoView.start();
                //} else {
                if(listIterator.hasPrevious()) {
                    Uri video = Uri.parse(String.valueOf(listIterator.previous()));
                    videoView.setVideoURI(video);
                    videoView.start();
                }
                //}


            }
        });





        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (videoView.isPlaying())
                {
                    videoView.pause();
                }
                else
                {
                    videoView.start();
                }

            }});

    }
}
