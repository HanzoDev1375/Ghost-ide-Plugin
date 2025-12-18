package ir.ninjacoder.json5.editor;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.misc.*;
import java.io.StringReader;
import java.util.*;

public class JSON5Formatter {

  public static String format(String json5String, FormatOptions options) {
    try {
      JSON5Lexer lexer = new JSON5Lexer(CharStreams.fromReader(new StringReader(json5String)));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      JSON5Parser parser = new JSON5Parser(tokens);

      parser.removeErrorListeners();
      parser.addErrorListener(
          new BaseErrorListener() {
            @Override
            public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
              //throw new RuntimeException("Invalid JSON5: " + msg);
            }
          });

      JSON5Parser.Json5Context tree = parser.json5();
      FormatterVisitor visitor = new FormatterVisitor(options);
      return visitor.visit(tree);

    } catch (Exception e) {
      // اگر پارس نشد، حداقل indent ساده انجام دهیم
      return simpleFormat(json5String, options);
    }
  }

  private static String simpleFormat(String json5String, FormatOptions options) {
    StringBuilder result = new StringBuilder();
    int indentLevel = 0;
    boolean inString = false;
    char stringChar = 0;
    boolean escapeNext = false;

    for (int i = 0; i < json5String.length(); i++) {
      char c = json5String.charAt(i);

      if (escapeNext) {
        result.append(c);
        escapeNext = false;
        continue;
      }

      if (inString) {
        result.append(c);
        if (c == '\\') {
          escapeNext = true;
        } else if (c == stringChar) {
          inString = false;
        }
        continue;
      }

      switch (c) {
        case '"':
        case '\'':
          inString = true;
          stringChar = c;
          result.append(c);
          break;

        case '{':
        case '[':
          result.append(c);
          indentLevel++;
          result.append(newlineAndIndent(indentLevel, options));
          break;

        case '}':
        case ']':
          indentLevel--;
          result.append(newlineAndIndent(indentLevel, options));
          result.append(c);
          break;

        case ',':
          result.append(c);
          result.append(newlineAndIndent(indentLevel, options));
          break;

        case ':':
          result.append(c);
          if (options.spaceBeforeColon) result.append(' ');
          break;

        default:
          result.append(c);
          break;
      }
    }

    return result.toString();
  }

  private static String newlineAndIndent(int level, FormatOptions options) {
    if (options.collapse) return " ";

    StringBuilder sb = new StringBuilder();
    sb.append('\n');
    for (int i = 0; i < level * options.indentSize; i++) {
      sb.append(options.useTabs ? '\t' : ' ');
    }
    return sb.toString();
  }

  public static class FormatOptions {
    public int indentSize = 2;
    public boolean useTabs = false;
    public boolean collapse = false;
    public boolean spaceBeforeColon = false;
    public boolean keepComments = true;
    public boolean sortKeys = false;

    public static FormatOptions defaultOptions() {
      return new FormatOptions();
    }

    public static FormatOptions compact() {
      FormatOptions opts = new FormatOptions();
      opts.collapse = true;
      opts.spaceBeforeColon = false;
      return opts;
    }

    public static FormatOptions pretty() {
      FormatOptions opts = new FormatOptions();
      opts.indentSize = 4;
      opts.useTabs = false;
      opts.spaceBeforeColon = false;
      return opts;
    }
  }

  private static class FormatterVisitor extends JSON5BaseVisitor<String> {
    private final FormatOptions options;
    private int indentLevel = 0;

    public FormatterVisitor(FormatOptions options) {
      this.options = options;
    }

    private String indent() {
      if (options.collapse) return "";

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < indentLevel * options.indentSize; i++) {
        sb.append(options.useTabs ? '\t' : ' ');
      }
      return sb.toString();
    }

    private String nl() {
      return options.collapse ? "" : "\n";
    }

    @Override
    public String visitJson5(JSON5Parser.Json5Context ctx) {
      if (ctx.value() == null) return "";
      return visitValue(ctx.value());
    }

    @Override
    public String visitObj(JSON5Parser.ObjContext ctx) {
      if (ctx.pair().isEmpty()) {
        return "{}";
      }

      StringBuilder sb = new StringBuilder();
      sb.append('{');
      indentLevel++;

      List<JSON5Parser.PairContext> pairs = ctx.pair();
      if (options.sortKeys) {
        pairs = new ArrayList<>(pairs);
        pairs.sort(Comparator.comparing(p -> getKeyText(p.key())));
      }

      for (int i = 0; i < pairs.size(); i++) {
        sb.append(nl()).append(indent());
        sb.append(visitKey(pairs.get(i).key()));
        sb.append(options.spaceBeforeColon ? " : " : ":");
        sb.append(visitValue(pairs.get(i).value()));

        if (i < pairs.size() - 1 || ctx.COMMA(i) != null) {
          sb.append(',');
        }
      }

      indentLevel--;
      if (!pairs.isEmpty()) {
        sb.append(nl()).append(indent());
      }
      sb.append('}');

      return sb.toString();
    }

    private String getKeyText(JSON5Parser.KeyContext key) {
      if (key.STRING() != null) {
        return key.STRING().getText();
      } else if (key.IDENTIFIER() != null) {
        return key.IDENTIFIER().getText();
      } else if (key.LITERAL() != null) {
        return key.LITERAL().getText();
      } else if (key.NUMERIC_LITERAL() != null) {
        return key.NUMERIC_LITERAL().getText();
      }
      return "";
    }

    @Override
    public String visitArr(JSON5Parser.ArrContext ctx) {
      if (ctx.value().isEmpty()) {
        return "[]";
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      indentLevel++;

      List<JSON5Parser.ValueContext> values = ctx.value();
      for (int i = 0; i < values.size(); i++) {
        sb.append(nl()).append(indent());
        sb.append(visitValue(values.get(i)));

        if (i < values.size() - 1 || ctx.COMMA(i) != null) {
          sb.append(',');
        }
      }

      indentLevel--;
      if (!values.isEmpty()) {
        sb.append(nl()).append(indent());
      }
      sb.append(']');

      return sb.toString();
    }

    @Override
    public String visitKey(JSON5Parser.KeyContext ctx) {
      if (ctx.STRING() != null) {
        return ctx.STRING().getText();
      } else if (ctx.IDENTIFIER() != null) {
        return ctx.IDENTIFIER().getText();
      } else if (ctx.LITERAL() != null) {
        return ctx.LITERAL().getText();
      } else if (ctx.NUMERIC_LITERAL() != null) {
        return ctx.NUMERIC_LITERAL().getText();
      }
      return "";
    }

    @Override
    public String visitValue(JSON5Parser.ValueContext ctx) {
      if (ctx.STRING() != null) {
        return ctx.STRING().getText();
      } else if (ctx.number() != null) {
        return visitNumber(ctx.number());
      } else if (ctx.obj() != null) {
        return visitObj(ctx.obj());
      } else if (ctx.arr() != null) {
        return visitArr(ctx.arr());
      } else if (ctx.LITERAL() != null) {
        return ctx.LITERAL().getText();
      }
      return "";
    }

    @Override
    public String visitNumber(JSON5Parser.NumberContext ctx) {
      StringBuilder sb = new StringBuilder();
      if (ctx.SYMBOL() != null) {
        sb.append(ctx.SYMBOL().getText());
      }
      if (ctx.NUMERIC_LITERAL() != null) {
        sb.append(ctx.NUMERIC_LITERAL().getText());
      } else if (ctx.NUMBER() != null) {
        sb.append(ctx.NUMBER().getText());
      }
      return sb.toString();
    }

    @Override
    public String visitPair(JSON5Parser.PairContext ctx) {
      return visitKey(ctx.key())
          + (options.spaceBeforeColon ? " : " : ": ")
          + visitValue(ctx.value());
    }
  }

  // روش استفاده:
  public static void main(String[] args) {
    String messyJson5 = "{name:'John',age:25,active:true,hobbies:['coding','music']}";

    // فرمت استاندارد
    String formatted = JSON5Formatter.format(messyJson5, FormatOptions.defaultOptions());
    System.out.println("Formatted:");
    System.out.println(formatted);

    // فرمت فشرده
    String compact = JSON5Formatter.format(messyJson5, FormatOptions.compact());
    System.out.println("\nCompact:");
    System.out.println(compact);

    // فرمت زیبا
    String pretty = JSON5Formatter.format(messyJson5, FormatOptions.pretty());
    System.out.println("\nPretty:");
    System.out.println(pretty);
  }
}
