package com.cisetech.pulltorefreshview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Author:Yqy
 * Date:2016-08-23
 * Desc:下拉刷新View
 * Company:cisetech
 */
public class PullRefreshView extends LinearLayout implements AbsListView.OnScrollListener {
    /**上下文*/
    private Context mContext;
    /**是否支持下拉刷新 默认支持*/
    private boolean mEnablePullRefresh=true;
    /**是否支持加载更多，默认支持*/
    private boolean mEnableLoadMore=true;
    /**上一次滑动的X轴坐标*/
    private int mLastMotionX;
    /**上一次滑动的Y轴坐标*/
    private int mLastMotionY;
    /**header view*/
    private PullHeaderView mHeaderView;
    /**ListView or GridView*/
    private AdapterView<?>mAdapterView;
    /**ScrollerView*/
    private ScrollView mScrollerView;
    /**header view高度*/
    private int mHeaderViewHeight;
    /**滑动的动作*/
    private int mPullState;
    /**上拉动作*/
    private static final int PULL_UP_STATE=0;
    /**下拉动作*/
    private static final int PULL_DOWN_STATE=1;
    /**上一次刷新时候View的数量*/
    private int mCount=0;
    /**正在下拉刷新*/
    private boolean mPullRefreshing=false;
    /**正在加载更多*/
    private boolean mPullLoading=false;
    /**下拉刷新监听*/
    private IOnHeaderRefreshListener onHeaderRefreshListener;


    private PullFootView mFootView;
    private int mFootViewHeight;
    private IOnfootRefreshListener onfootRefreshListener;
    public PullRefreshView(Context context) {
        this(context, null);
    }

    public PullRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(LinearLayout.VERTICAL);
        this.mContext=context;
        /**
         * 添加头部刷新view
         */
        addHeaderView();
    }

    /**
     * 添加头部刷新View
     */
    private void addHeaderView() {
        mHeaderView=new PullHeaderView(mContext);
        mHeaderViewHeight=mHeaderView.getHeaderHeight();
        mHeaderView.setGravity(Gravity.BOTTOM);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mHeaderViewHeight);
        // 设置topMargin的值为负的header View高度,隐藏在最上方
        params.topMargin = -mHeaderViewHeight;
        addView(mHeaderView, params);
    }

    /**
     * 当加载完布局后，获取ListView
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //添加footView,
        addFootView();
        //获取AdapterView
        initContentAdapterView();
    }

    /**
     * 添加FootView
     */
    private void addFootView() {
        mFootView=new PullFootView(mContext);
        mFootViewHeight=mFootView.getFootViewHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mFootViewHeight);
        mFootView.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(mFootView, params);
    }

    /**
     * 获取AdapterView
     */
    private void initContentAdapterView() {
        int count=getChildCount();
        if(count<2){
            throw new IllegalArgumentException("this layout must contain 2 child views,and AdapterView or ScrollView must in the second position!");
        }
        View view=null;
        for (int i = 0; i < count; i++) {
            view=getChildAt(i);
            if(view instanceof AdapterView<?>){
                mAdapterView= (AdapterView<?>) view;
                /**
                 * 只针对ListView做上拉加载操作
                 */
                ListView lv= (ListView) mAdapterView;
                /**
                 * 设置ListView的滚动监听
                 */
                lv.setOnScrollListener(this);
            }else if(view instanceof ScrollView){
                mScrollerView = (ScrollView) view;
            }
        }
        if (mAdapterView == null && mScrollerView == null) {
            throw new IllegalArgumentException("must contain a AdapterView or ScrollView in this layout!");
        }
    }
    /**xx
     * 处理事件拦截
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x= (int) ev.getX();
        int y= (int) ev.getY();
        int action=ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN://记录按下时候的位置
                mLastMotionX=x;
                mLastMotionY=y;
                break;
            case MotionEvent.ACTION_MOVE:
                //duraY>0是向下滑动，<0是向上滑动
                int duraX=x-mLastMotionX;
                int duraY=y-mLastMotionY;
                //解决错误滑动操作
                if(Math.abs(duraX)<Math.abs(duraY)&&Math.abs(duraY)>10){
                    if(isRefreshScroll(duraY)){//判断是否滑动到底部或者顶部
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 判断是否需要拦截事件，也就是是否滑动到底部或者顶部
     * @param duraY
     * @return boolean
     */
    private boolean isRefreshScroll(int duraY) {
        //如果正在加载或者正在刷新的时候，直接不拦截事件
        if(mPullRefreshing||mPullLoading){
            return false;
        }
        if(mAdapterView!=null){
            if(duraY>0){//向下滑动
                if(!mEnablePullRefresh){
                    return false;//如果禁止了下拉刷新那么不拦截
                }
                View child=mAdapterView.getChildAt(0);
                if(child==null){
                    return false;
                }
                int top=child.getTop();
                int padding=mAdapterView.getPaddingTop();
                //判断是否滑动到了顶部
                if(mAdapterView.getFirstVisiblePosition()==0&&Math.abs(top - padding)<=11){
                    mPullState=PULL_DOWN_STATE;
                    return true;
                }
            }else if(duraY<0){//上拉的时候
                View child = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
                if(child!=null&&child.getBottom()<=getMeasuredHeight()&&mAdapterView.getLastVisiblePosition()==mAdapterView.getCount()-1){
                    mPullState=PULL_UP_STATE;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y= (int) event.getY();
        int action=event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int duraY=y-mLastMotionY;
                if(mPullState==PULL_DOWN_STATE){
                    //执行下拉操作
                    headerPrepareToRefresh(duraY);
                }else if(mPullState==PULL_UP_STATE){
                    //执行上拉
                    footPrepareToRefresh(duraY);
                }
                mLastMotionY=y;
                break;
            //抬手的时候，判断是否进行刷新或加载操作
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                int topMargin=((LayoutParams)mHeaderView.getLayoutParams()).topMargin;
                if(mPullState==PULL_DOWN_STATE){
                    if(topMargin>0){
                        //提示正在刷新中，
                        headerRefresh();
                    }else{
                        //从新隐藏headerView
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                }else if(mPullState==PULL_UP_STATE){
                    //上拉加载中
                   if(topMargin<=-(mHeaderViewHeight+mFootViewHeight)){
                        footRefreshing();
                    }else{
                        //从新隐藏footView
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 下拉刷新中
     */
    private void headerRefresh() {
        mPullRefreshing=true;
        mHeaderView.setState(PullHeaderView.STATE_REFRESHING);
        setHeaderTopMargin(0);
        if(onHeaderRefreshListener!=null){
            onHeaderRefreshListener.onHeaderRefresh(this);
        }
    }
    /**
     * 设置header view 的topMargin的值.
     *
     * @param topMargin the new header top margin
     */
    private void setHeaderTopMargin(int topMargin) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
    }
    /**
     * 执行下拉操作
     * @param duraY
     */
    private void headerPrepareToRefresh(int duraY) {
        if(mPullRefreshing||mPullLoading){
            return ;
        }
        /**
         * 根据滑动的距离计算出HeaderView的topMargin
         */
        int newTopMargin=updateHeaderViewTopMargin(duraY);
        /**
         * 当HeaderView新的topMargin>=0的时候，也就是我们上面演示的marginTop=-50dp
         * 的时候，也就是HeaderView全部显示出来
         * HeaderView此时显示“松手刷新”
         *
         * 反之如果还有一截在没有显示出来就显示
         * “下拉刷新”状态
         */
        if(newTopMargin>=0&&mHeaderView.getState()!=PullHeaderView.STATE_REFRESHING){
            mHeaderView.setState(PullHeaderView.STATE_READY);
        }else if(newTopMargin<0&&newTopMargin>-mHeaderViewHeight){
            mHeaderView.setState(PullHeaderView.STATE_NORMAL);
        }
    }

    /**
     * 执行上拉加载操作
     * @param duraY
     */
    private void footPrepareToRefresh(int duraY) {
        if(mPullRefreshing||mPullLoading){
            return ;
        }
        int newTopMargin=updateHeaderViewTopMargin(duraY);
        mFootView.setState(PullHeaderView.STATE_READY);
    }


    /**
     * HeaderView新的topMargin=当前margin+滑动的距离
     * @param duraY
     * @return
     */
    private int updateHeaderViewTopMargin(int duraY) {
        LinearLayout.LayoutParams params= (LayoutParams) mHeaderView.getLayoutParams();
        /**
         * 给一个阻尼的效果，所以*了一个0.3f
         */
        float newTopMargin=params.topMargin+duraY*0.3f;
        /**
         * 防止正在下拉的时候然后手指没有释放又进行上拉操作，
         */
        if(duraY>0&&mPullState==PULL_UP_STATE&&Math.abs(params.topMargin)<=mHeaderViewHeight){
            return params.topMargin;
        }
        /**
         * 防止正在上拉的时候然后手指没有释放又进行下拉操作，
         */
        if(duraY<0&&mPullState==PULL_DOWN_STATE&&Math.abs(params.topMargin)>=mHeaderViewHeight){
            return params.topMargin;
        }
        params.topMargin= (int) newTopMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
        return params.topMargin;
    }

    /**
     * 刷新完成,隐藏刷新view
     */
    public void refreshComplete(){
        mPullLoading=false;//是否上拉加载中置为false
        mPullRefreshing=false;//是否下拉加载置为false
        /**
         * HeaderView的状态置为初始状态
         */
        mHeaderView.setState(PullHeaderView.STATE_NORMAL);
        /**
         * 隐藏HeaderView跟FooterView，
         * 不明白的可以看我一开始演示的那几个过程
         */
        setHeaderTopMargin(-mHeaderViewHeight);
        mFootView.setState(PullHeaderView.STATE_NORMAL);
    }

    /**
     * ListView滑动的状态，当为SCROLL_STATE_FLING的时候滑动到底部就自动加载更多
     */
    private int mScrollState;
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState=scrollState;
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mAdapterView!=null&&mAdapterView.getCount()>0){
            View child = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
            if(child!=null&&child.getBottom()<=getMeasuredHeight()&&mAdapterView.getLastVisiblePosition()==mAdapterView.getCount()-1){
                if(mScrollState== AbsListView.OnScrollListener.SCROLL_STATE_FLING){
                    footRefreshing();
                }
            }
        }
    }

    /**
     * 底部加载中..
     */
    private void footRefreshing() {
        if(mPullLoading||mPullRefreshing){
            return;
        }
        mPullLoading=true;
        mFootView.setState(PullHeaderView.STATE_REFRESHING);
        setHeaderTopMargin(-(mFootViewHeight + mHeaderViewHeight));
        if(onfootRefreshListener!=null){
            onfootRefreshListener.onFootRefresh(this);
        }
    }
    /**
     * 下拉刷新监听
     */
    public interface IOnHeaderRefreshListener{
        void onHeaderRefresh(PullRefreshView view);
    }

    /**
     * 加载更多监听接口
     */
    public interface IOnfootRefreshListener{
        void onFootRefresh(PullRefreshView view);
    }
    /**
     * 设置下拉刷新监听器
     * @param onHeaderRefreshListener
     */
    public void setHeaderRefreshListener(IOnHeaderRefreshListener onHeaderRefreshListener){
        this.onHeaderRefreshListener=onHeaderRefreshListener;
    }

    /**
     * 设置加载更多监听
     * @param onfootRefreshListener
     */
    public void setOnfootRefreshListener(IOnfootRefreshListener onfootRefreshListener) {
        this.onfootRefreshListener = onfootRefreshListener;
    }
}
