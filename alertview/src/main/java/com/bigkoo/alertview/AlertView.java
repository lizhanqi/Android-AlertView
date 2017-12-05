package com.bigkoo.alertview;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
    private View divier;
    public enum Style{
        ACTIONSHEET,
        ALERT
    }
    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
    );
    public static final int HORIZONTAL_BUTTONS_MAXCOUNT = 2;
    public static final int CANCELPOSITION = -1;//点击取消按钮返回 －1，其他按钮从0开始算

    private String title;
    private String msg;
    private String[] destructive;
    private String[] others;
    private List<String> mDestructive;
    private List<String> mOthers;
    private String cancel;
    private ArrayList<String> mDatas = new ArrayList<String>();

    private WeakReference<Context> contextWeak;
    private ViewGroup contentContainer;
    private ViewGroup decorView;//activity的根View
    private ViewGroup rootView;//AlertView 的 根View
    private ViewGroup loAlertHeader;//窗口headerView
    private Style style = Style.ALERT;
    private OnDismissListener onDismissListener;
    private OnItemClickListener onItemClickListener;
    private boolean isShowing;
    private Animation outAnim;
    private Animation inAnim;
    private int gravity = Gravity.CENTER;
    public AlertView(Builder builder) {
        this.contextWeak = new WeakReference<>(builder.context);
        this.style = builder.style;
        this.title = builder.title;
        this.msg = builder.msg;
        this.cancel = builder.cancel;
        this.destructive = builder.destructive;
        this.others = builder.others;
        this.onItemClickListener = builder.onItemClickListener;

        initData(title, msg, cancel, destructive, others);
        initViews();
        init();
    }
    public    AlertView(String title, String msg, String cancel, String[] destructive, String[] others, Context context, Style style,OnItemClickListener onItemClickListener){
        this.contextWeak = new WeakReference<>(context);
        if(style != null){this.style = style;}
        this.onItemClickListener = onItemClickListener;

        initData(title, msg, cancel, destructive, others);
        initViews();
        init();
    }

    /**
     * 获取数据
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
     * @param paddingBottom
     * @return
     */
    public AlertView setPaddingBottom(int  paddingBottom){
        rootView.setPadding(0,0,0,paddingBottom);
        return this;
    }
    protected void initViews(){
        Context context = contextWeak.get();
        if(context == null){ return;}
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        decorView = (ViewGroup) ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentContainer = (ViewGroup) rootView.findViewById(R.id.content_container);
        int margin_alert_left_right = 0;
        switch (style){
            default:
            case ACTIONSHEET:
                params.gravity = Gravity.BOTTOM;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
                params.setMargins(margin_alert_left_right,0,margin_alert_left_right,margin_alert_left_right);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.BOTTOM;
                initActionSheetViews(layoutInflater);
                break;
            case ALERT:
                params.gravity = Gravity.CENTER;
                margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
                params.setMargins(margin_alert_left_right,0,margin_alert_left_right,0);
                contentContainer.setLayoutParams(params);
                gravity = Gravity.CENTER;
                initAlertViews(layoutInflater);
                break;
        }
    }
    protected void initHeaderView(ViewGroup viewGroup){
        loAlertHeader = (ViewGroup) viewGroup.findViewById(R.id.loAlertHeader);
        //标题和消息
        TextView tvAlertTitle = (TextView) viewGroup.findViewById(R.id.tvAlertTitle);
        TextView tvAlertMsg = (TextView) viewGroup.findViewById(R.id.tvAlertMsg);
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


    protected void initListView(){
        Context context = contextWeak.get();
        if(context == null) {return;}

        ListView alertButtonListView = (ListView) contentContainer.findViewById(R.id.alertButtonListView);
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
    TextView tvActionSheetCancel;
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
    boolean centerBreak;
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
    protected void init() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }

    /**
     * 添加自定义view
     * @param extView
     * @return
     */
    public AlertView addExtView(View extView){
        loAlertHeader.addView(extView);
        return this;
    }
    /**
     * show的时候调用
     *
     * @param view 这个View
     */
    private void onAttached(View view) {
        isShowing = true;
        decorView.addView(view);
        contentContainer.startAnimation(inAnim);
    }
    /**
     * 添加这个View到Activity的根视图
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        onAttached(rootView);
    }
    /**
     * 检测该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    public boolean isShowing() {
        return rootView.getParent() != null && isShowing;
    }

    /**
     * 关闭
     */
    public void dismiss() {
        //消失动画
        outAnim.setAnimationListener(outAnimListener);
        contentContainer.startAnimation(outAnim);
    }

    /**
     * 直接关闭
     */
    public void dismissImmediately() {
        decorView.removeView(rootView);
        isShowing = false;
        if(onDismissListener != null){
            onDismissListener.onDismiss(this);
        }

    }

    public Animation getInAnimation() {
        Context context = contextWeak.get();
        if(context == null) {return null;}

        int res = AlertAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }

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
     */
    public void setMarginBottom(int marginBottom){
        Context context = contextWeak.get();
        if(context == null) {return;}

        int margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
        params.setMargins(margin_alert_left_right,0,margin_alert_left_right,marginBottom);
        contentContainer.setLayoutParams(params);
    }
    public AlertView setCancelable(boolean isCancelable) {
        View view = rootView.findViewById(R.id.outmost_container);

        if (isCancelable) {
            view.setOnTouchListener(onCancelableTouchListener);
        }
        else{
            view.setOnTouchListener(null);
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

    /**
     * Builder for arguments
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

        public Builder setContext(Context context) {
            this.context = context;
            return this;
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
