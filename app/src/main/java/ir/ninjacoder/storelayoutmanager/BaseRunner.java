package ir.ninjacoder.storelayoutmanager;

import ir.ninjacoder.ghostide.core.Store.StoreAcitvity;
import ir.ninjacoder.ghostide.core.Store.StoreAdapter;
import ir.ninjacoder.ghostide.core.Store.ThemeFragment;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleEventObserver;

import android.os.Handler;
import android.os.Looper;
import java.lang.reflect.Field;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class BaseRunner implements PluginManagerCompat {
  protected StoreAcitvity store;
  private Handler handler = new Handler(Looper.getMainLooper());
  private static final String TAG = "StoreLayoutManager";
  private static final int MAX_RETRY_COUNT = 8;
  private static final int RETRY_DELAY = 250; // milliseconds
  private static final int INITIAL_DELAY = 800; // milliseconds

  private ViewPager2 viewPager2;
  private StoreAdapter storeAdapter;
  private ThemeFragment themeFragment;

  @Override
  public void getEditor(CodeEditor arg0) {}

  @Override
  public String setName() {
    return "Store Layout Manager Plugin";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getBaseCompat(BaseCompat base) {
    if (base instanceof StoreAcitvity) {
      store = (StoreAcitvity) base;

      Log.d(TAG, "StoreActivity detected, starting setup...");

      // تاخیر اولیه برای اطمینان از لود کامل Activity
      handler.postDelayed(
          new Runnable() {
            @Override
            public void run() {
              initializeSetup();
            }
          },
          INITIAL_DELAY);
    }
  }

  @Override
  public String langModel() {
    return "all";
  }

  private void initializeSetup() {
    try {
      // 1. پیدا کردن ViewPager2
      viewPager2 = getViewPager2FromStoreActivity();

      if (viewPager2 == null) {
        Log.e(TAG, "ViewPager2 not found!");
        return;
      }

      Log.d(TAG, "ViewPager2 found successfully");

      // 2. پیدا کردن StoreAdapter
      storeAdapter = (StoreAdapter) viewPager2.getAdapter();

      if (storeAdapter == null) {
        Log.e(TAG, "StoreAdapter is null!");
        return;
      }

      // 3. اضافه کردن listener برای تغییر صفحات
      setupPageChangeListener();

      // 4. اگر الان در صفحه Theme هستیم، مستقیما setup کنیم
      if (viewPager2.getCurrentItem() == 2) {
        Log.d(TAG, "Currently on ThemeFragment page, setting up immediately...");
        setupThemeFragmentWithRetry(0);
      }

    } catch (Exception e) {
      Log.e(TAG, "Error in initializeSetup: " + e.getMessage(), e);
    }
  }

  private void setupPageChangeListener() {
    viewPager2.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            Log.d(TAG, "Page changed to: " + position);

            if (position == 2) { // ThemeFragment position
              Log.d(TAG, "ThemeFragment page selected, setting up layout manager...");

              // تاخیر کوچک برای اطمینان از لود کامل fragment
              handler.postDelayed(
                  () -> {
                    setupThemeFragmentWithRetry(0);
                  },
                  100);
            }
          }
        });
  }

  private void setupThemeFragmentWithRetry(int retryCount) {
    if (retryCount >= MAX_RETRY_COUNT) {
      Log.e(TAG, "Failed to setup ThemeFragment after " + MAX_RETRY_COUNT + " retries");
      return;
    }

    try {
      Log.d(TAG, "Setup attempt " + (retryCount + 1) + " for ThemeFragment...");

      // 1. پیدا کردن ThemeFragment از طریق adapter
      themeFragment = getThemeFragmentFromAdapter();

      if (themeFragment == null) {
        Log.d(TAG, "ThemeFragment not found in adapter, retrying...");
        handler.postDelayed(() -> setupThemeFragmentWithRetry(retryCount + 1), RETRY_DELAY);
        return;
      }

      // 2. بررسی اینکه fragment در وضعیت مناسبی باشد
      if (!themeFragment.isAdded() || themeFragment.getView() == null) {
        Log.d(
            TAG,
            "ThemeFragment not ready (isAdded="
                + themeFragment.isAdded()
                + ", hasView="
                + (themeFragment.getView() != null)
                + "), retrying...");

        // اضافه کردن observer برای lifecycle
        themeFragment
            .getLifecycle()
            .addObserver(
                new LifecycleEventObserver() {
                  @Override
                  public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_CREATE) {
                      Log.d(TAG, "ThemeFragment resumed, trying to setup...");
                      handler.postDelayed(() -> setupThemeFragmentWithRetry(retryCount + 1), 100);
                    }
                  }
                });

        handler.postDelayed(() -> setupThemeFragmentWithRetry(retryCount + 1), RETRY_DELAY);
        return;
      }

      Log.d(TAG, "ThemeFragment is ready, getting RecyclerView...");

      // 3. پیدا کردن RecyclerView از ThemeFragment
      RecyclerView recyclerView = getRecyclerViewFromThemeFragment();

      if (recyclerView == null) {
        Log.d(TAG, "RecyclerView not found in ThemeFragment, retrying...");
        handler.postDelayed(() -> setupThemeFragmentWithRetry(retryCount + 1), RETRY_DELAY);
        return;
      }

      Log.d(TAG, "RecyclerView found successfully!");

      // 4. اعمال LayoutManager دلخواه
      applyCustomLayoutManager(recyclerView);

    } catch (Exception e) {
      Log.e(TAG, "Error in setupThemeFragmentWithRetry: " + e.getMessage(), e);
      handler.postDelayed(() -> setupThemeFragmentWithRetry(retryCount + 1), RETRY_DELAY);
    }
  }

  private ThemeFragment getThemeFragmentFromAdapter() {
    try {
      if (storeAdapter == null || storeAdapter.getList() == null) {
        return null;
      }

      List<Fragment> fragments = storeAdapter.getList();

      if (fragments.size() > 2) {
        Fragment fragment = fragments.get(2);
        if (fragment instanceof ThemeFragment && fragment.isAdded()) {
          return (ThemeFragment) fragment;
        }
      }

      // اگر fragment در position 2 نبود، در کل لیست جستجو کنیم
      for (Fragment fragment : fragments) {
        if (fragment instanceof ThemeFragment && fragment.isAdded()) {
          return (ThemeFragment) fragment;
        }
      }

    } catch (Exception e) {
      Log.e(TAG, "Error getting ThemeFragment from adapter: " + e.getMessage());
    }

    return null;
  }

  private RecyclerView getRecyclerViewFromThemeFragment() {
    try {
      if (themeFragment == null || themeFragment.getView() == null) {
        return null;
      }

      // روش مستقیم: دسترسی به فیلد bind.rv از ThemeFragment
      Field bindField = ThemeFragment.class.getDeclaredField("bind");
      bindField.setAccessible(true);
      Object bindObject = bindField.get(themeFragment);

      if (bindObject != null) {
        Class<?> bindingClass = bindObject.getClass();

        // جستجوی فیلد rv در binding
        Field rvField = bindingClass.getDeclaredField("rv");
        rvField.setAccessible(true);

        Object rvObject = rvField.get(bindObject);
        if (rvObject instanceof RecyclerView) {
          return (RecyclerView) rvObject;
        }
      }

    } catch (NoSuchFieldException e) {
      Log.d(TAG, "Field not found in ThemeFragment, trying alternative method...");

      // روش جایگزین: جستجوی سلسله مراتبی در view
      return findRecyclerViewRecursive(themeFragment.getView());

    } catch (Exception e) {
      Log.e(TAG, "Error getting RecyclerView from ThemeFragment: " + e.getMessage(), e);
    }

    return null;
  }

  private RecyclerView findRecyclerViewRecursive(View view) {
    if (view instanceof RecyclerView) {
      return (RecyclerView) view;
    }

    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) {
        RecyclerView found = findRecyclerViewRecursive(group.getChildAt(i));
        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }

  private ViewPager2 getViewPager2FromStoreActivity() {
    try {
      // روش مستقیم: دسترسی به فیلد bind.viewpagerstore
      Field bindField = StoreAcitvity.class.getDeclaredField("bind");
      bindField.setAccessible(true);
      Object binding = bindField.get(store);

      if (binding != null) {
        Class<?> bindingClass = binding.getClass();

        // جستجوی فیلد viewpagerstore در binding
        Field viewPagerField = bindingClass.getDeclaredField("viewpagerstore");
        viewPagerField.setAccessible(true);

        Object viewPager = viewPagerField.get(binding);
        if (viewPager instanceof ViewPager2) {
          return (ViewPager2) viewPager;
        }
      }

    } catch (NoSuchFieldException e) {
      Log.d(TAG, "Using fallback method to find ViewPager2...");

      // روش جایگزین: جستجوی سلسله مراتبی
      return findViewPager2Recursive(store.getWindow().getDecorView());

    } catch (Exception e) {
      Log.e(TAG, "Error getting ViewPager2: " + e.getMessage(), e);
    }

    return null;
  }

  private ViewPager2 findViewPager2Recursive(View view) {
    if (view instanceof ViewPager2) {
      return (ViewPager2) view;
    }

    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) {
        ViewPager2 found = findViewPager2Recursive(group.getChildAt(i));
        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }

  private void applyCustomLayoutManager(RecyclerView recyclerView) {
    try {
      Log.i(TAG, "Applying SkidRightLayoutManager to RecyclerView...");

      // بررسی LayoutManager فعلی
      RecyclerView.LayoutManager currentLayoutManager = recyclerView.getLayoutManager();
      if (currentLayoutManager != null) {
        Log.d(TAG, "Current layout manager: " + currentLayoutManager.getClass().getSimpleName());
      }

      // ایجاد و تنظیم LayoutManager ج
      recyclerView.setPadding(20, 20, 20, 20);
       recyclerView.setLayoutManager(new CircleLayoutManager(recyclerView.getContext(),true));

      // اگر adapter وجود دارد، notifyDataSetChanged بزنیم
      if (recyclerView.getAdapter() != null) {
        recyclerView.getAdapter().notifyDataSetChanged();
      }

      // درخواست layout مجدد
      recyclerView.post(
          () -> {
            recyclerView.requestLayout();
            recyclerView.invalidate();
          });

      Log.i(TAG, "Custom layout manager applied successfully!");

      // لاگ اطلاعات اضافی برای debug
      Log.d(
          TAG,
          "RecyclerView info: "
              + "Width="
              + recyclerView.getWidth()
              + ", Height="
              + recyclerView.getHeight()
              + ", ChildCount="
              + recyclerView.getChildCount());

    } catch (Exception e) {
      Log.e(TAG, "Error applying custom layout manager: " + e.getMessage(), e);
    }
  }
}
