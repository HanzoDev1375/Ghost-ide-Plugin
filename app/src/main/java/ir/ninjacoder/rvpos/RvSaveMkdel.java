package ir.ninjacoder.rvpos;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RvSaveMkdel implements DefaultLifecycleObserver {

  private RecyclerView rv;
  private SharedPreferences sh;
  private Context context;
  private static final String SCROLL_POSITION_KEY = "recycler_scroll_position";
  private static final String SCROLL_OFFSET_KEY = "recycler_scroll_offset";

  public RvSaveMkdel(RecyclerView rv, Context context) {
    this.rv = rv;
    this.context = context;
    sh = context.getSharedPreferences("rv_scroll_prefs", Context.MODE_PRIVATE);
  }

  @Override
  public void onResume(LifecycleOwner owner) {
    // بازیابی موقعیت اسکرول
    restoreScrollPosition();
  }

  @Override
  public void onPause(LifecycleOwner owner) {
    // ذخیره موقعیت اسکرول
    saveScrollPosition();
  }

  @Override
  public void onStop(LifecycleOwner owner) {
    // ذخیره موقعیت اسکرول در onStop هم برای اطمینان بیشتر
    saveScrollPosition();
  }

  @Override
  public void onCreate(LifecycleOwner arg0) {
    // کد مورد نیاز در onCreate
  }

  @Override
  public void onDestroy(LifecycleOwner owner) {
    // ذخیره موقعیت اسکرول در onDestroy
    saveScrollPosition();
  }

  /** ذخیره موقعیت اسکرول RecyclerView */
  private void saveScrollPosition() {
    if (rv == null || rv.getLayoutManager() == null) return;

    if (rv.getLayoutManager() instanceof GridLayoutManager) {
      GridLayoutManager layoutManager = (GridLayoutManager) rv.getLayoutManager();

      // موقعیت اولین آیتم قابل مشاهده
      int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

      if (firstVisibleItemPosition != RecyclerView.NO_POSITION) {
        // گرفتن view اولین آیتم قابل مشاهده
        View firstVisibleView = layoutManager.findViewByPosition(firstVisibleItemPosition);

        if (firstVisibleView != null) {
          // محاسبه آفست (فاصله از بالای RecyclerView)
          int offset = firstVisibleView.getTop();

          // ذخیره در SharedPreferences
          SharedPreferences.Editor editor = sh.edit();
          editor.putInt(SCROLL_POSITION_KEY, firstVisibleItemPosition);
          editor.putInt(SCROLL_OFFSET_KEY, offset);
          editor.apply();
        }
      }
    }
  }

  private void restoreScrollPosition() {
    if (rv == null || rv.getLayoutManager() == null) return;

    int savedPosition = sh.getInt(SCROLL_POSITION_KEY, 0);
    int savedOffset = sh.getInt(SCROLL_OFFSET_KEY, 0);

    if (savedPosition >= 0 && rv.getLayoutManager() instanceof LinearLayoutManager) {
      rv.postDelayed(
          () -> {
            GridLayoutManager layoutManager = (GridLayoutManager) rv.getLayoutManager();
            if (layoutManager != null && savedPosition < layoutManager.getItemCount()) {
              layoutManager.scrollToPositionWithOffset(savedPosition, savedOffset);
            }
          },
          10);
    }
  }
}
