package com.nx.nx6313.mp4radio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VideoPlayer extends RelativeLayout {
	
	/**----------------------布局-----------------------**/
	
	RelativeLayout root;
	
	SurfaceView playerView;
	
	// 顶部布局
	RelativeLayout topLayout;
	
	ImageView videoBack;
	
	TextView videoTime;
	
	TextView videoTitle;
	
	ImageView videoDownload;
	
	ImageView videoFavorite;
	
	ImageView videoPower;
	
	// 底部布局
	LinearLayout bottomLayout;
	
	ImageView videoPlay;
	
	SeekBar videoPlayProgress;
	
	TextView videoTimeProgress;
	
	ImageView videoFullScreen;
	
	// 加载进度条
	ProgressBar videoLoading;
	
	/**----------------------电量监控-----------------------**/
	
    private BroadcastReceiver batteryLevelRcvr;  
    
    private IntentFilter batteryLevelFilter;  

	/**----------------------播放数据-----------------------**/
	
	int screenWidth;
	
	int screenHeight;
	
	int position;
	
	int duration;
	
	int controller_time = 5000;
	
	/**
	 * 是否隐藏控制条
	 */
	boolean bool = true;
	
	TelephonyManager manager;
	
	private MediaPlayer mPlayer;
	
	/**----------------------控制数据-----------------------**/
	
	Session session = Session.getSession();
	
	GravityUtil gu;
	
	Activity playerActivity;
	
	boolean fullScreen = true;
	
	private String address = "";
	
	private String title = "";
	
	VideoDownloder vd;
	
	String filePath;
	
	long videoCacheSize;
	
	long videoTotalSize;
	
	/**
	 * 下载进度
	 */
	int cachepercent;
	
	OnClickListener backClickListener;
	
	OnClickListener favoriteClickListener;
	
	OnClickListener downloadClickListener;
	
	boolean isFavorite;
	
	/**
	 * 状态：0，初始状态，1，正在初始化视频数据，2，视频数据初始化完成，
	 * 3，播放器初始化完成(prepared)，4，播放中，5， 暂停中（用户点击了暂停），
	 * 6，加载中（也会暂停播放），7，手机锁屏或电话打入暂停，8，播放完毕，9，播放错误
	 */
	int state = 0;
	
	//构造方法，必有
	public VideoPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	//构造方法
	public VideoPlayer(Context context) {
		super(context);
	}

	//初始化界面
	public void onCreate(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		root = (RelativeLayout) inflater.inflate(R.layout.widget_player, VideoPlayer.this);
		
		playerView = findViewById(R.id.playerView);
		topLayout = findViewById(R.id.topLayout);
		bottomLayout = findViewById(R.id.bottomLayout);
		videoLoading = findViewById(R.id.videoLoading);
		
		videoBack = findViewById(R.id.videoBack);
		videoTitle = findViewById(R.id.videoTitle);
		videoTime = findViewById(R.id.videoTime);
		videoDownload = findViewById(R.id.videoDownload);
		videoFavorite = findViewById(R.id.videoFavorite);
		videoPower = findViewById(R.id.videoPower);
		
		videoPlay = findViewById(R.id.videoPlay);
		videoPlayProgress = findViewById(R.id.videoPlayProgress);
		videoTimeProgress = findViewById(R.id.videoTimeProgress);
		videoFullScreen = findViewById(R.id.videoFullScreen);
		videoLoading = findViewById(R.id.videoLoading);
		
		screenWidth = (Integer) session.get(Session.WIDTH);
		screenHeight = (Integer) session.get(Session.HEIGHT);
		
		videoBack.setOnClickListener(backClickListener);
		videoDownload.setOnClickListener(downloadClickListener);
		videoFavorite.setOnClickListener(favoriteClickListener);
		videoPlay.setOnClickListener(favoriteClickListener);
		if(isFavorite){
			videoFavorite.setImageResource(R.drawable.icv_favorite_selected);
		} else {
			videoFavorite.setImageResource(R.drawable.icv_favorite);
		}
		
		initialize();
	}
	
	public void initialize(){
		gu = new GravityUtil(getContext());
		gu.setOnLandscapeListener(new GravityUtil.OnLandscapeListener() {
			@Override
			public void onLandscape() {
				if(playerActivity == null){
					return;
				}
				if(playerActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
					playerActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
			}
		});
		gu.setOnPortraitListener(new GravityUtil.OnPortraitListener() {
			@Override
			public void onPortrait() {
				if(playerActivity == null){
					return;
				}
				if(playerActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT){
					playerActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
			}
		});
		
		videoFullScreen.setOnClickListener(clickListener);
		videoPlay.setOnClickListener(clickListener);
		videoTitle.setOnClickListener(clickListener);
		videoTime.setOnClickListener(clickListener);
		videoPower.setOnClickListener(clickListener);
		videoTimeProgress.setOnClickListener(clickListener);
		bottomLayout.setOnClickListener(clickListener);
		topLayout.setOnClickListener(clickListener);
		
		videoPlayProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// 延迟工具条消失的时间
				handler.removeCallbacks(hide_controller);
				handler.postDelayed(hide_controller, controller_time);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				//根据光标移动，控制播放进度，移动到最后则前进500毫秒，以使系统能够相应结束事件
				if(fromUser && mPlayer != null && state > 3 && state < 9){
					int point = 0;
					if(progress == 500)
						point = progress*mPlayer.getDuration()/500 - 500;
					else
						point = progress*mPlayer.getDuration()/500;
					seek(point);
				}
			}
		});
		
		videoLoading.setVisibility(View.GONE);
		
		videoTitle.setText(title);
		monitorBatteryState();
	}
	
	OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			
			switch (view.getId()) {
			case R.id.videoFullScreen:
				if(fullScreen){
					backFromFullScreen();
				} else {
					goToFullScreen();
				}
				break;
				
			case R.id.videoPlay:
				if(state == 4){
					//正在播放的时候，则暂停
					state = 5;
					pause();
				} else {
					//暂停的时候，则开始播放
					play();
				}
				break;

			default:
				break;
			}
			
			// 延迟工具条消失的时间
			handler.removeCallbacks(hide_controller);
			handler.postDelayed(hide_controller, controller_time);
		}
	};
	
	public void goToFullScreen(){
		if(!fullScreen){
			fullScreen = true;
			playerActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			
			if(getLayoutParams() == null){
				setLayoutParams(new RelativeLayout
						.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, screenWidth));
			} else {
				getLayoutParams().height = screenWidth;
			}
			playerActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			videoFavorite.setVisibility(View.GONE);
			videoDownload.setVisibility(View.GONE);
			videoPower.setVisibility(View.VISIBLE);
			videoTime.setVisibility(View.VISIBLE);
			videoFullScreen.setImageResource(R.drawable.icv_shrink_screen);
			
			handler.post(update_time);
		}
	}
	
	public void backFromFullScreen(){
		if(fullScreen){
			fullScreen = false;
			playerActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			
			if(getLayoutParams() == null){
				setLayoutParams(new RelativeLayout
						.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, screenWidth * 9 / 16));
			} else {
				getLayoutParams().height = screenWidth * 9 / 16;
			}
			final WindowManager.LayoutParams attrs = playerActivity.getWindow().getAttributes();
	        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        playerActivity.getWindow().setAttributes(attrs);
			playerActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			
			videoFavorite.setVisibility(View.VISIBLE);
			videoDownload.setVisibility(View.VISIBLE);
			videoPower.setVisibility(View.GONE);
			videoTime.setVisibility(View.GONE);
			videoFullScreen.setImageResource(R.drawable.icv_full_screen);
			
			handler.removeCallbacks(update_time);
		}
	}
	
	public void onPause() {
		if (mPlayer != null && state == 4 && mPlayer.isPlaying()){
			// 保存当前的播放位置
			position = mPlayer.getCurrentPosition();
			mPlayer.pause();
		}
		playerActivity.unregisterReceiver(batteryLevelRcvr);
		if(gu != null) {
			gu.onPause();
		}
	}
	
	public void onResume() {
		if(gu != null) {
			gu.onResume();
		}
		playerActivity.registerReceiver(batteryLevelRcvr, batteryLevelFilter);
		handler.postDelayed(start_video, 100);
	}
	
	public void onDestroy() {
		handler.removeCallbacks(hide_controller);
		handler.removeCallbacks(start_video);
		handler.removeCallbacks(update_progress);
		handler.removeCallbacks(update_time);
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
		}
	}
	
	//即时修改播放进度
	Runnable update_progress = new Runnable() {
		@Override
		public void run() {
			if(state == 4){
				position = mPlayer.getCurrentPosition();
				// 推进下载进度
				vd.setCurrentDuration(position);
				if(position + 500 > mPlayer.getDuration()){
					//没有播放完则在500毫秒后继续修改进度
					stop();
				} else {
					if(duration < 2 || (mPlayer.getDuration() > 0 && mPlayer.getDuration() < duration))
						duration = mPlayer.getDuration();
					if(duration < 1)
						duration = 1;
					//播放完则停止修改进度
					videoPlayProgress.setProgress(500*position/duration);
					videoPlayProgress.setSecondaryProgress(cachepercent);
					// videoPlayProgress.setSecondaryProgress(500*position/mPlayer.getDuration() + 50);
					videoTimeProgress.setText(getTime(position)+"/"+getTime(duration));
					handler.postDelayed(update_progress, 500);
				}
			} else {
				handler.postDelayed(update_progress, 500);
			}
		}
	};
	
	//即时修改播放进度
	Runnable update_time = new Runnable() {
		@Override
		public void run() {
			videoTime.setText(DateHandler.formatDate(null, "HH:mm"));
			handler.postDelayed(update_time, 10000);
		}
	};

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		if(fullScreen){
			goToFullScreen();
		} else {
			backFromFullScreen();
		}
	}

	public void setPlayerActivity(Activity playerActivity) {
		this.playerActivity = playerActivity;
		onCreate();
		
		playerView.setBackgroundResource(R.drawable.back_ground_pic);
		// 设置SurfaceView自己不管理的缓冲区
		playerView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 设置播放时打开屏幕
		playerView.getHolder().setKeepScreenOn(true);
		playerView.getHolder().addCallback(new SurfaceListener());
		
		//处理屏幕的触摸事件
		playerView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(bool){
					//当工具条显示的时候的处理
					topLayout.setVisibility(View.GONE);
					bottomLayout.setVisibility(View.GONE);
					bool = false;
				} else {
					//当工具条隐藏的时候的处理
					handler.removeCallbacks(hide_controller);
					topLayout.setVisibility(View.VISIBLE);
					bottomLayout.setVisibility(View.VISIBLE);
					handler.postDelayed(hide_controller, controller_time);
					bool = true;
				}
				return false;
			}
		});
		handler.postDelayed(hide_controller, controller_time);
		
		//监听手机电话
    	manager = (TelephonyManager) playerActivity.getSystemService(playerActivity.TELEPHONY_SERVICE);
    	manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	public void onConfigurationChanged(Configuration newConfig){
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
			goToFullScreen();
		} else {
			backFromFullScreen();
		}
	}
	
	//隐藏工具条的方法
	Runnable hide_controller = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(hide_controller);
			bottomLayout.setVisibility(View.GONE);
			topLayout.setVisibility(View.GONE);
			bool = false;
		}
	};
	
	//开始播放视频的方法
	Runnable start_video = new Runnable() {
		@Override
		public void run() {
			try {
				// 开始播放
				play();
			} catch (Exception e) {
				e.printStackTrace();
			}
			videoPlay.setImageResource(R.drawable.icv_pause);
		}
	};
	
	/**
	 * TODO 播放
	 */
	private void play() {
		Log.i("test", "----------------------------state : "+state+"--------------------------");
		
		switch (state) {
		case 0:
			playerActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			playerView.setBackgroundColor(0x00ffffff);
			
			// 设置需要播放的视频
			Random ran =new Random(System.currentTimeMillis());
			FileHandler fh = new FileHandler(playerActivity);
			filePath = fh.SDPATH + fh.Path + ran.nextInt(10000) + ".mp4";
			
			videoLoading.setVisibility(View.VISIBLE);
			new AsyncTask<Object, Object, Object>() {
				@Override
				protected Object doInBackground(Object... arg0) {
					try {
						prepareVideo();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			}.execute();
			state = 1;
			break;
			
		case 2:
			try {
				// 创建MediaPlayer
				if(mPlayer == null){
					createMediaPlayer(playerView.getHolder());
				}
				
				mPlayer.setDataSource(filePath);
				try {
					mPlayer.prepareAsync();
				} catch (Exception e) {
					e.printStackTrace();
				}
				handler.post(update_progress);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
		case 3:
			videoLoading.setVisibility(View.GONE);
			mPlayer.seekTo(position);
			mPlayer.start();
			state = 4;
			break;
			
		case 5:
			mPlayer.start();
			handler.post(update_progress);
			videoPlay.setImageResource(R.drawable.icv_pause);
			state = 4;
			break;
			
		case 9:
			videoLoading.setVisibility(View.VISIBLE);
			try {
				mPlayer.setDataSource(filePath);
				
				int next5sec = position + 5 * 1000;
				if (next5sec > duration) {
					next5sec = duration;
				}

				if (vd.checkIsBuffered(next5sec / 1000)) {
					Log.e("test", "----------------------------checkIsBuffered--------------------------");
					mPlayer.prepareAsync();
				}
				state = 2;
			} catch (IOException e) {
				e.printStackTrace();
				handler.sendEmptyMessageDelayed(VIDEO_STATE_UPDATE, 1000);
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * TODO 暂停
	 */
	private void pause(){
		switch (state) {
		case 5:
			// 用户点击了暂停
			break;
			
		case 6:
			// 数据缓冲中
			videoLoading.setVisibility(View.VISIBLE);
			break;
			
		case 7:
			// 锁屏或者电话打入
			break;

		default:
			break;
		}
		Log.e("test", "--------------------state:"+state+"---------------------");
		if (state > 4 && state < 8 && mPlayer.isPlaying()) {
			mPlayer.pause();
			position = mPlayer.getCurrentPosition();
			videoPlayProgress.setProgress(500*position/mPlayer.getDuration());
			videoTimeProgress.setText(getTime(position)+"/"+getTime(duration));
		}
		handler.removeCallbacks(update_progress);
		videoPlay.setImageResource(R.drawable.icv_play);
	}
	
	/**
	 * TODO 跳转到某个时间点，毫秒
	 * @param point
	 */
	private void seek(int point){
		position = point;
		if(state < 4 || state > 8){
			return;
		}
		if(state == 6){
			videoLoading.setVisibility(View.GONE);
		}
		if(vd.checkIsBuffered(point / 1000)){
			mPlayer.seekTo(point);
			state = 5;
			play();
		} else {
			vd.seekLoadVideo(point / 1000);
			state = 6;
			pause();
		}
		videoTimeProgress.setText(getTime(point)+"/"+getTime(duration));
	}
	
	/**
	 * TODO 切换播放的视频
	 * @param url
	 */
	private void shift(String url){
		
	}
	
	public void stop(){
		handler.removeCallbacks(update_progress);
		// 停止播放
		if (mPlayer.isPlaying())
			mPlayer.stop();
		// 释放资源
		mPlayer.release();
	}
	
	/**
	 * TODO 下载和缓冲mp4文件头部数据
	 * */
	private void prepareVideo() throws IOException {
		
		URL url = new URL(address);
		HttpURLConnection httpConnection = (HttpURLConnection) url
				.openConnection();
		httpConnection.setConnectTimeout(3000);
		httpConnection.setRequestProperty("RANGE", "bytes=" + 0 + "-");

		InputStream is = httpConnection.getInputStream();

		videoTotalSize = httpConnection.getContentLength();
		Log.i("test", "videoTotalSize : " + videoTotalSize);
		if (videoTotalSize == -1) {
			return;
		}

		File cacheFile = new File(filePath);

		if (!cacheFile.exists()) {
			cacheFile.getParentFile().mkdirs();
			cacheFile.createNewFile();
		} else {
			cacheFile.delete();
			cacheFile.createNewFile();
		}

		RandomAccessFile raf = new RandomAccessFile(cacheFile, "rws");
		raf.setLength(videoTotalSize);
		raf.seek(0);

		byte buf[] = new byte[10 * 1024];
		byte bts[] = new byte[8];
		int size = 0;

		videoCacheSize = 0;
		// 下载1000KB作为初始缓存
		int len = 100 * buf.length;
		// 视频总长度
		int videoLen = 0;
		
		while (true) {//is.available() > 0
			size += is.read(bts);
			raf.write(bts, 0, 8);
			videoLen = byteArrayToInt(bts);
			Log.i("test", "-----------------"+new String(bts)+" : "+videoLen+"----------------");
			if(new String(bts).contains("mdat")){
				videoCacheSize = size;
				Log.i("test", "-----------------oooooo : "+size+"----------------");
				int n = 0;
				while(n < len){
					int s = 0;
					if(buf.length > len - n)
						s = is.read(buf, 0, len - n);
					else
						s = is.read(buf, 0, buf.length);
					if(s > 0){
						n += s;
						raf.write(buf, 0, s);
					}
				}
				size += videoLen - 8;
				videoCacheSize += len;
				raf.seek(size); //(num - 8 - len);
				break;
			} else {
				int n = 0;
				while(n  < videoLen - 8){
					int s = 0;
					if(buf.length > videoLen - 8 - n)
						s = is.read(buf, 0, videoLen - 8 - n);
					else
						s = is.read(buf, 0, buf.length);
					if(s > 0){
						n += s;
						raf.write(buf, 0, s);
					}
				}
				size += n;
			}
		}
		is.close();
		
		httpConnection.disconnect();
		url = new URL(address);
		httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setConnectTimeout(3000);
		httpConnection.setRequestProperty("RANGE", "bytes=" + size + "-");
		
		Log.i("test", size + " + " + httpConnection.getContentLength() + " = " + videoTotalSize);

		is = httpConnection.getInputStream();
		if (httpConnection.getContentLength() > 0) {
			is.read(bts);
			raf.write(bts, 0, 8);
			int num = byteArrayToInt(bts);
			Log.i("test", "------------------"+new String(bts)+" : "+num+"-------------------");
			
			int n = 0;
			while(n  < num - 8){
				int s = 0;
				if(buf.length > num - 8 - n)
					s = is.read(buf, 0, num - 8 - n);
				else
					s = is.read(buf, 0, buf.length);
				if(s > 0){
					n += s;
					raf.write(buf, 0, s);
				}
			}
		}

		raf.close();
		is.close();
		
		state = 2;
		handler.sendEmptyMessage(CACHE_VIDEO_READY);
		
		vd = new VideoDownloder(handler, address, filePath);
		vd.initVideoDownloder(len, videoLen);
	}

	private final static int VIDEO_STATE_UPDATE = 0;
	private final static int CACHE_VIDEO_READY = 1;
	
	private final Handler handler = new Handler(this.getContext().getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// TODO 状态变化
			case VIDEO_STATE_UPDATE:
				

				//handler.sendEmptyMessageDelayed(VIDEO_STATE_UPDATE, 1000);
				break;

			// TODO 准备就绪
			case CACHE_VIDEO_READY:
				play();
				break;

			// TODO 下载完毕
			case VideoDownloder.MSG_DOWNLOADFINISH:
				videoCacheSize = videoTotalSize;
				cachepercent = 500;
				break;

			// TODO 下载数据刷新
			case VideoDownloder.MSG_DOWNLOADUPDATE:
				if(mPlayer == null){
					return;
				}
				try {
					videoCacheSize = (Long) msg.obj;
				} catch (Exception e) {
					e.printStackTrace();
				}
				cachepercent = (int) (videoCacheSize * 500 / (videoTotalSize == 0 ? 1
						: videoTotalSize));

				if(state == 6){
					try {
						int next5sec = position + 5 * 1000;
						if (next5sec > duration) {
							next5sec = duration;
						}

						if (vd.checkIsBuffered(next5sec / 1000)) {
							mPlayer.start();
							videoLoading.setVisibility(View.GONE);
							
							state = 4;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			}

			super.handleMessage(msg);
		}
	};
	
	private void createMediaPlayer(SurfaceHolder holder) {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
		}
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		mPlayer.reset();
		mPlayer.setDisplay(holder);

		mPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				state = 3;

				videoLoading.setVisibility(View.GONE);
				mp.seekTo(position);
				mp.start();
				
				state = 4;
			}
		});

		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				state = 8;
				mp.pause();
				videoPlayProgress.setProgress(500);
				videoTimeProgress.setText(getTime(duration)+"/"+getTime(duration));
				handler.removeCallbacks(update_progress);
			}
		});

		mPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {

				state = 9;
				mPlayer.stop();
				mPlayer.reset();
				mPlayer.setDisplay(playerView.getHolder());
				
				handler.postDelayed(start_video, 100);
				//play();

				return true;
			}
		});
	}
	
	public String getTime(long time) {
		String format_time = "";

		time = time / 1000;
		long temp = time % 60;
		if (temp > 9)
			format_time += ":" + temp;
		else
			format_time += ":0" + temp;

		time = time / 60;
		temp = time % 60;
		if (temp > 9)
			format_time = temp + format_time;
		else
			format_time = "0" + temp + format_time;

		temp = time / 60;
		if (temp > 0)
			format_time = temp + ":" + format_time;

		return format_time;
	}
	
	private void monitorBatteryState() {  
        batteryLevelRcvr = new BroadcastReceiver() {  
        	@Override
            public void onReceive(Context context, Intent intent) {  
                int rawlevel = intent.getIntExtra("level", -1);  
                int scale = intent.getIntExtra("scale", -1);  
                int status = intent.getIntExtra("status", -1);  
                int health = intent.getIntExtra("health", -1);  
                int level = -1; // percentage, or -1 for unknown  
                if (rawlevel >= 0 && scale > 0) {  
                    level = (rawlevel * 100) / scale;  
                }
                if (BatteryManager.BATTERY_HEALTH_OVERHEAT == health) {  
                    // TODO 电池过热
                }
                if(level > 90){
                	videoPower.setImageResource(R.drawable.icv_power_05);
                } else if(level > 70){
                	videoPower.setImageResource(R.drawable.icv_power_04);
                } else if(level > 50){
                	videoPower.setImageResource(R.drawable.icv_power_03);
                } else if(level > 30){
                	videoPower.setImageResource(R.drawable.icv_power_02);
                } else if(level > 10 || status == BatteryManager.BATTERY_STATUS_CHARGING){
                	videoPower.setImageResource(R.drawable.icv_power_01);
                } else {
                	videoPower.setImageResource(R.drawable.icv_power_00);
                }
            }
        };  
        batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  
    }  
	
	public static int byteArrayToInt(byte[] bytes) {
        int value= 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
	}
	
	public void setData(String title, String address){
		this.title = title;
		this.address = address;
	}
	
	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
		if(isFavorite){
			videoFavorite.setImageResource(R.drawable.icv_favorite_selected);
		} else {
			videoFavorite.setImageResource(R.drawable.icv_favorite);
		}
	}
	
	public boolean isFavorite() {
		return isFavorite;
	}
	
	public void setBackClickListener(OnClickListener backClickListener) {
		this.backClickListener = backClickListener;
		videoBack.setOnClickListener(backClickListener);
	}
	
	public void setFavoriteClickListener(OnClickListener favoriteClickListener) {
		this.favoriteClickListener = favoriteClickListener;
		videoFavorite.setOnClickListener(favoriteClickListener);
	}
	
	public void setDownloadClickListener(OnClickListener downloadClickListener) {
		this.downloadClickListener = downloadClickListener;
		videoDownload.setOnClickListener(downloadClickListener);
	}
	
	private class SurfaceListener implements SurfaceHolder.Callback {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (position > 0)
			{
				try
				{
					// 开始播放
					play();
					// 并直接从指定位置开始播放
					mPlayer.seekTo(position);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder){}
	}
	
	//处理电话打来时的情况
	class MyPhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				//手机铃声响了
				if(state == 4){
					state = 7;
					pause();
				}
				break;
			default:
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
}
