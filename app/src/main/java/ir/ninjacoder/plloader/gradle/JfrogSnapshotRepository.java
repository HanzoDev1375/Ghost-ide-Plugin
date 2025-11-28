package ir.ninjacoder.plloader.gradle;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.concurrent.TimeUnit;

public class JfrogSnapshotRepository implements Repository {

  private final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(5, TimeUnit.SECONDS)
          .build();

  @Override
  public String getName() {
    return "JFrog Snapshot";
  }

  @Override
  public String getLatestVersion(String group, String artifact) throws Exception {
    String url =
        String.format(
            "https://oss.jfrog.org/artifactory/oss-snapshot-local/%s/%s/maven-metadata.xml",
            group.replace('.', '/'), artifact);

    Request request =
        new Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (compatible; GradleUpdateChecker/1.0)")
            .build();

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
      var doc = Jsoup.parse(xmlContent);

      var versionElement = doc.select("versioning > latest").first();
      if (versionElement != null) {
        return versionElement.text().trim();
      }
      var versionsElement = doc.select("versioning > versions").first();
      if (versionsElement != null) {
        var lastVersion = versionsElement.select("version").last();
        if (lastVersion != null) {
          return lastVersion.text().trim();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
