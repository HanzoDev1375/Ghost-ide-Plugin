package ir.ninjacoder.plloader;

import io.github.rosemoe.sora.widget.CodeEditor;

public interface PluginManagerCompat {

  void getEditor(CodeEditor editor);

  String setName();

  boolean hasuseing();
}
