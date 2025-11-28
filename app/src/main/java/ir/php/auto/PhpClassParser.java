package ir.php.auto;

import com.google.gson.Gson;
import io.github.rosemoe.sora.data.CompletionItem;
import ir.ninjacoder.ghostide.core.utils.FileUtil;
import java.util.List;

public class PhpClassParser {

   static PhpClassDetails parseJsonFile() {
    Gson gson = new Gson();
    return gson.fromJson(
        FileUtil.readFile("/storage/emulated/0/GhostWebIDE/plugins/betterphp/data/php_class_details.json"),
        PhpClassDetails.class);
  }

  public static void main(String prefix, List<CompletionItem> mod) {
    try {
      PhpClassDetails classDetails = parseJsonFile();

      // کال کردن متدهای دیگه
      findMethodsByClass(classDetails, "Exception", prefix, mod);
      findMethodsByClass(classDetails, "DateTime", prefix, mod);
      findConstructors(classDetails, prefix, mod);

      // برای بقیه کلاس‌ها هم
      classDetails
          .getClassMethods()
          .forEach(
              (className, methods) -> {
                methods.forEach(
                    method -> {
                      if (method.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                        mod.add(
                            new CompletionItem(
                                method.getName(),
                                method.getName(),
                                className + "::" + method.getName()));
                      }
                    });
              });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void findMethodsByClass(
      PhpClassDetails classDetails, String className, String prefix, List<CompletionItem> mod) {
    if (classDetails.getClassMethods().containsKey(className)) {
      classDetails
          .getClassMethods()
          .get(className)
          .forEach(
              method -> {
                if (method.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                  mod.add(
                      new CompletionItem(
                          method.getName(), method.getName(), className + "::" + method.getName()));
                }
              });
    }
  }

  private static void findConstructors(
      PhpClassDetails classDetails, String prefix, List<CompletionItem> mod) {
    classDetails
        .getClassMethods()
        .forEach(
            (className, methods) -> {
              methods.stream()
                  .filter(ClassMethod::isConstructor)
                  .forEach(
                      constructor -> {
                        if (constructor.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                          mod.add(
                              new CompletionItem(
                                  constructor.getName(),
                                  constructor.getName(),
                                  className + "::" + constructor.getName()));
                        }
                      });
            });
  }
}
