package ir.ninjacoder.json;

import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.langs.json.JsonLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;

import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

public class JsonPlugin implements PluginManagerCompat {

  private CodeEditor editor;
  private CodeEditorActivity activity;

  @Override
  public void getEditor(CodeEditor arg0) {
    this.editor = arg0;
  }

  @Override
  public String setName() {
    return "Better Json";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getBaseCompat(BaseCompat arg0) {}

  @Override
  public String langModel() {
    return ".json,.ghost";
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {

    editor.postDelayed(
        () -> {
          try {
            JsonLanguage lang =
                new JsonLanguage(editor) {
                  @Override
                  public CodeAnalyzer getAnalyzer() {
                    return new CodeAz();
                  }
                };
            editor.setEditorLanguage(lang);
          } catch (Exception ignored) {
          }
        },
        500);
  }
}
