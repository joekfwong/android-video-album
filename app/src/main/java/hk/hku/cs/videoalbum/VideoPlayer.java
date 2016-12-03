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
            }

        });
        final VideoView videoView = (VideoView) findViewById(R.id.videoView);

        final List<String> fileList = this.getIntent().getStringArrayListExtra("filelist");
        final int position = this.getIntent().getIntExtra("position", 0);

        final ListIterator<String> listIterator = fileList.listIterator(position);

        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new OnClickListener() {


            public void onClick(View v) {
                if(listIterator.hasNext()) {
                    Uri video = Uri.parse(String.valueOf(listIterator.next()));
                    videoView.setVideoURI(video);
                    videoView.start();
                }
            }
        });

        Button prev = (Button) findViewById(R.id.prev);
        prev.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if(listIterator.hasPrevious()) {
                    Uri video = Uri.parse(String.valueOf(listIterator.previous()));
                    videoView.setVideoURI(video);
                    videoView.start();
                }
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

        Uri video = Uri.parse(listIterator.next());
        videoView.setVideoURI(video);
        videoView.start();
    }
}
