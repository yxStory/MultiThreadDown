package app.test.web.multithreaddown;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yx on 2017/3/10.
 */
public class MultiThreadDown extends Activity {

    EditText edit1,edit2;
    Button bn;
    ProgressBar progressBar;
    private int mDownStatus;
    DownUtil downUtil;
    TextView textView;
    String path;
    /**
     * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
     *
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
    /**
     * 获取SD卡根目录路径
     *
     * @return
     */
    public static String getSdCardPath() {
        boolean exist = isSdCardExist();
        String sdpath = "";
        if (exist) {
            sdpath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        } else {
            sdpath = "不适用";
        }
        return sdpath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        edit1=(EditText)findViewById(R.id.url);
        edit2=(EditText)findViewById(R.id.target);
        bn=(Button)findViewById(R.id.down);
        progressBar=(ProgressBar)findViewById(R.id.bar);
        textView=(TextView)findViewById(R.id.text);
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==0x123){
                    progressBar.setProgress(mDownStatus);
                }
            }
        };
        if(isSdCardExist()){
            Toast.makeText(MultiThreadDown.this,"有sd卡",Toast.LENGTH_SHORT).show();
            path=getSdCardPath();
            textView.setText(path);
        }
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //初始化DownUtil对象
                downUtil=new DownUtil(edit1.getText().toString(),
                        edit2.getText().toString(),
                        6);
                new Thread(){
                    @Override
                    public void run(){
                        try{
                            downUtil.download();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        //定义每秒调度获取一次系统的下载进度
                        final Timer timer=new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                double completeRate=downUtil.getCompleteRate();
                                mDownStatus=(int)(completeRate*100);
                                handler.sendEmptyMessage(0x123);
                                //当文件下载完毕，关闭时间调度
                                if(mDownStatus>=100){
                                    System.out.println("文件下载完毕！");
                                    timer.cancel();
                                }
                            }
                        },0,100);
                    }
                }.start();
            }
        });
    }
}