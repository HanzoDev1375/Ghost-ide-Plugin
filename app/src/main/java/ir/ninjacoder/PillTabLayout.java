package ir.ninjacoder;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import com.google.android.material.tabs.TabLayout;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

public class PillTabLayout implements PluginManagerCompat {
  private CodeEditorActivity editor;
  private Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public void getEditor(CodeEditor arg0) {}

  @Override
  public String setName() {
    return "PillTabLayout";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity editor) {
    this.editor = editor;
    applyPillIndicator();
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {
    if (arg0 instanceof CodeEditorActivity) {
      this.editor = (CodeEditorActivity) arg0;
      applyPillIndicator();
    }
  }

  @Override
  public String langModel() {
    return "all";
  }

  private void applyPillIndicator() {
    if (editor == null) return;

    handler.postDelayed(
        () -> {
          try {
            TabLayout tab = editor.getEditorTabLayout();
            if (tab == null) {
              // اگر تب لیاوت هنوز آماده نیست، دوباره تلاش کن
              handler.postDelayed(this::applyPillIndicator, 300);
              return;
            }

            // مطمئن شویم که تب‌ها وجود دارند
            if (tab.getTabCount() == 0) {
              // اگر تب‌ها هنوز بارگذاری نشده‌اند، منتظر بمان
              handler.postDelayed(this::applyPillIndicator, 300);
              return;
            }

            // تنظیمات TabLayout
            tab.setTabGravity(TabLayout.GRAVITY_FILL);
            tab.setInlineLabel(true);
            tab.setTabIndicatorFullWidth(true);
            tab.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_CENTER);

            // ایجاد و اعمال اندیکاتور
            GradientDrawable pillIndicator = createPillIndicator(editor, tab);
            tab.setSelectedTabIndicator(pillIndicator);

          } catch (Exception e) {
            e.printStackTrace();
            // در صورت خطا دوباره تلاش کن
            handler.postDelayed(this::applyPillIndicator, 500);
          }
        },
        500);
  }

  private GradientDrawable createPillIndicator(Context context, TabLayout tab) {
    GradientDrawable shape = new GradientDrawable();

    // گرفتن رنگ از تم فعلی
    int color;
    try {
      Drawable indicator = tab.getTabSelectedIndicator();
      if (indicator instanceof ColorDrawable) {
        color = ((ColorDrawable) indicator).getColor();
      } else {
        // رنگ پیش‌فرض اگر نتوانستیم رنگ را بگیریم
        color = Color.parseColor("#6200EE"); // رنگ بنفش متریال
      }
    } catch (Exception e) {
      color = Color.parseColor("#6200EE");
    }

    // تنظیمات شکل
    shape.setShape(GradientDrawable.RECTANGLE);
    shape.setCornerRadius(dp(context, 50)); // گوشه‌های گرد
    shape.setColor(Color.TRANSPARENT);
    shape.setStroke(dp(context, 2), color); // حاشیه با رنگ انتخابی
    shape.setSize(tab.getWidth() / Math.max(1, tab.getTabCount()), dp(context, 36));

    return shape;
  }

  private int dp(Context context, int dp) {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
  }
}
