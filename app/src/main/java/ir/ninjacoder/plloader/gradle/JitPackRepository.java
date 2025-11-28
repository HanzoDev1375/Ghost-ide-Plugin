package ir.ninjacoder.plloader.gradle;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class JitPackRepository implements Repository {

  private final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(5, TimeUnit.SECONDS)
          .build();

  @Override
  public String getName() {
    return "JitPack";
  }

  @Override
  public String getLatestVersion(String group, String artifact) throws Exception {
    String url = String.format("https://jitpack.io/api/builds/%s/%s/latest", group, artifact);

    Request request = new Request.Builder().url(url).header("Accept", "application/json").build();

    try (Response response = client.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        String responseBody = response.body().string();
        JSONObject json = new JSONObject(responseBody);
        return json.optString("version", null);
      }
    }

    return getLatestVersionFromGitHub(group, artifact);
  }

  private String getLatestVersionFromGitHub(String group, String artifact) throws Exception {
    String url = String.format("https://jitpack.io/api/github/%s/%s/latest", group, artifact);

    Request request = new Request.Builder().url(url).header("Accept", "application/json").build();

    try (Response response = client.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        String responseBody = response.body().string();
        JSONObject json = new JSONObject(responseBody);
        return json.optString("version", null);
      }
    }
    return null;
  }
}
