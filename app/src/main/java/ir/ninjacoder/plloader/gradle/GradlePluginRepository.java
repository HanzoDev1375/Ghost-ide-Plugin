// GradlePluginRepository.java
package ir.ninjacoder.plloader.gradle;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class GradlePluginRepository implements Repository {

  private final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(5, TimeUnit.SECONDS)
          .build();

  @Override
  public String getName() {
    return "Gradle Plugin Portal";
  }

  @Override
  public String getLatestVersion(String group, String artifact) throws Exception {
    // برای پلاگین‌های Gradle
    String url =
        String.format(
            "https://plugins.gradle.org/m2/%s/%s/maven-metadata.xml",
            group.replace('.', '/'), artifact);

    Request request = new Request.Builder().url(url).build();

    try (Response response = client.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        String xmlContent = response.body().string();
        return extractLatestVersionFromMetadata(xmlContent);
      }
    }
    return null;
  }

  private String extractLatestVersionFromMetadata(String xmlContent) {
    try {
      // استخراج آخرین نسخه از maven-metadata.xml
      String versionPattern = "<latest>([^<]+)</latest>";
      java.util.regex.Pattern r = java.util.regex.Pattern.compile(versionPattern);
      java.util.regex.Matcher m = r.matcher(xmlContent);
      if (m.find()) {
        return m.group(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
