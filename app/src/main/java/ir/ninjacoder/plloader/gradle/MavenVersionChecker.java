package ir.ninjacoder.plloader.gradle;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class MavenVersionChecker {
  private static final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(5, TimeUnit.SECONDS)
          .build();

  public static String getLatestVersion(String group, String artifact) {
    try {
      String url =
          String.format(
              "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&rows=1&wt=json",
              group, artifact);

      Request request = new Request.Builder().url(url).build();

      try (Response response = client.newCall(request).execute()) {
        if (response.isSuccessful() && response.body() != null) {
          String responseBody = response.body().string();
          JSONObject json = new JSONObject(responseBody);
          return json.getJSONObject("response")
              .getJSONArray("docs")
              .getJSONObject(0)
              .getString("latestVersion");
        }
      }
    } catch (Exception e) {
      Log.e("MavenChecker", "Error: " + e.getMessage());
    }
    return null;
  }
}
