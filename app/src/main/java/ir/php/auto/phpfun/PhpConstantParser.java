package ir.php.auto.phpfun;

import com.google.gson.Gson;
import io.github.rosemoe.sora.data.CompletionItem;
import ir.ninjacoder.ghostide.core.utils.FileUtil;
import java.util.List;

public class PhpConstantParser {

  static PhpConstants parseJsonFile() {
    Gson gson = new Gson();
    return gson.fromJson(
        FileUtil.readFile("/storage/emulated/0/GhostWebIDE/plugins/betterphp/data/php_constants.json"), PhpConstants.class);
  }

  public static void main(String prefix, List<CompletionItem> mod) {
    try {
      PhpConstants constants = parseJsonFile();
      constants
          .getConstants()
          .forEach(
              (categoryName, category) -> {
                category
                    .getConstants()
                    .forEach(
                        constantName -> {
                          if (constantName.contains(prefix)) {
                            mod.add(
                                new CompletionItem(
                                    constantName,
                                    constantName,
                                    "constant " + categoryName + "::" + constantName));
                          }
                        });
              });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
