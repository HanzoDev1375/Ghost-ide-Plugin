package ir.ninjacoder.plloader;

import android.graphics.Color;
import android.widget.TextView;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import io.noties.markwon.Markwon;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssColorPreview implements PluginManagerCompat {

  private List<ColorPreview> colorList;
  private List<CssProperty> cssProperties;
  private List<HtmlTag> htmlTags;
  private Pattern colorPattern;
  private Pattern propertyPattern;
  private Pattern htmlTagPattern;
  private int originalCursorColor;
  private boolean isProcessing;
  private boolean isCursorColorChanged = false;
  private CodeEditor currentEditor;

  private final ExecutorService executor =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private Future<?> currentTask;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  // متغیرهای جدید برای مدیریت تاخیر و جلوگیری از نمایش مکرر
  private final Handler detectionHandler = new Handler(Looper.getMainLooper());
  private final Runnable detectionRunnable = this::detectAndUpdateBackground;
  private static final long DETECTION_DELAY = 800; // 800ms تاخیر
  private static final long MIN_DETECTION_INTERVAL = 1500; // حداقل 1.5 ثانیه بین بررسی‌ها
  private long lastDetectionTime = 0;
  private boolean isUserTyping = false;
  private long lastUserActivity = 0;
  private String lastDetectedContent = "";
  private static final long TYPING_TIMEOUT = 1000; // 1 ثانیه پس از تایپ

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getEditor(CodeEditor editor) {
    currentEditor = editor;

    Log.d("CssColorPreview", "Plugin loaded for editor");

    boolean shouldApply = typeLangApply(editor);
    Log.d("CssColorPreview", "Should apply: " + shouldApply);

    if (shouldApply) {
      Log.d("CssColorPreview", "Initializing plugin for supported language");

      originalCursorColor = editor.getColorScheme().getColor(EditorColorScheme.SELECTION_INSERT);
      Log.d("CssColorPreview", "Original cursor color: " + originalCursorColor);

      // بارگذاری اولیه رنگ‌ها، properties و تگ‌های HTML در پس‌زمینه
      loadColorsInBackground();
      loadCssPropertiesInBackground();
      loadHtmlTagsInBackground();

      editor.subscribeEvent(
          ContentChangeEvent.class,
          (ev, un) -> {
            if (ev.getAction() == ContentChangeEvent.ACTION_DELETE
                || ev.getAction() == ContentChangeEvent.ACTION_INSERT
                || ev.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {

              isUserTyping = true;
              lastUserActivity = System.currentTimeMillis();

              // لغو درخواست قبلی و شروع جدید با تاخیر
              detectionHandler.removeCallbacks(detectionRunnable);
              detectionHandler.postDelayed(detectionRunnable, DETECTION_DELAY);

              // بعد از تایم‌اوت، تایپ کردن تمام شده در نظر گرفته می‌شود
              detectionHandler.postDelayed(
                  () -> {
                    if (System.currentTimeMillis() - lastUserActivity >= TYPING_TIMEOUT) {
                      isUserTyping = false;
                    }
                  },
                  TYPING_TIMEOUT + 100);
            }
          });

      editor.subscribeEvent(
          SelectionChangeEvent.class,
          (event, unsubscribe) -> {
            // فقط وقتی کاربر در حال تایپ نیست بررسی کنیم
            if (!isUserTyping) {
              detectionHandler.removeCallbacks(detectionRunnable);
              detectionHandler.postDelayed(detectionRunnable, 300);
            }
          });

      Log.d("CssColorPreview", "Event subscription completed");
    } else {
      Log.d("CssColorPreview", "Language not supported, plugin disabled");
    }
  }

  private void detectAndUpdateBackground() {
    long currentTime = System.currentTimeMillis();

    // جلوگیری از بررسی‌های مکرر در بازه زمانی کوتاه
    if (currentTime - lastDetectionTime < MIN_DETECTION_INTERVAL) {
      return;
    }

    lastDetectionTime = currentTime;

    // لغو تسک قبلی
    cancelCurrentTask();

    // شروع تسک جدید
    currentTask = executor.submit(this::detectAndUpdate);
  }

  private void loadColorsInBackground() {
    executor.submit(
        () -> {
          try {
            colorList =
                new Gson()
                    .fromJson(
                        new StringReader(CssColors.getJsonColor()),
                        new TypeToken<List<ColorPreview>>() {}.getType());

            if (colorList != null && !colorList.isEmpty()) {
              Log.d("CssColorPreview", "Loaded " + colorList.size() + " colors from JSON");

              StringBuilder patternBuilder = new StringBuilder();
              patternBuilder.append("\\b(");
              for (int i = 0; i < colorList.size(); i++) {
                if (i > 0) patternBuilder.append("|");
                patternBuilder.append(colorList.get(i).getColorName());
              }
              patternBuilder.append(")\\b");
              colorPattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
              Log.d("CssColorPreview", "Color pattern created: " + colorPattern.pattern());
            }
          } catch (Exception e) {
            Log.e("CssColorPreview", "Error loading colors: " + e.getMessage());
          }
        });
  }

  private void loadCssPropertiesInBackground() {
    executor.submit(
        () -> {
          try {
            // بارگذاری CSS properties از فایل JSON
            File jsonFile =
                new File("/storage/emulated/0/GhostWebIDE/plugins/csscolorview/output.json");
            if (jsonFile.exists()) {
              FileReader reader = new FileReader(jsonFile);
              cssProperties =
                  new Gson().fromJson(reader, new TypeToken<List<CssProperty>>() {}.getType());
              reader.close();

              if (cssProperties != null && !cssProperties.isEmpty()) {
                Log.d(
                    "CssColorPreview",
                    "Loaded " + cssProperties.size() + " CSS properties from JSON");

                StringBuilder patternBuilder = new StringBuilder();
                patternBuilder.append("\\b(");
                for (int i = 0; i < cssProperties.size(); i++) {
                  if (i > 0) patternBuilder.append("|");
                  String propertyName = cssProperties.get(i).getName();
                  if (propertyName == null) {
                    propertyName = cssProperties.get(i).getProperty();
                  }
                  if (propertyName != null) {
                    patternBuilder.append(Pattern.quote(propertyName));
                  }
                }
                patternBuilder.append(")\\b");
                propertyPattern =
                    Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
                Log.d("CssColorPreview", "Property pattern created: " + propertyPattern.pattern());
              }
            } else {
              Log.e("CssColorPreview", "JSON file not found: " + jsonFile.getPath());
            }
          } catch (Exception e) {
            Log.e("CssColorPreview", "Error loading CSS properties: " + e.getMessage());
          }
        });
  }

  private void loadHtmlTagsInBackground() {
    executor.submit(
        () -> {
          try {
            File htmlJsonFile =
                new File("/storage/emulated/0/GhostWebIDE/plugins/csscolorview/html.json");
            if (htmlJsonFile.exists()) {
              FileReader reader = new FileReader(htmlJsonFile);
              htmlTags = new Gson().fromJson(reader, new TypeToken<List<HtmlTag>>() {}.getType());
              reader.close();

              if (htmlTags != null && !htmlTags.isEmpty()) {
                Log.d("CssColorPreview", "Loaded " + htmlTags.size() + " HTML tags");

                // الگوی ساده‌تر
                StringBuilder patternBuilder = new StringBuilder();
                patternBuilder.append("</?(");
                for (int i = 0; i < htmlTags.size(); i++) {
                  if (i > 0) patternBuilder.append("|");
                  patternBuilder.append(htmlTags.get(i).getTag());
                }
                patternBuilder.append(")");
                htmlTagPattern =
                    Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);

                Log.d("CssColorPreview", "Pattern: " + htmlTagPattern.pattern());
              }
            }
          } catch (Exception e) {
            Log.e("CssColorPreview", "Error loading HTML tags", e);
          }
        });
  }

  private void detectAndUpdate() {
    if (Thread.currentThread().isInterrupted()) {
      return;
    }

    try {
      // گرفتن اطلاعات در background thread
      final int cursorLine = currentEditor.getCursor().getLeftLine();
      final int cursorColumn = currentEditor.getCursor().getLeftColumn();
      final String currentLine = currentEditor.getText().getLineString(cursorLine);

      if (currentLine == null || currentLine.isEmpty()) {
        mainHandler.post(this::restoreOriginalCursorColor);
        return;
      }

      // جلوگیری از بررسی محتوای تکراری
      String currentContent = cursorLine + ":" + cursorColumn + ":" + currentLine;
      if (currentContent.equals(lastDetectedContent)) {
        return;
      }
      lastDetectedContent = currentContent;

      // اول چک کردن برای HTML tags
      HtmlTag detectedHtmlTag = null;
      if (htmlTagPattern != null) {
        Matcher htmlMatcher = htmlTagPattern.matcher(currentLine);
        while (htmlMatcher.find() && !Thread.currentThread().isInterrupted()) {
          int start = htmlMatcher.start();
          int end = htmlMatcher.end();
          String foundTagName = htmlMatcher.group(1);

          if (cursorColumn >= start && cursorColumn <= end) {
            for (HtmlTag tag : htmlTags) {
              if (Thread.currentThread().isInterrupted()) break;
              if (tag.getTag().equalsIgnoreCase(foundTagName)) {
                detectedHtmlTag = tag;
                break;
              }
            }
            if (detectedHtmlTag != null) break;
          }
        }
      }

      // اگر HTML tag پیدا شد، نمایش documentation
      if (detectedHtmlTag != null) {
        final HtmlTag finalTag = detectedHtmlTag;
        mainHandler.post(() -> showHtmlTagDocumentation(finalTag, currentEditor));
        return;
      }

      // دوم چک کردن برای CSS properties
      CssProperty detectedProperty = null;
      if (propertyPattern != null) {
        Matcher propertyMatcher = propertyPattern.matcher(currentLine);
        while (propertyMatcher.find() && !Thread.currentThread().isInterrupted()) {
          int start = propertyMatcher.start();
          int end = propertyMatcher.end();
          String foundPropertyName = propertyMatcher.group(1);

          if (cursorColumn >= start && cursorColumn <= end) {
            for (CssProperty property : cssProperties) {
              if (Thread.currentThread().isInterrupted()) break;
              String propName = property.getName();
              if (propName == null) {
                propName = property.getProperty();
              }
              if (propName != null && propName.equalsIgnoreCase(foundPropertyName)) {
                detectedProperty = property;
                break;
              }
            }
            if (detectedProperty != null) break;
          }
        }
      }

      // اگر property پیدا شد، نمایش documentation
      if (detectedProperty != null) {
        final CssProperty finalProperty = detectedProperty;
        mainHandler.post(() -> showPropertyDocumentation(finalProperty, currentEditor));
        return;
      }

      // اگر property پیدا نشد، چک کردن برای رنگ‌ها
      if (colorPattern != null) {
        ColorPreview detectedColor = null;
        Matcher colorMatcher = colorPattern.matcher(currentLine);

        while (colorMatcher.find() && !Thread.currentThread().isInterrupted()) {
          int start = colorMatcher.start();
          int end = colorMatcher.end();
          String foundColorName = colorMatcher.group(1);

          if (cursorColumn >= start && cursorColumn <= end) {
            for (ColorPreview color : colorList) {
              if (Thread.currentThread().isInterrupted()) break;
              if (color.getColorName().equalsIgnoreCase(foundColorName)) {
                detectedColor = color;
                break;
              }
            }
            if (detectedColor != null) break;
          }
        }

        final ColorPreview finalColor = detectedColor;
        mainHandler.post(
            () -> {
              if (finalColor != null) {
                changeCursorColor(finalColor, currentEditor);
              } else {
                restoreOriginalCursorColor();
              }
            });
      } else {
        mainHandler.post(this::restoreOriginalCursorColor);
      }

    } catch (Exception e) {
      Log.e("CssColorPreview", "Error in detection: " + e.getMessage());
      mainHandler.post(this::restoreOriginalCursorColor);
    }
  }

  private void cancelCurrentTask() {
    if (currentTask != null && !currentTask.isDone()) {
      currentTask.cancel(true);
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
    return "CSS Color Preview & Documentation";
  }

  void showHtmlTagDocumentation(HtmlTag tag, CodeEditor editor) {
    try {

      editor.postDelayed(
          () -> {
            TextView tv = new TextView(editor.getContext());
            tv.setPadding(32, 16, 32, 16);

            String tagName = tag.getTag();
            String description = tag.getDescription();
            if (description == null) {
              description = "No documentation available for <" + tagName + ">";
            }

            Markwon md = Markwon.create(editor.getContext());
            md.setMarkdown(tv, "**<" + tagName + ">**\n\n" + description);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(12);
            tv.setMaxLines(10);

            EditorPopUp.showCustomViewAtCursor(editor, tv);
          },
          700);
    } catch (Exception e) {
      Log.e("CssColorPreview", "Error showing HTML tag documentation: " + e.getMessage());
    }
  }

  void showPropertyDocumentation(CssProperty property, CodeEditor editor) {
    try {

      editor.postDelayed(
          () -> {
            TextView tv = new TextView(editor.getContext());
            tv.setPadding(32, 16, 32, 16);

            String propertyName = property.getName();
            if (propertyName == null) {
              propertyName = property.getProperty();
            }

            String description = property.getDesc();
            if (description == null) {
              description = "No documentation available";
            }

            Markwon md = Markwon.create(editor.getContext());
            md.setMarkdown(tv, description);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.parseColor("#2b2b2b"));
            tv.setTextSize(12);
            tv.setMaxLines(10);

            EditorPopUp.showCustomViewAtCursor(editor, tv);
          },
          700);
    } catch (Exception e) {
      Log.e("CssColorPreview", "Error showing property documentation: " + e.getMessage());
    }
  }

  void showColorPreview(ColorPreview color, CodeEditor editor) {
    try {

      editor.postDelayed(
          () -> {
            TextView tv = new TextView(editor.getContext());
            tv.setPadding(32, 16, 32, 16);
            tv.setText(color.getColorName() + ": " + color.getCssColor());
            tv.setTextColor(Color.parseColor(color.getCssColor()));
            tv.setTextSize(14);

            EditorPopUp.showCustomViewAtCursor(editor, tv);
          },
          700);
    } catch (Exception e) {
      Log.e("CssColorPreview", "Error showing color preview: " + e.getMessage());
    }
  }

  void changeCursorColor(ColorPreview color, CodeEditor editor) {
    try {
      int newColor = Color.parseColor(color.getCssColor());
      editor.getColorScheme().setColor(EditorColorScheme.SELECTION_INSERT, newColor);
      isCursorColorChanged = true;
      showColorPreview(color, editor);
      editor.invalidate();
    } catch (Exception e) {
      Log.e("CssColorPreview", "Error changing cursor color: " + e.getMessage());
      restoreOriginalCursorColor();
    }
  }

  void restoreOriginalCursorColor() {
    if (isCursorColorChanged && currentEditor != null) {
      currentEditor
          .getColorScheme()
          .setColor(EditorColorScheme.SELECTION_INSERT, originalCursorColor);
      isCursorColorChanged = false;
      currentEditor.invalidate();
    }
  }

  static class ColorPreview {
    private String colorName;
    private String cssColor;

    public String getColorName() {
      return colorName;
    }

    public String getCssColor() {
      return cssColor;
    }
  }

  static class HtmlTag {
    private String tag;
    private String description;

    public String getTag() {
      return tag;
    }

    public String getDescription() {
      return description;
    }
  }

  static class CssProperty {
    private String name;
    private String property;
    private String desc;

    public String getName() {
      return name;
    }

    public String getProperty() {
      return property;
    }

    public String getDesc() {
      return desc;
    }
  }

  boolean typeLangApply(CodeEditor editor) {
    return true;
  }

  @Override
  public String langModel() {
    return ".html,.css";
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
