package ir.ninjacoder.wallpaper;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import ir.ninjacoder.ghostide.core.glidecompat.BlurTransformation;
import ir.ninjacoder.ghostide.core.utils.AnimUtils;
import ir.ninjacoder.ghostide.core.utils.ObjectUtils;

public class CustomFab extends LinearLayout {
  ImageView icon;
  private Context context;
  private ValueAnimator fabBackgroundRotationAnimator; // انیمیشن چرخش بک‌گراند FAB
  private ValueAnimator imageViewRotationAnimator; // انیمیشن چرخش ImageView

  public CustomFab(Context c) {
    super(c);
    this.context = c;
    init();
  }

  void init() {

    LayoutParams layoutParams = new LayoutParams(dipToPx(56), dipToPx(56));
    setLayoutParams(layoutParams);

    removeAllViews();
    setElevation(dipToPx(6));
    setPadding(dipToPx(8), dipToPx(8), dipToPx(8), dipToPx(8));
    setGravity(Gravity.CENTER);
    icon = new ImageView(getContext());
    LayoutParams iconParams = new LayoutParams(dipToPx(24), dipToPx(24));
    icon.setLayoutParams(iconParams);
    icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
    Glide.with(getContext())
        .load("/storage/emulated/0/GhostWebIDE/plugins/wallpaper/icon/settings.png")
        .override(45, 45)
        .centerInside()
        .error(new ColorDrawable(Color.RED))
        .placeholder(new ColorDrawable(Color.GRAY))
        .into(icon);
    addView(icon);
    setClickable(true);
    setFocusable(true);
    Drawable rotatingDrawable = createRotatingDrawable();
    setBackground(rotatingDrawable);
    Drawable background = getBackground();
    if (background instanceof RotateDrawable) {
      if (fabBackgroundRotationAnimator != null && fabBackgroundRotationAnimator.isRunning()) {
        fabBackgroundRotationAnimator.cancel();
      }
      fabBackgroundRotationAnimator = ValueAnimator.ofInt(0, 10000);
      fabBackgroundRotationAnimator.setDuration(10000);
      fabBackgroundRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
      fabBackgroundRotationAnimator.setInterpolator(new LinearInterpolator());

      fabBackgroundRotationAnimator.addUpdateListener(
          animator -> {
            if (this != null && getBackground() instanceof RotateDrawable) {
              RotateDrawable rotateDrawable = (RotateDrawable) getBackground();
              int level = (int) animator.getAnimatedValue();
              rotateDrawable.setLevel(level);
              invalidate();
            }
          });

      fabBackgroundRotationAnimator.start();
    }
  }

  private int dipToPx(float dip) {
    float density = getContext().getResources().getDisplayMetrics().density;
    return (int) (dip * density + 0.5f);
  }

  @Override
  public void setOnClickListener(OnClickListener l) {

    setClickable(true);
    setFocusable(true);
    AnimUtils.Sacla(this);
    super.setOnClickListener(l);
  }

  private Drawable createRotatingDrawable() {
    RotateDrawable rotateDrawable = new RotateDrawable();
    rotateDrawable.setDrawable(ObjectUtils.getCookieShape());
    rotateDrawable.setFromDegrees(0f);
    rotateDrawable.setToDegrees(360f);
    rotateDrawable.setColorFilter(Color.parseColor("#2F8AEAFF"), PorterDuff.Mode.SRC_IN);
    rotateDrawable.setLevel(0);

    return rotateDrawable;
  }
}
