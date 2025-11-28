package ir.php.auto;

import io.github.rosemoe.sora.data.CompletionItem;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.GhostIdeAppLoader;

import ir.php.auto.phpfun.PhpConstantParser;
import ir.php.auto.phpfun.PhpFunctionParser;
import java.util.ArrayList;
import java.util.List;
import io.github.rosemoe.sora.interfaces.AutoCompleteProvider;

public class PhpTextHelper implements AutoCompleteProvider {
  CodeEditor editor;

  public PhpTextHelper(CodeEditor editor) {
    this.editor = editor;
  }

  @Override
  public List<CompletionItem> getAutoCompleteItems(
      String prefix, TextAnalyzeResult arg1, int arg2, int arg3) {
    List<CompletionItem> list = new ArrayList<>();
        PhpClassParser.main(prefix, list);
        PhpFunctionParser.main(prefix, list);
        PhpConstantParser.main(prefix, list);
    return list;
  }
}
