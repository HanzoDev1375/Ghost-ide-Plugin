package ir.ninjacoder.plloader.gradle;

public class DependencyInfo {

  final int line, start;
  final String group, artifact, version, fullMatch;

  public DependencyInfo(
      int line, int start, String group, String artifact, String version, String fullMatch) {
    this.line = line;
    this.start = start;
    this.group = group;
    this.artifact = artifact;
    this.version = version;
    this.fullMatch = fullMatch;
  }
}
