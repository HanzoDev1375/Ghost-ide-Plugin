package ir.ninjacoder.json;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.graphics.Color;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.tabs.TabLayout;

import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.langs.json.JsonLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;

import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.ghostide.core.utils.ObjectUtils;

public class JsonPlugin implements PluginManagerCompat {

  private CodeEditor editor;
  private CodeEditorActivity activity;

  @Override
  public void getEditor(CodeEditor arg0) {
    this.editor = arg0;
  }

  @Override
  public String setName() {
    return "Better Json";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getBaseCompat(BaseCompat arg0) {}

  @Override
  public String langModel() {
    return ".json,.ghost";
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {
    TabLayout tab = arg0.getEditorTabLayout();

    tab.post(
        () -> {
          tab.setTabGravity(TabLayout.GRAVITY_FILL);
          tab.setInlineLabel(true);
          tab.setTabIndicatorFullWidth(true);
          tab.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_CENTER);
          tab.setSelectedTabIndicator(DrawableUtils.createPillIndicator(arg0,tab));
        });

    editor.postDelayed(
        () -> {
          try {
            JsonLanguage lang =
                new JsonLanguage(editor) {
                  @Override
                  public CodeAnalyzer getAnalyzer() {
                    return new CodeAz();
                  }
                };
            editor.setEditorLanguage(lang);
          } catch (Exception ignored) {
          }
        },
        500);
  }

  static class DrawableUtils {

    public static GradientDrawable createPillIndicator(Context context, TabLayout tab) {
      GradientDrawable shape = new GradientDrawable();
      Drawable indicator = tab.getTabSelectedIndicator();
      int color = Color.BLACK;

      if (indicator instanceof ColorDrawable) {
        color = ((ColorDrawable) indicator).getColor();
      }

      shape.setStroke(8, color);
      shape.setColor(Color.TRANSPARENT);
      shape.setShape(GradientDrawable.RECTANGLE);
      shape.setCornerRadius(dp(context, 100)); // گوشه‌های کاملاً گرد
      // ارتفاع و عرض
      shape.setSize(0, dp(context, 36)); // 24dp ارتفاع (می‌تونی کمتر کنی)

      return shape;
    }

    private static int dp(Context context, int dp) {
      return (int)
          TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
  }
}
