package com.bigkoo.alertviewdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnDismissListener;
import com.bigkoo.alertview.OnItemClickListener;

/**
 * 精仿iOSAlertViewController控件Demo
 */
public class MainActivity extends Activity implements OnItemClickListener, OnDismissListener {

    private AlertView mAlertView;//避免创建重复View，先创建View，然后需要的时候show出来，推荐这个做法
    private AlertView mAlertViewExt;//窗口拓展例子
    private EditText etName;//拓展View内容
    private InputMethodManager imm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mAlertView = new AlertView("标题", "内容", "取消", new String[]{"确定"}, null, this, AlertView.Style.ACTIONSHEET, this).setCancelable(true).setOnDismissListener(this);
        mAlertView.setLeftOrCancelColor(getResources().getColor(android.R.color.holo_red_dark));
        mAlertView.setAlertRightColor(30);
        mAlertView.setLeftOrCancelSize(25);
        mAlertView.setAlertRightColor(getResources().getColor(android.R.color.tab_indicator_text));
        //拓展窗口
        mAlertViewExt = new AlertView("提示", "请完善你的个人资料！", "取消", null, new String[]{"完成"}, this, AlertView.Style.ALERT, this);
        mAlertViewExt.setTitleSize(30);
        mAlertViewExt.setTitleColor(getResources().getColor(android.R.color.holo_blue_light));
        mAlertViewExt.setMsgSize(20);
        mAlertViewExt.setMsgColor(getResources().getColor(android.R.color.holo_red_dark));

        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.alertext_form,null);
        etName = (EditText) extView.findViewById(R.id.etName);
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen=imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen&&focus ? 240:0);
                System.out.println(isOpen);
            }
        });
        mAlertViewExt.addExtView(extView);
    }

    AlertView ac;
    public   void systemAalter(View view) {
        ac= new AlertView.Builder(this).setStyle(AlertView.Style.SYSTEMTOP).build();
        ac.setCancelable(true);
        ac .setSystemDialogHeight(550);
        ac.setRootViewHeightWrapContent();
        ac.setContentContainerMargins(50,20,30,10);
        ac.setContentContainerPadding(10,10,10,10);
        ac.setSystemDialogHeight(200);
//       ac.setContentContainerPadding(50,20,30,0);
//       ac.setRootViewMarginBootom(250);
        //ac.setContainerBackgroundResource(R.color.textColor_alert_button_destructive);
        ac.setContainerBackgroundResource(0);
        ac.setGotoSetting(new AlertView.GotoSetting() {
            @Override
            public void alreadToSetting() {

            }
        });
        final View inflate = getLayoutInflater().inflate(R.layout.pop_package_product_addbutton, null);
        inflate.findViewById(R.id.aac).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ac.dismiss();
            }
        });
//         ac.autoDismiss(5000);
        //ac.addExtView(inflate);
        ac.showSystemAlert(inflate);
        // ac.show();
    }
    public void alertShow1(View view) {
        Dialog dialog = new Dialog(this);
        dialog     .setTitle("fasdddd");
        dialog.show();
  }



    public void alertShow2(View view) {
        new AlertView("标题", "内容", null, new String[]{"确定","111"}, null, this, AlertView.Style.ALERT, this).autoDismiss(2000).show();
    }

    public void alertShow3(View view) {
//        new AlertView(null, null, null, new String[]{"高亮按钮1", "高亮按钮2", "高亮按钮3"},
//                new String[]{"其他按钮1", "其他按钮2", "其他按钮3", "其他按钮4", "其他按钮5", "其他按钮6",
//                        "其他按钮7", "其他按钮8", "其他按钮9", "其他按钮10", "其他按钮11", "其他按钮12"},
//                this, AlertView.Style.ALERT, this).show();
      //  new AlertView(this,AlertView.Style .ALERT,"111","sss","取消",null).show();
        String[] strings = {"确定", "111"};
   //  new AlertView(this,AlertView.Style .ACTIONSHEET,"1112","sss",strings,null).show();
        new AlertView(this,AlertView.Style .ALERT,strings,"1112","取消",null).show();
    }

    public void alertShow4(View view) {
        new AlertView("标题", null,
                "取消", new String[]{"高亮按钮1"}, new String[]{"其他按钮1", "其他按钮2", "其他按钮3"}, this, AlertView.Style.ACTIONSHEET, this).show();
    }

    public void alertShow5(View view) {
        new AlertView("标题", "内容", "取消", null, null, this, AlertView.Style.ACTIONSHEET, this).setCancelable(true).setLeftOrCancelSize(25).setLeftOrCancelColor(getResources().getColor(android.R.color.holo_purple)).show();
    }
    AlertView alertView;
    public void alertShow6(View view) {
        new AlertView("上传头像", "eeeee", "取消", new String[]{"put"},
                new String[]{"拍照", "从相册中选择"},
                this, AlertView.Style.ACTIONSHEET, new OnItemClickListener(){
            @Override
            public void onItemClick(Object o, int position){
                Toast.makeText(MainActivity.this, "点击了第" + position + "个",
                        Toast.LENGTH_SHORT).show();
            }
        })
            .setCancelable(true)   .show();
    }

    /**
     * 拓展
     * @param view
     */
    public void alertShowExt(View view) {
        mAlertViewExt.show();
    }


    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(),0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }
    @Override
    public void onItemClick(Object o,int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if(o == mAlertViewExt && position != AlertView.CANCELPOSITION){
            String name = etName.getText().toString();
            if(name.isEmpty()){
                Toast.makeText(this, "啥都没填呢", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "hello,"+name, Toast.LENGTH_SHORT).show();
            }

            return;
        }
        Toast.makeText(this, "点击了第" + position + "个", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDismiss(Object o) {
        closeKeyboard();
        Toast.makeText(this, "消失了", Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            if(mAlertView!=null && mAlertView.isShowing()){
                mAlertView.dismiss();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
