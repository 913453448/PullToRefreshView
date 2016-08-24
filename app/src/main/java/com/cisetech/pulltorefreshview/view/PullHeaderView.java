package com.cisetech.pulltorefreshview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cisetech.pulltorefreshview.R;
import com.cisetech.pulltorefreshview.view.util.Utils;

/**
 * Author:Yqy
 * Date:2016-08-23
 * Desc:下拉刷新HeaderView
 * Company:cisetech
 */
public class PullHeaderView extends LinearLayout {
    private Context mContext;
    /**
     * 主View
     */
    private LinearLayout headerView;
    /**
     * 箭头图标View
     */
    private ImageView arrowImageView;
    /**
     * 进度图标View
     */
    private ProgressBar headerProgressBar;
    /**
     * 箭头图标
     */
    private Bitmap arrowBitmap;
    /**
     * 文本提示的View
     */
    private TextView tipsTextView;
    /**
     * 提示刷新时间的View
     */
    private TextView headerTimeView;
    /**
     * 当前控件的状态
     */
    private int mState = -1;
    /**
     * 箭头向上时候的动画
     */
    private Animation mRotateUpAnim;
    /**
     * 箭头向下时候的动画
     */
    private Animation mRotateDownAnim;
    /**
     * 动画持续的时间
     */
    private final int ROTATE_ANIM_DURATION = 180;
    /**
     * 提示下拉刷新
     */
    public final static int STATE_NORMAL = 0;
    /**
     * 提示松开刷新
     */
    public final static int STATE_READY = 1;
    /**
     * 提示正在刷新
     */
    public final static int STATE_REFRESHING = 2;
    /**
     * 上一次刷新的时间
     */
    private String lastRefreshTime;
    /**
     * Header的高度
     */
    private int headerHeight;

    public PullHeaderView(Context context) {
        this(context, null);
    }

    public PullHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        /**
         * 创建一个整体的刷新栏布局，然后放在this中,
         * 也就是我们画图中的第二层，方向为水平
         * setOrientation(LinearLayout.HORIZONTAL);
         */
        headerView = new LinearLayout(mContext);
        headerView.setOrientation(LinearLayout.HORIZONTAL);
        headerView.setGravity(Gravity.CENTER);
        headerView.setPadding(0, Utils.dp2px(mContext, 10), 0, Utils.dp2px(mContext, 10));//设置padding
        /**
         * 创建一个FramLayout，
         * 因为进度条跟箭头是放在一起的
         */
        FrameLayout headImage = new FrameLayout(mContext);
        arrowBitmap = Utils.getBitmapFromSrc(mContext,R.mipmap.arrow);
        arrowImageView=new ImageView(mContext);
        arrowImageView.setImageBitmap(arrowBitmap);
        /**
         * 创建一个进度条，默认Style为
         * android.R.attr.progressBarStyleSmall
         * 默认是不显示的
         */
        headerProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall);
        headerProgressBar.setVisibility(View.GONE);
        /**
         * 然后把箭头跟进度条放入FramLayout中
         * 大小为40dp
         */
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(-2, -2);
        imgLp.gravity = Gravity.CENTER;
        imgLp.width = Utils.dp2px(mContext, 40);
        imgLp.height = Utils.dp2px(mContext, 40);
        headImage.addView(arrowImageView, imgLp);
        headImage.addView(headerProgressBar, imgLp);
        /**
         * 添加提示文字跟刷新时间
         * 放入headTextLayout（LinearLayout中）
         * 方向为LinearLayout.VERTICAL
         */
        LinearLayout headTextLayout = new LinearLayout(mContext);
        headTextLayout.setOrientation(LinearLayout.VERTICAL);
        headTextLayout.setGravity(Gravity.CENTER);
        tipsTextView = new TextView(mContext);
        tipsTextView.setTextColor(Color.DKGRAY);
        tipsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        headerTimeView = new TextView(mContext);
        headerTimeView.setTextColor(Color.DKGRAY);
        headerTimeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(-2, -2);
        headTextLayout.addView(tipsTextView, textLp);
        headTextLayout.addView(headerTimeView, textLp);

        /**
         * 创建一个叫headerLayout（LinearLayout）
         * 把headImage跟headTextLayout包裹起来
         */
        LinearLayout.LayoutParams headLp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headLp.gravity = Gravity.CENTER;
        headLp.rightMargin = Utils.dp2px(mContext, 10);
        LinearLayout headerLayout = new LinearLayout(mContext);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER);
        headerLayout.addView(headImage, headLp);
        headerLayout.addView(headTextLayout, headLp);
        /**
         * 把创建一个叫headerLayout放入到headerView中
         */
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM;
        headerView.addView(headerLayout, lp);
        /**
         * 最后把headerView主布局添加到PullHeaderView中
         */
        this.addView(headerView, lp);
        //获取控件的高度，获取之前先measure一下，不然拿不到宽高
        Utils.measureView(this);
        headerHeight = this.getMeasuredHeight();
        /**
         * 初始化箭头朝上的动画
         */
        mRotateUpAnim = new RotateAnimation(0.0f, -180f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        /**
         * 初始化箭头朝下的动画
         */
        mRotateDownAnim= new RotateAnimation(-180f,0.0f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

    }

    /**
     * 设置当前HeaderView的状态
     * @param state
     */
    public void setState(int state){
        //当前状态跟设置的状态一致的时候
        if(state==mState){
            return ;
        }
        //为刷新中的时候，箭头隐藏，进度条显示
        if(state==STATE_REFRESHING){
            arrowImageView.clearAnimation();
            arrowImageView.setVisibility(View.GONE);
            headerProgressBar.setVisibility(View.VISIBLE);
        }else{
            arrowImageView.setVisibility(View.VISIBLE);
            headerProgressBar.setVisibility(View.GONE);
        }
        switch (state){
            //当为下拉刷新的时候，箭头朝下
            case STATE_NORMAL:
                if(mState==STATE_READY){//如果前面状态是箭头朝上
                    arrowImageView.startAnimation(mRotateDownAnim);
                }
                if(mState==STATE_REFRESHING){
                    arrowImageView.clearAnimation();
                }
                tipsTextView.setText("下拉刷新");
                if(TextUtils.isEmpty(lastRefreshTime)){
                    lastRefreshTime=Utils.getCurrentDate();
                    headerTimeView.setText("刷新时间: "+lastRefreshTime);
                }else{
                    headerTimeView.setText("上次刷新时间：" +lastRefreshTime);
                }
                break;
            case STATE_READY:
                if (mState != STATE_READY) {
                    arrowImageView.clearAnimation();
                    arrowImageView.startAnimation(mRotateUpAnim);
                    tipsTextView.setText("该放手啦!");
                    headerTimeView.setText("上次刷新时间：" + lastRefreshTime);
                }
                break;
            case STATE_REFRESHING:
                lastRefreshTime=Utils.getCurrentDate();
                tipsTextView.setText("正在刷新...");
                headerTimeView.setText("本次刷新时间：" + lastRefreshTime);
                break;
            default:
        }
        mState=state;
    }

    /**
     * 获取控件高度
     * @return 高度
     */
    public int getHeaderHeight() {
        return headerHeight;
    }

    /**
     * 获取当前headvIEW的状态
     * @return 状态
     */
    public int getState() {
        return mState;
    }
}
