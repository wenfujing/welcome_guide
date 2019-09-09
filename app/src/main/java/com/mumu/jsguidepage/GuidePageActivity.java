package com.mumu.jsguidepage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnPageChangeListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * author : wfj
 * date   : 2019/9.9
 */
public class GuidePageActivity extends AppCompatActivity {

    Unbinder unbinder;
    @BindView(R.id.cb_test)
    ConvenientBanner cbTest;
    @BindView(R.id.btn_test)
    Button btnTest;

    private ArrayList<Integer> arrayList;
    /**倒计时文本*/
    private TextView mCountdownTextView;
    private static final int NUM = 9;
    private int countdownNum;//倒计时的秒数
    private MyHandler countdownHandle;//用于控制倒计时子线程
    private Runnable runnable;//倒计时子线程
    private static Timer timer;//计时器
    private static final int MSG_COUNT_WHAT = 99;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*==========设置全屏======必须在setContentView前面=======*/
        /*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guide_page);

        unbinder = ButterKnife.bind(this);
        //初始化控件
        initView();
        //初始化底部圆点
        initGuidePage();
        //初始化Handler和Runnable
        initThread();
    }

    private void initView() {
        mCountdownTextView = (TextView) findViewById(R.id.id_countdownTextView);
        //引导欢迎界面
        arrayList = new ArrayList<>();
        arrayList.add(R.mipmap.b1);
        arrayList.add(R.mipmap.b2);
        arrayList.add(R.mipmap.b3);
    }
    //底部圆点
    private void initGuidePage() {
        cbTest.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new LocalImageHolderView(itemView);
            }

            @Override
            public int getLayoutId() {
                //设置加载哪个布局
                return R.layout.item_guide_page;
            }
        }, arrayList)
                .setPageIndicator(new int[]{R.mipmap.ic_page_indicator, R.mipmap.ic_page_indicator_focused})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .setPointViewVisible(true)
                .setCanLoop(false)
                .setOnPageChangeListener(new OnPageChangeListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                    }

                    @Override
                    public void onPageSelected(int index) {
                        //总共添加了三张图片，如果index为2表示到了最后一张图片，隐藏下面的指示器，显示跳转到主页的按钮
                        if (index == 2) {
                            btnTest.setVisibility(View.VISIBLE);
                            cbTest.setPointViewVisible(false);
                        } else {
                            btnTest.setVisibility(View.GONE);
                            cbTest.setPointViewVisible(true);

                        }
                    }
                });
    }
    /**
     * 初始化Handler和Runnable
     * */
    private void initThread(){
        //倒计时变量
        initCountdownNum();
        //handler对象
        countdownHandle = new MyHandler(this);
        //runnable
        runnable = new Runnable() {
            @Override
            public void run() {
                //执行倒计时代码
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    public void run() {
                        countdownNum --;

                        Message msg = countdownHandle.obtainMessage();
                        msg.what = MSG_COUNT_WHAT;//message的what值
                        msg.arg1 = countdownNum;//倒计时的秒数

                        countdownHandle.sendMessage(msg);
                    }
                };
                timer.schedule(task,0,1000);
            }
        };
    }

    /**必须使用静态类：
     * 解决问题：This Handler class should be static or leaks might occur Android
     * http://www.cnblogs.com/jevan/p/3168828.html*/
    private class MyHandler extends Handler {
        // WeakReference to the outer class's instance.
        private WeakReference<GuidePageActivity> mOuter;

        public MyHandler(GuidePageActivity activity) {
            mOuter = new WeakReference<GuidePageActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            GuidePageActivity theActivity = mOuter.get();
            if (theActivity != null) {

                switch (msg.what) {
                    case MSG_COUNT_WHAT:
                        if(msg.arg1 == 0){//表示倒计时完成

                            //在这里执行的话，不会出现-1S的情况
                            if(timer != null){
                                timer.cancel();//销毁计时器
                            }
                            openNextActivity(GuidePageActivity.this);//打开下一个界面
//                            openNextActivity(theActivity);


                        }else{

                            theActivity.mCountdownTextView.setText("跳过" + msg.arg1 + "s");
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    @OnClick({R.id.btn_test,R.id.id_countdownTextView})
    public void onViewClicked(View view) {
        stopThread();
        openNextActivity(GuidePageActivity.this);//打开下一个界面
    }

    //打开下一个界面
    private static void openNextActivity(Activity mActivity) {
        //跳转到登录界面并销毁当前界面
        Intent intent = new Intent(mActivity, MainActivity.class);
        mActivity.startActivity(intent);

        mActivity.finish();
    }

    /**
     * 轮播图2 对应的holder
     */
    public class LocalImageHolderView extends Holder<Integer> {
        private ImageView mImageView;

        //构造器
        public LocalImageHolderView(View itemView) {
            super(itemView);
        }

        @Override
        protected void initView(View itemView) {
            mImageView = itemView.findViewById(R.id.iv_guide_page);
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        @Override
        public void updateUI(Integer data) {
            mImageView.setImageResource(data);
        }
    }
    @Override
    protected void onResume() {
        //开启线程
        countdownHandle.post(runnable);
        super.onResume();
    }

    @Override
    protected void onStop() {

        initCountdownNum();//初始化倒计时的秒数，这样按home键后再次进去欢迎界面，则会重新倒计时

        stopThread();

        super.onStop();
    }

    //停止倒计时
    private void stopThread(){
        //在这里执行的话，用户点击home键后，不会继续倒计时进入登录界面
        if(timer != null){
            timer.cancel();//销毁计时器
        }

        //将线程销毁掉
        countdownHandle.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /*初始化倒计时的秒数*/
    private void initCountdownNum(){
        countdownNum = NUM;
    }
}
