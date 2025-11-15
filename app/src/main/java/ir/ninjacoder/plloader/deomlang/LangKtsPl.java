package ir.ninjacoder.plloader.deomlang;

import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.prograsssheet.listchild.Child;

public class LangKtsPl implements PluginManagerCompat {
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
    Child child =
        new Child(
            langModel(), "/storage/emulated/0/GhostWebIDE/plugins/ktslang/ic_material_kotlin.png");
    ac.addChild(child);
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {
    Child child =
        new Child(
            true,
            () -> {
              editor.setEditorLanguage(new LangKts(editor));
            },
            1902);
    ac.addChild(child);
  }

  @Override
  public String langModel() {
    return ".kts";
  }
}
