package ir.ninjacoder.javapreviews;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import androidx.viewpager.widget.PagerAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import io.github.rosemoe.sora.widget.EditorPopupWindow;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;

import ir.ninjacoder.ghostide.core.IdeEditor;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.ghostide.core.utils.FileUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Javapreview implements PluginManagerCompat {

  private static final long HOVER_DELAY = 350;
  private static final long MAX_FILE_SIZE = 200 * 1024;
  private static final int MAX_CACHE = 40;

  private final String javaPath =
      "/storage/emulated/0/GhostWebIDE/plugins/javafileview/data/android/";
  private final String fontPath =
      "/storage/emulated/0/GhostWebIDE/plugins/javafileview/font/ghostfont.ttf";

  private CodeEditor editor;
  private EditorPopupWindow popup;
  private CodeEditorActivity a;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Handler main = new Handler(Looper.getMainLooper());
  private final Handler hoverHandler = new Handler(Looper.getMainLooper());

  private int requestToken = 0;
  private String lastWord = "";
  private String pendingWord = null;

  private final Map<String, List<File>> fileNameIndex = new LinkedHashMap<>();
  private final Map<String, String> contentCache =
      new LinkedHashMap<String, String>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
          return size() > MAX_CACHE;
        }
      };

  
  @Override
  public void getEditor(CodeEditor editor) {
    this.editor = editor;
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity a) {
    this.a = a;
    executor.execute(this::buildFileNameIndex);

    editor.subscribeEvent(SelectionChangeEvent.class, (e, u) -> schedule());

    editor.subscribeEvent(
        ContentChangeEvent.class,
        (e, u) -> {
          lastWord = "";
          requestToken++;
          hoverHandler.removeCallbacksAndMessages(null);
          dismiss();
        });
  }

  
  private List<File> findProjectRoots(File openedFile) {
    List<File> roots = new ArrayList<>();

    
    File androidRoot = findAndroidModuleRoot(openedFile);
    if (androidRoot != null) {
      File java = new File(androidRoot, "src/main/java");
      if (java.exists()) roots.add(java);

      File kotlin = new File(androidRoot, "src/main/kotlin");
      if (kotlin.exists()) roots.add(kotlin);
    }

    
    File current = openedFile.getParentFile();
    int depth = 0;
    while (current != null && depth < 10) {
      
      File[] children = current.listFiles();
      if (children != null) {
        for (File child : children) {
          if (child.isDirectory()) {
            String name = child.getName().toLowerCase();
            if (name.equals("src")
                || name.equals("source")
                || name.equals("sources")
                || name.equals("java")
                || name.equals(".idea")
                || name.equals(".vscode")
                || child.getName().endsWith(".gradle")
                || child.getName().endsWith(".mvn")
                || child.getName().equals("pom.xml")
                || child.getName().equals("build.gradle")) {
              roots.add(current);
              break;
            }
          }
        }
      }

      
      if (!roots.isEmpty()) break;

      current = current.getParentFile();
      depth++;
    }

    
    if (roots.isEmpty()) {
      if (openedFile.getParentFile() != null) {
        roots.add(openedFile.getParentFile());
      }
      if (openedFile.getParentFile() != null
          && openedFile.getParentFile().getParentFile() != null) {
        roots.add(openedFile.getParentFile().getParentFile());
      }
    }

    
    File pluginRoot = new File(javaPath);
    if (pluginRoot.exists()) roots.add(pluginRoot);

    return roots;
  }

  private File findAndroidModuleRoot(File file) {
    File cur = file;
    while (cur != null) {
      if (cur.getName().equals("java")
          && cur.getParentFile() != null
          && "main".equals(cur.getParentFile().getName())
          && cur.getParentFile().getParentFile() != null
          && "src".equals(cur.getParentFile().getParentFile().getName())) {
        return cur.getParentFile().getParentFile().getParentFile();
      }
      cur = cur.getParentFile();
    }
    return null;
  }

  private void buildFileNameIndex() {
    if (a == null) return;
    fileNameIndex.clear();
    File current = new File(a.getPathBytab());
    List<File> roots = findProjectRoots(current);
    for (File root : roots) indexDir(root);
  }

  private void indexDir(File dir) {
    File[] files = dir.listFiles();
    if (files == null) return;

    for (File f : files) {
      if (f.isDirectory()) {
        indexDir(f);
      } else if (f.getName().endsWith(".java")) {
        String name = f.getName().replace(".java", "");
        fileNameIndex.computeIfAbsent(name, k -> new ArrayList<>()).add(f);
      }
    }
  }

  
  private void schedule() {
    hoverHandler.removeCallbacksAndMessages(null);

    String word = getWordUnderCursor();
    if (word == null || word.equals(lastWord)) return;

    pendingWord = word;
    int token = ++requestToken;

    hoverHandler.postDelayed(
        () -> {
          if (pendingWord != null && !pendingWord.equals(lastWord)) {
            executor.execute(() -> detectType(pendingWord, token));
          }
        },
        HOVER_DELAY);
  }

  private void detectType(String word, int token) {
    if (token != requestToken) return;

    lastWord = word;
    List<File> files = fileNameIndex.get(word);
    if (files == null || files.isEmpty()) {
      main.post(this::dismiss);
      return;
    }

    main.post(
        () -> {
          if (token == requestToken) {
            updatePopup(word);
          }
        });
  }

  
  private String getRelativePath(File file) {
    try {
      String full = file.getAbsolutePath();
      File currentFile = new File(a.getPathBytab());
      String project = currentFile.getParent();
      if (project != null && full.startsWith(project))
        return "..." + full.substring(project.length());
      return full;
    } catch (Exception e) {
      return file.getAbsolutePath();
    }
  }

  private String loadFile(File f) {
    String path = f.getAbsolutePath();
    String cached = contentCache.get(path);
    if (cached != null) return cached;

    try {
      String text = new String(Files.readAllBytes(f.toPath()));
      contentCache.put(path, text);
      return text;
    } catch (Exception e) {
      return null;
    }
  }

  private String getWordUnderCursor() {
    try {
      if (editor == null || editor.getCursor() == null) return null;

      int line = editor.getCursor().getLeftLine();
      int col = editor.getCursor().getLeftColumn();
      String text = editor.getText().getLineString(line);
      if (text == null || text.isEmpty() || col < 0 || col > text.length()) return null;

      int s = col, e = col;
      while (s > 0 && Character.isJavaIdentifierPart(text.charAt(s - 1))) s--;
      while (e < text.length() && Character.isJavaIdentifierPart(text.charAt(e))) e++;

      String word = s == e ? null : text.substring(s, e);
      if (word == null || word.trim().isEmpty() || word.matches("^_+$")) return null;
      if (word.length() < 2) return null;
      if (!Character.isUpperCase(word.charAt(0))) return null;

      return word;
    } catch (Exception e) {
      return null;
    }
  }

  
  private void updatePopup(String className) {
    dismiss();

    View content = createPopupContent(className);
    showAtCursor(content);
  }

  private View createPopupContent(String className) {
    LinearLayout root = new LinearLayout(editor.getContext());
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(16, 16, 16, 16);

    TextView view = new TextView(editor.getContext());
    view.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    Markwon md =
        Markwon.builder(view.getContext())
            .usePlugin(
                new AbstractMarkwonPlugin() {
                  @Override
                  public void configureTheme(MarkwonTheme.Builder b) {
                    b.codeBackgroundColor(
                        editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_BG));
                    b.codeBlockTextColor(
                        editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_CORNER));
                    if (FileUtil.isExistFile(fontPath))
                      b.codeBlockTypeface(Typeface.createFromFile(new File(fontPath)));
                  }
                })
            .build();

    md.setMarkdown(view, "**Click to view** `" + className + "`");
    view.setTextSize(15);
    view.setOnClickListener(v -> showClassInTabs(className));

    root.addView(view);
    return root;
  }

  private void showAtCursor(View v) {
    if (editor == null || v == null) return;

    dismiss();

    try {
      popup =
          new EditorPopupWindow(
              editor,
              EditorPopupWindow.FEATURE_SCROLL_AS_CONTENT
                  | EditorPopupWindow.FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED);
      popup.setContentView(v);
      popup.setOutsideTouchable(true);

      v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
      popup.setSize(v.getMeasuredWidth(), v.getMeasuredHeight());

      var c = editor.getCursor().left();
      popup.setLocationAbsolutely(
          (int) editor.getCharOffsetX(c.getLine(), c.getColumn()),
          (int) (editor.getCharOffsetY(c.getLine(), c.getColumn()) + editor.getRowHeight()));

      GradientDrawable bg = new GradientDrawable();
      bg.setCornerRadius(18);
      bg.setColor(editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_BG));
      bg.setStroke(3, editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_CORNER));
      v.setBackground(bg);
      popup.show();
    } catch (Exception e) {
    }
  }

  private void dismiss() {
    if (popup != null) {
      popup.dismiss();
      popup = null;
    }
  }

  
  private void showClassInTabs(String className) {
    dismiss();

    List<File> files = fileNameIndex.get(className);
    if (files == null || files.isEmpty()) return;

    showTabsDialog(files, className);
  }

  private void showTabsDialog(List<File> files, String className) {
    try {
      
      LinearLayout mainLayout = new LinearLayout(editor.getContext());
      mainLayout.setOrientation(LinearLayout.VERTICAL);
      mainLayout.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

      
      TabLayout tabLayout = new TabLayout(editor.getContext());
      tabLayout.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

      
      ViewPager viewPager =
          new ViewPager(editor.getContext()) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
              return false;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
              return false;
            }
          };
      viewPager.setLayoutParams(
          new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

      
      TabsPagerAdapter adapter = new TabsPagerAdapter(files);
      viewPager.setAdapter(adapter);

      
      tabLayout.setBackgroundTintList(
          ColorStateList.valueOf(getEditorColor(EditorColorScheme.WHOLE_BACKGROUND)));

      
      tabLayout.setupWithViewPager(viewPager);

      
      for (int i = 0; i < tabLayout.getTabCount(); i++) {
        TabLayout.Tab tab = tabLayout.getTabAt(i);
        if (tab != null) {
          TextView tabTitle = new TextView(editor.getContext());
          String fileName = files.get(i).getName().replace(".java", "");
          tabTitle.setText((i + 1) + ". " + fileName);
          tabTitle.setMaxLines(2);
          tabTitle.setTextColor(getTabColor(i)); 
          tabTitle.setTextSize(15);
          tabTitle.setPadding(8, 4, 8, 4);
          tab.setCustomView(tabTitle);

          
          tab.view.setBackgroundTintList(ColorStateList.valueOf(getTabColor(i)));
        }
      }

      
      tabLayout.setSelectedTabIndicatorColor(getTabColor(0));
      tabLayout.addOnTabSelectedListener(
          new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
              tabLayout.setSelectedTabIndicatorColor(getTabColor(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
          });

      
      mainLayout.addView(tabLayout);
      mainLayout.addView(viewPager);
      
      MaterialAlertDialogBuilder dialog =
          new MaterialAlertDialogBuilder(editor.getContext())
              .setTitle(className + " (" + files.size() + " files)")
              .setView(mainLayout)
              .setPositiveButton("Close", null)
              .setCancelable(true);

      dialog.show();

    } catch (Exception e) {
      
      if (!files.isEmpty()) {
        String content = loadFile(files.get(0));
        if (content != null) {
          showSingleFileDialog(content, className);
        }
      }
    }
  }

  
  private int getTabColor(int position) {
    
    int[] tabColors = {
      getEditorColor(EditorColorScheme.tscolormatch1),
      getEditorColor(EditorColorScheme.tscolormatch2),
      getEditorColor(EditorColorScheme.tscolormatch3),
      getEditorColor(EditorColorScheme.tscolormatch4),
      getEditorColor(EditorColorScheme.tscolormatch5),
      getEditorColor(EditorColorScheme.tscolormatch6)
    };

    
    return tabColors[position % tabColors.length];
  }

  
  private class TabsPagerAdapter extends PagerAdapter {
    private final List<File> files;
    private final List<IdeEditor> editors = new ArrayList<>();

    public TabsPagerAdapter(List<File> files) {
      this.files = files;
    }

    @Override
    public int getCount() {
      return files.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      
      IdeEditor editorView = new IdeEditor(container.getContext());
      editorView.setLayoutParams(
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

      
      String content = loadFile(files.get(position));
      if (content != null) {
        editorView.setText(content);
        editorView.setEditorLanguage(new JavaLanguage(editorView));
        editorView.setEditable(false);
        editorView.setColorScheme(new EditorColor());
        editorView.formatCodeAsync();
        editorView.setPinLineNumber(true);
      } else {
        editorView.setText("// Error to load file");
      }

      editors.add(editorView);
      container.addView(editorView);
      return editorView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      if (object instanceof View) {
        container.removeView((View) object);
      }
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return "File " + (position + 1);
    }
  }

  
  private void showSingleFileDialog(String code, String title) {
    try {
      MaterialAlertDialogBuilder d = new MaterialAlertDialogBuilder(editor.getContext());
      d.setTitle("Class " + title);

      IdeEditor e = new IdeEditor(editor.getContext());
      e.setText(code);
      e.setEditorLanguage(new JavaLanguage(e));
      e.setEditable(false);
      e.setColorScheme(new EditorColor());
      e.formatCodeAsync();
      e.setPinLineNumber(true);

      d.setView(e);
      d.setPositiveButton("Close", null);
      if (e.getTextActionWindow() != null) e.getTextActionWindow().dismiss();
      d.show();
    } catch (Exception e) {
    }
  }

  @Override
  public String setName() {
    return "Java Type Preview (Stable)";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public String langModel() {
    return ".java";
  }

  @Override
  public void getFileManagerAc(FileManagerActivity a) {}

  @Override
  public void getBaseCompat(BaseCompat a) {}

  int getEditorColor(int id) {
    return editor.getColorScheme().getColor(id);
  }
}
