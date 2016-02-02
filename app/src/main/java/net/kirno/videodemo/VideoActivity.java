package net.kirno.videodemo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {

    //    private static final Uri URI = Uri.parse("rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");
    private ProgressDialog mProgress;
    private EditText mRriEdit;
    private VideoView mVideoView;

    private String[] mUriList = new String[]{
            "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp",
            "rtsp://192.168.0.1/vs1"
    };
    private AlertDialog.Builder mUriListAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mVideoView = (VideoView) findViewById(R.id.videoView);
        Button btn = (Button) findViewById(R.id.btn);
        mRriEdit = (EditText) findViewById(R.id.edit_uri);
        Button btnList = (Button) findViewById(R.id.btn_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(VideoActivity.this, android.R.layout.simple_list_item_1,mUriList);

        mUriListAlert = new AlertDialog.Builder(VideoActivity.this)
                .setTitle("地址列表")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String uri = mUriList[which];
                        playVideo(uri);
                    }
                });
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUriListAlert.show();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = mRriEdit.getText().toString();
                playVideo(uri);
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VideoActivity.this, "播放出错", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
                return true;
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mProgress.dismiss();
            }
        });
    }

    private void playVideo(String videoUri) {
        mProgress = ProgressDialog.show(VideoActivity.this, "加载中", "努力中，别急");
        mVideoView.setVideoURI(Uri.parse(videoUri));
        mVideoView.start();
        mVideoView.requestFocus();
    }
}
