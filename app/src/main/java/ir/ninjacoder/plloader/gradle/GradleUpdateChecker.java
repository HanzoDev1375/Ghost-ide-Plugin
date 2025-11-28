package ir.ninjacoder.plloader.gradle;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.color.MaterialColors;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.langs.groovy.lang.GroovyLanguage;
import io.noties.markwon.Markwon;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.utils.ObjectUtils;
import ir.ninjacoder.plloader.EditorPopUp;
import android.os.Handler;
import android.os.Looper;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import java.lang.reflect.Field;
import com.google.android.material.tabs.TabLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleUpdateChecker implements PluginManagerCompat {

  private CodeEditor currentEditor;
  private CodeEditorActivity codeEditorActivity;
  private TabLayout tabLayout;
  private boolean isChecking = false, isgradleFile;
  private Handler handler = new Handler(Looper.getMainLooper());
  private Runnable checkRunnable;

  private static final Pattern DEPENDENCY_PATTERN =
      Pattern.compile(
          "(implementation|api|compile|runtimeOnly|annotationProcessor)\\s*(?:\\(?['\"]([^:]+):([^:]+):([^)'\"]+)['\"]\\)?)");

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {
    codeEditorActivity = arg0;
  }

  @Override
  public void getEditor(CodeEditor editor) {
    currentEditor = editor;
    if (editor == null) return;

    editor.postDelayed(
        () -> {
          try {
            if (editor.getContext() instanceof CodeEditorActivity) {
              codeEditorActivity = (CodeEditorActivity) editor.getContext();
              setupTabChangeListener();

              String fileType = codeEditorActivity.getcurrentFileType();

              if (fileType != null && fileType.endsWith(".gradle") || fileType.endsWith(".kts")) {
                Toast.makeText(codeEditorActivity, "gradle Plugin Activated!", Toast.LENGTH_SHORT)
                    .show();
                applyCustomLanguage();
                setupEventListeners(editor);
              }
            }
          } catch (Exception e) {
            Log.e("GradleUpdateChecker", "Error: " + e.getMessage());
          }
        },
        1000);
  }

  void applyCustomLanguage() {
    try {
      var lang =
          new GroovyLanguage() {
            @Override
            public CodeAnalyzer getAnalyzer() {
              return new GradleCodeAnalyzer(currentEditor);
            }
          };
      currentEditor.setEditorLanguage(lang);
    } catch (Exception err) {
      Log.e("GradleUpdateChecker", "Error applying custom language: " + err.getMessage());
    }
  }

  private void updateFileType() {
    try {
      if (codeEditorActivity == null) return;

      String fileType = codeEditorActivity.getcurrentFileType();
      isgradleFile = fileType != null && fileType.endsWith(".gradle") || fileType.endsWith(".kts");

    } catch (Exception e) {
      Log.e("GradleUpdateChecker", "Error updating file type: " + e.getMessage());
      isgradleFile = false;
    }
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
                updateFileType();
              }

              @Override
              public void onTabUnselected(TabLayout.Tab tab) {}

              @Override
              public void onTabReselected(TabLayout.Tab tab) {}
            });
        updateFileType();
      }
    } catch (Exception e) {
      Log.e("GradleUpdateChecker", "❌ Error setting up tab listener: " + e.getMessage());
    }
  }

  private void setupEventListeners(CodeEditor editor) {
    editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          if (isChecking || !isgradleFile) return;

          try {
            int cursorLine = editor.getCursor().getLeftLine();
            int cursorColumn = editor.getCursor().getLeftColumn();
            String currentLine = editor.getText().getLineString(cursorLine);

            DependencyInfo dep = getDependencyAtCursor(currentLine, cursorLine, cursorColumn);
            if (dep != null) {
              // حذف درخواست قبلی اگر وجود دارد
              if (checkRunnable != null) {
                handler.removeCallbacks(checkRunnable);
              }
              checkRunnable = () -> checkDependencyUpdate(dep);
              handler.postDelayed(checkRunnable, 300); // تاخیر 300 میلی‌ثانیه
            }

          } catch (Exception e) {

            if (checkRunnable != null) {
              handler.removeCallbacks(checkRunnable);
              checkRunnable = null;
            }
          }
        });
  }

  private DependencyInfo getDependencyAtCursor(String lineText, int lineNumber, int cursorColumn) {
    if (lineText == null || lineText.isEmpty()) return null;

    Matcher matcher = DEPENDENCY_PATTERN.matcher(lineText);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();

      if (cursorColumn >= start && cursorColumn <= end) {
        return new DependencyInfo(
            lineNumber,
            start,
            matcher.group(2),
            matcher.group(3),
            matcher.group(4),
            matcher.group(0));
      }
    }
    return null;
  }

  private void checkDependencyUpdate(DependencyInfo dep) {
    if (isChecking) return;
    isChecking = true;
    RepositoryManager.getLatestVersion(
        dep.group,
        dep.artifact,
        new RepositoryManager.VersionCheckCallback() {
          @Override
          public void onVersionFound(String latestVersion) {
            isChecking = false;
            if (latestVersion != null && !dep.version.equals(latestVersion)) {
              showUpdateDialog(dep, latestVersion);
            } else {
              showUpToDateMessage(dep);
            }
          }

          @Override
          public void onError(String error) {
            isChecking = false;
            Log.e("GradleUpdateChecker", "Error: " + error);
            showTemporaryTooltip(" خطا در بررسی بروزرسانی");
          }
        });
  }

  private void showUpdateDialog(DependencyInfo dep, String newVersion) {
    if (currentEditor == null) return;

    try {
      Markwon markwon = Markwon.create(currentEditor.getContext());

      LinearLayout popupLayout = new LinearLayout(currentEditor.getContext());
      popupLayout.setOrientation(LinearLayout.VERTICAL);
      popupLayout.setPadding(40, 30, 40, 30);
      TextView titleView = new TextView(currentEditor.getContext());
      markwon.setMarkdown(titleView, "## Update Available");
      titleView.setGravity(Gravity.CENTER);
      titleView.setPadding(0, 0, 0, 20);
      TextView infoView = new TextView(currentEditor.getContext());
      infoView.setEllipsize(TextUtils.TruncateAt.END);
      titleView.setEllipsize(TextUtils.TruncateAt.END);
      String markdownText =
          String.format(
              "**%s:%s**\n\n" + " **Current:** `%s`\n\n" + " **New:** `%s`\n\n",
              dep.group, dep.artifact, dep.version, newVersion);
      markwon.setMarkdown(infoView, markdownText);
      infoView.setPadding(0, 10, 0, 20);
      LinearLayout buttonLayout = new LinearLayout(currentEditor.getContext());
      buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
      buttonLayout.setGravity(Gravity.CENTER);

      TextView updateBtn = new TextView(currentEditor.getContext());
      markwon.setMarkdown(updateBtn, "**Update Now**");
      GradientDrawable f = new GradientDrawable();
      f.setColor(Color.GREEN);
      f.setCornerRadius(50);
      updateBtn.setBackground(f);
      updateBtn.setTextColor(Color.BLACK);
      updateBtn.setPadding(40, 15, 40, 15);
      LinearLayout.LayoutParams btnParams =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      btnParams.setMargins(10, 0, 10, 0);
      buttonLayout.addView(updateBtn, btnParams);

      popupLayout.addView(titleView);
      popupLayout.addView(infoView);
      popupLayout.addView(buttonLayout);

      EditorPopUp.showCustomViewAtCursor(currentEditor, popupLayout);

      updateBtn.setOnClickListener(v -> updateDependency(dep, newVersion));
    } catch (Exception e) {
      Log.e("GradleUpdateChecker", "Error showing update dialog: " + e.getMessage());
    }
  }

  private void showUpToDateMessage(DependencyInfo dep) {
    if (currentEditor == null) return;
    showTemporaryTooltip("✅ " + dep.artifact + " is up to date.");
  }

  private void showTemporaryTooltip(String message) {
    if (currentEditor == null) return;

    try {
      Toast.makeText(currentEditor.getContext(), message, Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      Log.e("GradleUpdateChecker", "Error showing tooltip: " + e.getMessage());
    }
  }

  private void updateDependency(DependencyInfo dep, String newVersion) {
    try {
      String newDependency =
          dep.fullMatch.replace(
              ":" + dep.version + (dep.fullMatch.endsWith("'") ? "'" : "\""),
              ":" + newVersion + (dep.fullMatch.endsWith("'") ? "'" : "\""));

      currentEditor
          .getText()
          .replace(
              dep.line, dep.start, dep.line, dep.start + dep.fullMatch.length(), newDependency);

      showTemporaryTooltip("✅ Update completed");

    } catch (Exception e) {
      Log.e("GradleUpdateChecker", "Error updating dependency: " + e.getMessage());
    }
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public String setName() {
    return "Gradle Update Checker";
  }

  @Override
  public String langModel() {
    return ".gradle,.kts";
  }

  GradientDrawable get(View v) {
    var dg = new GradientDrawable();
    dg.setShape(GradientDrawable.RECTANGLE);
    dg.setColor(ColorStateList.valueOf(MaterialColors.getColor(v, ObjectUtils.Back, 0)));
    dg.setCornerRadius(30);
    return dg;
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
