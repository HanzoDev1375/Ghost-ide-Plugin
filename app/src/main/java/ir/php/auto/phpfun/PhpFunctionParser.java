package ir.php.auto.phpfun;

import com.google.gson.Gson;
import io.github.rosemoe.sora.data.CompletionItem;
import ir.ninjacoder.ghostide.core.utils.FileUtil;
import java.util.List;

public class PhpFunctionParser {

 static PhpFunctions parseJsonFile() {
    Gson gson = new Gson();
    return gson.fromJson(
        FileUtil.readFile("/storage/emulated/0/GhostWebIDE/plugins/betterphp/data/php_functions.json"),
        PhpFunctions.class);
  }

  public static void main(String prefix, List<CompletionItem> mod) {
    try {
      PhpFunctions functions = parseJsonFile();
      functions
          .getInternal()
          .forEach(
              funcName -> {
                if (funcName.contains(prefix)) {
                  mod.add(new CompletionItem(funcName, funcName, "function " + funcName));
                }
              });
      functions
          .getUser()
          .forEach(
              funcName -> {
                if (funcName.contains(prefix)) {
                  mod.add(new CompletionItem(funcName, funcName, "user_function " + funcName));
                }
              });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
