package ir.ninjacoder.json5;

import io.github.rosemoe.sora.interfaces.AutoCompleteProvider;
import io.github.rosemoe.sora.interfaces.NewlineHandler;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.widget.SymbolPairMatch;
import io.github.rosemoe.sora.interfaces.EditorLanguage;
import ir.ninjacoder.json5.editor.CodeAz;
import ir.ninjacoder.json5.editor.JSON5Formatter;

public class JsonLang implements EditorLanguage {

  @Override
  public CodeAnalyzer getAnalyzer() {
    return new CodeAz();
  }

  @Override
  public AutoCompleteProvider getAutoCompleteProvider() {
    return null;
  }

  @Override
  public boolean isAutoCompleteChar(char arg0) {
    return false;
  }

  @Override
  public int getIndentAdvance(String it) {
    return 0;
  }

  @Override
  public boolean useTab() {
    return true;
  }

  @Override
  public CharSequence format(CharSequence arg0) {
    try {
    	return JSON5Formatter.format(arg0.toString(), JSON5Formatter.FormatOptions.defaultOptions());
    } catch(Exception err) {
    	return arg0;
    }
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
