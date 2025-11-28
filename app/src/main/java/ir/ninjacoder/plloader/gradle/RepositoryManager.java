// RepositoryManager.java
package ir.ninjacoder.plloader.gradle;

import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.util.*;

public class RepositoryManager {

  public interface VersionCheckCallback {
    void onVersionFound(String version);

    void onError(String error);
  }

  private static final List<Repository> repositories =
      Arrays.asList(
          new MavenCentralRepository(),
          new GoogleRepository(),
          new JCenterRepository(),
          new GradlePluginRepository(),
          new JitPackRepository(),
          new JfrogSnapshotRepository());

  private static final String CACHE_FILE_NAME = "dependency_cache.json";
  private static Map<String, CacheEntry> cache = new HashMap<>();
  private static boolean cacheLoaded = false;

  private static class CacheEntry {
    String version;
    long timestamp;
    boolean isOutdated;

    CacheEntry(String version, boolean isOutdated) {
      this.version = version;
      this.isOutdated = isOutdated;
      this.timestamp = System.currentTimeMillis();
    }
  }

  public static void getLatestVersion(
      String group, String artifact, VersionCheckCallback callback) {
    String cacheKey = group + ":" + artifact;

    // اول از کش بررسی کن
    loadCacheIfNeeded();
    CacheEntry cached = cache.get(cacheKey);

    if (cached != null && !isCacheExpired(cached)) {
      if (cached.isOutdated) {
        callback.onVersionFound(cached.version);
      } else {
        callback.onVersionFound(null); // یعنی آپدیتی نیست
      }
      return;
    }

    // اگر در کش نبود یا منقضی شده، از مخازن چک کن
    new Thread(
            () -> {
              for (Repository repo : repositories) {
                try {
                  String version = repo.getLatestVersion(group, artifact);
                  if (version != null && !version.isEmpty()) {
                    // در کش ذخیره کن
                    boolean isOutdated = true;
                    cache.put(cacheKey, new CacheEntry(version, isOutdated));
                    saveCache();

                    callback.onVersionFound(version);
                    return;
                  }
                } catch (Exception e) {
                  Log.d(
                      "RepositoryManager",
                      "Failed to check " + repo.getName() + ": " + e.getMessage());
                }
              }

              // اگر هیچ مخزنی نسخه‌ای پیدا نکرد
              cache.put(cacheKey, new CacheEntry(null, false));
              saveCache();
              callback.onError("Version not found in any repository");
            })
        .start();
  }

  public static boolean isDependencyOutdated(String group, String artifact, String currentVersion) {
    String cacheKey = group + ":" + artifact;
    loadCacheIfNeeded();

    CacheEntry cached = cache.get(cacheKey);
    if (cached != null && !isCacheExpired(cached)) {
      return cached.isOutdated && cached.version != null && !cached.version.equals(currentVersion);
    }
    return false;
  }

  public static String getCachedLatestVersion(String group, String artifact) {
    String cacheKey = group + ":" + artifact;
    loadCacheIfNeeded();

    CacheEntry cached = cache.get(cacheKey);
    if (cached != null && !isCacheExpired(cached) && cached.isOutdated) {
      return cached.version;
    }
    return null;
  }

  private static void loadCacheIfNeeded() {
    if (cacheLoaded) return;

    try {
      File cacheFile = new File(getCacheDir(), CACHE_FILE_NAME);
      if (cacheFile.exists()) {
        BufferedReader reader = new BufferedReader(new FileReader(cacheFile));
        StringBuilder jsonContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          jsonContent.append(line);
        }
        reader.close();

        JSONObject json = new JSONObject(jsonContent.toString());
        JSONArray cacheArray = json.getJSONArray("dependencies");

        for (int i = 0; i < cacheArray.length(); i++) {
          JSONObject item = cacheArray.getJSONObject(i);
          String key = item.getString("key");
          String version = item.optString("version", null);
          boolean isOutdated = item.getBoolean("isOutdated");
          long timestamp = item.getLong("timestamp");

          CacheEntry entry = new CacheEntry(version, isOutdated);
          entry.timestamp = timestamp;
          cache.put(key, entry);
        }
      }
    } catch (Exception e) {
      Log.e("RepositoryManager", "Error loading cache: " + e.getMessage());
    }

    cacheLoaded = true;
  }

  private static void saveCache() {
    try {
      JSONObject json = new JSONObject();
      JSONArray cacheArray = new JSONArray();

      for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
        JSONObject item = new JSONObject();
        item.put("key", entry.getKey());
        item.put("version", entry.getValue().version);
        item.put("isOutdated", entry.getValue().isOutdated);
        item.put("timestamp", entry.getValue().timestamp);
        cacheArray.put(item);
      }

      json.put("dependencies", cacheArray);

      File cacheFile = new File(getCacheDir(), CACHE_FILE_NAME);
      FileWriter writer = new FileWriter(cacheFile);
      writer.write(json.toString());
      writer.close();

    } catch (Exception e) {
      Log.e("RepositoryManager", "Error saving cache: " + e.getMessage());
    }
  }

  private static boolean isCacheExpired(CacheEntry entry) {
    return (System.currentTimeMillis() - entry.timestamp) > (24 * 60 * 60 * 1000);
  }

  private static File getCacheDir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  public static void clearCache() {
    cache.clear();
    cacheLoaded = false;
    try {
      File cacheFile = new File(getCacheDir(), CACHE_FILE_NAME);
      if (cacheFile.exists()) {
        cacheFile.delete();
      }
    } catch (Exception e) {
      Log.e("RepositoryManager", "Error clearing cache: " + e.getMessage());
    }
  }
}
