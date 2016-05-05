package com.trojx.androidhaloview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**HaloBackground
 * Created by Trojx on 2016/5/4 0004.
 */
public class HaloBackground extends RelativeLayout {

    private static final int DEFAULT_DURATION_TIME=3000;
    private static final int DEFAULT_HALO_AMOUNT =3;
    private static final float DEFAULT_HALO_SCALE = 6.0f;
    private int haloColor;
    private float haloRadius;
    private int haloDuration;
    private int haloAmount;
    private Paint paint;
    private float haloWidthIn;
    private float haloWidthOut;
    private float haloScale;
    private LayoutParams haloParams;
    private AnimatorSet animatorSet;
    private ArrayList<Animator> animatorList;
    private ArrayList<HaloViewBlur> haloViewList=new ArrayList<>();
    private int haloDelay;
    private boolean animationRunning=false;

    public HaloBackground(Context context) {
        super(context);
    }

    public HaloBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public HaloBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        if(isInEditMode()){
            return;
        }
        if(attrs==null){
            throw new IllegalArgumentException("Attributes should be provided to this view.");
        }

        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.HaloBackground);
        haloColor = typedArray.getColor(R.styleable.HaloBackground_halo_color,
                getResources().getColor(R.color.colorPrimary));
        haloRadius = typedArray.getDimension(R.styleable.HaloBackground_halo_radius,
                getResources().getDimension(R.dimen.haloRadius));
        haloWidthIn = typedArray.getDimension(R.styleable.HaloBackground_halo_width_in,
                getResources().getDimension(R.dimen.haloWidthIn));
        haloWidthOut = typedArray.getDimension(R.styleable.HaloBackground_halo_width_out,
                getResources().getDimension(R.dimen.haloWidthOut));
        haloDuration = typedArray.getInteger(R.styleable.HaloBackground_halo_duration,
                DEFAULT_DURATION_TIME);
        haloAmount = typedArray.getInteger(R.styleable.HaloBackground_halo_amount,
                DEFAULT_HALO_AMOUNT);
        haloScale = typedArray.getFloat(R.styleable.HaloBackground_halo_scale,DEFAULT_HALO_SCALE);
        typedArray.recycle();


        haloDelay = haloDuration/haloAmount;


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(haloColor);

        haloParams = new LayoutParams((int)(2*(haloRadius+haloWidthOut)),(int)(2*(haloRadius+haloWidthOut)));
        haloParams.addRule(CENTER_IN_PARENT,TRUE);


        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorList = new ArrayList<>();

        for(int i=0;i<haloAmount;i++){
            HaloViewPure haloViewPure=new HaloViewPure(context);
            Bitmap bitmapPure=loadBitmapFromView(haloViewPure,context);
            Bitmap bitmapBlur=blurBitmap(bitmapPure,context);

            HaloViewBlur haloViewBlur=new HaloViewBlur(context);
            haloViewBlur.setBitmapBlur(bitmapBlur);
            addView(haloViewBlur,haloParams);
            haloViewList.add(haloViewBlur);
//            addView(haloViewPure,haloParams);
//            haloViewList.add(haloViewPure);

            final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(haloViewBlur, "ScaleX", 1.0f, haloScale);
            scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleXAnimator.setStartDelay(i * haloDelay);
            scaleXAnimator.setDuration(haloDuration);
            animatorList.add(scaleXAnimator);
            final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(haloViewBlur, "ScaleY", 1.0f, haloScale);
            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleYAnimator.setStartDelay(i * haloDelay);
            scaleYAnimator.setDuration(haloDuration);
            animatorList.add(scaleYAnimator);
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(haloViewBlur, "Alpha", 1.0f, 0f);
            alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setStartDelay(i * haloDelay);
            alphaAnimator.setDuration(haloDuration);
            animatorList.add(alphaAnimator);
        }
        animatorSet.playTogether(animatorList);
    }

    private class HaloViewPure extends View{

        public HaloViewPure(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            int radius=(Math.min(getWidth(),getHeight()))/2;

            //画外圈
            paint.setStrokeWidth(haloWidthOut);
            paint.setAlpha(50);
            canvas.drawCircle(radius,radius,radius-haloWidthOut,paint);
            //画内圈
            paint.setStrokeWidth(haloWidthIn);
            paint.setAlpha(255);
            canvas.drawCircle(radius,radius,radius-haloWidthOut,paint);

        }
    }
    private class HaloViewBlur extends  View{

        private Bitmap bitmapBlur;

        public void setBitmapBlur(Bitmap bitmapBlur){
            this.bitmapBlur=bitmapBlur;
        }

        public HaloViewBlur(Context context) {
            super(context);
//            setVisibility(INVISIBLE);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            canvas.drawBitmap(bitmapBlur,0,0,paint);
            try {
                bitmapBlur.compress(Bitmap.CompressFormat.PNG,100,new FileOutputStream(new File("sdcard/HaloViewBlur.png")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 将view转换成bitmap
     * @param view
     * @return
     */
    private Bitmap loadBitmapFromView(View view,Context context) {
        if (view == null) {
            return null;
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(dip2px(context, 60f),
                View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                dip2px(context, 80f), View.MeasureSpec.EXACTLY));
        // 这个方法也非常重要，设置布局的尺寸和位置
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        // 生成bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        // 利用bitmap生成画布
        Canvas canvas = new Canvas(bitmap);
        // 把view中的内容绘制在画布上
        view.draw(canvas);

        return bitmap;
    }


    /**
     * 将bitmap高斯模糊化
     * @param bitmap
     * @return
     */
    private Bitmap blurBitmap(Bitmap bitmap,Context context){

        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
            Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
            blurScript.setRadius(5.f);
            blurScript.setInput(allIn);
            blurScript.forEach(allOut);
            allOut.copyTo(outBitmap);
            bitmap.recycle();
            rs.destroy();
        }
        return  outBitmap;
    }

    public void startHaloAnimation(){
        if(!isAnimationRunning()){
            for(HaloViewBlur haloViewBlur:haloViewList){
                haloViewBlur.setVisibility(VISIBLE);
            }
//            for(HaloViewPure haloViewPure:haloViewList){
//                haloViewPure.setVisibility(VISIBLE);
//            }
            animatorSet.start();
            animationRunning=true;
        }
    }

    public void stopHaloAnimation(){
        if(isAnimationRunning()){
            animatorSet.end();
            animationRunning=false;
        }
    }

    public boolean isAnimationRunning(){
        return animationRunning;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    private int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
