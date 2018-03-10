package com.nx.nx6313.mp4radio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class FileHandler {
	
	public static String Path = "/mp4radio/";
	
	private Context context;
	
	/** SD卡是否存在**/
    public boolean hasSD = false;
    /** SD卡的路径**/ 
    public static String SDPATH;
    /** 缓存路径**/ 
    public static String CACHE;
    /** 当前程序包的路径**/ 
    public static String FILESPATH;
	
	public FileHandler(Context context) { 
        this.context = context; 
        hasSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); 
        if(hasSD && null == SDPATH)
        	SDPATH = Environment.getExternalStorageDirectory().getPath();
        else  if(!hasSD && null == SDPATH)
        	SDPATH = this.context.getFilesDir().getPath();
        if(null == FILESPATH)
        	FILESPATH = this.context.getFilesDir().getPath(); 
        if(null == CACHE)
        	CACHE = this.context.getCacheDir().getPath();
    }
	
	/**
	 * 有后缀名的文件存储
	 * @param path 路径
	 * @param inputStream 输入流
	 * @param filename 文件名称
	 * @return 文件路径
	 */
	public String storeWithExtension(String path, InputStream inputStream, String filename){
		
		String savePath = SDPATH + Path + path;
		String name = null;
		
		if(hasSD){
			// 保存文件，路径不存在则创建路径
			File out = new File(savePath);
			if (!out.exists())
				out.mkdirs();
			name = DateHandler.getcode(3)
					+ filename.substring(filename.lastIndexOf("."));
			
			//取得不包含后缀名的文件名
			out = new File(savePath + "/" + name);
			OutputStream output = null;
			
			try {
				//保存文件
				out.createNewFile();
				output = new FileOutputStream(out);
				byte buffer[] = new byte[1024];
				
				while((inputStream.read(buffer))!=-1){
				    output.write(buffer);
				}
				output.flush();
				name = path + "/" + name;
			} catch (IOException e) {
				name = null;
				e.printStackTrace();
			} finally {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return path + "/" + name;
	}
	
	/**
	 * 拷贝文件
	 * @param inFile
	 * @param filename
	 * @return
	 */
	public String copy(File inFile, String filename){
		
		String savePath = SDPATH + Path + "cache/" + filename;
		
		if(hasSD){
			//取得不包含后缀名的文件名
			File out = new File(savePath);
			OutputStream output = null;
			
			try {
				//保存文件
				out.createNewFile();
				output = new FileOutputStream(out);
				byte buffer[] = new byte[1024];
				InputStream inputStream = new FileInputStream(inFile);
				
				while((inputStream.read(buffer))!=-1){
				    output.write(buffer);
				}
				output.flush();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return savePath;
	}
	
	/**
	 * 无后缀名的文件存储
	 * @param path 路径
	 * @param inputStream 输入流
	 * @return 文件路径
	 */
	public String store(String path, InputStream inputStream){
		
		String savePath = SDPATH + Path + path;
		String name = null;

		if(hasSD){
			// 保存文件，路径不存在则创建路径
			File out = new File(savePath);
			if (!out.exists())
				out.mkdirs();
			name = DateHandler.getcode(3) + ".jpg";
			
			//获得文件名
			out = new File(savePath + "/" + name);
			OutputStream output = null;
			
			try {
				//保存文件
				out.createNewFile();
				output = new FileOutputStream(out);
				byte buffer[] = new byte[1024];
				
				while((inputStream.read(buffer))!=-1){
				    output.write(buffer);
				}
				output.flush();
				name = path + "/" + name;
			} catch (IOException e) {
				name = null;
				e.printStackTrace();
			} finally {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return name;
	}
	
	public String getStoreName(String path, String ext){
		String savePath = SDPATH + Path + path;
		String name = null;

		if(!hasSD){
			savePath = FILESPATH + "/" + path;
		}
		
		// 保存文件，路径不存在则创建路径
		File out = new File(savePath);
		if (!out.exists())
			out.mkdirs();
		name = DateHandler.getcode(3);
		
		//获得文件名
		savePath = savePath + "/" + name;
		if(!StringHandler.isEmpty(ext)){
			savePath += ext;
		}
		
		return savePath;
	}
	
	/**
	 * 获得文件流
	 * @param path
	 * @return
	 */
	public InputStream getFileStream(String path){
		InputStream input = null;
		if(hasSD){
			try {
				input = new FileInputStream(path);
			} catch (FileNotFoundException e) {
				input = null;
				e.printStackTrace();
			}
		}
		return input;
	}
	
	/**
	 * 删除文件
	 * @param path
	 * @return
	 */
	public void deleteFile(String path){
		File file = new File(path);
		if(file.exists())
			file.delete();
	}

	public static void setPath(String path) {
		Path = "/" +  path + "/";
	}

	public static long getAvailaleSize() {
		
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			return 0;
		}

		File path = Environment.getExternalStorageDirectory();// 取得sdcard文件路径

		StatFs stat = new StatFs(path.getPath());

		long blockSize = stat.getBlockSize();

		long availableBlocks = stat.getAvailableBlocks();

		return (availableBlocks * blockSize) / 1024 / 1024;

		// (availableBlocks * blockSize)/1024 KIB 单位

		// (availableBlocks * blockSize)/1024 /1024 MIB单位

	}
}
