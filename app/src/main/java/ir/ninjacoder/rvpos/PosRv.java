package ir.ninjacoder.rvpos;

import android.os.Handler;
import android.view.View;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

public class PosRv implements PluginManagerCompat {

  @Override
  public void getEditor(CodeEditor arg0) {}

  @Override
  public String setName() {
    return "RV Position Saver";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity activity) {
    if (activity == null) return;

    // کمی تأخیر بده تا layout کامل لود شه
    new Handler()
        .postDelayed(
            () -> {
              try {
                // پیدا کردن RecyclerView در layout اصلی
                ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();
                RecyclerView recyclerView = findRecyclerView(rootView);

                if (recyclerView != null) {
                  // حالا RvSaveMkdel رو راه بنداز
                  RvSaveMkdel saver = new RvSaveMkdel(recyclerView, activity);
                  activity.getLifecycle().addObserver(saver);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            },
            200); // 200ms تأخیر
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getBaseCompat(BaseCompat arg0) {}

  @Override
  public String langModel() {
    return "all";
  }
  private RecyclerView findRecyclerView(ViewGroup parent) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      View child = parent.getChildAt(i);

      if (child instanceof RecyclerView) {
        return (RecyclerView) child;
      }

      if (child instanceof ViewGroup) {
        RecyclerView found = findRecyclerView((ViewGroup) child);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
