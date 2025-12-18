package ir.ninjacoder.difflang;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.prograsssheet.listchild.Child;

public class Diff implements PluginManagerCompat {
  CodeEditor editor;

  @Override
  public void getEditor(CodeEditor editor) {
    this.editor = editor;
  }

  @Override
  public String setName() {
    return getClass().getSimpleName();
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity ac) {

    String extensions = langModel();

    String firstExtension = extensions;
    if (extensions.contains(",")) {
      firstExtension = extensions.split(",")[0].trim();
    }

    Child child =
        new Child(firstExtension, "/storage/emulated/0/GhostWebIDE/plugins/difflang/icon.png");

    ac.addChild(child);
    if (extensions.contains(",")) {
      String[] allExtensions = extensions.split(",");
      for (int i = 1; i < allExtensions.length; i++) {
        String ext = allExtensions[i].trim();
        if (!ext.isEmpty()) {
          Child extraChild =
              new Child(ext, "/storage/emulated/0/GhostWebIDE/plugins/difflang/icon.png");
          ac.addChild(extraChild);
        }
      }
    }
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {
    Child child = new Child(false, () -> {
      Log.e(getClass().getSimpleName(),"اینجا خالی باشه بهتره");
      //اینجا رو خالی بزار چون هین ما جا به شدن تب به مشکل میخوری
    }, 1100);
    ac.addChild(child);
    new Handler()
        .postDelayed(
            () -> {
              try {
                if (editor != null) {
                  editor.getColorScheme().setColor(EditorColorScheme.green, Color.GREEN);
                  editor.getColorScheme().setColor(EditorColorScheme.magenta, Color.MAGENTA);
                  editor
                      .getColorScheme()
                      .setColor(EditorColorScheme.greenyellow, Color.parseColor("#FFD9FF00"));
                  editor
                      .getColorScheme()
                      .setColor(EditorColorScheme.goldenrod, Color.parseColor("#FFFFCC00"));
                  editor.getColorScheme().setColor(EditorColorScheme.gray, Color.GRAY);
                  editor
                      .getColorScheme()
                      .setColor(EditorColorScheme.orange, Color.parseColor("#FFFF9900"));
                  editor.setEditorLanguage(new DiffLangEditor());
                  editor.analyze(true);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            },
            500);
  }

  @Override
  public void getBaseCompat(BaseCompat base) {}

  @Override
  public String langModel() {
    return ".path,.diff,.rej";
  }
}
