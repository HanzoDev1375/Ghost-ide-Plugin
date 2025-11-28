package ir.ninjacoder.plloader.liveserver;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.transition.Transition;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.widget.PopupWindow;
import android.graphics.Point;
import android.content.Context;
import android.webkit.WebView;
import android.view.View;
import android.widget.FrameLayout;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.webkit.WebChromeClient;
import android.view.*;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.utils.AnimUtils;
import ir.ninjacoder.ghostide.core.utils.DataUtil;
import ir.ninjacoder.ghostide.core.utils.ObjectUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.File;

import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.prograsssheet.listchild.ChildIconEditorManager;

public class LiveServerPl implements PluginManagerCompat {
  CodeEditor editor;
  CodeEditorActivity codeEditorActivity;
  private TabLayout tabLayout;
  private boolean isHtmlFile = false;
  private LiveServerPopup live;
  int pos = -1;

  @Override
  public void getEditor(CodeEditor editor) {
    this.editor = editor;
  }

  @Override
  public String setName() {
    return "liveserver";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity aceditor) {
    this.codeEditorActivity = aceditor;
    setupTabChangeListener();
  }

  private void setupTabChangeListener() {
    try {
      if (codeEditorActivity == null) return;

      Field field = codeEditorActivity.getClass().getDeclaredField("tablayouteditor");
      field.setAccessible(true);
      tabLayout = (TabLayout) field.get(codeEditorActivity);

      if (tabLayout != null) {
        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
              @Override
              public void onTabSelected(TabLayout.Tab tab) {
                // تاخیر برای اطمینان از لود کامل تب جدید
                new Handler()
                    .postDelayed(
                        () -> {
                          updateFileType();
                          updateIconVisibility();
                        },
                        100);
              }

              @Override
              public void onTabUnselected(TabLayout.Tab tab) {}

              @Override
              public void onTabReselected(TabLayout.Tab tab) {}
            });
        updateFileType();
        updateIconVisibility();
      }
    } catch (Exception err) {
      Log.e("LiveServer", "Error setting up tab listener", err);
    }
  }

  private void updateFileType() {
    try {
      if (codeEditorActivity == null) return;
      Method method = codeEditorActivity.getClass().getMethod("getcurrentFileType");
      String fileType = (String) method.invoke(codeEditorActivity);

      boolean wasHtmlFile = isHtmlFile;
      isHtmlFile = fileType != null && (fileType.endsWith(".html"));

      if (wasHtmlFile != isHtmlFile) {
        updateIconVisibility();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateIconVisibility() {
    if (codeEditorActivity == null) return;

    var child = codeEditorActivity.getChildIconEditorManager();

    if (isHtmlFile) {
      boolean alreadyExists = false;
      for (int i = 0; i < child.size(); i++) {
        if (child.get(i).getIconFile().contains("liveserve")) {
          alreadyExists = true;
          break;
        }
      }

      if (!alreadyExists) {
        ChildIconEditorManager ed =
            new ChildIconEditorManager(
                "/storage/emulated/0/GhostWebIDE/plugins/liveserve/image.png",
                (v, pos, id, using) -> {
                  this.pos = pos;
                  String filePath = getCurrentFilePath();
                  Log.d("LiveServer", "Current file path: " + filePath);

                  if (filePath != null && filePath.endsWith(".html")) {
                    live = new LiveServerPopup(v.getContext(), filePath);
                    live.showAtLocation(v, Gravity.TOP, 0, 0);
                  } else {
                    String message = "open html file" + (filePath != null ? filePath : "null");
                    DataUtil.showMessage(v.getContext(), message);
                  }
                });
        child.add(ed);
      }
    } else {
      // اگر HTML نیست، آیکون LiveServer رو حذف کن
      for (int i = 0; i < child.size(); i++) {
        if (child.get(i).getIconFile().contains("liveserve")) {
          child.remove(i);
          live.dismiss();
          break;
        }
      }
    }
  }

  private String getCurrentFilePath() {
    return codeEditorActivity.getPathBytab();
  }

  @Override
  public String langModel() {
    return ".html";
  }

  class LiveServerPopup extends PopupWindow {

    private static int MIN_WIDTH_DP = 200;
    private static int MIN_HEIGHT_DP = 140;
    private static int RESIZE_HANDLE_SIZE_DP = 48;

    private final Point initialTouch = new Point();
    private final Point initialSize = new Point();
    private final Point initialPosition = new Point();
    private String path;
    private WebView webView;
    private EditText localbar;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private boolean isAutoRefreshEnabled = true;

    public LiveServerPopup(Context context, String path) {
      super(context);
      this.path = path;
      setupPopup(context);
    }

    private void startAutoRefresh() {

      editor.subscribeEvent(
          ContentChangeEvent.class,
          (ev, un) -> {
            if (webView != null) {
              webView.post(webView::reload);
            }
          });
    }

    private void setupPopup(Context context) {

      MaterialCardView roots = new MaterialCardView(context);
      roots.setId(View.generateViewId());
      roots.setLayoutParams(
          new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 250)));
      roots.setCardElevation(dpToPx(context, 2));
      roots.setRadius(dpToPx(context, 20));
      startAutoRefresh();

      LinearLayout linear1 = new LinearLayout(context);
      linear1.setId(View.generateViewId());
      linear1.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
      linear1.setPadding(
          dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8));
      linear1.setOrientation(LinearLayout.VERTICAL);

      LinearLayout linear2 = new LinearLayout(context);
      linear2.setId(View.generateViewId());
      linear2.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      linear2.setPadding(
          dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8));
      linear2.setOrientation(LinearLayout.HORIZONTAL);

      ImageView refresh = new ImageView(context);
      refresh.setId(View.generateViewId());
      refresh.setFocusable(false);
      LinearLayout.LayoutParams refreshParams =
          new LinearLayout.LayoutParams(dpToPx(context, 30), dpToPx(context, 30));
      refresh.setLayoutParams(refreshParams);

      refresh.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      localbar = new EditText(context);
      localbar.setId(View.generateViewId());
      localbar.setFocusable(false);
      LinearLayout.LayoutParams localbarParams =
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
      localbarParams.setMargins(dpToPx(context, 8), 0, dpToPx(context, 8), 0);
      localbar.setLayoutParams(localbarParams);
      localbar.setPadding(
          dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8));
      localbar.setTextSize(14);
      localbar.setHint("128.100.100");
      localbar.setBackgroundColor(Color.WHITE);

      String loadUrl = convertFilePathToUrl(path);
      localbar.setText(loadUrl);

      ImageView close = new ImageView(context);
      close.setId(View.generateViewId());
      close.setFocusable(false);
      LinearLayout.LayoutParams closeParams =
          new LinearLayout.LayoutParams(dpToPx(context, 30), dpToPx(context, 30));
      close.setLayoutParams(closeParams);
      close.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      linear2.addView(refresh);
      linear2.addView(localbar);
      linear2.addView(close);

      webView = createWebView(context);

      linear1.addView(linear2);
      linear1.addView(webView);
      GradientDrawable d = new GradientDrawable();
      d.setColor(ColorStateList.valueOf(MaterialColors.getColor(localbar, ObjectUtils.Back)));
      d.setCornerRadius(20);
      localbar.setBackground(d);
      localbar.setSingleLine(true);
      localbar.setEllipsize(TextUtils.TruncateAt.END);
      localbar.setMarqueeRepeatLimit(-1);
      roots.addView(linear1);
      Glide.with(close.getContext())
          .load("/storage/emulated/0/GhostWebIDE/plugins/liveserve/data/close.png")
          .fitCenter()
          .override(100, 100)
          .into(close);
      Glide.with(refresh.getContext())
          .load("/storage/emulated/0/GhostWebIDE/plugins/liveserve/data/refresh.png")
          .fitCenter()
          .override(100, 100)
          .into(refresh);

      View resizeHandle = createResizeHandle(context);
      roots.addView(resizeHandle);

      setContentView(roots);
      setWidth(dpToPx(context, 400));
      setHeight(dpToPx(context, 300));
      setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      setClippingEnabled(false);
      setTouchable(true);
      setOutsideTouchable(false);
      setFocusable(false);
      Transition enter = new MaterialSharedAxis(MaterialSharedAxis.Z, true);
      enter.setDuration(200);
      setEnterTransition(enter);
      Transition exit = new MaterialSharedAxis(MaterialSharedAxis.Z, false);
      exit.setDuration(200);
      setExitTransition(exit);
      setupButtonListeners(refresh, close, context);

      setupDragAndResizeLogic(context, linear2, resizeHandle);
    }

    private void setupDragAndResizeLogic(Context context, View dragHandle, View resizeHandle) {

      dragHandle.setOnTouchListener(
          new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
              return handleDragEvent(event);
            }
          });

      webView.setOnTouchListener(
          new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
              return handleDragEvent(event);
            }
          });

      resizeHandle.setOnTouchListener(
          new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
              return handleResizeEvent(context, event);
            }
          });
    }

    private boolean handleDragEvent(MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          isDragging = true;
          initialTouch.set((int) event.getRawX(), (int) event.getRawY());

          int[] location = new int[2];
          getContentView().getLocationOnScreen(location);
          initialPosition.set(location[0], location[1]);
          return true;

        case MotionEvent.ACTION_MOVE:
          if (isDragging) {
            int dx = (int) (event.getRawX() - initialTouch.x);
            int dy = (int) (event.getRawY() - initialTouch.y);

            int newX = initialPosition.x + dx;
            int newY = initialPosition.y + dy;

            update(newX, newY, getWidth(), getHeight());
          }
          return true;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
          isDragging = false;
          return true;
      }
      return false;
    }

    private boolean handleResizeEvent(Context context, MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          isResizing = true;
          initialTouch.set((int) event.getRawX(), (int) event.getRawY());
          initialSize.set(getWidth(), getHeight());

          int[] location = new int[2];
          getContentView().getLocationOnScreen(location);
          initialPosition.set(location[0], location[1]);
          return true;

        case MotionEvent.ACTION_MOVE:
          if (isResizing) {
            int dx = (int) (event.getRawX() - initialTouch.x);
            int dy = (int) (event.getRawY() - initialTouch.y);

            int newWidth = Math.max(dpToPx(context, MIN_WIDTH_DP), initialSize.x + dx);
            int newHeight = Math.max(dpToPx(context, MIN_HEIGHT_DP), initialSize.y + dy);

            update(initialPosition.x, initialPosition.y, newWidth, newHeight);
          }
          return true;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
          isResizing = false;
          return true;
      }
      return false;
    }

    private WebView createWebView(Context context) {
      webView = new WebView(context);
      webView.setId(View.generateViewId());
      LinearLayout.LayoutParams webViewParams =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
      webView.setLayoutParams(webViewParams);

      var settings = webView.getSettings();
      settings.setJavaScriptEnabled(true);
      settings.setDomStorageEnabled(true);
      settings.setLoadWithOverviewMode(true);
      settings.setUseWideViewPort(true);
      settings.setAllowFileAccess(true);
      settings.setAllowContentAccess(true);
      settings.setMediaPlaybackRequiresUserGesture(false);

      webView.setWebChromeClient(new WebChromeClient());
      String loadUrl = convertFilePathToUrl(path);
      Log.d("LiveServer", "Final URL: " + loadUrl);

      if (loadUrl != null && !loadUrl.equals("about:blank")) {
        webView.loadUrl(loadUrl);
      } else {
        webView.loadData(
            "<html><body><h1>HTML file not found</h1></body></html>", "text/html", "UTF-8");
      }

      return webView;
    }

    private void setupButtonListeners(ImageView refresh, ImageView close, Context context) {

      refresh.setOnClickListener(
          v -> {
            if (webView != null) {
              webView.reload();
              refresh.animate().rotationBy(360).setDuration(500).start();
            }
          });
      refresh.setColorFilter(
          MaterialColors.getColor(refresh, ObjectUtils.colorPrimary), PorterDuff.Mode.SRC_IN);
      close.setColorFilter(
          MaterialColors.getColor(refresh, ObjectUtils.colorPrimary), PorterDuff.Mode.SRC_IN);
      close.setOnClickListener(
          v -> {
            dismiss();
          });

      localbar.setOnClickListener(
          v -> {
            localbar.setFocusableInTouchMode(true);
            localbar.requestFocus();

            InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(localbar, InputMethodManager.SHOW_IMPLICIT);
          });

      localbar.setOnEditorActionListener(
          (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
              String url = localbar.getText().toString();
              if (url != null && !url.isEmpty()) {
                if (!url.startsWith("http://")
                    && !url.startsWith("https://")
                    && !url.startsWith("file://")) {
                  url = "file://" + url;
                }
                webView.loadUrl(url);

                InputMethodManager imm =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(localbar.getWindowToken(), 0);
                localbar.clearFocus();
                localbar.setFocusable(false);
              }
              return true;
            }
            return false;
          });
    }

    private String convertFilePathToUrl(String filePath) {
      if (filePath == null) return "about:blank";
      if (filePath.startsWith("file://")) return filePath;

      var file = new File(filePath);
      if (file.exists()) {
        return "file://" + file.getAbsolutePath();
      }
      return "about:blank";
    }

    private View createResizeHandle(Context context) {

      int size = dpToPx(context, RESIZE_HANDLE_SIZE_DP);

      // فریم اصلی هندل
      FrameLayout frame = new FrameLayout(context);
      FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(size, size);
      frameParams.gravity = Gravity.BOTTOM | Gravity.END;
      frameParams.setMargins(0, 0, dpToPx(context, 24), dpToPx(context, 24));
      frame.setLayoutParams(frameParams);

      // آیکون دست (pinch)
      ImageView handle = new ImageView(context);
      FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(65, 65);

      // این خط مهمه → آیکون دقیقا وسط FrameLayout
      iconParams.gravity = Gravity.CENTER;

      handle.setLayoutParams(iconParams);

      Glide.with(handle.getContext())
          .load("/storage/emulated/0/GhostWebIDE/plugins/liveserve/data/pinch.png")
          .error(new ColorDrawable(Color.parseColor("#ff7201")))
          .override(80, 80)
          .centerInside()
          .into(handle);

      // پس زمینه فریم
      GradientDrawable gd = new GradientDrawable();
      gd.setColor(ColorStateList.valueOf(MaterialColors.getColor(handle, ObjectUtils.Back)));
      gd.setCornerRadius(25f);
      frame.setBackground(gd);

      // انیمیشن
      AnimUtils.Sacla(handle);

      // رنگ آیکون
      handle.setColorFilter(
          MaterialColors.getColor(handle, ObjectUtils.colorPrimary), PorterDuff.Mode.SRC_IN);

      frame.addView(handle);

      return frame;
    }

    private int dpToPx(Context context, int dp) {
      return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
