package com.nx.nx6313.mp4radio;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        inputUrl(null);
    }

    public void inputUrl(View view) {
        final LinearLayout playLayout = new LinearLayout(StartActivity.this);
        playLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText playName = new EditText(StartActivity.this);
        playName.setHint("输入视频的名字");
        playName.setText("测试视频");
        playName.setPadding(DisplayUtil.dip2px(StartActivity.this, 30), DisplayUtil.dip2px(StartActivity.this, 16), DisplayUtil.dip2px(StartActivity.this, 30), DisplayUtil.dip2px(StartActivity.this, 16));
        playLayout.addView(playName);
        final EditText playUrl = new EditText(StartActivity.this);
        playUrl.setHint("输入视频的链接");
        playUrl.setText("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4");
        playUrl.setPadding(DisplayUtil.dip2px(StartActivity.this, 30), DisplayUtil.dip2px(StartActivity.this, 16), DisplayUtil.dip2px(StartActivity.this, 30), DisplayUtil.dip2px(StartActivity.this, 16));
        playLayout.addView(playUrl);
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setTitle("请输入视频网址链接").setIcon(R.drawable.edit).setView(playLayout).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playNameStr = playName.getText().toString();
                String playUrlStr = playUrl.getText().toString();

                Intent playIntent = new Intent(StartActivity.this, MainActivity.class);
                playIntent.putExtra("playNameStr", playNameStr);
                playIntent.putExtra("playUrlStr", playUrlStr);
                StartActivity.this.startActivity(playIntent);
            }
        });
        builder.show();
    }
}
