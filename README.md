
# Android-AlertView
仿iOS的AlertViewController
几乎完美还原iOS 的 AlertViewController ，同时支持Alert和ActionSheet模式，每一个细节都是精雕细琢，并把api封装成懒到极致模式，一行代码就可以进行弹窗.
   
      支持设置距离底部margin值（为沉浸式准备的）setPaddingBottom
      设置alter下面两个按钮点击中间线距离上下位置setDivierMargin
      设置中心弹窗的内容与按钮分割线是否中断（主要是针对俩按钮的设置了中间分割线距离顶部和底部距离的两种方案）alterDivide
      支持设置取消（中心弹窗底部左边）按钮颜色，与大小.setLeftOrCancelColor(getResources().getColor(android.R.color.holo_purple);.setLeftOrCancelSize(25);
      支持设置确定（中心弹窗底部右边）按钮颜色.setAlertRightColor(getResources().getColor(android.R.color.holo_red_dark));.setAlertRightSize


## Demo
![](https://github.com/lizhanqi/Android-AlertView/blob/master/preview/alertviewdemo.gif)

demo是用Module方式依赖，你也可以使用gradle 依赖:
```java
   compile 'com.lizhanqi:alertview:1.0.0'
```

### config in java code
```java
new AlertView("上传头像", null, "取消", null,
                new String[]{"拍照", "从相册中选择"},
                this, AlertView.Style.ActionSheet, new OnItemClickListener(){
                    public void onItemClick(Object o,int position){
                        Toast.makeText(this, "点击了第" + position + "个", 
                        Toast.LENGTH_SHORT).show();
                    }
                }).show();
                
//或者builder模式创建
new AlertView.Builder().setContext(context)
                .setStyle(AlertView.Style.ACTIONSHEET)
                .setTitle("选择操作")
                .setMessage(null)
                .setCancelText("取消")
                .setDestructive("拍照", "从相册中选择")
                .setOthers(null)
                .setOnItemClickListener(listener)
                .build()
                .show();
```
```java
new AlertView("标题", "内容", null, new String[]{"确定"}, null, this, 
                AlertView.Style.ALERT, null).show();
```
另外还支持窗口界面拓展，更多操作请下载Demo看。

## 动画时间

       ``` <integer name="animation_default_duration">300</integer>```
## 大小
 <!--标题文字大小-->
    <dimen name="textSize_alert_title">22sp</dimen>
    <dimen name="textSize_actionsheet_title">14sp</dimen>
    <!--标题文字高度-->
    <dimen name="height_alert_title">60dp</dimen>
    <dimen name="height_actionsheet_title">40dp</dimen>
    <!--消息文字大小-->
    <dimen name="textSize_alert_msg">15sp</dimen>
    <dimen name="textSize_actionsheet_msg">14sp</dimen>
    <!--消息距离底部间距-->
    <dimen name="marginBottom_alert_msg">25dp</dimen>
    <dimen name="marginBottom_actionsheet_msg">14dp</dimen>
    <!--窗口模式左右两边到屏幕的间距-->
    <dimen name="margin_alert_left_right">40dp</dimen>
    <dimen name="margin_actionsheet_left_right">10dp</dimen>
    <!--线条粗细-->
    <dimen name="size_divier">1dp</dimen>
    <!--按钮的高度-->
    <dimen name="height_alert_button">48dp</dimen>
    <!--按钮文字大小-->
    <dimen name="textSize_alert_button">18sp</dimen>
    <!--圆角-->
    <dimen name="radius_alertview">15px</dimen>
    <!--设置中心弹窗底部两个按钮分割线距离顶部和底部距离-->
    <dimen name="alert_divier_margin">0px</dimen>
        
## 颜色
 ```
 .<!--覆盖颜色-->
    <color name="bgColor_overlay">#60000000</color> 
    <!--窗口背景颜色-->
    <color name="bgColor_alertview_alert">#f8f8f8</color>
    <color name="bgColor_alertview_alert_start">#f0f0f0</color>
    <!--标题颜色-->
    <color name="textColor_alert_title">#000000</color>
    <color name="textColor_actionsheet_title">#8f8f8f</color>
    <!--消息颜色-->
    <color name="textColor_alert_msg">#000000</color>
    <color name="textColor_actionsheet_msg">#8f8f8f</color>
    <!--线条颜色-->
    <color name="bgColor_divier">#d7d7db</color>
    <!--取消按钮颜色-->
    <color name="textColor_alert_button_cancel">#007aff</color>
    <!--确定按钮颜色-->
    <color name="textColor_alert_button_confirm">#5ac921</color>
    <!--高亮按钮颜色-->
    <color name="textColor_alert_button_destructive">#ff3b30</color>
    <!--普通按钮文字颜色-->
    <color name="textColor_alert_button_others">#007aff</color>
    <!--点击颜色-->
    <color name="bgColor_alert_button_press">#EAEAEA</color>
    <!--actionsheet模式的取消按钮背景颜色-->
    <color name="bgColor_actionsheet_cancel_nor">#ffffff</color> ```

    
    感谢：https://github.com/saiwu-bigkoo/Android-AlertView
    这个是在其基础上新增一个属性alert_divier_margin,
    以及方法setDivierMargin  setPaddingBottom  alterDivide三个方法


