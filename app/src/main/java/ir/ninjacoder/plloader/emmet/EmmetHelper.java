package ir.ninjacoder.plloader.emmet;

import io.github.rosemoe.sora.data.CompletionItem;
import java.util.List;

public abstract class EmmetHelper {
  public abstract List<CompletionItem> listMethod(String prefix);
}
