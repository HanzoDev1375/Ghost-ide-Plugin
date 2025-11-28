package ir.php;

import android.graphics.Color;
import androidx.activity.EdgeToEdge;
import io.github.rosemoe.sora.interfaces.AutoCompleteProvider;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.langs.php.PHPLanguage;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.IdeEditor;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.plloader.ObjectFile;
import ir.php.auto.PhpTextHelper;

public class BetterPhp implements PluginManagerCompat {
  private CodeEditorActivity editorac;
  private CodeEditor editor;

  @Override
  public void getEditor(CodeEditor editor) {
    this.editor = editor;
    editor.postDelayed(
        () -> {
          var php =
              new PHPLanguage((IdeEditor) editor) {

                @Override
                public CodeAnalyzer getAnalyzer() {
                  return new PhpCodeAnalyzer();
                }

                @Override
                public AutoCompleteProvider getAutoCompleteProvider() {
                  return new PhpTextHelper(editor);
                }
              };
          editor.setEditorLanguage(php);
        },
        1000);
    editor.getColorScheme().setColor(EditorColorScheme.aqua, Color.parseColor("#00FFFF"));
    editor.getColorScheme().setColor(EditorColorScheme.azure, Color.parseColor("#F0FFFF"));
    editor.getColorScheme().setColor(EditorColorScheme.beige, Color.parseColor("#F5F5DC"));
    editor.getColorScheme().setColor(EditorColorScheme.bisque, Color.parseColor("#FFE4C4"));
    editor.getColorScheme().setColor(EditorColorScheme.black, Color.parseColor("#000000"));
    editor.getColorScheme().setColor(EditorColorScheme.blanchedalmond, Color.parseColor("#FFEBCD"));
    editor.getColorScheme().setColor(EditorColorScheme.blue, Color.parseColor("#0000FF"));
    editor.getColorScheme().setColor(EditorColorScheme.blueviolet, Color.parseColor("#8A2BE2"));
    editor.getColorScheme().setColor(EditorColorScheme.brown, Color.parseColor("#A52A2A"));
    editor.getColorScheme().setColor(EditorColorScheme.burlywood, Color.parseColor("#DEB887"));
    editor.getColorScheme().setColor(EditorColorScheme.cadetblue, Color.parseColor("#5F9EA0"));
    editor.getColorScheme().setColor(EditorColorScheme.chartreuse, Color.parseColor("#7FFF00"));
    editor.getColorScheme().setColor(EditorColorScheme.chocolate, Color.parseColor("#D2691E"));
    editor.getColorScheme().setColor(EditorColorScheme.coral, Color.parseColor("#FF7F50"));
    editor.getColorScheme().setColor(EditorColorScheme.cornflowerblue, Color.parseColor("#6495ED"));
    editor.getColorScheme().setColor(EditorColorScheme.cornsilk, Color.parseColor("#FFF8DC"));
    editor.getColorScheme().setColor(EditorColorScheme.crimson, Color.parseColor("#DC143C"));
    editor.getColorScheme().setColor(EditorColorScheme.cyan, Color.parseColor("#00FFFF"));
    editor.getColorScheme().setColor(EditorColorScheme.darkblue, Color.parseColor("#00008B"));
    editor.getColorScheme().setColor(EditorColorScheme.darkcyan, Color.parseColor("#008B8B"));
    editor.getColorScheme().setColor(EditorColorScheme.darkgoldenrod, Color.parseColor("#B8860B"));
    editor.getColorScheme().setColor(EditorColorScheme.darkgray, Color.parseColor("#A9A9A9"));
    editor.getColorScheme().setColor(EditorColorScheme.darkgreen, Color.parseColor("#006400"));
    editor.getColorScheme().setColor(EditorColorScheme.darkkhaki, Color.parseColor("#BDB76B"));
    editor.getColorScheme().setColor(EditorColorScheme.darkmagenta, Color.parseColor("#8B008B"));
    editor.getColorScheme().setColor(EditorColorScheme.darkolivegreen, Color.parseColor("#556B2F"));
    editor.getColorScheme().setColor(EditorColorScheme.darkorange, Color.parseColor("#FF8C00"));
    editor.getColorScheme().setColor(EditorColorScheme.darkorchid, Color.parseColor("#9932CC"));
    editor.getColorScheme().setColor(EditorColorScheme.darkred, Color.parseColor("#8B0000"));
    editor.getColorScheme().setColor(EditorColorScheme.darksalmon, Color.parseColor("#E9967A"));
    editor.getColorScheme().setColor(EditorColorScheme.darkseagreen, Color.parseColor("#8FBC8F"));
    editor.getColorScheme().setColor(EditorColorScheme.darkslateblue, Color.parseColor("#483D8B"));
    editor.getColorScheme().setColor(EditorColorScheme.darkslategray, Color.parseColor("#2F4F4F"));
    editor.getColorScheme().setColor(EditorColorScheme.darkturquoise, Color.parseColor("#00CED1"));
    editor.getColorScheme().setColor(EditorColorScheme.darkviolet, Color.parseColor("#9400D3"));
    editor.getColorScheme().setColor(EditorColorScheme.deeppink, Color.parseColor("#FF1493"));
    editor.getColorScheme().setColor(EditorColorScheme.deepskyblue, Color.parseColor("#00BFFF"));
    editor.getColorScheme().setColor(EditorColorScheme.dimgray, Color.parseColor("#696969"));
    editor.getColorScheme().setColor(EditorColorScheme.dodgerblue, Color.parseColor("#1E90FF"));
    editor.getColorScheme().setColor(EditorColorScheme.firebrick, Color.parseColor("#B22222"));
    editor.getColorScheme().setColor(EditorColorScheme.floralwhite, Color.parseColor("#FFFAF0"));
    editor.getColorScheme().setColor(EditorColorScheme.forestgreen, Color.parseColor("#228B22"));
    editor.getColorScheme().setColor(EditorColorScheme.fuchsia, Color.parseColor("#FF00FF"));
    editor.getColorScheme().setColor(EditorColorScheme.gainsboro, Color.parseColor("#DCDCDC"));
    editor.getColorScheme().setColor(EditorColorScheme.ghostwhite, Color.parseColor("#F8F8FF"));
    editor.getColorScheme().setColor(EditorColorScheme.gold, Color.parseColor("#FFD700"));
    editor.getColorScheme().setColor(EditorColorScheme.goldenrod, Color.parseColor("#DAA520"));
    editor.getColorScheme().setColor(EditorColorScheme.gray, Color.parseColor("#808080"));
    editor.getColorScheme().setColor(EditorColorScheme.green, Color.parseColor("#008000"));
    editor.getColorScheme().setColor(EditorColorScheme.greenyellow, Color.parseColor("#ADFF2F"));
    editor.getColorScheme().setColor(EditorColorScheme.honeydew, Color.parseColor("#F0FFF0"));
    editor.getColorScheme().setColor(EditorColorScheme.hotpink, Color.parseColor("#FF69B4"));
    editor.getColorScheme().setColor(EditorColorScheme.indianred, Color.parseColor("#CD5C5C"));
    editor.getColorScheme().setColor(EditorColorScheme.indigo, Color.parseColor("#4B0082"));
    editor.getColorScheme().setColor(EditorColorScheme.ivory, Color.parseColor("#FFFFF0"));
    editor.getColorScheme().setColor(EditorColorScheme.khaki, Color.parseColor("#F0E68C"));
    editor.getColorScheme().setColor(EditorColorScheme.lavender, Color.parseColor("#E6E6FA"));
    editor.getColorScheme().setColor(EditorColorScheme.lavenderblush, Color.parseColor("#FFF0F5"));
    editor.getColorScheme().setColor(EditorColorScheme.lawngreen, Color.parseColor("#7CFC00"));
    editor.getColorScheme().setColor(EditorColorScheme.lemonchiffon, Color.parseColor("#FFFACD"));
    editor.getColorScheme().setColor(EditorColorScheme.lightblue, Color.parseColor("#ADD8E6"));
    editor.getColorScheme().setColor(EditorColorScheme.lightcoral, Color.parseColor("#F08080"));
    editor.getColorScheme().setColor(EditorColorScheme.lightcyan, Color.parseColor("#E0FFFF"));
    editor
        .getColorScheme()
        .setColor(EditorColorScheme.lightgoldenrodyellow, Color.parseColor("#FAFAD2"));
    editor.getColorScheme().setColor(EditorColorScheme.lightgray, Color.parseColor("#D3D3D3"));
    editor.getColorScheme().setColor(EditorColorScheme.lightgreen, Color.parseColor("#90EE90"));
    editor.getColorScheme().setColor(EditorColorScheme.lightpink, Color.parseColor("#FFB6C1"));
    editor.getColorScheme().setColor(EditorColorScheme.lightsalmon, Color.parseColor("#FFA07A"));
    editor.getColorScheme().setColor(EditorColorScheme.lightseagreen, Color.parseColor("#20B2AA"));
    editor.getColorScheme().setColor(EditorColorScheme.lightskyblue, Color.parseColor("#87CEFA"));
    editor.getColorScheme().setColor(EditorColorScheme.lightslategray, Color.parseColor("#778899"));
    editor.getColorScheme().setColor(EditorColorScheme.lightsteelblue, Color.parseColor("#B0C4DE"));
    editor.getColorScheme().setColor(EditorColorScheme.lightyellow, Color.parseColor("#FFFFE0"));
    editor.getColorScheme().setColor(EditorColorScheme.lime, Color.parseColor("#00FF00"));
    editor.getColorScheme().setColor(EditorColorScheme.limegreen, Color.parseColor("#32CD32"));
    editor.getColorScheme().setColor(EditorColorScheme.linen, Color.parseColor("#FAF0E6"));
    editor.getColorScheme().setColor(EditorColorScheme.magenta, Color.parseColor("#FF00FF"));
    editor.getColorScheme().setColor(EditorColorScheme.maroon, Color.parseColor("#800000"));
    editor
        .getColorScheme()
        .setColor(EditorColorScheme.mediumaquamarine, Color.parseColor("#66CDAA"));
    editor.getColorScheme().setColor(EditorColorScheme.mediumblue, Color.parseColor("#0000CD"));
    editor.getColorScheme().setColor(EditorColorScheme.mediumorchid, Color.parseColor("#BA55D3"));
    editor.getColorScheme().setColor(EditorColorScheme.mediumpurple, Color.parseColor("#9370DB"));
    editor.getColorScheme().setColor(EditorColorScheme.mediumseagreen, Color.parseColor("#3CB371"));
    editor
        .getColorScheme()
        .setColor(EditorColorScheme.mediumslateblue, Color.parseColor("#7B68EE"));
    editor
        .getColorScheme()
        .setColor(EditorColorScheme.mediumspringgreen, Color.parseColor("#00FA9A"));
    editor
        .getColorScheme()
        .setColor(EditorColorScheme.mediumturquoise, Color.parseColor("#48D1CC"));
    editor
        .getColorScheme()
        .setColor(EditorColorScheme.mediumvioletred, Color.parseColor("#C71585"));
    editor.getColorScheme().setColor(EditorColorScheme.midnightblue, Color.parseColor("#191970"));
    editor.getColorScheme().setColor(EditorColorScheme.mintcream, Color.parseColor("#F5FFFA"));
    editor.getColorScheme().setColor(EditorColorScheme.mistyrose, Color.parseColor("#FFE4E1"));
    editor.getColorScheme().setColor(EditorColorScheme.moccasin, Color.parseColor("#FFE4B5"));
    editor.getColorScheme().setColor(EditorColorScheme.navajowhite, Color.parseColor("#FFDEAD"));
    editor.getColorScheme().setColor(EditorColorScheme.navy, Color.parseColor("#000080"));
    editor.getColorScheme().setColor(EditorColorScheme.oldlace, Color.parseColor("#FDF5E6"));
    editor.getColorScheme().setColor(EditorColorScheme.olive, Color.parseColor("#808000"));
    editor.getColorScheme().setColor(EditorColorScheme.olivedrab, Color.parseColor("#6B8E23"));
    editor.getColorScheme().setColor(EditorColorScheme.orange, Color.parseColor("#FFA500"));
  }

  @Override
  public String setName() {
    return getClass().getName();
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity filamnager) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity editoractivity) {
    this.editorac = editoractivity;
  }

  @Override
  public String langModel() {
    return ".php";
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
