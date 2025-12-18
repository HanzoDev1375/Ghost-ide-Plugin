package ir.ninjacoder.difflang;

import io.github.rosemoe.sora.interfaces.AutoCompleteProvider;
import io.github.rosemoe.sora.interfaces.NewlineHandler;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.widget.SymbolPairMatch;
import io.github.rosemoe.sora.interfaces.EditorLanguage;

public class DiffLangEditor implements EditorLanguage {

  @Override
  public CodeAnalyzer getAnalyzer() {
    return new DiffAz();
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
