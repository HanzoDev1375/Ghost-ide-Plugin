package ir.ninjacoder.plloader.betterhtml;

import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.langs.html.HTMLLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.IdeEditor;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.ghostide.core.utils.ObjectUtils;
import ir.ninjacoder.ghostide.core.widget.data.ColorChecker;

public class BetterHtml implements PluginManagerCompat {

  private CodeEditor currentEditor;

  @Override
  public void getEditor(CodeEditor editor) {
    this.currentEditor = editor;

    editor.postDelayed(
        () -> {
          var html =
              new HTMLLanguage((IdeEditor) editor) {
                @Override
                public CodeAnalyzer getAnalyzer() {
                  return new HTMLAnalyzerCompat((IdeEditor) editor);
                }

                @Override
                public CharSequence format(CharSequence arg0) {

                  if (ObjectUtils.hasCpuArm64()) { //run in nodejs
                    var htmlformat = new HTMLBeautifier();
                    if (htmlformat.isReady()) {
                      editor.postDelayed(htmlformat::destroy,1000);
                      return htmlformat.beautifyHTMLMixed(arg0.toString());
                      
                    }
                  } else {
                    return ColorChecker.fromatWeb(arg0.toString());
                  }
                  return arg0;
                }
              };
          editor.setEditorLanguage(html);
        },
        1000);
  }

  @Override
  public String setName() {
    return "BetterHTML with Ghost Text";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity activity) {}

  @Override
  public void getBaseCompat(BaseCompat arg0) {}

  @Override
  public String langModel() {
    return ".html";
  }
}
