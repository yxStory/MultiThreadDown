package app.test.web.multithreaddown;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yx on 2017/3/10.
 */
public class DownUtil {

    //下载的资源文件的路径
    private String path;
    //资源的保存位置
    private String targetFile;
    //定义需要多少线程下载文件
    private int threadNum;
    //定义线程对象
    private DownThread[] threads;
    //定义文件总大小
    private int fileSize;
    public DownUtil(String path,String targetFile,int threadNum){
        this.path=path;
        this.targetFile=targetFile;
        this.threadNum=threadNum;
        //初始化数组
        threads=new DownThread[threadNum];
    }

    /**
     * 下载文件的方法
     * @throws Exception
     */
    public void download() throws Exception{
        URL url=new URL(path);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        //定义一些请求属性
        conn.setConnectTimeout(5*1000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty(
                "Accept",
                "image/gif, image/jpeg, image/pjpeg, image/pjpeg, " +
                "application/x-shockwave-flash, application/xaml+xml, " +
                "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                "application/x-ms-application, application/vnd.ms-excel, " +
                "application/vnd.ms-powerpoint, application/msword, */*");
        conn.setRequestProperty("Accept-Language","zh-CN");
        conn.setRequestProperty("Charset","UTF-8");
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        conn.setRequestProperty("Connection","Keep-Alive");
        //获取文件大小
        fileSize=conn.getContentLength();
        //关闭url连接
        conn.disconnect();
        //定义每一部分要下载的文件大小
        int currentPartSize=fileSize/threadNum+1;
        //此时调用RandomAccessFile，设置下载文件大小
        RandomAccessFile file=new RandomAccessFile(targetFile,"rw");
        file.setLength(fileSize);
        file.close();
        for(int i=0;i<threadNum;i++){
            //设置每个线程开始下载的位置
            int startPos=i*currentPartSize;
            //给每个线程创建一个RandomAccessFile，多线程下载文件
            RandomAccessFile currentFile=new RandomAccessFile(targetFile,"rw");
            //定位下载位置
            currentFile.seek(startPos);
            //调用DownThread子线程执行下载任务，传递（开始位置，每个部分的大小，RandomAccessFile）
            threads[i]=new DownThread(startPos,currentPartSize,currentFile);
            threads[i].start();
        }
    }

    /**
     *
     * @return 下载的百分比
     */
    public double getCompleteRate(){
        int sumSize=0;
        for(int i=0;i<threadNum;i++){
            sumSize+=threads[i].length;
        }
        return sumSize * 1.0 / fileSize;
    }

    public class DownThread extends Thread{

        private int startPos;
        private int currentPartSize;
        private RandomAccessFile currentFile;
        //下载文件大小
        public int length;
        public DownThread(int startPos,int currentPartSize,RandomAccessFile currentFile){
            this.startPos=startPos;
            this.currentPartSize=currentPartSize;
            this.currentFile=currentFile;
        }
        public void run(){
            try {
                URL url=new URL(path);
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                //定义一些请求属性
                conn.setConnectTimeout(5*1000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                conn.setRequestProperty("Accept-Language","zh-CN");
                conn.setRequestProperty("Charset","UTF-8");
                //定义输入流，从HttpURLConnection获取输入流
                InputStream inStream=conn.getInputStream();
                //跳过起始位置，开始读写文件
                inStream.skip(this.startPos);
                //定义一个字节数组
                byte[] buff=new byte[1024];
                int hasRead=0;
                //当写的文件大小小于该部分文件大小，并且从输入流读取的有内容时
                while(length<currentPartSize&&(hasRead=inStream.read(buff))>0){
                    //写入RandomAccessFile文件
                    currentFile.write(buff,0,hasRead);
                    //写的文件长度增加
                    length+=hasRead;
                }
                //关闭RandomAccessFile和输入流
                currentFile.close();
                inStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
