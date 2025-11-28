package ir.ninjacoder.plloader.gradle;

public interface Repository {
  String getName();

  String getLatestVersion(String group, String artifact) throws Exception;
}
