package com.cmri.moudleapp.moudlevoip.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by zhangchao on 17-9-22.
 */

@SuppressLint("AppCompatCustomView")
public class TextViewLoadingAnimator extends TextView {

    private Boolean mIsAnimator=false;
    private int currentPoint;
    private Paint pointPaint;
    private ValueAnimator animator;
    private boolean reduceTextFlag=false;

    public TextViewLoadingAnimator(Context context){
       this(context,null);
    }
    public TextViewLoadingAnimator(Context context, AttributeSet attrs){
      this(context,attrs,0);

    }
    public TextViewLoadingAnimator(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        pointPaint=new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setStrokeWidth(2);
    }

    public void setAnimator(Boolean isAnimator){
        mIsAnimator=isAnimator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mIsAnimator&&getText().length()>0){

            float height=(float) getHeight();
            float width=(float) getWidth();
            float textEndPosition=width*85/100;

            CharSequence newText=getText().length()>3?getText().subSequence(0,3):getText();
            float newTextWidth=getPaint().measureText(newText.toString());
            if(newTextWidth>width*85/100){
                getPaint().setTextSize(getTextSize()*2/3);
                reduceTextFlag=true;
                //newText=getText().subSequence(0,2);
            }
            setText(newText);
            if(currentPoint==1){
                canvas.drawPoint(textEndPosition,height/2,pointPaint);
            }
            else if(currentPoint==2){
                canvas.drawPoint(textEndPosition,height/2,pointPaint);
                canvas.drawPoint(textEndPosition+3,height/2,pointPaint);
            }
            else if(currentPoint==3){
                canvas.drawPoint(textEndPosition,height/2,pointPaint);
                canvas.drawPoint(textEndPosition+3,height/2,pointPaint);
                canvas.drawPoint(textEndPosition+6,height/2,pointPaint);
            }
            super.onDraw(canvas);
            if(reduceTextFlag){
                getPaint().setTextSize(getTextSize()*3/2);
                reduceTextFlag=false;
            }

            return;
        }

        super.onDraw(canvas);
    }

    public void startAnimation(){
        animator=ValueAnimator.ofInt(0,4);
        animator.setDuration(3000);
        animator.setRepeatCount(20);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentPoint=(int)valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

}
