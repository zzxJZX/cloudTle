package com.cmri.moudleapp.moudlevoip.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.cmri.moudleapp.moudlevoip.R;

/**
 * Created by anderson on 17/8/24.
 */

public class RoundRelativeLayout extends RelativeLayout{

    private float roundLayoutRadius = 20;

    private RectF rectAllF = new RectF();

    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();

    public RoundRelativeLayout(Context context) {
        super(context);
    }

    public RoundRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setRoundLayoutRadius(float roundLayoutRadius) {
        this.roundLayoutRadius = roundLayoutRadius;
        postInvalidate();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        roundLayoutRadius = getResources().getDimension(R.dimen.px_positive_20);

        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        zonePaint.setAntiAlias(true);
        //zonePaint.setColor(getResources().getColor(R.color.color_gradient_right_bu));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        rectAllF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(rectAllF, zonePaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawRoundRect(rectAllF, roundLayoutRadius, roundLayoutRadius, zonePaint);
        canvas.saveLayer(rectAllF, maskPaint, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

}
