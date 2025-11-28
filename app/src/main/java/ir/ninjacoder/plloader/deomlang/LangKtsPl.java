package ir.ninjacoder.plloader.deomlang;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.animation.ArgbEvaluator;
import android.graphics.Color;
import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.plloader.ObjectFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.google.android.material.tabs.TabLayout;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.ghostide.core.utils.DataUtil;
import ir.ninjacoder.prograsssheet.listchild.Child;
import ir.ninjacoder.prograsssheet.listchild.ChildIconEditorManager;

public class LangKtsPl implements PluginManagerCompat {
  CodeEditor editor;
  CodeEditorActivity codeEditorActivity;
  private TabLayout tabLayout;
  private boolean isKtsFile = false;
  private ChildIconEditorManager currentIconEditor;

  @Override
  public void getEditor(CodeEditor editor) {
    this.editor = editor;
  }

  @Override
  public String setName() {
    return getClass().getSimpleName();
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity ac) {
    Child child =
        new Child(
            langModel(), "/storage/emulated/0/GhostWebIDE/plugins/ktslang/ic_material_kotlin.png");
    ac.addChild(child);

    var fab = ac.getFab();
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
                    if (fab != null) {
                      fab.setIconTint(ColorStateList.valueOf(Color.BLACK));
                      fab.setElevation(0f);
                      fab.invalidate();
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

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {
    this.codeEditorActivity = ac;
    setupTabChangeListener();
  }

  private void setupTabChangeListener() {
    try {
      if (codeEditorActivity == null) return;

      Field field = codeEditorActivity.getClass().getDeclaredField("tablayouteditor");
      field.setAccessible(true);
      tabLayout = (TabLayout) field.get(codeEditorActivity);

      if (tabLayout != null) {
        Log.d("LangKtsPl", "TabLayout listener setup successfully");

        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
              @Override
              public void onTabSelected(TabLayout.Tab tab) {
                Log.d("LangKtsPl", "Tab selected: " + tab.getPosition());

                new Handler()
                    .postDelayed(
                        () -> {
                          updateFileType();
                        },
                        200);
              }

              @Override
              public void onTabUnselected(TabLayout.Tab tab) {}

              @Override
              public void onTabReselected(TabLayout.Tab tab) {
                Log.d("LangKtsPl", "Tab reselected: " + tab.getPosition());
                updateFileType();
              }
            });
        updateFileType();
      } else {
        Log.e("LangKtsPl", "TabLayout is null!");
      }
    } catch (Exception e) {
      Log.e("LangKtsPl", "Error setting up tab listener: " + e.getMessage());
    }
  }

  private void updateFileType() {
    try {
      if (codeEditorActivity == null) return;

      boolean wasKtsFile = isKtsFile;
      isKtsFile = ObjectFile.getType(codeEditorActivity, "kts");
      if (isKtsFile && editor != null) {
        editor.post(
            () -> {
              try {
                editor.setEditorLanguage(new LangKts(editor));
                editor.analyze(true);
                Log.d("LangKtsPl", "Kotlin language applied successfully");
              } catch (Exception e) {
                Log.e("LangKtsPl", "Error applying Kotlin language", e);
              }
            });
      }

      if (wasKtsFile != isKtsFile) {
        updateIconVisibility();
      }

    } catch (Exception e) {
      Log.e("LangKtsPl", "Error updating file type", e);
      isKtsFile = false;
    }
  }

  private void updateIconVisibility() {
    if (codeEditorActivity == null) return;

    var list = codeEditorActivity.getChildIconEditorManager();

    // پاک کردن و اضافه کردن
    for (int i = list.size() - 1; i >= 0; i--) {
      if (list.get(i).getIconFile().contains("ktslang")) {
        list.remove(i);
      }
    }

    if (isKtsFile) {
      var iconEditor =
          new ChildIconEditorManager(
              "/storage/emulated/0/GhostWebIDE/plugins/ktslang/ic_material_kotlin.png",
              (v, pos, id, isusing) -> {
                DataUtil.showMessage(v.getContext(), "Kotlin Script Tool کلیک شد!");
                if (editor != null) {
                  editor
                      .getText()
                      .insert(
                          editor.getCursor().getLeftLine(),
                          editor.getCursor().getLeftColumn(),
                          "// Kotlin Script Code\n");
                }
              });
      list.add(iconEditor);
    }

    //  forceRefreshRecycler();
  }

  //
  //  private void forceRefreshRecycler() {
  //    try {
  //      Field field = codeEditorActivity.getClass().getDeclaredField("rvmenueditor");
  //      field.setAccessible(true);
  //      RecyclerView recyclerView = (RecyclerView) field.get(codeEditorActivity);
  //
  //      if (recyclerView != null) {
  //        recyclerView.post(
  //            () -> {
  //              recyclerView.getAdapter().notifyDataSetChanged();
  //              recyclerView.invalidate();
  //              recyclerView.requestLayout();
  //            });
  //      }
  //    } catch (Exception e) {
  //    }
  //  }

  @Override
  public String langModel() {
    return ".kts";
  }

  // متد برای ریفرش دستی
  public void forceRefresh() {
    if (codeEditorActivity != null) {
      Log.d("LangKtsPl", "Force refreshing...");
      updateFileType();
      updateIconVisibility();
    }
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
