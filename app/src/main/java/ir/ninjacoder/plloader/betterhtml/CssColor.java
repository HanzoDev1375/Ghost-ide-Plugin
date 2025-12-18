package ir.ninjacoder.plloader.betterhtml;

import java.io.Serializable;

class CssColor implements Serializable {
  private final String colorName;
  private final String cssColor;

  public CssColor(String colorName, String cssColor) {
    this.colorName = colorName;
    this.cssColor = cssColor;
  }

  public String getColorName() {
    return this.colorName;
  }

  public String getCssColor() {
    return this.cssColor;
  }
}
