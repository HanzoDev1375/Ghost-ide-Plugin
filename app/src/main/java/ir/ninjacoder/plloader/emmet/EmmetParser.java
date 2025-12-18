package ir.ninjacoder.plloader.emmet;

import io.github.rosemoe.sora.data.CompletionItem;
import java.util.ArrayList;
import java.util.List;

public class EmmetParser {

  public static List<CompletionItem> parser(String prefix) {
    List<CompletionItem> is = new ArrayList<>();

    // فقط featureهایی که با pattern مطابقت دارند را اضافه کن
    if (containsChild(prefix)) {
      is.addAll(new ChildFeature().listMethod(prefix));
    }

    if (containsSibling(prefix)) {
      is.addAll(new SiblingFeature().listMethod(prefix));
    }

    if (containsClimbUp(prefix)) {
      is.addAll(new ClimbUpFeature().listMethod(prefix));
    }

    return is;
  }

  private static boolean containsChild(String prefix) {
    return prefix.contains(">");
  }

  private static boolean containsSibling(String prefix) {
    return prefix.contains("+");
  }

  private static boolean containsClimbUp(String prefix) {
    return prefix.contains("^");
  }
}
