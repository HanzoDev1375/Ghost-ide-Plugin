// GoogleRepository.java
package ir.ninjacoder.plloader.gradle;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class GoogleRepository implements Repository {

  private final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(5, TimeUnit.SECONDS)
          .build();

  @Override
  public String getName() {
    return "Google";
  }

  @Override
  public String getLatestVersion(String group, String artifact) throws Exception {
    // برای کتابخانه‌های اندروید گوگل
    if (group.startsWith("androidx.")
        || group.startsWith("com.android.")
        || group.startsWith("com.google.android.")) {
      String url =
          String.format(
              "https://dl.google.com/dl/android/maven2/%s/%s/group-index.xml",
              group.replace('.', '/'), artifact);

      Request request = new Request.Builder().url(url).build();

      try (Response response = client.newCall(request).execute()) {
        if (response.isSuccessful() && response.body() != null) {
          String xmlContent = response.body().string();
          // پردازش XML برای استخراج نسخه
          return extractVersionFromXml(xmlContent, artifact);
        }
      }
    }
    return null;
  }

  private String extractVersionFromXml(String xmlContent, String artifact) {
    // پیاده‌سازی ساده برای استخراج نسخه از XML
    try {
      String pattern = artifact + "=\"([^\"]+)\"";
      java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
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
