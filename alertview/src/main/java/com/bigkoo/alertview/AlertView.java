package com.bigkoo.alertview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Created by Sai on 15/8/9.
 * 精仿iOSAlertViewController控件
 * 点击取消按钮返回 －1，其他按钮从0开始算
 */
public class AlertView {
    /**
     * 几种弹窗样式
     */
    public enum Style{
        /**
         * 底部弹窗
         */
        ACTIONSHEET,
        /**
         * 中间弹窗
         */
        ALERT,
        /**
         * 顶部弹窗
         */
        ACTIONTOP,
        /**
         * 系统级弹窗(弹窗始终在其他应用前)
         */
        SYSTEMTOP,
    }
    /**
     * 分割线
     */
    private View divier;
    /**
     * 标题
     */
    private TextView tvAlertTitle;
    /**
     * 内容
     */
    private TextView tvAlertMsg;
    /**
     * 系统性弹窗的高度
     */
    private int systemDialogHeight;
    private  int autoDismissTime;
    //最大的view的宽高设置
    FrameLayout.LayoutParams rootParam= new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
    //包裹内容的
    FrameLayout.LayoutParams  contentContainerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM );
    /**
     * 横向按钮最大数量
     */
    public static final int HORIZONTAL_BUTTONS_MAXCOUNT = 2;
    /**
     *   点击取消按钮返回 －1，其他按钮从0开始算
     */
    public static final int CANCELPOSITION = -1;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String msg;
    /**
     * 高亮按钮文字
     */
    private String[] destructive;
    /**
     * 普通按钮文字
     */
    private String[] others;
    /**
     * 高亮按钮文字(数组转换后)
     */
    private List<String> mDestructive;
    /**
     * 普通按钮文字
     */
    private List<String> mOthers;
    /**
     * 取消按钮文本
     */
    private String cancel;
    /**
     * 取消按钮
     */
    TextView tvActionSheetCancel;
    /**
     * 初始化用的
     */
    private ArrayList<String> mDatas = new ArrayList<String>();
    /**
     * 弱引用的上下文
     */
    private WeakReference<Context> contextWeak;
    /**
     * 包裹内容的父容器
     */
    private ViewGroup contentContainer;
    /**
     * activity的根View
      */
    private ViewGroup decorView;
    /**
     * AlertView 的 根View
     */
    private ViewGroup rootView;
    /**
     * 窗口headerView
     */
    private ViewGroup alertHeader;
    /**
     * 默认样式
     */
    private Style style = Style.ALERT;
    /**
     * 弹窗关闭的监听
     */
    private OnDismissListener onDismissListener;
    /**
     * 弹窗条目点击的监听
     */
    private OnItemClickListener onItemClickListener;
    /**
     * 是否在展示
     */
    private boolean isShowing;

    /**
     * 当前打开窗口的位置
     */
    private int gravity = Gravity.CENTER;
    /**
     * 窗口管理
     */
    WindowManager windowManager;
    /**
     * 设置中心弹窗的内容与按钮分割线是否中断
     * （主要是针对俩按钮的设置了中间分割线距离顶部和底部距离的两种方案）
     */
    boolean centerBreak;

    /**
     * 关闭动画
     */
    private Animation outAnim;
    /**
     * 打开的动画
     */
    private Animation inAnim;

    /*------------------------创建---------------------------------------------*/
    /**
     * 通过Build方式创建的
     * @param builder
     */
    public AlertView(Builder builder) {
        this(builder.title,builder.msg,builder.cancel,builder.destructive,builder.others,builder.context,builder.style, builder.onItemClickListener);
    }
    public AlertView(Context context){
       this(null,null,null,null,null,context,null,null);
    }
    /**
     * 构造创建出来
     * @param title
     * @param msg
     * @param cancel
     * @param destructive
     * @param others
     * @param context
     * @param style
     * @param onItemClickListener
     */
    public    AlertView(String title, String msg, String cancel, String[] destructive, String[] others, Context context, Style style,OnItemClickListener onItemClickListener){
        if (context==null){
           throw  new IllegalArgumentException("参数错误，Context不能为空");
        }
        this.contextWeak = new WeakReference<>(context);
        if(style != null){this.style = style;}
        this.onItemClickListener = onItemClickListener;
        initData(title, msg, cancel, destructive, others);
        initViews();
        initAnimation();
    }
        /*---------------创建end------------------------------*/
    /**
     * 最大的根布局高度使用包裹内容的方式
     * @return
     */
    public AlertView setRootViewHeightWrapContent(){
        rootParam.height= ViewGroup.LayoutParams.WRAP_CONTENT;
        rootView.setLayoutParams(rootParam);
        return this;
    }
    Handler hd=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dismiss();
        }
    };
    /**
     * view依赖到系统,(系统级窗体)
     * @param view
     */
    View tempView;
    private void  attach2System  ( View view)    {
        tempView=view;
       windowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams   params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //这里设置的动画必须是系统的,不能是应用内的,这里是要求
        params.windowAnimations= android.R.style.Animation_Translucent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
             params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
         }else {
             params.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
         }
      /* * 如果设置为params.type = WindowManager.LayoutParams.TYPE_PHONE; 那么优先级会降低一些,
        * 即拉下通知栏不可见         */
        params.format = PixelFormat.RGBA_8888;
        // 设置图片格式，效果为背景透明
        // 设置Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
      /* * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
           * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL| LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
            * */
        // 设置悬浮窗的长得宽
        params.height=systemDialogHeight;
//         params.x=0;
         params.y=-1080;
       /* btn_floatView.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY;
            int paramX, paramY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        // 更新悬浮窗位置
                        wm.updateViewLayout(finalBtn_floatView, params);
                        break;
                }                return true;
            }
        });*/
        windowManager.addView(view, params);
        isShowing = true;
        if(autoDismissTime>0){
            hd.sendEmptyMessageDelayed(0,autoDismissTime);
        }
    }

    /**
     * 自动关闭
     * @param time 多久后自动关闭,小于0,就不自动关闭(毫秒)
     * @return
     */
    public AlertView autoDismiss(int time){
        autoDismissTime=time;
        return this;
    }
    /**
     * 直接添加一个view展示到应用最上层,这里会绕过设置属性(这里全部就是你view了)
     * 与addExtView不同,都可以,如果使用addExtView,需要使用show
     * @param view
     */
    public  void  showSystemAlert(View view){
        //权限判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(contextWeak.get())) {
                //启动Activity让用户授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                contextWeak.get().startActivity(intent);
                PackageManager pm = contextWeak.get().getPackageManager();
                String appName = contextWeak.get().getApplicationInfo().loadLabel(pm).toString();
                Toast.makeText(contextWeak.get(),appName+"需要在其他应用上层显示权限",Toast.LENGTH_LONG).show();
                return;
            } else {
                //执行6.0以上绘制代码
                attach2System(view);
            }
        } else {
            //执行6.0以下绘制代码
            attach2System(view);
        }
    }
    /**
     * 移除系统弹窗性的view
     */
    private void removeSystemAlert(){
        windowManager.removeView(tempView);
    }

    /**
     * 设置系统高度
     * @param height
     * @return
     */
     public AlertView setSystemDialogHeight(int height){
        systemDialogHeight=height;
        return this;
     }
    /**
     * 获取数据初始化
     */
    protected void initData(String title, String msg, String cancel, String[] destructive, String[] others) {
        this.title = title;
        this.msg = msg;
        if (destructive != null){
            this.mDestructive = Arrays.asList(destructive);
            this.mDatas.addAll(mDestructive);
        }
        if (others != null){
            this.mOthers = Arrays.asList(others);
            this.mDatas.addAll(mOthers);
        }
        if (cancel != null){
            this.cancel = cancel;
            if(style == Style.ALERT && mDatas.size() < HORIZONTAL_BUTTONS_MAXCOUNT){
                this.mDatas.add(0,cancel);
            }
        }

    }
    /**
     * 设置距离底部距离（这个是为沉浸式准备的如果当前页面是虚拟导航栏的，直接吧虚拟导航栏的高度给过来就行了）
     *@deprecated use {@link # setContentContainerMarginBottom(int)} {@link #  steadsetContentContainerMargins(int ,int,int,int);
     * @param paddingBottom
     * @return
     */
    @Deprecated
    public AlertView setPaddingBottom(int  paddingBottom){
        if (rootView==null){
            return  null;
        }
        rootView.setPadding(0,0,0,paddingBottom);
        return this;
    }
    /**
     * 设置内容距离缩进(置包裹内容容器view 的Padding属性)
     * @return
     */
    public AlertView setContentContainerPadding(int  l,int t,int r,int b){
        if (contentContainer==null){
            return null;
        }
        contentContainer.setPadding(l,t,r,b);
        return this;
    }
    /**
     *设置包裹内容容器view 的margin属性
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public AlertView setContentContainerMargins(int l,int t,int r,int b){
        if(contentContainerParams == null||contentContainer==null) {return null;}
        contentContainerParams.setMargins(l,t,r,b);
        contentContainer.setLayoutParams(contentContainerParams);
        return this;
    }

    /**
     * 设置距离底部距离（沉浸式）
     * @param margin
     * @return
     */
    public AlertView setContentContainerMarginBottom(int margin){
        if(contentContainerParams == null||contentContainer==null) {return null;}
        contentContainerParams.setMargins(contentContainerParams.leftMargin,contentContainerParams.topMargin,contentContainerParams.rightMargin,contentContainerParams.bottomMargin+margin);
        contentContainer.setLayoutParams(contentContainerParams);
        return this;
    }

    /**
     * 设置最外层的view 的背景色
     * @param backgroundResid
     * @return
     */
    public AlertView setRootBackgroundResource(int backgroundResid){
        rootView.setBackgroundResource(backgroundResid);
        return this;
    }

    /**
     * 设置包裹内容的View的背景
     * @param backgroundResid
     * @return
     */
    public AlertView setContainerBackgroundResource(int backgroundResid){
        contentContainer.setBackgroundResource(backgroundResid);
        return this;
    }

    /**
     * 设置最外层的view距离底部的距离(这里用于沉浸式的)
     * @param bootom
     * @return
     */
    public AlertView setRootViewMarginBootom(int bootom){
            if (rootView!=null){
                rootParam.bottomMargin=bootom;
                rootView.setLayoutParams(rootParam);
            }
        return this;
    }
 /**
     * 初始化弹窗View
     */
    protected void initViews(){
         Context context = contextWeak.get();
        if(context == null){ return;}
        systemDialogHeight = context.getResources().getDimensionPixelSize(R.dimen.system_dialog_height);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        decorView = (ViewGroup) ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        if (style!=Style.SYSTEMTOP){
            rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview, decorView, false);
        }else {
            rootParam=new FrameLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
            rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview, null, false);
        }
         rootView.setLayoutParams(rootParam);
         contentContainer =  rootView.findViewById(R.id.content_container);
        int marginAlertLeftRight = 0;
        switch (style){
            default:
            case ACTIONSHEET:
                gravity =  contentContainerParams.gravity = Gravity.BOTTOM;
                marginAlertLeftRight = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                contentContainerParams.setMargins(marginAlertLeftRight,0,marginAlertLeftRight,0);
                contentContainer.setLayoutParams(contentContainerParams);
                initActionSheetViews(layoutInflater);
                break;
            case SYSTEMTOP://弹出系统级别的顶部
                gravity =   contentContainerParams.gravity = Gravity.TOP;
                marginAlertLeftRight = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                contentContainerParams.setMargins(marginAlertLeftRight,0,marginAlertLeftRight,0);
                contentContainer.setLayoutParams(contentContainerParams);
                initActionSheetViews(layoutInflater);
                break;
            case ACTIONTOP:
                gravity= contentContainerParams.gravity = Gravity.TOP;
                rootView.setLayoutParams(rootParam);
                marginAlertLeftRight = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                contentContainerParams.setMargins(marginAlertLeftRight,0,marginAlertLeftRight,0);
                contentContainer.setLayoutParams(contentContainerParams);
                initActionSheetViews(layoutInflater);
                break;
            case ALERT:
                gravity = contentContainerParams.gravity = Gravity.CENTER;
                marginAlertLeftRight = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                contentContainerParams.setMargins(marginAlertLeftRight,0,marginAlertLeftRight,0);
                contentContainer.setLayoutParams(contentContainerParams);
                initAlertViews(layoutInflater);
                break;
        }
    }
    /**
     * 设置标题大小
     * @param mSize
     * @return
     */
    public AlertView setTitleSize(int mSize){
        if (tvAlertTitle!=null){
            tvAlertTitle.setTextSize(mSize);
        }
        return this;
    }

    /**
     * 设置标题颜色
     * @param mColor
     * @return
     */
    public AlertView setTitleColor(int mColor){
        if (tvAlertTitle!=null){
            tvAlertTitle.setTextColor(mColor);
        }
        return this;
    }

    /**
     * 设置消息字体大小
     * @param mSize
     * @return
     */
    public AlertView setMsgSize(int mSize){
        if (tvAlertMsg!=null){
            tvAlertMsg.setTextSize(mSize);
        }
        return this;
    }

    /**
     * 设置消息颜色
     * @param mColor
     * @return
     */
    public AlertView setMsgColor(int mColor){
        if (tvAlertMsg!=null){
            tvAlertMsg.setTextColor(mColor);
        }
        return this;
    }

    /**
     * 初始化(顶部信息)标题
     * @param viewGroup
     */
    protected void initHeaderView(ViewGroup viewGroup){
        alertHeader = (ViewGroup) viewGroup.findViewById(R.id.loAlertHeader);
        //标题和消息
        tvAlertTitle = (TextView) viewGroup.findViewById(R.id.tvAlertTitle);
        tvAlertMsg = (TextView) viewGroup.findViewById(R.id.tvAlertMsg);
        if(title != null) {
            tvAlertTitle.setText(title);
        }else{
            tvAlertTitle.setVisibility(View.GONE);
        }
        if(msg != null) {
            tvAlertMsg.setText(msg);
        }else{
            tvAlertMsg.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化内容
     */
    protected void initListView(){
        Context context = contextWeak.get();
        if(context == null) {return;}

        ListView alertButtonListView = (ListView) contentContainer.findViewById(R.id.alertButtonListView);
        if (mDatas==null||mDatas.size()<0){
            alertButtonListView.setVisibility(View.GONE);
        }
        //把cancel作为footerView
        if(cancel != null && style == Style.ALERT){
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
            tvAlertCancel = (TextView) itemView.findViewById(R.id.tvAlert);
            tvAlertCancel.setText(cancel);
            tvAlertCancel.setClickable(true);
            setLeftOrCancelColor( context.getResources().getColor(R.color.textColor_alert_button_cancel));
            tvAlertCancel.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
            tvAlertCancel.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
            alertButtonListView.addFooterView(itemView);
            }
        AlertViewAdapter adapter = new AlertViewAdapter(mDatas,mDestructive);
        alertButtonListView.setAdapter(adapter);
        alertButtonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(onItemClickListener != null){onItemClickListener.onItemClick(AlertView.this,position);}
                dismiss();
            }
        });

    }

    /**
     * 初始化
     * @param layoutInflater
     */
    protected void initActionSheetViews(LayoutInflater layoutInflater) {
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_actionsheet,contentContainer);
        initHeaderView(viewGroup);
        initListView();
         tvActionSheetCancel = (TextView) contentContainer.findViewById(R.id.tvAlertCancel);
        if(cancel != null){
            tvActionSheetCancel.setVisibility(View.VISIBLE);
            tvActionSheetCancel.setText(cancel);
        }
        tvActionSheetCancel.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
    }
    /**
     * 设置alter下面两个按钮点击中间线距离上下位置
     * @param mDivierMargin
     * @return
     */
    public AlertView setDivierMargin(int   mDivierMargin){
        if (divier!=null){
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) divier.getLayoutParams();
            p.setMargins(0, mDivierMargin, 0, mDivierMargin);
            divier.requestLayout();
        }
        return  this;
    }

    /**
     * 设置中心弹窗的内容与按钮分割线是否中断
     * （主要是针对俩按钮的设置了中间分割线距离顶部和底部距离的两种方案）
     * @param centerBreak
     * @return
     */
    public AlertView alterDivide(boolean centerBreak){
        this .centerBreak=centerBreak;
        return this;
    }
    TextView tvAlertConfirm;
    /**
     *中心弹窗左边按钮的颜色
     */
    public AlertView setAlertRightColor( int mColor){
        if (tvAlertConfirm!=null){
            tvAlertConfirm.setTextColor(mColor);
        }
        return this;
    }
    /**
     *中心弹窗左边按钮的文字大小
     */
    public AlertView setAlertRightSize(int mSize){
        if (tvAlertConfirm!=null){
            tvAlertConfirm.setTextSize(mSize);
        }
        return this;
    }
    TextView tvAlertCancel;
    /**
     *中心弹窗取消按钮的颜色
     */
    public AlertView setLeftOrCancelColor(int mColor){
                if(  style ==Style.ACTIONSHEET){
                    if (tvActionSheetCancel!=null){
                        tvActionSheetCancel.setTextColor(mColor);
                    }
                }else {
                    if (tvAlertCancel!=null){
                        tvAlertCancel.setTextColor(mColor);
                    }
                }

        return this;
    }
    /**
     *取消按钮的文字大小
     */
     public AlertView setLeftOrCancelSize(int mSize){
        if(  style ==Style.ACTIONSHEET){
            if (tvActionSheetCancel!=null){
                tvActionSheetCancel.setTextSize(mSize);
            }
        }else {
            if (tvAlertCancel!=null){
                tvAlertCancel.setTextSize(mSize);
            }
        }
        return this;
    }
    /**
     * 初始化中心弹窗
     * @param layoutInflater
     */
    protected void initAlertViews(LayoutInflater layoutInflater) {
        Context context = contextWeak.get();
        if(context == null) {return;}
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_alert, contentContainer);
        if (!centerBreak){
            viewGroup.findViewById(R.id.devideline).setVisibility(View.VISIBLE);
        }
        initHeaderView(viewGroup);
        int position = 0;
        //如果总数据小于等于HORIZONTAL_BUTTONS_MAXCOUNT，则是横向button
        if(mDatas.size()<=HORIZONTAL_BUTTONS_MAXCOUNT){
                ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubHorizontal);
                viewStub.inflate();
            LinearLayout loAlertButtons = (LinearLayout) contentContainer.findViewById(R.id.loAlertButtons);
                for (int i = 0; i < mDatas.size(); i ++) {
                //如果不是第一个按钮
                if (i != 0){
                    //添加上按钮之间的分割线
                    divier = new View(context);
                    divier.setBackgroundColor(context.getResources().getColor(R.color.bgColor_divier));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)context.getResources().getDimension(R.dimen.size_divier), LinearLayout.LayoutParams.MATCH_PARENT);
                    loAlertButtons.addView(divier,params);
                    setDivierMargin((int)context.getResources().getDimension(R.dimen.alert_divier_margin));
                }
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
                if (!centerBreak){
                    itemView.findViewById(R.id.topline).setVisibility(View.GONE);
                }
                TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
                tvAlert.setClickable(true);
                //设置点击效果
                if(mDatas.size() == 1){
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
                }
                else if(i == 0){//设置最左边的按钮效果
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_left);
                }
                else if(i == mDatas.size() - 1){//设置最右边的按钮效果
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_right);
                }
                String data = mDatas.get(i);
                tvAlert.setText(data);
                //取消按钮的样式默认第0个也就是左边是取消
                if (i == 0){
                    //tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
                    tvAlertCancel=tvAlert;
                    setLeftOrCancelColor(context.getResources().getColor(R.color.textColor_alert_button_cancel));
                    tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
                    position = position - 1;
                }
                //取消按钮的样式
                else if (mDestructive!= null && mDestructive.contains(data)){
                    tvAlertConfirm=tvAlert;
                    setAlertRightColor(context.getResources().getColor(R.color.textColor_alert_button_right));
                }
                tvAlert.setOnClickListener(new OnTextClickListener(position));
                position++;
                loAlertButtons.addView(itemView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            }
        }
        else{
            ViewStub viewStub = (ViewStub) contentContainer.findViewById(R.id.viewStubVertical);
            viewStub.inflate();
            initListView();
        }
    }
    /**
     * 初始化动画
     */
    protected void initAnimation() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }
    /**
     * 添加自定义view
     * @param extView
     * @return
     */
    public AlertView addExtView(View extView){
        alertHeader.addView(extView);
        return this;
    }
    /**
     * show的时候调用
     * @param view View
     */
    private void onAttached(View view) {
        decorView.addView(view);
        rootView.startAnimation(inAnim);
        isShowing = true;
        if(autoDismissTime>0){
            hd.sendEmptyMessageDelayed(0,autoDismissTime);
        }
    }
    /**
      * 添加这个View到Activity的根视图
      */
     public void show() {

         if (isShowing()) {
             return;
         }

          if (Style.SYSTEMTOP==style){
              showSystemAlert(rootView);
         }else {
              onAttached(rootView);
         }
    }
//    public void showViewTop(View view) {
//        int[] location = new int[2];    view.getLocationOnScreen(location);
//        WindowManager windowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = windowManager.getDefaultDisplay();
//        Point point = new Point();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//                display.getSize(point);
//                if (rootParam!=null){
//                    rootParam.height= location[1]-rootParam.bottomMargin;
//                    rootParam.bottomMargin=point.y - location[1]+rootParam.bottomMargin;
//                }
//                if (rootView!=null){
//                    rootView.setLayoutParams(rootParam);
//                }
//        }else {
//            if (rootParam!=null){
//                rootParam.height= location[1]-rootParam.bottomMargin;
//                rootParam.bottomMargin= display.getHeight() - location[1]+rootParam.bottomMargin;
//            }
//            if (rootView!=null){
//                rootView.setLayoutParams(rootParam);
//            }
//        }
//
//        if (isShowing()) {
//            return;
//        }
//        onAttached(rootView);
//    }

    /**
     * 关闭的动画
     */
    public void dismiss() {
        //消失动画
        outAnim.setAnimationListener(outAnimListener);
        rootView.startAnimation(outAnim);
        if (Style.SYSTEMTOP==style){
            dismissImmediately();
        }
    }
    /**
     * 直接关闭(没动画)
     */
    public void dismissImmediately() {
        if (!isShowing()){
            return;
        }
        if (Style.SYSTEMTOP==style){
            removeSystemAlert();
        }else {
            decorView.removeView(rootView);
        }
        isShowing = false;
        if(onDismissListener != null){
            onDismissListener.onDismiss(this);
        }
    }
    /**
     * 获取进入的动画
      * @return
     */
    public Animation getInAnimation() {
        Context context = contextWeak.get();
        if(context == null) {return null;}

        int res = AlertAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }
    /**
     *     获取关闭动画
     * @return
     */
    public Animation getOutAnimation() {
        Context context = contextWeak.get();
        if(context == null){ return null;}
        int res = AlertAnimateUtil.getAnimationResource(this.gravity, false);
        return AnimationUtils.loadAnimation(context, res);
    }
    /**
     * 设置关闭监听
     * @param onDismissListener
     * @return
     */
    public AlertView setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }
    /**
     * 点击监听
     */
    class OnTextClickListener implements View.OnClickListener{
        private int position;
        public OnTextClickListener(int position){
            this.position = position;
        }
        @Override
        public void onClick(View view) {
            if(onItemClickListener != null){onItemClickListener.onItemClick(AlertView.this,position);}
            dismiss();
        }
    }
    /**
     * 动画效果的监听()主要还是动画完成后移除窗体
     */
    private Animation.AnimationListener outAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            dismissImmediately();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    /**
     * 主要用于拓展View的时候有输入框，键盘弹出则设置MarginBottom往上顶，避免输入法挡住界面
     *  例如：
     *    etName = (EditText) extView.findViewById(R.id.etName);
      *   etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
     *    @Override
     *   public void onFocusChange(View view, boolean focus) {
     *  //输入框出来则往上移动
     *   boolean isOpen=imm.isActive();
     *   mAlertViewExt.setMarginBottom(isOpen&&focus ? 240 :0);
     *  System.out.println(isOpen);
     *   }
     *  });
     *  mAlertViewExt.addExtView(extView);
     *    private void closeKeyboard() {
     * //关闭软键盘
     * imm.hideSoftInputFromWindow(etName.getWindowToken(),0);
     * //恢复位置
     * mAlertViewExt.setMarginBottom(0);
     * }
     */
    public void setMarginBottom(int marginBottom){
       setContentContainerMarginBottom(marginBottom);
    }
    /**
     * 窗体是否能取消
     * @param isCancelable
     * @return
     */
    public AlertView setCancelable(boolean isCancelable) {
      //  View view = rootView.findViewById(R.id.outmost_container);
        if (isCancelable) {
            rootView.setOnTouchListener(onCancelableTouchListener);
        }
        else{
            rootView.setOnTouchListener(null);
        }
        return this;
    }
    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    private final View.OnTouchListener onCancelableTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dismiss();
            }
            return false;
        }
    };

    /*----------get-------------*/
    /**
     * 检测该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    public boolean isShowing() {
        return  isShowing;
    }
    public View getDivier() {
        return divier;
    }

    public TextView getTvAlertTitle() {
        return tvAlertTitle;
    }

    public TextView getTvAlertMsg() {
        return tvAlertMsg;
    }

    public int getSystemDialogHeight() {
        return systemDialogHeight;
    }

    public FrameLayout.LayoutParams getRootParam() {
        return rootParam;
    }

    public FrameLayout.LayoutParams getContentContainerParams() {
        return contentContainerParams;
    }

    public TextView getTvActionSheetCancel() {
        return tvActionSheetCancel;
    }

    public ViewGroup getContentContainer() {
        return contentContainer;
    }

    public ViewGroup getDecorView() {
        return decorView;
    }

    public ViewGroup getRootView() {
        return rootView;
    }

    public ViewGroup getAlertHeader() {
        return alertHeader;
    }

    public Style getStyle() {
        return style;
    }

    public int getGravity() {
        return gravity;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public boolean isCenterBreak() {
        return centerBreak;
    }

    public TextView getTvAlertConfirm() {
        return tvAlertConfirm;
    }

    public TextView getTvAlertCancel() {
        return tvAlertCancel;
    }
    public View.OnTouchListener getOnCancelableTouchListener() {
        return onCancelableTouchListener;
    }


    /*------------------Build方式构建-------------------------*/
    /**
     * Builder for arguments
     * 通过Build方式传参数
     */
    public static class Builder {
        private Context context;
        private Style style;
        private String title;
        private String msg;
        private String cancel;
        private String[] destructive;
        private String[] others;
        private OnItemClickListener onItemClickListener;

        public Builder(Context context) {
            if (context==null){
                throw new IllegalArgumentException("参数错误，Context不能为空");
            }
            this.context = context;
        }

        public Builder setStyle(Style style) {
            if(style != null) {
                this.style = style;
            }
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder setCancelText(String cancel) {
            this.cancel = cancel;
            return this;
        }

        public Builder setDestructive(String... destructive) {
            this.destructive = destructive;
            return this;
        }

        public Builder setOthers(String[] others) {
            this.others = others;
            return this;
        }

        public Builder setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        public AlertView build() {
            return new AlertView(this);
        }
    }
}
