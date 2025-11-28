package ir.ninjacoder.plloader;

import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;

public class ObjectFile {

  public static boolean getType(CodeEditorActivity ac, String type) {
    return ac.getcurrentFileType().endsWith("." + type);
  }
}
