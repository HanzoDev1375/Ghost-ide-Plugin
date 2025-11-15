package ir.ninjacoder.plloader.editorrgb;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.animation.ArgbEvaluator;
import com.google.android.material.tabs.TabLayout;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import java.lang.reflect.Field;

public class EditorRgbColor implements PluginManagerCompat {
  CodeEditor editor;
  CodeEditorActivity ac;
  TabLayout tabLayout;

  @Override
  public void getEditor(CodeEditor editor) {
    this.editor = editor;
    try {
      startColorAnimation();
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  @Override
  public String setName() {
    return getClass().getName();
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {
    this.ac = ac;
    try {
      Field field = ac.getClass().getDeclaredField("tablayouteditor");
      field.setAccessible(true);
      tabLayout = (TabLayout) field.get(ac);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String langModel() {
    return ".html,.js,.kt,.java,.dart,.css,.c,.cpp";
  }

  private void startColorAnimation() throws Exception {
    final int[] targetColors = {
      // Java colors
      EditorColorScheme.javakeyword,
      EditorColorScheme.javaoprator,
      EditorColorScheme.javafun,
      EditorColorScheme.javafield,
      EditorColorScheme.javatype,
      EditorColorScheme.javaparament,
      EditorColorScheme.javanumber,
      EditorColorScheme.javastring,
      EditorColorScheme.javakeywordoprator,

      // HTML colors
      EditorColorScheme.htmltag,
      EditorColorScheme.htmlattr,
      EditorColorScheme.htmlattrname,
      EditorColorScheme.htmlstr,
      EditorColorScheme.htmlsymbol,
      EditorColorScheme.htmlblockhash,
      EditorColorScheme.htmlblocknormal,

      // JavaScript colors
      EditorColorScheme.jskeyword,
      EditorColorScheme.jsfun,
      EditorColorScheme.jsoprator,
      EditorColorScheme.jsattr,
      EditorColorScheme.jsstring,

      // Python colors
      EditorColorScheme.pystring,
      EditorColorScheme.pykeyword,
      EditorColorScheme.pysymbol,
      EditorColorScheme.pynumber,
      EditorColorScheme.pycolormatch1,
      EditorColorScheme.pycolormatch2,
      EditorColorScheme.pycolormatch3,
      EditorColorScheme.pycolormatch4,

      // PHP colors
      EditorColorScheme.phpkeyword,
      EditorColorScheme.phpattr,
      EditorColorScheme.phpsymbol,
      EditorColorScheme.phphtmlattr,
      EditorColorScheme.phphtmlkeyword,
      EditorColorScheme.phpcolormatch1,
      EditorColorScheme.phpcolormatch2,
      EditorColorScheme.phpcolormatch3,
      EditorColorScheme.phpcolormatch4,
      EditorColorScheme.phpcolormatch5,
      EditorColorScheme.phpcolormatch6,

      // TypeScript colors
      EditorColorScheme.tskeyword,
      EditorColorScheme.tsattr,
      EditorColorScheme.tssymbols,
      EditorColorScheme.tscolormatch1,
      EditorColorScheme.tscolormatch2,
      EditorColorScheme.tscolormatch3,
      EditorColorScheme.tscolormatch4,
      EditorColorScheme.tscolormatch5,
      EditorColorScheme.tscolormatch6,
      EditorColorScheme.tscolormatch7,

      // Bracket colors
      EditorColorScheme.breaklevel1,
      EditorColorScheme.breaklevel2,
      EditorColorScheme.breaklevel3,
      EditorColorScheme.breaklevel4,
      EditorColorScheme.breaklevel5,
      EditorColorScheme.breaklevel6,
      EditorColorScheme.breaklevel7,
      EditorColorScheme.breaklevel8,
      EditorColorScheme.LINE_NUMBER,

      // Other colors
      EditorColorScheme.wars,
      EditorColorScheme.FOREGROUND,
      EditorColorScheme.TEXTCOLORINIER,
      EditorColorScheme.TEXTCOLORHDER,
      EditorColorScheme.TEXTCOLORFORGRAND,
      EditorColorScheme.TEXTCOLORIGOR,
      EditorColorScheme.LITERAL,
      EditorColorScheme.OPERATOR,
      EditorColorScheme.ANNOTATION,
      EditorColorScheme.KEYWORD,
      EditorColorScheme.COMMENT,
      EditorColorScheme.IDENTIFIER_VAR,
      EditorColorScheme.IDENTIFIER_NAME,
      EditorColorScheme.FUNCTION_NAME,
      EditorColorScheme.ATTRIBUTE_NAME,
      EditorColorScheme.ATTRIBUTE_VALUE,
      EditorColorScheme.HTML_TAG
    };

    Handler handler = new Handler();
    ArgbEvaluator evaluator = new ArgbEvaluator();

    final int[] colorPalette = {
      Color.parseColor("#FF0BFCEC"),
      Color.parseColor("#FFFF6B6B"),
      Color.parseColor("#FF4ECDC4"),
      Color.parseColor("#FF45B7D1"),
      Color.parseColor("#FF96CEB4"),
      Color.parseColor("#FFFDEA73"),
      Color.parseColor("#FFFF9F68"),
      Color.parseColor("#FFA594F9"),
      Color.parseColor("#FF6AEB6F"),
      Color.parseColor("#FFFF87FB"),
      Color.parseColor("#FF5DE2E7"),
      Color.parseColor("#FFFFD166"),
      Color.parseColor("#FFEF7C8E"),
      Color.parseColor("#FFB0FC38"),
      Color.parseColor("#FF7B68EE"),
      Color.parseColor("#FFFFA07A"),
      Color.parseColor("#FF20B2AA"),
      Color.parseColor("#FFFF69B4"),
      Color.parseColor("#FF7FFFD4"),
      Color.parseColor("#FFFFD700"),
      Color.parseColor("#FFDA70D6"),
      Color.parseColor("#FF98FB98"),
      Color.parseColor("#FFFF6347"),
      Color.parseColor("#FF40E0D0"),
      Color.parseColor("#FFFF1493"),
      Color.parseColor("#FF00FF7F"),
      Color.parseColor("#FFFF4500"),
      Color.parseColor("#FF9370DB"),
      Color.parseColor("#FF32CD32"),
      Color.parseColor("#FFFF00FF")
    };

    final int[] currentIndex = {0};

    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            int endColor = colorPalette[currentIndex[0]];
            int startColor =
                colorPalette[(currentIndex[0] - 1 + colorPalette.length) % colorPalette.length];

            ValueAnimator animator = ValueAnimator.ofObject(evaluator, startColor, endColor);
            animator.setDuration(1000);
            animator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                  @Override
                  public void onAnimationUpdate(ValueAnimator animator) {
                    int color = (int) animator.getAnimatedValue();

                    // تغییر رنگ‌های ادیتور
                    for (int colorType : targetColors) {
                      editor.getColorScheme().setColor(colorType, color);
                    }

                    // تغییر رنگ تب‌لیاوت (اگر وجود دارد)
                    if (tabLayout != null) {
                      tabLayout.setTabTextColors(ColorStateList.valueOf(color));
                      tabLayout.setSelectedTabIndicatorColor(color);
                    }
                  }
                });
            animator.start();

            currentIndex[0] = (currentIndex[0] + 1) % colorPalette.length;
            handler.postDelayed(this, 1000);
          }
        };
    handler.post(runnable);
  }
}
