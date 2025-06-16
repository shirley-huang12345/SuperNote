package com.asus.supernote.ratingus;

import com.asus.supernote.R;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class RateUsToastAnimation implements Runnable, Animation.AnimationListener {

    private Context mContext;
    public RateUsToastAnimation(Context context) {
        mContext = context;
    }

    @Override
    public void run() {
        showAnimation();
    }

    private void showAnimation() {
        ViewGroup view = (ViewGroup) LayoutInflater.from(mContext)
                .inflate(R.layout.rate_us_animation_toast, null);

        Toast toast = new Toast(mContext);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.setGravity(Gravity.FILL, 0, 100);
        toast.show();

        ImageView circle = (ImageView) view.findViewById(R.id.circle);
        circle.startAnimation(getAnimationSet());
    }

    private AnimationSet getAnimationSet() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF,
                mContext.getResources().getInteger(R.integer.scroll_toast_translate_y));
        translateAnimation.setDuration(1300);
        translateAnimation.setStartOffset(480);
        translateAnimation.setFillAfter(true);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setStartOffset(0);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1f, 1.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setStartOffset(0);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setAnimationListener(this);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);

        return animationSet;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        animation.start();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
