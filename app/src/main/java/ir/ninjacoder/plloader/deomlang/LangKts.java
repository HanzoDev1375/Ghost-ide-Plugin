package ir.ninjacoder.plloader.deomlang;

import io.github.rosemoe.sora.interfaces.AutoCompleteProvider;
import io.github.rosemoe.sora.interfaces.NewlineHandler;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.SymbolPairMatch;
import io.github.rosemoe.sora.interfaces.EditorLanguage;

public class LangKts implements EditorLanguage {
  private CodeEditor editor;

  public LangKts(CodeEditor editor) {
    this.editor = editor;
  }

  @Override
  public CodeAnalyzer getAnalyzer() {
    return new GradleCodeAnalyzerkts(editor);
  }

  @Override
  public AutoCompleteProvider getAutoCompleteProvider() {
    return null;
  }

  @Override
  public boolean isAutoCompleteChar(char arg0) {
    return arg0 == '.';
  }

  @Override
  public int getIndentAdvance(String arg0) {
    return 0;
  }

  @Override
  public boolean useTab() {
    return true;
  }

  @Override
  public CharSequence format(CharSequence arg0) {
    return arg0;
  }

  @Override
  public SymbolPairMatch getSymbolPairs() {

    return null;
  }

  @Override
  public NewlineHandler[] getNewlineHandlers() {
    return null;
  }
}
