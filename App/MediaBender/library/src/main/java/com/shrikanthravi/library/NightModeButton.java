package com.shrikanthravi.library;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;

import androidx.cardview.widget.CardView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ConcurrentModificationException;


public class NightModeButton extends RelativeLayout {

    public NightModeButton(Context context) {
        super(context);
    }

    //Context
    protected Context mContext;
    protected LayoutInflater mLayoutInflater;

    //Views
    protected ImageView switchIV;
    protected CardView switchRL;

    //Listener
    protected OnSwitchListener onSwitchListener;
    private boolean inAnimation = false;

    public NightModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    boolean isNight;

    public void setMode(Boolean mode) {
        this.isNight = mode;
//        setButtonFirstAppearance(mContext);
//        changeButtonAppearance(mContext);
        loadCorrectAppearance(mContext);
    }


    public void init(final Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        View rootView = mLayoutInflater.inflate(R.layout.night_mode_button_layout, this, true);
        switchRL = rootView.findViewById(R.id.switchRL);
        switchIV = rootView.findViewById(R.id.switchIV);

        setButtonFirstAppearance(context);

        switchRL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonAppearance(mContext);
            }
        });

    }

    private void changeButtonAppearance(final Context context) {
        if (isNight && !inAnimation) {
            isNight = false;
            inAnimation = true;
            ObjectAnimator
                    .ofFloat(switchIV, "rotation", 0, 360)
                    .setDuration(400)
                    .start();
            ObjectAnimator
                    .ofFloat(switchIV, "translationX", switchRL.getWidth() / 2, 0)
                    .setDuration(400)
                    .start();
            android.os.Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    switchIV.setImageDrawable(context.getDrawable(R.drawable.day_icon));
                }
            }, 350);
            ValueAnimator valueAnimator = ValueAnimator.ofArgb(Color.parseColor("#353535"), Color.parseColor("#dadada"));
            valueAnimator.setDuration(400);
            valueAnimator.start();
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    inAnimation = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    switchRL.setCardBackgroundColor((int) animation.getAnimatedValue());
                }
            });

            nightModeButtonClicked(isNight);

        } else {

            if (!inAnimation) {
                isNight = true;
                ObjectAnimator
                        .ofFloat(switchIV, "rotation", 360, 0)
                        .setDuration(400)
                        .start();
                ObjectAnimator
                        .ofFloat(switchIV, "translationX", 0, switchRL.getWidth() / 2)
                        .setDuration(400)
                        .start();
                ValueAnimator valueAnimator = ValueAnimator.ofArgb(Color.parseColor("#dadada"), Color.parseColor("#353535"));
                valueAnimator.setDuration(400);
                valueAnimator.start();
                valueAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        inAnimation = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        switchRL.setCardBackgroundColor((int) animation.getAnimatedValue());
                    }
                });
                switchIV.setImageDrawable(context.getDrawable(R.drawable.night_icon));

                nightModeButtonClicked(isNight);
            }
        }
    }

    private void loadCorrectAppearance(final Context context) {
        if (!isNight) {

            ObjectAnimator
                    .ofFloat(switchIV, "rotation", 0, 360)
                    .setDuration(400)
                    .start();
            ObjectAnimator
                    .ofFloat(switchIV, "translationX", 0, switchRL.getWidth() / 2)
                    .setDuration(400)
                    .start();
            android.os.Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    switchIV.setImageDrawable(context.getDrawable(R.drawable.day_icon));
                }
            }, 350);
            ValueAnimator valueAnimator = ValueAnimator.ofArgb(Color.parseColor("#353535"), Color.parseColor("#dadada"));
            valueAnimator.setDuration(400);
            valueAnimator.start();
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    //inAnimation = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    switchRL.setCardBackgroundColor((int) animation.getAnimatedValue());
                }
            });

//            nightModeButtonClicked(isNight);

        } else {

//            if (!inAnimation) {
//                isNight = true;
            ObjectAnimator
                    .ofFloat(switchIV, "rotation", 360, 0)
                    .setDuration(400)
                    .start();
            ObjectAnimator
                    .ofFloat(switchIV, "translationX", switchRL.getWidth() / 2, 0)
                    .setDuration(400)
                    .start();
            ValueAnimator valueAnimator = ValueAnimator.ofArgb(Color.parseColor("#dadada"), Color.parseColor("#353535"));
            valueAnimator.setDuration(400);
            valueAnimator.start();
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    //inAnimation = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    switchRL.setCardBackgroundColor((int) animation.getAnimatedValue());
                }
            });
            switchIV.setImageDrawable(context.getDrawable(R.drawable.night_icon));

//                nightModeButtonClicked(isNight);
//            }
        }
    }

    private void setButtonFirstAppearance(Context context) {
        if (isNight) {
            switchIV.setImageDrawable(context.getDrawable(R.drawable.night_icon));
            switchRL.setCardBackgroundColor(Color.parseColor("#353535"));
        } else {
            switchIV.setImageDrawable(context.getDrawable(R.drawable.day_icon));
            switchRL.setCardBackgroundColor(Color.parseColor("#dadada"));
        }
    }

    public interface OnSwitchListener {
        public void onSwitchListener(boolean isNight);
    }

    public void setOnSwitchListener(OnSwitchListener onSwitchListener) {
        this.onSwitchListener = onSwitchListener;
    }

    private void nightModeButtonClicked(boolean isNight) {
        if (onSwitchListener != null) {
            onSwitchListener.onSwitchListener(isNight);
        }
    }
}
