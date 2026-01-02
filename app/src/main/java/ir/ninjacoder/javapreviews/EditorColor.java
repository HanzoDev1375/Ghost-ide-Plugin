package ir.ninjacoder.javapreviews;

import android.graphics.Color;
import io.github.rosemoe.sora.widget.EditorColorScheme;



public class EditorColor extends EditorColorScheme {
  @Override
  public void applyDefault() {
    super.applyDefault();

    // ===== Backgrounds =====
    setColor(WHOLE_BACKGROUND, 0xFF0F111A);
    setColor(LINE_NUMBER_BACKGROUND, 0xFF0C0E16);
    setColor(CURRENT_LINE, 0x221E2233);
    setColor(SELECTED_TEXT_BACKGROUND, 0x334A5FFF);

    // ===== Text =====
    setColor(TEXT_NORMAL, 0xFFE6E6E6);
    setColor(TEXT_SELECTED, 0xFFFFFFFF);
    setColor(black,Color.BLACK);
    setColor(white,Color.WHITE);

    // ===== Java =====
    setColor(javakeyword, 0xFF82AAFF);
    setColor(javakeywordoprator, 0xFF82AAFF);
    setColor(javatype, 0xFFC792EA);
    setColor(javafun, 0xFF7FDBCA);
    setColor(javafield, 0xFFE6E6E6);
    setColor(javaparament, 0xFFFFCB6B);
    setColor(javanumber, 0xFFF78C6C);
    setColor(javastring, 0xFFC3E88D);
    setColor(javaoprator, 0xFF89DDFF);

    // ===== JavaScript =====
    setColor(jskeyword, color("#ff7b10"));
    setColor(jsfun, color("#ff5027"));
    setColor(jsattr, color("#830220"));
    setColor(jsoprator, color("#ff9720"));
    setColor(jsstring, 0xFFC3E88D);

    // ===== TypeScript =====
    setColor(tskeyword, color("#cb8201"));
    setColor(tsattr, color("#ffb281"));
    setColor(tssymbols, 0xFF89DDFF);
    setColor(tscolormatch1, color("#ffb402"));
    setColor(tscolormatch2, color("#c72020"));
    setColor(tscolormatch3, color("#10b100"));
    setColor(tscolormatch4, color("#fb8200"));
    setColor(tscolormatch5, color("#bb2017"));
    setColor(tscolormatch6, 0xFF4EC9B0);
    setColor(tscolormatch7, 0xFFC586C0);

    // ===== HTML (بدون CSS) =====
    setColor(htmltag, 0xFF82AAFF);
    setColor(htmlattr, 0xFFFFCB6B);
    setColor(htmlattrname, 0xFF7FDBCA);
    setColor(htmlstr, 0xFFC3E88D);
    setColor(htmlsymbol, 0xFF89DDFF);
    setColor(htmlblockhash, 0xFF5C6370);
    setColor(htmlblocknormal, 0xFFE6E6E6);

    // ===== Python =====
    setColor(pykeyword, 0xFF82AAFF);
    setColor(pystring, 0xFFC3E88D);
    setColor(pynumber, 0xFFF78C6C);
    setColor(pysymbol, 0xFF89DDFF);
    setColor(pycolormatch1, 0xFF4EC9B0);
    setColor(pycolormatch2, 0xFFC586C0);
    setColor(pycolormatch3, 0xFFFFCB6B);
    setColor(pycolormatch4, 0xFF82AAFF);

    // ===== PHP =====
    setColor(phpkeyword, 0xFF82AAFF);
    setColor(phpattr, 0xFFFFCB6B);
    setColor(phpsymbol, 0xFF89DDFF);
    setColor(phphtmlattr, 0xFF7FDBCA);
    setColor(phphtmlkeyword, 0xFFC792EA);
    setColor(phpcolormatch1, 0xFF4EC9B0);
    setColor(phpcolormatch2, 0xFFC586C0);
    setColor(phpcolormatch3, 0xFFFFCB6B);
    setColor(phpcolormatch4, 0xFFF78C6C);
    setColor(phpcolormatch5, 0xFF82AAFF);
    setColor(phpcolormatch6, 0xFF7FDBCA);

    // ===== Generic Tokens =====
    setColor(KEYWORD, 0xFF82AAFF);
    setColor(FUNCTION_NAME, 0xFF7FDBCA);
    setColor(IDENTIFIER_NAME, 0xFFE6E6E6);
    setColor(IDENTIFIER_VAR, 0xFFD4D4D4);
    setColor(LITERAL, 0xFFC3E88D);
    setColor(OPERATOR, 0xFF89DDFF);
    setColor(COMMENT, 0xFF5C6370);
    setColor(ANNOTATION, 0xFF82AAFF);

    // ===== Brackets =====
    setColor(breaklevel1, 0xFF82AAFF);
    setColor(breaklevel2, 0xFFC792EA);
    setColor(breaklevel3, 0xFF7FDBCA);
    setColor(breaklevel4, 0xFFFFCB6B);
    setColor(breaklevel5, 0xFFF78C6C);
    setColor(breaklevel6, 0xFF4EC9B0);
    setColor(breaklevel7, 0xFFC586C0);
    setColor(breaklevel8, 0xFF89DDFF);

    // ===== UI =====
    setColor(LINE_NUMBER, 0xFF5C6370);
    setColor(LINE_DIVIDER, 0x221E2233);
    setColor(SCROLL_BAR_THUMB, 0xFF3B3F51);
    setColor(SCROLL_BAR_THUMB_PRESSED, 0xFF565F89);
    setColor(BLOCK_LINE, 0xFF2A2F3A);
    setColor(BLOCK_LINE_CURRENT, 0xFF3A3F4A);
    setColor(BLOCK_LINE_SELECTOR, 0x643A93FF);

    // ===== Search =====
    setColor(searchcolor1, 0xFF264F78);
    setColor(searchcolor2, 0xFF3A7AFE);
    setColor(searchcolor3, 0xFF9CDCFE);
    setColor(searchcolor4, 0xFFCE9178);
    setColor(searchcolor5, 0xFF4EC9B0);
    setColor(searchcolor6, 0xFFC586C0);

    // ===== Problems =====
    setColor(PROBLEM_ERROR, 0xFFFF5370);
    setColor(PROBLEM_WARNING, 0xFFFFC777);
    setColor(PROBLEM_TYPO, 0xFFFFFFFF);

    // ===== Logs / Static =====
    setColor(COLOR_DEBUG, 0xFF4EC9B0);
    setColor(COLOR_LOG, 0xFF82AAFF);
    setColor(COLOR_WARNING, 0xFFFFC777);
    setColor(COLOR_ERROR, 0xFFFF5370);
    setColor(COLOR_TIP, 0xFFC586C0);
    setColor(STATIC_SPAN_BACKGROUND, 0x33264F78);
    setColor(STATIC_SPAN_FOREGROUND, 0xFFE6E6E6);
  }

  int color(String c) {
    return Color.parseColor(c);
  }
}
