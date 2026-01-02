package ir.pypreview;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
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
import io.github.rosemoe.sora.langs.python.PythonLang;
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

public class PythonPreview implements PluginManagerCompat {

  private static final long HOVER_DELAY = 350;
  private static final long MAX_FILE_SIZE = 200 * 1024;
  private static final int MAX_CACHE = 40;

  private final String PythonPath = "/storage/emulated/0/GhostWebIDE/plugins/pypreview/pydata/";
  private final String fontPath =
      "/storage/emulated/0/GhostWebIDE/plugins/pypreview/font/ghostfont.ttf";

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

  private void buildFileNameIndex() {
    if (a == null) return;
    fileNameIndex.clear();
    File current = new File(a.getPathBytab());
    List<File> roots = new ArrayList<>();
    roots.add(current);
    roots.add(new File(PythonPath));
    roots.forEach(
        it -> {
          indexDir(it);
        });
  }

  private void indexDir(File dir) {
    File[] files = dir.listFiles();
    if (files == null) return;

    for (File f : files) {
      if (f.isDirectory()) {
        indexDir(f);
      } else if (f.getName().endsWith(".py")) {
        String name = f.getName().replace(".py", "");
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
      //  if (word == null || word.trim().isEmpty() || word.matches("^_+$")) return null;
      // if (word.length() < 2) return null;
      // if (!Character.isUpperCase(word.charAt(0))) return null;

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
          String fileName = files.get(i).getName().replace(".py", "");
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
        editorView.setEditorLanguage(new PythonLang(editorView));
        editorView.setEditable(false);
        editorView.setColorScheme(new EditorColor());
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
      d.setTitle("PyClass " + title);

      IdeEditor e = new IdeEditor(editor.getContext());
      e.setText(code);
      e.setEditorLanguage(new PythonLang(e));
      e.setEditable(false);
      e.setColorScheme(new EditorColor());
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
    return "Py Type Preview (Stable)";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public String langModel() {
    return ".py";
  }

  @Override
  public void getFileManagerAc(FileManagerActivity a) {}

  @Override
  public void getBaseCompat(BaseCompat a) {}

  int getEditorColor(int id) {
    return editor.getColorScheme().getColor(id);
  }

  public class EditorColor extends EditorColorScheme {
    @Override
    public void applyDefault() {
      super.applyDefault();

      // ===== Backgrounds =====
      setColor(WHOLE_BACKGROUND, 0xFF0F111A);
      setColor(LINE_NUMBER_BACKGROUND, 0xFF0C0E16);
      setColor(CURRENT_LINE, 0x221E2233);
      setColor(SELECTED_TEXT_BACKGROUND, 0x334A5FFF);

      // ===== Text =====
      setColor(TEXT_NORMAL, 0xFFE6E6E6);
      setColor(TEXT_SELECTED, 0xFFFFFFFF);
      setColor(black, Color.BLACK);
      setColor(white, Color.WHITE);

      // ===== Java =====
      setColor(javakeyword, 0xFF82AAFF);
      setColor(javakeywordoprator, 0xFF82AAFF);
      setColor(javatype, 0xFFC792EA);
      setColor(javafun, 0xFF7FDBCA);
      setColor(javafield, 0xFFE6E6E6);
      setColor(javaparament, 0xFFFFCB6B);
      setColor(javanumber, 0xFFF78C6C);
      setColor(javastring, 0xFFC3E88D);
      setColor(javaoprator, 0xFF89DDFF);

      // ===== JavaScript =====
      setColor(jskeyword, color("#ff7b10"));
      setColor(jsfun, color("#ff5027"));
      setColor(jsattr, color("#830220"));
      setColor(jsoprator, color("#ff9720"));
      setColor(jsstring, 0xFFC3E88D);

      // ===== TypeScript =====
      setColor(tskeyword, color("#cb8201"));
      setColor(tsattr, color("#ffb281"));
      setColor(tssymbols, 0xFF89DDFF);
      setColor(tscolormatch1, color("#ffb402"));
      setColor(tscolormatch2, color("#c72020"));
      setColor(tscolormatch3, color("#10b100"));
      setColor(tscolormatch4, color("#fb8200"));
      setColor(tscolormatch5, color("#bb2017"));
      setColor(tscolormatch6, 0xFF4EC9B0);
      setColor(tscolormatch7, 0xFFC586C0);

      // ===== HTML (بدون CSS) =====
      setColor(htmltag, 0xFF82AAFF);
      setColor(htmlattr, 0xFFFFCB6B);
      setColor(htmlattrname, 0xFF7FDBCA);
      setColor(htmlstr, 0xFFC3E88D);
      setColor(htmlsymbol, 0xFF89DDFF);
      setColor(htmlblockhash, 0xFF5C6370);
      setColor(htmlblocknormal, 0xFFE6E6E6);

      // ===== Python =====
      setColor(pykeyword, 0xFF82AAFF);
      setColor(pystring, 0xFFC3E88D);
      setColor(pynumber, 0xFFF78C6C);
      setColor(pysymbol, 0xFF89DDFF);
      setColor(pycolormatch1, 0xFF4EC9B0);
      setColor(pycolormatch2, 0xFFC586C0);
      setColor(pycolormatch3, 0xFFFFCB6B);
      setColor(pycolormatch4, 0xFF82AAFF);

      // ===== PHP =====
      setColor(phpkeyword, 0xFF82AAFF);
      setColor(phpattr, 0xFFFFCB6B);
      setColor(phpsymbol, 0xFF89DDFF);
      setColor(phphtmlattr, 0xFF7FDBCA);
      setColor(phphtmlkeyword, 0xFFC792EA);
      setColor(phpcolormatch1, 0xFF4EC9B0);
      setColor(phpcolormatch2, 0xFFC586C0);
      setColor(phpcolormatch3, 0xFFFFCB6B);
      setColor(phpcolormatch4, 0xFFF78C6C);
      setColor(phpcolormatch5, 0xFF82AAFF);
      setColor(phpcolormatch6, 0xFF7FDBCA);

      // ===== Generic Tokens =====
      setColor(KEYWORD, 0xFF82AAFF);
      setColor(FUNCTION_NAME, 0xFF7FDBCA);
      setColor(IDENTIFIER_NAME, 0xFFE6E6E6);
      setColor(IDENTIFIER_VAR, 0xFFD4D4D4);
      setColor(LITERAL, 0xFFC3E88D);
      setColor(OPERATOR, 0xFF89DDFF);
      setColor(COMMENT, 0xFF5C6370);
      setColor(ANNOTATION, 0xFF82AAFF);

      // ===== Brackets =====
      setColor(breaklevel1, 0xFF82AAFF);
      setColor(breaklevel2, 0xFFC792EA);
      setColor(breaklevel3, 0xFF7FDBCA);
      setColor(breaklevel4, 0xFFFFCB6B);
      setColor(breaklevel5, 0xFFF78C6C);
      setColor(breaklevel6, 0xFF4EC9B0);
      setColor(breaklevel7, 0xFFC586C0);
      setColor(breaklevel8, 0xFF89DDFF);

      // ===== UI =====
      setColor(LINE_NUMBER, 0xFF5C6370);
      setColor(LINE_DIVIDER, 0x221E2233);
      setColor(SCROLL_BAR_THUMB, 0xFF3B3F51);
      setColor(SCROLL_BAR_THUMB_PRESSED, 0xFF565F89);
      setColor(BLOCK_LINE, 0xFF2A2F3A);
      setColor(BLOCK_LINE_CURRENT, 0xFF3A3F4A);
      setColor(BLOCK_LINE_SELECTOR, 0x643A93FF);

      // ===== Search =====
      setColor(searchcolor1, 0xFF264F78);
      setColor(searchcolor2, 0xFF3A7AFE);
      setColor(searchcolor3, 0xFF9CDCFE);
      setColor(searchcolor4, 0xFFCE9178);
      setColor(searchcolor5, 0xFF4EC9B0);
      setColor(searchcolor6, 0xFFC586C0);

      // ===== Problems =====
      setColor(PROBLEM_ERROR, 0xFFFF5370);
      setColor(PROBLEM_WARNING, 0xFFFFC777);
      setColor(PROBLEM_TYPO, 0xFFFFFFFF);

      // ===== Logs / Static =====
      setColor(COLOR_DEBUG, 0xFF4EC9B0);
      setColor(COLOR_LOG, 0xFF82AAFF);
      setColor(COLOR_WARNING, 0xFFFFC777);
      setColor(COLOR_ERROR, 0xFFFF5370);
      setColor(COLOR_TIP, 0xFFC586C0);
      setColor(STATIC_SPAN_BACKGROUND, 0x33264F78);
      setColor(STATIC_SPAN_FOREGROUND, 0xFFE6E6E6);
    }

    int color(String c) {
      return Color.parseColor(c);
    }
  }
}
