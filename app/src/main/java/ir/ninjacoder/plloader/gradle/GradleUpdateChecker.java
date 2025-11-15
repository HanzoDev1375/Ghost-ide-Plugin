package ir.ninjacoder.plloader.gradle;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import io.github.rosemoe.sora.widget.EditorColorScheme;
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
import java.util.Random;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleUpdateChecker implements PluginManagerCompat {

  private CodeEditor currentEditor;
  private CodeEditorActivity codeEditorActivity;
  private View progressTooltip;
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

              if (fileType != null && fileType.endsWith(".gradle")) {
                Toast.makeText(codeEditorActivity, "gradle Plugin Activated!", Toast.LENGTH_SHORT)
                    .show();
                applyCustomLanguage();
                setupEventListeners(editor);
              }
            }
          } catch (Exception e) {
            Log.e("CssLspPlugin", "Error: " + e.getMessage());
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

    }
  }

  private void updateFileType() {
    try {
      if (codeEditorActivity == null) return;

      String fileType = codeEditorActivity.getcurrentFileType();
      isgradleFile = fileType != null && fileType.endsWith(".css");
      
    } catch (Exception e) {
      Log.e("CssLspPlugin", "❌ Error updating file type: " + e.getMessage());
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
      Log.e("CssLspPlugin", "❌ Error setting up tab listener: " + e.getMessage());
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

              // ایجاد درخواست جدید با تاخیر
              checkRunnable =
                  () -> {
                    checkDependencyUpdate(dep);
                  };
              handler.postDelayed(checkRunnable, 300); // تاخیر 300 میلی‌ثانیه
            }

          } catch (Exception e) {
            // در صورت خطا، درخواست را حذف کن
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

    // اول چک کن اگر در cache آنالایزر هست
    String cacheKey = dep.group + ":" + dep.artifact;
    if (GradleCodeAnalyzer.outdatedCache.containsKey(cacheKey)) {
      isChecking = false;
      if (GradleCodeAnalyzer.outdatedCache.get(cacheKey)) {
        String latestVersion = GradleCodeAnalyzer.latestVersions.get(cacheKey);
        showUpdateDialog(dep, latestVersion);
      } else {
        showUpToDateMessage(dep);
      }
      return;
    }

    // اگر در cache نبود، خودت چک کن
    new Thread(
            () -> {
              try {
                String latestVersion = getLatestVersion(dep.group, dep.artifact);

                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          isChecking = false;
                          if (latestVersion != null && !dep.version.equals(latestVersion)) {
                            showUpdateDialog(dep, latestVersion);
                          } else if (latestVersion != null) {
                            showUpToDateMessage(dep);
                          }
                        });
              } catch (Exception e) {
                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          isChecking = false;
                          Log.e("Error: ", e.getMessage());
                        });
              }
            })
        .start();
  }

  private String getLatestVersion(String group, String artifact) throws Exception {
    String url =
        String.format(
            "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&rows=1&wt=json",
            group, artifact);

    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(8000);
    conn.setReadTimeout(8000);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }

      JSONObject json = new JSONObject(response.toString());
      return json.getJSONObject("response")
          .getJSONArray("docs")
          .getJSONObject(0)
          .getString("latestVersion");
    } finally {
      conn.disconnect();
    }
  }

  private void showUpdateDialog(DependencyInfo dep, String newVersion) {
    if (currentEditor == null) return;

    try {

      LinearLayout popupLayout = new LinearLayout(currentEditor.getContext());
      popupLayout.setOrientation(LinearLayout.VERTICAL);
      popupLayout.setPadding(40, 30, 40, 30);

      TextView titleView = new TextView(currentEditor.getContext());
      titleView.setText("📦 Update available");
      titleView.setTextSize(16);
      titleView.setTypeface(null, Typeface.BOLD);
      titleView.setTextColor(Color.parseColor("#1976D2"));
      titleView.setGravity(Gravity.CENTER);

      TextView infoView = new TextView(currentEditor.getContext());
      infoView.setText(
          String.format(
              ": %s:%s\n\nCurrent version: %s\nNew version: %s",
              dep.group, dep.artifact, dep.version, newVersion));
      infoView.setTextSize(14);
      infoView.setPadding(0, 20, 0, 30);
      infoView.setLineSpacing(0, 1.2f);

      LinearLayout buttonLayout = new LinearLayout(currentEditor.getContext());
      buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
      buttonLayout.setGravity(Gravity.CENTER);

      Button updateBtn = new Button(currentEditor.getContext());
      updateBtn.setText("Update Now");
      updateBtn.setBackground(get(updateBtn));
      updateBtn.setPadding(40, 15, 40, 15);

      LinearLayout.LayoutParams btnParams =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
      btnParams.setMargins(10, 0, 10, 0);

      buttonLayout.addView(updateBtn, btnParams);

      popupLayout.addView(titleView);
      popupLayout.addView(infoView);
      popupLayout.addView(buttonLayout);

      EditorPopUp.showCustomViewAtCursor(currentEditor, popupLayout);

      updateBtn.setOnClickListener(
          v -> {
            updateDependency(dep, newVersion);
          });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showUpToDateMessage(DependencyInfo dep) {
    if (currentEditor == null) return;

    showTemporaryTooltip("✅ " + dep.artifact + " It is an update.");
  }

  private void showTemporaryTooltip(String message) {
    if (currentEditor == null) return;

    try {
      Toast.makeText(currentEditor.getContext(), message, 2).show();
    } catch (Exception e) {
      e.printStackTrace();
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

      showTemporaryTooltip("✅ آپدیت انجام شد");

    } catch (Exception e) {
      Log.e("خطا در آپدیت: ", e.getMessage());
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
    return ".gradle";
  }

  GradientDrawable get(View v) {
    var dg = new GradientDrawable();
    dg.setShape(GradientDrawable.RECTANGLE);
    dg.setColor(ColorStateList.valueOf(MaterialColors.getColor(v, ObjectUtils.Back, 0)));
    dg.setCornerRadius(30);
    return dg;
  }
}
