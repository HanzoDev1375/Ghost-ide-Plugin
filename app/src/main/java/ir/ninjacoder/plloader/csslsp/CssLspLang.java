package ir.ninjacoder.plloader.csslsp;

import android.widget.Toast;
import android.util.Log;
import io.github.rosemoe.sora.data.CompletionItem;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.langs.css3.CSS3Language;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.interfaces.AutoCompleteProvider;
import ir.ninjacoder.ghostide.core.IdeEditor;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import com.google.android.material.tabs.TabLayout;
import ir.ninjacoder.plloader.EditorPopUp;
import java.lang.reflect.Field;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.json.JSONObject;
import java.util.Iterator;

public class CssLspLang implements PluginManagerCompat {

  private CodeEditor currentEditor;
  private CodeEditorActivity codeEditorActivity;
  private TabLayout tabLayout;
  private boolean isCssFile = false;

  private boolean isProcessing = false;
  private long lastProcessTime = 0;
  private final int PROCESS_DELAY = 150;

  private CssLsp cssLsp;

  @Override
  public void getEditor(CodeEditor editor) {
    this.currentEditor = editor;
    editor.postDelayed(
        () -> {
          try {
            if (editor.getContext() instanceof CodeEditorActivity) {
              codeEditorActivity = (CodeEditorActivity) editor.getContext();
              setupTabChangeListener();

              String fileType = codeEditorActivity.getcurrentFileType();

              if (fileType != null && fileType.endsWith(".css")) {
                Toast.makeText(codeEditorActivity, "CSS LSP Plugin Activated!", Toast.LENGTH_SHORT)
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

  private void updateFileType() {
    try {
      if (codeEditorActivity == null) return;

      String fileType = codeEditorActivity.getcurrentFileType();
      isCssFile = fileType != null && fileType.endsWith(".css");
      currentEditor.removeLineIcon(1);
      currentEditor.removeLineIcon(2);
      Log.d("CssLspPlugin", "📁 File type: " + fileType + " - isCssFile: " + isCssFile);

    } catch (Exception e) {
      Log.e("CssLspPlugin", "❌ Error updating file type: " + e.getMessage());
      isCssFile = false;
    }
  }

  private void setupEventListeners(CodeEditor editor) {

    editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          if (isProcessing || !isCssFile) return;

          long currentTime = System.currentTimeMillis();
          if (currentTime - lastProcessTime < PROCESS_DELAY) {
            return;
          }
          lastProcessTime = currentTime;

          try {
            int cursorLine = editor.getCursor().getLeftLine();
            int cursorColumn = editor.getCursor().getLeftColumn();
            String currentLine = editor.getText().getLineString(cursorLine);

            checkAndDisplayValue(cursorLine, cursorColumn, currentLine);

          } catch (Exception e) {
            Log.e("CssLspPlugin", "❌ Error in selection change: " + e.getMessage());
          }
        });

    editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, sub) -> {
          if (event.getAction() == ContentChangeEvent.ACTION_DELETE
              || event.getAction() == ContentChangeEvent.ACTION_INSERT
              || event.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {
            isProcessing = false;
          }
        });
  }

  private void checkAndDisplayValue(int cursorLine, int cursorColumn, String currentLine) {
    try {

      List<CssProperty> properties = findCssProperties(currentLine, cursorLine);

      for (CssProperty property : properties) {
        if (isCursorOnProperty(property, cursorLine, cursorColumn)) {

          displayPropertyValue(property);
          return;
        }
      }
      hidePropertyDisplay();
    } catch (Exception e) {
      Log.e("CssLspPlugin", "❌ Error checking property: " + e.getMessage());
    }
  }

  private List<CssProperty> findCssProperties(String lineText, int lineNumber) {
    List<CssProperty> properties = new ArrayList<>();

    if (lineText == null || lineText.isEmpty()) {
      return properties;
    }

    String propertyPattern = "([a-zA-Z-]+)\\s*:";
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(propertyPattern);
    java.util.regex.Matcher matcher = pattern.matcher(lineText);

    while (matcher.find()) {
      String propertyName = matcher.group(1).trim();
      properties.add(new CssProperty(lineNumber, matcher.start(), propertyName));
    }

    return properties;
  }

  private boolean isCursorOnProperty(CssProperty property, int cursorLine, int cursorColumn) {
    if (property.getLine() != cursorLine) return false;

    int start = property.getCol();
    int end = start + property.getName().length();
    return cursorColumn >= start && cursorColumn <= end;
  }

  private void displayPropertyValue(CssProperty property) {
    try {

      String propertyValue = findPropertyValueFromLsp(property.getName());

      if (propertyValue != null && !propertyValue.isEmpty()) {

        showValueTooltip(property.getName(), propertyValue);
        Log.d(
            "CssLspPlugin", "📝 Displaying value for " + property.getName() + ": " + propertyValue);
      }
    } catch (Exception e) {
      Log.e("CssLspPlugin", "❌ Error displaying property value: " + e.getMessage());
    }
  }

  private String findPropertyValueFromLsp(String propertyName) {

    try {
      if (cssLsp != null && cssLsp.lspList != null) {
        for (NameValue item : cssLsp.lspList) {
          if (item.getName() != null && item.getName().equalsIgnoreCase(propertyName)) {
            return item.getValue();
          }
        }
      }
    } catch (Exception e) {
      Log.e("CssLspPlugin", "❌ Error finding property value: " + e.getMessage());
    }
    return null;
  }

  private void showValueTooltip(String propertyName, String propertyValue) {

    try {
      if (currentEditor != null && currentEditor.getContext() != null) {
        String displayText = propertyName + ": " + propertyValue;

        if (displayText.length() > 100) {
          displayText = displayText.substring(0, 100) + "...";
        }
        EditorPopUp.showPowerMenuAtCursor(currentEditor, displayText);
      }
    } catch (Exception e) {
      Log.e("CssLspPlugin", "❌ Error showing tooltip: " + e.getMessage());
    }
  }

  private void hidePropertyDisplay() {}

  @Override
  public String setName() {
    return "CSS LSP Provider with Value Display";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  private void applyCustomLanguage() {
    Log.d("CssLspPlugin", "🌈 applyCustomLanguage called");

    if (currentEditor == null) {
      Log.e("CssLspPlugin", "❌ Editor is null in applyCustomLanguage");
      return;
    }

    try {
      cssLsp = new CssLsp();
      applyStylingChanges();
      var customLang =
          new CSS3Language((IdeEditor) currentEditor) {
            @Override
            public AutoCompleteProvider getAutoCompleteProvider() {
              return cssLsp;
            }

            @Override
            public CodeAnalyzer getAnalyzer() {
              return new CSS3Analyzer(editor);
            }
          };

      currentEditor.setEditorLanguage(customLang);
      Log.d("CssLspPlugin", "✅ Custom language set");

    } catch (Exception e) {
      Log.e("CssLspPlugin", "💥 Error applying custom language: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void applyStylingChanges() {
    if (currentEditor == null) return;
    currentEditor.setCursorAnimationEnabled(false);

    currentEditor.post(
        () -> {
          currentEditor.setDividerWidth(3f);
          currentEditor.setDividerMargin(49f);
          currentEditor.postDelayed(
              () -> {
                currentEditor.setCursorAnimationEnabled(true);
                currentEditor.invalidate();
              },
              300);
        });
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {
    this.codeEditorActivity = arg0;
  }

  @Override
  public String langModel() {
    return ".css";
  }

  static class CssProperty {
    final int line, col;
    final String name;

    public CssProperty(int line, int col, String name) {
      this.line = line;
      this.col = col;
      this.name = name;
    }

    public int getLine() {
      return line;
    }

    public int getCol() {
      return col;
    }

    public String getName() {
      return name;
    }
  }

  class CssLsp implements AutoCompleteProvider {

    List<NameValue> lspList = null;
    private Map<String, Boolean> filePropertiesMap = new HashMap<>();
    List<CompletionItem> snippetItems = new ArrayList<>();
    private boolean snippetsLoaded = false;

    public CssLsp() {
      loadLspData();

      loadCssSnippets();
    }

    private void loadCssSnippets() {
      try {
        File file = new File("/storage/emulated/0/GhostWebIDE/plugins/csslsp/snippets.json");
        if (!file.exists()) {
          Log.e("CssPlugin", "❌ Snippets file not found: " + file.getAbsolutePath());
          return;
        }

        Log.d("CssPlugin", "✅ Loading snippets from: " + file.getAbsolutePath());

        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
          content.append(line);
        }
        reader.close();

        JSONObject root = new JSONObject(content.toString());
        JSONObject css = root.getJSONObject("css");
        JSONObject snippets = css.getJSONObject("snippets");

        for (Iterator<String> it = snippets.keys(); it.hasNext(); ) {
          String key = it.next();
          String rawValue = snippets.getString(key);

          String cleanValue = cleanSnippet(rawValue);

          CompletionItem item = new CompletionItem();
          item.label = key;
          item.commit = cleanValue;
          item.desc = cleanValue;
          item.cursorOffset(item.commit.length());

          snippetItems.add(item);
        }

        snippetsLoaded = true;
        Log.d("CssPlugin", "✨ Loaded " + snippetItems.size() + " snippets");

      } catch (Exception e) {
        Log.e("CssPlugin", "❌ Error loading CSS snippets: " + e.getMessage());
        e.printStackTrace();
      }
    }

    private void loadLspData() {
      lspList = new ArrayList<>();
      String directoryPath = "/storage/emulated/0/GhostWebIDE/plugins/csslsp/data/";

      try {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
          File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));

          if (files != null) {
            for (File file : files) {
              try {
                StringBuilder content = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                  content.append(line);
                }
                reader.close();

                String jsonContent = content.toString();

                boolean hasProperties = LspContent.hasProperties(jsonContent);
                filePropertiesMap.put(file.getName(), hasProperties);

                List<NameValue> fileItems = LspContent.extractAllNameValue(jsonContent);
                lspList.addAll(fileItems);

              } catch (Exception e) {
                Log.e("CssPlugin", "❌ Error reading " + file.getName());
              }
            }

            lspList = removeDuplicates(lspList);
            Log.d("CssPlugin", "✅ Loaded " + lspList.size() + " LSP items");
          }
        }
      } catch (Exception e) {
        Log.e("CssPlugin", "💥 Error loading LSP data: " + e.getMessage());
      }
    }

    private List<NameValue> removeDuplicates(List<NameValue> list) {
      Set<String> seenNames = new HashSet<>();
      List<NameValue> uniqueList = new ArrayList<>();

      for (NameValue item : list) {
        if (item.getName() != null && !seenNames.contains(item.getName())) {
          seenNames.add(item.getName());
          uniqueList.add(item);
        }
      }
      return uniqueList;
    }

    private String cleanSnippet(String raw) {
      if (raw == null) return "";

      raw = raw.replaceAll("\\$\\{\\d+:[^}]*\\}", "");

      raw = raw.replaceAll("\\$\\{\\d+\\}", "");

      raw = raw.replace("|", "");

      raw = raw.replace("${child}", "");

      raw = raw.replaceAll("\\$\\{[^}]*\\}", "");

      return raw.trim();
    }

    @Override
    public List<CompletionItem> getAutoCompleteItems(
        String prefix, TextAnalyzeResult colors, int line, int column) {

      List<CompletionItem> list = new ArrayList<>();

      if (prefix == null || prefix.isEmpty()) {
        return list;
      }

      String lowerPrefix = prefix.toLowerCase();
      Log.d("CssPlugin", "🔍 Searching for: '" + prefix + "'");
      var jslsp = new JavaScriptAutoCompleter(currentEditor.getContext());
      list.addAll(jslsp.complete(currentEditor.getText().toString(), line, column, prefix));
      if (snippetsLoaded) {
        for (CompletionItem snippet : snippetItems) {
          if (snippet.label.toLowerCase().startsWith(lowerPrefix)) {
            list.add(createSnippetItem(snippet));
            if (list.size() >= 20) break;
          }
        }
      }

      if (lspList != null && list.size() < 20) {
        for (NameValue lspItem : lspList) {
          String name = lspItem.getName();
          if (name != null && name.toLowerCase().contains(lowerPrefix)) {
            list.add(createLspItem(lspItem));
            if (list.size() >= 20) break;
          }
        }
      }

      Log.d("CssPlugin", "📦 Found " + list.size() + " items for '" + prefix + "'");
      return list;
    }

    private CompletionItem createSnippetItem(CompletionItem original) {
      CompletionItem item = new CompletionItem();
      item.label = original.label;
      item.commit = original.commit;
      item.desc = original.desc;
      item.cursorOffset(item.commit.length());
      return item;
    }

    private CompletionItem createLspItem(NameValue lspItem) {
      CompletionItem item = new CompletionItem();
      item.label = lspItem.getName();

      boolean isProperty = false;
      for (boolean hasProps : filePropertiesMap.values()) {
        if (hasProps) {
          isProperty = true;
          break;
        }
      }

      if (isProperty) {
        item.commit = lspItem.getName() + ": ";
      } else {
        item.commit = lspItem.getName();
      }

      item.cursorOffset(item.commit.length());
      item.desc =
          lspItem.getValue() != null && !lspItem.getValue().isEmpty()
              ? lspItem.getValue()
              : "CSS Property";

      return item;
    }
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
