package com.cmri.moudleapp.moudlevoip.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cmri.moudleapp.moudlevoip.R;
import com.cmri.moudleapp.moudlevoip.utils.StringUtils;

/**
 * Created by anderson on 17/8/24.
 */

public class ContactHeadLayout extends LinearLayout{

    private ImageView mHeadImageView;
    private ImageView mIconTVRightTopCorner;
    private View mInvertedView;
    private RoundRelativeLayout mHeadLayout;
    private LinearLayout mContactLayout;

    private TextView mNameTextView;
    private TextView mPhoneTextView;

    private int mTextColor;
    private int mTextSize;
    private int mTextBackgroundHeight;
    private boolean mSmallDeafultHead = false;
    private boolean mShowSingleName = false;
    private String mNameText;
    private String mPhoneText;

    private boolean makeInverted = false;
    private boolean isInitInverted = false;
    private RectF rectAllF = new RectF();

    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();

    public ContactHeadLayout(Context context) {
        super(context);
        initView(context);
    }

    public ContactHeadLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initAttrs(context, attrs);

    }

    public ContactHeadLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
        initAttrs(context, attrs);
    }

    private void initView(Context context) {
        inflate(context, R.layout.view_contact_head, this);
    }


    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.ContactHeadLayout);
        mTextColor = mTypedArray.getColor(R.styleable.ContactHeadLayout_nameTextColor, Color.WHITE);
        mTextSize = mTypedArray.getDimensionPixelSize(R.styleable.ContactHeadLayout_nameTextSize, (int) getResources().getDimensionPixelSize(R.dimen.t6));
        mNameText = mTypedArray.getString(R.styleable.ContactHeadLayout_nameText);
        mPhoneText = mTypedArray.getString(R.styleable.ContactHeadLayout_phoneText);
        mSmallDeafultHead = mTypedArray.getBoolean(R.styleable.ContactHeadLayout_smallDefaultHead, false);
        mShowSingleName = mTypedArray.getBoolean(R.styleable.ContactHeadLayout_showSingleName, false);
        mShowSingleName = mTypedArray.getBoolean(R.styleable.ContactHeadLayout_showSingleName, false);
        mTextBackgroundHeight = mTypedArray.getDimensionPixelSize(R.styleable.ContactHeadLayout_textBackgroundHeight, (int) getResources().getDimensionPixelSize(R.dimen.px_positive_80));
        mTypedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        zonePaint.setAntiAlias(true);
        //zonePaint.setColor(getResources().getColor(R.color.color_gradient_right_bu));

        mHeadImageView = findViewById(R.id.photo_iv);
        mInvertedView = findViewById(R.id.inverted_iv);
        mNameTextView = findViewById(R.id.name_tv);
        mPhoneTextView = findViewById(R.id.number_tv);

        mHeadLayout = findViewById(R.id.head_layout);
        mContactLayout = findViewById(R.id.contact_layout);

        mIconTVRightTopCorner=findViewById(R.id.icon_tv_righttopcorner);

        clear();

        setHeadNameText(mNameText);
        setHeadPhoneText(mPhoneText);
        setHeadTextColor(mTextColor);
        setHeadTextSize(mTextSize);
        setShowSingleName(mShowSingleName);
        setSmallDeafultHead(mSmallDeafultHead);
        setTextBackgroundHeight(mTextBackgroundHeight);
    }

    public void setTextBackgroundHeight(int height) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContactLayout.getLayoutParams();
        params.height = height;
        mContactLayout.setLayoutParams(params);
    }

    public void changeTextBackground(){
        mContactLayout.setBackgroundColor(getResources().getColor(R.color.setting_contact_textBg));
    }

    public void setHeadTextSize(float size) {
        mPhoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        mNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void setHeadTextColor(int mTextColor) {
        mPhoneTextView.setTextColor(mTextColor);
        mNameTextView.setTextColor(mTextColor);
    }

    public void setHeadPhoneText(String text) {
        mPhoneTextView.setText(text);
    }

    public void setHeadNameText(String text) {
        mNameTextView.setText(text);

    }

    public void setShowSingleName(boolean isSingle) {
        this.mShowSingleName = isSingle;
    }

    public void setSmallDeafultHead(boolean isSmall) {
       this.mSmallDeafultHead = isSmall;
    }

    public void clear(){
        mNameTextView.setText("");
        mPhoneTextView.setText("");
        mPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mHeadImageView.setScaleType(ImageView.ScaleType.CENTER);
        mHeadImageView.setImageResource(mSmallDeafultHead? R.mipmap.iconface_smallcard: R.mipmap.img_faceicon_card);
        mIconTVRightTopCorner.setVisibility(GONE);
    }

    public void setContactInfo(String imgUrl, String name, String number){
        mNameTextView.setText(name);
        mPhoneTextView.setText(number);
        if (!mShowSingleName) {
            if (StringUtils.isChinaMobileNum(number)) {
                mPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_phone, 0, 0, 0);
            } else {
                mPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_tv, 0, 0, 0);
            }
        } else {
            mPhoneTextView.setVisibility(GONE);
            mNameTextView.setGravity(Gravity.CENTER);
        }
        if (StringUtils.isChinaMobileNum(number)) {
            mIconTVRightTopCorner.setVisibility(INVISIBLE);
        } else {
            mIconTVRightTopCorner.setVisibility(VISIBLE);
        }

        Glide.with(getContext()).load(imgUrl).placeholder(mSmallDeafultHead? R.mipmap.iconface_smallcard: R.mipmap.img_faceicon_card).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                mHeadImageView.setScaleType(ImageView.ScaleType.CENTER);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                mHeadImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mHeadImageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postInvalidate();
                    }
                }, 500);
                return false;
            }
        }).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mHeadImageView);

    }

    public void setRoundLayoutRadius(float roundLayoutRadius) {
        mHeadLayout.setRoundLayoutRadius(roundLayoutRadius);
    }

    public void setMakeInverted(boolean makeInverted){
        this.makeInverted = makeInverted;
        mInvertedView.setVisibility(View.VISIBLE);

        if(!isInitInverted){
            isInitInverted = true;
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height = layoutParams.height * 2;
            setLayoutParams(layoutParams);
        }
    }

    public void setOnlyName(boolean isOnlyName){
        if(isOnlyName){
            mPhoneTextView.setVisibility(GONE);
            mNameTextView.setTextSize(getResources().getDimension(R.dimen.t6));
            mNameTextView.setTextColor(getResources().getColor(R.color.color_wt));
            mNameTextView.setGravity(Gravity.CENTER);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        rectAllF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if(makeInverted){
            canvas.saveLayer(rectAllF, zonePaint, Canvas.ALL_SAVE_FLAG);
            Matrix mMatrix = new Matrix();
            mMatrix.preScale(1, -1);
            mHeadLayout.setDrawingCacheEnabled(true);
            int height = mHeadLayout.getHeight();
            int width = mHeadLayout.getWidth();
            Bitmap originalImage = Bitmap.createBitmap(mHeadLayout.getDrawingCache());
            mHeadLayout.destroyDrawingCache();
            Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, 0, width, height, mMatrix, false);
            canvas.drawBitmap(reflectionImage, 0, height, zonePaint);

            LinearGradient shader = new LinearGradient(0, height, 0,
                    height * 2, new int[]{0x80ffffff, 0x00ffffff, 0x00ffffff}, null, Shader.TileMode.MIRROR);
            maskPaint.setShader(shader);
            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawRect(0, height, width, height * 2, maskPaint);
            canvas.restore();
            maskPaint.setShader(null);
        }
        super.dispatchDraw(canvas);
    }

}
