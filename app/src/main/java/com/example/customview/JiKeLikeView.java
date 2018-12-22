package com.example.customview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Describe :
 * Created by Knight on 2018/12/19
 * 点滴之行,看世界
 **/
public class JiKeLikeView extends View {

    //点赞数量
    private int likeNumber;
    //文字上下移动的最大距离
    private int textMaxMove;
    //文字上下移动的距离
    private float textMoveDistance;
    //点亮的透明度 0位隐藏 255是出现
    private float shiningAlpha;
    //点亮的缩放系数
    private float shiningScale;
    //文字的透明度系数
    private float textAlpha;
    //动画播放时间
    private int duration = 250;
    //文字显示范围
    private Rect textRounds;
    //数字位数
    private float[] widths;

    //图像画笔
    private Paint bitmapPaint;
    //文字画笔
    private Paint textPaint;
    //之前的文字画笔
    private Paint oldTextPaint;
    //没有点赞的图像
    private Bitmap unLikeBitmap;
    //点赞后的图像
    private Bitmap likeBitmap;
    //点亮的图像
    private Bitmap shiningBitmap;

    //是否点赞
    private boolean isLike = false;
    //小手的缩放倍数
    private float handScale = 1.0f;
    //刚进入 数字不要加一
    private boolean isFirst;


    public JiKeLikeView(Context context) {
        this(context, null);
    }

    public JiKeLikeView(Context context,  @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JiKeLikeView(Context context,  @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取attrs文件下配置属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JiKeLikeView);
        //点赞数量 第一个参数就是属性集合里面的属性 固定格式R.styleable+自定义属性名字
        //第二个参数，如果没有设置这个属性，则会取设置的默认值
        likeNumber = typedArray.getInt(R.styleable.JiKeLikeView_like_number, 1999);
        //记得把TypedArray对象回收
        typedArray.recycle();
        init();
    }

    private void init() {
        //创建文本显示范围
        textRounds = new Rect();
        //点赞数暂时8位
        widths = new float[8];
        //位图抗锯齿
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oldTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //文字颜色大小配置
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(SystemUtil.sp2px(getContext(), 14));
        oldTextPaint.setColor(Color.GRAY);
        oldTextPaint.setTextSize(SystemUtil.sp2px(getContext(), 14));
    }

    /**
     * 这个方法是在Activity resume的时候被调用的，Activity对应的window被添加的时候
     * 每个view只会调用一次，可以做一些初始化操作
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Resources resources = getResources();
        unLikeBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_message_unlike);
        likeBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_message_like);
        shiningBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_message_like_shining);
    }


    /**
     * 和onAttachedToWindow对应，在destroy view的时候调用
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //回收bitmap
        unLikeBitmap.recycle();
        likeBitmap.recycle();
        shiningBitmap.recycle();
    }

    /**
     * 测量宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //高度默认是bitmap的高度加上下margin各10dp
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(unLikeBitmap.getHeight() + SystemUtil.dp2px(getContext(),20), MeasureSpec.EXACTLY);
        //宽度默认是bitmap的宽度加左右margin各10dp和文字宽度和文字右侧10dp
        String textnum = String.valueOf(likeNumber);
        float textWidth = textPaint.measureText(textnum, 0, textnum.length());
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(((int) (unLikeBitmap.getWidth() + textWidth + SystemUtil.dp2px(getContext(),30))), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;
        Bitmap handBitmap = isLike ? likeBitmap : unLikeBitmap;
        int handBitmapWidth = handBitmap.getWidth();
        int handBitmapHeight = handBitmap.getHeight();

        //画小手
        int handTop = (height - handBitmapHeight) / 2;
        //先保存画布的状态
        canvas.save();
        //根据bitmap中心进行缩放
        canvas.scale(handScale, handScale, handBitmapWidth / 2, centerY);
        //画bitmap小手，第一个是参数对应的bitmap，第二个参数是左上角坐标，第三个参数上顶部坐标，第四个是画笔
        canvas.drawBitmap(handBitmap, SystemUtil.dp2px(getContext(),10), handTop, bitmapPaint);
        //读取之前没有缩放画布的状态
        canvas.restore();

        //画上面三点闪亮
        //先确定顶部
        int shiningTop = handTop - shiningBitmap.getHeight() + SystemUtil.dp2px(getContext(),16);
        //根据隐藏系数设置点亮的透明度
        bitmapPaint.setAlpha((int) (255 * shiningAlpha));
        //保存画布状态
        canvas.save();
        //画布根据点亮的缩放系数进行缩放
        canvas.scale(shiningScale, shiningScale, handBitmapWidth / 2, handTop);
        //画出点亮的bitmap
        canvas.drawBitmap(shiningBitmap, SystemUtil.dp2px(getContext(),14), shiningTop, bitmapPaint);
        //恢复画笔之前的状态
        canvas.restore();
        //并且恢复画笔bitmapPaint透明度
        bitmapPaint.setAlpha(255);

        //画文字
        String textValue = String.valueOf(likeNumber);
        //如果点赞了，之前的数值就是点赞数-1，如果取消点赞，那么之前数值（对比点赞后）就是现在显示的
        String textCancelValue;
        if (isLike) {
            textCancelValue = String.valueOf(likeNumber - 1);
        } else {
            if(isFirst){
                textCancelValue = String.valueOf(likeNumber + 1);
            }else{
                isFirst = !isFirst;
                textCancelValue = String.valueOf(likeNumber);
            }
        }
        int textLength = textValue.length();
        //获取绘制文字的坐标 getTextBounds 返回所有文本的联合边界
        textPaint.getTextBounds(textValue, 0, textValue.length(), textRounds);
        //确定X坐标 距离手差10dp
        int textX = handBitmapWidth + SystemUtil.dp2px(getContext(), 10);
        //确定Y坐标 距离 大图像的一半减去 文字区域高度的一半 即可得出
        int textY = height / 2 - (textRounds.top + textRounds.bottom) / 2;
        //绘制文字
        if (textLength != textCancelValue.length() || textMaxMove == 0) {
            //第一个参数就是文字内容，第二个参数是文字的X坐标，第三个参数是文字的Y坐标，注意这个坐标
            //并不是文字的左上角 而是与左下角比较接近的位置
            //canvas.drawText(textValue,  textX, textY, textPaint);
            //点赞
            if (isLike) {
                oldTextPaint.setAlpha((int) (255 * (1 - textAlpha)));
                canvas.drawText(textCancelValue, textX + SystemUtil.dp2px(getContext(),10), textY - textMaxMove + textMoveDistance, oldTextPaint);
                textPaint.setAlpha((int) (255 * textAlpha));
                canvas.drawText(textValue, textX + SystemUtil.dp2px(getContext(),10), textY + textMoveDistance, textPaint);
            } else {
                oldTextPaint.setAlpha((int) (255 * (1 - textAlpha)));
                canvas.drawText(textCancelValue, textX + SystemUtil.dp2px(getContext(),10), textY + textMaxMove + textMoveDistance , oldTextPaint);
                textPaint.setAlpha((int) (255 * textAlpha));
                canvas.drawText(textValue, textX + SystemUtil.dp2px(getContext(),10), textY + textMoveDistance, textPaint);
            }
            return;
        }
        //把文字拆解成一个一个字符 就是获取字符串中每个字符的宽度，把结果填入参数widths
        //相当于measureText()的一个快捷方法，计算等价于对字符串中的每个字符分别调用measureText()，并把
        //它们的计算结果分别填入widths的不同元素
        textPaint.getTextWidths(textValue, widths);
        //将字符串转换为字符数组
        char[] chars = textValue.toCharArray();
        char[] oldChars = textCancelValue.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == oldChars[i]) {
                textPaint.setAlpha(255);
                if(i == 0){
                    canvas.drawText(String.valueOf(chars[i]), textX + SystemUtil.dp2px(getContext(),10), textY, textPaint);
                }else{
                    canvas.drawText(String.valueOf(chars[i]), textX, textY, textPaint);
                }

            } else {
                //点赞
                if (isLike) {
                    oldTextPaint.setAlpha((int) (255 * (1 - textAlpha)));
                    if(i == 0){
                        canvas.drawText(String.valueOf(oldChars[i]), textX + SystemUtil.dp2px(getContext(),10), textY - textMaxMove + textMoveDistance, oldTextPaint);
                    }else{
                        canvas.drawText(String.valueOf(oldChars[i]), textX, textY - textMaxMove + textMoveDistance, oldTextPaint);
                    }
                    textPaint.setAlpha((int) (255 * textAlpha));
                    if(i == 0){
                        canvas.drawText(String.valueOf(chars[i]), textX + SystemUtil.dp2px(getContext(),10), textY + textMoveDistance, textPaint);
                    }else{
                        canvas.drawText(String.valueOf(chars[i]), textX, textY + textMoveDistance, textPaint);
                    }

                } else {
                    oldTextPaint.setAlpha((int) (255 * (1 - textAlpha)));
                    if(i == 0){
                        canvas.drawText(String.valueOf(oldChars[i]), textX + SystemUtil.dp2px(getContext(),10), textY + textMaxMove + textMoveDistance , oldTextPaint);
                    }else{
                        canvas.drawText(String.valueOf(oldChars[i]), textX, textY + textMaxMove + textMoveDistance , oldTextPaint);
                    }

                    textPaint.setAlpha((int) (255 * textAlpha));
                    if(i == 0){
                        canvas.drawText(String.valueOf(chars[i]), textX + SystemUtil.dp2px(getContext(),10), textY + textMoveDistance, textPaint);
                    }else{
                        canvas.drawText(String.valueOf(chars[i]), textX, textY + textMoveDistance, textPaint);
                    }

                }
            }
            textX += widths[i];

        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                jump();
                break;
        }
        return super.onTouchEvent(event);
    }


    private void jump() {
        isLike = !isLike;
        if (isLike) {
            ++likeNumber;
            setLikeNum();
            //自定义属性 在ObjectAnimator中，是先根据属性值拼装成对应的set函数名字，比如下面handScale的拼装方法就是
            //将属性的第一个字母强制大写后与set拼接，所以就是setHandScale，然后通过反射找到对应控件的setHandScale(float handScale)函数
            //将当前数字值做为setHandScale（float handScale）的参数传入 set函数调用每隔十几毫秒就会被用一次
            //ObjectAnimator只负责把当前运动动画的数值传给set函数，set函数怎么来做就在里面写就行
            ObjectAnimator handScaleAnim = ObjectAnimator.ofFloat(this, "handScale", 1f, 0.8f, 1f);
            //设置动画时间
            handScaleAnim.setDuration(duration);

            //动画 点亮手指的四点 从0 - 1出现
            ObjectAnimator shingAlphaAnim = ObjectAnimator.ofFloat(this, "shingAlpha", 0f, 1f);
           // shingAlphaAnim.setDuration(duration);

            //放大 点亮手指的四点
            ObjectAnimator shingScaleAnim = ObjectAnimator.ofFloat(this, "shingScale", 0f, 1f);
          //  shingScaleAnim.setDuration(duration);

            //动画集一起播放
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(handScaleAnim, shingAlphaAnim, shingScaleAnim);
            animatorSet.start();


        } else {
            //取消点赞
            --likeNumber;
            setLikeNum();
            ObjectAnimator handScaleAnim = ObjectAnimator.ofFloat(this, "handScale", 1f, 0.8f, 1f);
            handScaleAnim.setDuration(duration);
            handScaleAnim.start();

            //手指上的四点消失，透明度设置为0
            setShingAlpha(0);




        }
    }

    /**
     * 手指缩放方法
     *
     * @param handScale
     */
    public void setHandScale(float handScale) {
        //传递缩放系数
        this.handScale = handScale;
        //请求重绘View树，即draw过程，视图发生大小没有变化就不会调用layout过程，并且重绘那些“需要重绘的”视图
        //如果是view就绘制该view，如果是ViewGroup,就绘制整个ViewGroup
        invalidate();
    }


    /**
     * 手指上四点从0到1出现方法
     *
     * @param shingAlpha
     */

    public void setShingAlpha(float shingAlpha) {
        this.shiningAlpha = shingAlpha;
        invalidate();
    }

    /**
     * 手指上四点缩放方法
     *
     * @param shingScale
     */
    @Keep
    public void setShingScale(float shingScale) {
        this.shiningScale = shingScale;
        invalidate();
    }


    /**
     * 设置数字变化
     *
     *
     */
    public void setLikeNum() {
        //开始移动的Y坐标
        float startY;
        //最大移动的高度
        textMaxMove = SystemUtil.dp2px(getContext(), 20);
        //如果点赞了 就下往上移
        if (isLike) {
            startY = textMaxMove;
        } else {
            startY = -textMaxMove;
        }
        ObjectAnimator textInAlphaAnim = ObjectAnimator.ofFloat(this, "textAlpha", 0f, 1f);
        textInAlphaAnim.setDuration(duration);
        ObjectAnimator textMoveAnim = ObjectAnimator.ofFloat(this, "textTranslate", startY, 0);
        textMoveAnim.setDuration(duration);


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(textInAlphaAnim,textMoveAnim);
        animatorSet.start();
    }


    /**
     * 设置数值透明度
     */

   public void setTextAlpha(float textAlpha) {
        this.textAlpha = textAlpha;
        invalidate();

    }

    /**
     *设置数值移动
     *
     */

    public void setTextTranslate(float textTranslate) {
        textMoveDistance = textTranslate;
        invalidate();
    }


}
