package com.nx.nx6313.mp4radio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.coremedia.iso.IsoFile;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class VideoDownloder {
	
	int length = 0;

	// 网络路径，本地路径
	private String url, localFilePath;
	
	private Handler handler;
	
	// 初始化成功
	private boolean isinitok = false;
	
	// 
	private int downloadvideoindex = 0;

	private final ArrayList<VideoInfo> vilists = new ArrayList<VideoDownloder.VideoInfo>();
	private final ExecutorService executorService = Executors.newFixedThreadPool(5);

	/* 以5s为分割点进行视频分段 */
	private static final int SEP_SECOND = 5;

	public static final int MSG_DOWNLOADUPDATE = 101;
	public static final int MSG_DOWNLOADFINISH = 102;
	
	private int currentDuration;
	
	private boolean downloading = false;
	
	private long startoffset;
	
	private boolean isallfinished;

	public VideoDownloder(Handler handler, String url, String localFilePath) {
		this.handler = handler;
		this.url = url;
		this.localFilePath = localFilePath;
	}

	public void initVideoDownloder(final long startoffset, final long totalsize) {
		Log.i("test", "----------------------initVideoDownloder-----------------------");
		if (isinitok) {
			return;
		}

		this.executorService.submit(new Runnable() {

			@Override
			public void run() {
				IsoFile isoFile = null;
				try {
					isoFile = new IsoFile(localFilePath);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (isoFile == null) {
					return;
				}

				CareyMp4Parser cmp4p = new CareyMp4Parser(isoFile);
				// cmp4p.printinfo();

				length = (int) (cmp4p.lengthInSeconds * 1000);

				vilists.clear();

				VideoInfo vi = null;
				for (int i = 0; i < cmp4p.syncSamples.length; i++) {
					if (vi == null) {
						vi = new VideoInfo();
						vi.timestart = cmp4p.timeOfSyncSamples[i];
						vi.offsetstart = cmp4p.syncSamplesOffset[i];
					}

					if (cmp4p.timeOfSyncSamples[i] < (vilists.size() + 1)
							* SEP_SECOND) {
						continue;
					}

					vi.offsetend = cmp4p.syncSamplesOffset[i];
					vilists.add(vi);
					vi = null;
					i--;
				}

				if (vi != null) {
					vi.offsetend = totalsize;
					vilists.add(vi);
					vi = null;
				}

				isinitok = true;

				VideoDownloder.this.startoffset = startoffset;
				downloadvideo();
			}
		});

		Log.i("test", "----------------------initVideoDownloder : END-----------------------");
	}

	public void cancelDownload() {
		Log.i("test", "----------------------cancelDownload-----------------------");
		this.executorService.shutdown();
	}

	/**
	 * 加载第 time 秒开始的数据流
	 * */
	public synchronized void seekLoadVideo(long time) {
		Log.i("test", "----------------------seekLoadVideo-----------------------");
		int index = -1;

		for (VideoDownloder.VideoInfo tvi : vilists) {
			if (tvi.timestart > time) {
				break;
			}
			index++;
		}

		if (index < 0 || index >= this.vilists.size()) {
			return;
		}

		final VideoInfo vi = this.vilists.get(index);

		if (vi.status == DownloadStatus.NOTSTART) {
			executorService.submit(new Runnable() {

				@Override
				public void run() {
					try {
						vi.status = DownloadStatus.DOWNLOADING;
						downloadbyvideoinfo(vi);
						vi.status = DownloadStatus.FINISH;
					} catch (IOException e) {
						vi.status = DownloadStatus.NOTSTART;
						e.printStackTrace();
					}
				}
			});
		}

		downloadvideoindex = index;
		Log.i("test", "----------------------seekLoadVideo : END-----------------------");
	}

	/**
	 * 检测指定时间的视频是否已经缓存好了
	 * */
	public boolean checkIsBuffered(long time) {
		Log.i("test", "----------------------checkIsBuffered-----------------------");
		int index = -1;

		for (VideoDownloder.VideoInfo tvi : vilists) {
			if (tvi.timestart > time) {
				break;
			}

			index++;
		}

		if (index < 0 || index >= this.vilists.size()) {
			return true;
		}

		final VideoInfo vi = this.vilists.get(index);

		if (vi.status == DownloadStatus.FINISH) {
			return true;
		} else if (vi.status == DownloadStatus.NOTSTART) {
			return false;
		} else if (vi.status == DownloadStatus.DOWNLOADING) {
			return (vi.downloadsize * 100 / (vi.offsetend - vi.offsetstart)) > ((time - vi.timestart) * 100 / SEP_SECOND);
		}

		Log.i("test", "----------------------checkIsBuffered : END-----------------------");
		return true;
	}

	/**
	 * 检测是否全部视频模块都已下载完毕
	 * */
	private boolean isallfinished() {
		if(isallfinished){
			return true;
		}
		for (VideoDownloder.VideoInfo vi : vilists) {
			if (vi.status != DownloadStatus.FINISH) {
				return false;
			}
		}
		handler.sendEmptyMessage(MSG_DOWNLOADFINISH);
		return true;
	}

	private void downloadvideo() {
		if(downloading){
			return;
		}
		downloading = true;
		Log.i("test", "----------------------downloadvideo-----------------------");
		this.downloadvideoindex = 0;

		for (VideoDownloder.VideoInfo tvi : vilists) {
			if (tvi.offsetend > startoffset) {
				break;
			}
			// 标记已下载
			tvi.status = DownloadStatus.FINISH;
			this.downloadvideoindex++;
		}

		// TODO 如果未下载完成，并且缓存数据不足一分钟（实际使用65秒作为基准），则继续下载
		while (!isallfinished() && currentDuration + 65000 > 
			vilists.get(downloadvideoindex %= vilists.size()).timestart * 1000) {
			VideoInfo vi = this.vilists
					.get(this.downloadvideoindex %= this.vilists.size());

			if (vi.status == DownloadStatus.NOTSTART) {
				try {
					vi.status = DownloadStatus.DOWNLOADING;
					downloadbyvideoinfo(vi);
					vi.status = DownloadStatus.FINISH;
				} catch (IOException e) {
					e.printStackTrace();
					vi.status = DownloadStatus.NOTSTART;
				}
			}
			this.downloadvideoindex++;
		}
		downloading = false;
		Log.i("test", "----------------------downloadvideo : END-----------------------");
	}

	/**
	 * 下载一段视频
	 * */
	private void downloadbyvideoinfo(VideoInfo vi) throws IOException {
		Log.i("test", "----------------------downloadbyvideoinfo-----------------------");
		System.out.println("download -> " + vi.toString());

		URL url = new URL(this.url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);
		conn.setRequestProperty("Range", "bytes=" + vi.offsetstart + "-"
				+ vi.offsetend);
		startoffset = vi.offsetstart;

		RandomAccessFile raf = new RandomAccessFile(new File(localFilePath),
				"rws");
		raf.seek(vi.offsetstart);

		InputStream in = conn.getInputStream();

		byte[] buf = new byte[1024 * 10];
		int len;
		vi.downloadsize = 0;
		while ((len = in.read(buf)) != -1) {
			raf.write(buf, 0, len);
			vi.downloadsize += len;
		}
		Message msg = new Message();
		msg.what = MSG_DOWNLOADUPDATE;
		msg.obj = new Long(vi.offsetstart + vi.downloadsize);
		handler.sendMessage(msg);
		
		raf.close();
		in.close();
		Log.i("test", "----------------------downloadbyvideoinfo : END-----------------------");
	}

	enum DownloadStatus {
		NOTSTART, DOWNLOADING, FINISH,
	}

	class VideoInfo {
		double timestart;
		long offsetstart;
		long offsetend;
		long downloadsize;
		DownloadStatus status;

		public VideoInfo() {
			status = DownloadStatus.NOTSTART;
		}

		@Override
		public String toString() {
			String s = "beginTime: <" + timestart + ">, fileoffset("
					+ offsetstart + " -> " + offsetend + "), isfinish: "
					+ status;

			return s;
		}
	}
	
	public static void skip(InputStream is, int num){
		int n = 0;
		while(n < num){
			try {
				n += is.skip(num - n);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

	public void setCurrentDuration(int currentDuration) {
		this.currentDuration = currentDuration;
		if(!downloading && !isallfinished() && currentDuration + 65000 > 
			vilists.get(downloadvideoindex %= vilists.size()).timestart * 1000){
			downloadvideo();
		}
	}
}
