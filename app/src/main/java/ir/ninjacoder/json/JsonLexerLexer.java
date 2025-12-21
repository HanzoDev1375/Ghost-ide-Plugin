// Generated from /storage/emulated/0/apk/JsonLexer.g4 by ANTLR 4.13.2
package ir.ninjacoder.json;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({
  "all",
  "warnings",
  "unchecked",
  "unused",
  "cast",
  "CheckReturnValue",
  "this-escape"
})
public class JsonLexerLexer extends Lexer {
  static {
    RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION);
  }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
  public static final int LCURLY = 1,
      RCURLY = 2,
      LBRACKET = 3,
      RBRACKET = 4,
      COLON = 5,
      COMMA = 6,
      LPAREN = 7,
      RPAREN = 8,
      MULTI_LINE_COMMENT = 9,
      LITERAL = 10,
      STRING = 11,
      NUMBER = 12,
      HEXCOLOR = 13,
      SYMBOL = 14,
      IDENTIFIER = 15,
      WS = 16;
  public static String[] channelNames = {"DEFAULT_TOKEN_CHANNEL", "HIDDEN"};

  public static String[] modeNames = {"DEFAULT_MODE"};

  private static String[] makeRuleNames() {
    return new String[] {
      "LCURLY",
      "RCURLY",
      "LBRACKET",
      "RBRACKET",
      "COLON",
      "COMMA",
      "LPAREN",
      "RPAREN",
      "MULTI_LINE_COMMENT",
      "LITERAL",
      "STRING",
      "DOUBLE_QUOTE_CHAR",
      "SINGLE_QUOTE_CHAR",
      "ESCAPE_SEQUENCE",
      "NUMBER",
      "HEXCOLOR",
      "SYMBOL",
      "HEX",
      "INT",
      "DIGIT",
      "EXP",
      "IDENTIFIER",
      "IDENTIFIER_START",
      "IDENTIFIER_PART",
      "UNICODE_SEQUENCE",
      "NEWLINE",
      "WS"
    };
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[] {null, "'{'", "'}'", "'['", "']'", "':'", "','", "'('", "')'"};
  }

  private static final String[] _LITERAL_NAMES = makeLiteralNames();

  private static String[] makeSymbolicNames() {
    return new String[] {
      null,
      "LCURLY",
      "RCURLY",
      "LBRACKET",
      "RBRACKET",
      "COLON",
      "COMMA",
      "LPAREN",
      "RPAREN",
      "MULTI_LINE_COMMENT",
      "LITERAL",
      "STRING",
      "NUMBER",
      "HEXCOLOR",
      "SYMBOL",
      "IDENTIFIER",
      "WS"
    };
  }

  private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
  public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

  /**
   * @deprecated Use {@link #VOCABULARY} instead.
   */
  @Deprecated public static final String[] tokenNames;

  static {
    tokenNames = new String[_SYMBOLIC_NAMES.length];
    for (int i = 0; i < tokenNames.length; i++) {
      tokenNames[i] = VOCABULARY.getLiteralName(i);
      if (tokenNames[i] == null) {
        tokenNames[i] = VOCABULARY.getSymbolicName(i);
      }

      if (tokenNames[i] == null) {
        tokenNames[i] = "<INVALID>";
      }
    }
  }

  @Override
  @Deprecated
  public String[] getTokenNames() {
    return tokenNames;
  }

  @Override
  public Vocabulary getVocabulary() {
    return VOCABULARY;
  }

  public JsonLexerLexer(CharStream input) {
    super(input);
    _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  @Override
  public String getGrammarFileName() {
    return "JsonLexer.g4";
  }

  @Override
  public String[] getRuleNames() {
    return ruleNames;
  }

  @Override
  public String getSerializedATN() {
    return _serializedATN;
  }

  @Override
  public String[] getChannelNames() {
    return channelNames;
  }

  @Override
  public String[] getModeNames() {
    return modeNames;
  }

  @Override
  public ATN getATN() {
    return _ATN;
  }

  public static final String _serializedATN =
      "\u0004\u0000\u0010\u00ea\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"
          + "\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"
          + "\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"
          + "\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"
          + "\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"
          + "\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"
          + "\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"
          + "\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"
          + "\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"
          + "\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"
          + "\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"
          + "\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"
          + "\b\u0001\b\u0005\bL\b\b\n\b\f\bO\t\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"
          + "\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"
          + "\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\tc\b\t\u0001\n\u0001\n\u0005"
          + "\ng\b\n\n\n\f\nj\t\n\u0001\n\u0001\n\u0001\n\u0005\no\b\n\n\n\f\nr\t\n"
          + "\u0001\n\u0003\nu\b\n\u0001\u000b\u0001\u000b\u0003\u000by\b\u000b\u0001"
          + "\f\u0001\f\u0003\f}\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"
          + "\r\u0001\r\u0001\r\u0003\r\u0087\b\r\u0001\u000e\u0001\u000e\u0001\u000e"
          + "\u0005\u000e\u008c\b\u000e\n\u000e\f\u000e\u008f\t\u000e\u0003\u000e\u0091"
          + "\b\u000e\u0001\u000e\u0003\u000e\u0094\b\u000e\u0001\u000e\u0001\u000e"
          + "\u0004\u000e\u0098\b\u000e\u000b\u000e\f\u000e\u0099\u0001\u000e\u0003"
          + "\u000e\u009d\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0004\u000e\u00a2"
          + "\b\u000e\u000b\u000e\f\u000e\u00a3\u0003\u000e\u00a6\b\u000e\u0001\u000f"
          + "\u0001\u000f\u0004\u000f\u00aa\b\u000f\u000b\u000f\f\u000f\u00ab\u0001"
          + "\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"
          + "\u0012\u0005\u0012\u00b5\b\u0012\n\u0012\f\u0012\u00b8\t\u0012\u0003\u0012"
          + "\u00ba\b\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0003\u0014"
          + "\u00c0\b\u0014\u0001\u0014\u0004\u0014\u00c3\b\u0014\u000b\u0014\f\u0014"
          + "\u00c4\u0001\u0015\u0001\u0015\u0005\u0015\u00c9\b\u0015\n\u0015\f\u0015"
          + "\u00cc\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u00d1\b"
          + "\u0016\u0001\u0017\u0001\u0017\u0003\u0017\u00d5\b\u0017\u0001\u0018\u0001"
          + "\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0003"
          + "\u0019\u00de\b\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u00e2\b\u0019"
          + "\u0001\u001a\u0004\u001a\u00e5\b\u001a\u000b\u001a\f\u001a\u00e6\u0001"
          + "\u001a\u0001\u001a\u0001M\u0000\u001b\u0001\u0001\u0003\u0002\u0005\u0003"
          + "\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015"
          + "\u000b\u0017\u0000\u0019\u0000\u001b\u0000\u001d\f\u001f\r!\u000e#\u0000"
          + "%\u0000\'\u0000)\u0000+\u000f-\u0000/\u00001\u00003\u00005\u0010\u0001"
          + "\u0000\u000b\u0004\u0000\n\n\r\r\"\"\\\\\u0004\u0000\n\n\r\r\'\'\\\\\n"
          + "\u0000\"\"\'\'/0\\\\bbffnnrrttvv\u0002\u0000XXxx\u0002\u0000++--\u0003"
          + "\u000009AFaf\u0001\u000019\u0001\u000009\u0002\u0000EEee\u0004\u0000$"
          + "$AZ__az\u0006\u0000\t\n\r\r  \u00a0\u00a0\u2003\u2003\u8000\ufeff\u8000"
          + "\ufeff\u00fc\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000"
          + "\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000"
          + "\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000"
          + "\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000"
          + "\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000"
          + "\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000"
          + "\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000"
          + "+\u0001\u0000\u0000\u0000\u00005\u0001\u0000\u0000\u0000\u00017\u0001"
          + "\u0000\u0000\u0000\u00039\u0001\u0000\u0000\u0000\u0005;\u0001\u0000\u0000"
          + "\u0000\u0007=\u0001\u0000\u0000\u0000\t?\u0001\u0000\u0000\u0000\u000b"
          + "A\u0001\u0000\u0000\u0000\rC\u0001\u0000\u0000\u0000\u000fE\u0001\u0000"
          + "\u0000\u0000\u0011G\u0001\u0000\u0000\u0000\u0013b\u0001\u0000\u0000\u0000"
          + "\u0015t\u0001\u0000\u0000\u0000\u0017x\u0001\u0000\u0000\u0000\u0019|"
          + "\u0001\u0000\u0000\u0000\u001b~\u0001\u0000\u0000\u0000\u001d\u00a5\u0001"
          + "\u0000\u0000\u0000\u001f\u00a7\u0001\u0000\u0000\u0000!\u00ad\u0001\u0000"
          + "\u0000\u0000#\u00af\u0001\u0000\u0000\u0000%\u00b9\u0001\u0000\u0000\u0000"
          + "\'\u00bb\u0001\u0000\u0000\u0000)\u00bd\u0001\u0000\u0000\u0000+\u00c6"
          + "\u0001\u0000\u0000\u0000-\u00d0\u0001\u0000\u0000\u0000/\u00d4\u0001\u0000"
          + "\u0000\u00001\u00d6\u0001\u0000\u0000\u00003\u00e1\u0001\u0000\u0000\u0000"
          + "5\u00e4\u0001\u0000\u0000\u000078\u0005{\u0000\u00008\u0002\u0001\u0000"
          + "\u0000\u00009:\u0005}\u0000\u0000:\u0004\u0001\u0000\u0000\u0000;<\u0005"
          + "[\u0000\u0000<\u0006\u0001\u0000\u0000\u0000=>\u0005]\u0000\u0000>\b\u0001"
          + "\u0000\u0000\u0000?@\u0005:\u0000\u0000@\n\u0001\u0000\u0000\u0000AB\u0005"
          + ",\u0000\u0000B\f\u0001\u0000\u0000\u0000CD\u0005(\u0000\u0000D\u000e\u0001"
          + "\u0000\u0000\u0000EF\u0005)\u0000\u0000F\u0010\u0001\u0000\u0000\u0000"
          + "GH\u0005/\u0000\u0000HI\u0005*\u0000\u0000IM\u0001\u0000\u0000\u0000J"
          + "L\t\u0000\u0000\u0000KJ\u0001\u0000\u0000\u0000LO\u0001\u0000\u0000\u0000"
          + "MN\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000NP\u0001\u0000\u0000"
          + "\u0000OM\u0001\u0000\u0000\u0000PQ\u0005*\u0000\u0000QR\u0005/\u0000\u0000"
          + "RS\u0001\u0000\u0000\u0000ST\u0006\b\u0000\u0000T\u0012\u0001\u0000\u0000"
          + "\u0000UV\u0005t\u0000\u0000VW\u0005r\u0000\u0000WX\u0005u\u0000\u0000"
          + "Xc\u0005e\u0000\u0000YZ\u0005f\u0000\u0000Z[\u0005a\u0000\u0000[\\\u0005"
          + "l\u0000\u0000\\]\u0005s\u0000\u0000]c\u0005e\u0000\u0000^_\u0005n\u0000"
          + "\u0000_`\u0005u\u0000\u0000`a\u0005l\u0000\u0000ac\u0005l\u0000\u0000"
          + "bU\u0001\u0000\u0000\u0000bY\u0001\u0000\u0000\u0000b^\u0001\u0000\u0000"
          + "\u0000c\u0014\u0001\u0000\u0000\u0000dh\u0005\"\u0000\u0000eg\u0003\u0017"
          + "\u000b\u0000fe\u0001\u0000\u0000\u0000gj\u0001\u0000\u0000\u0000hf\u0001"
          + "\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000ik\u0001\u0000\u0000\u0000"
          + "jh\u0001\u0000\u0000\u0000ku\u0005\"\u0000\u0000lp\u0005\'\u0000\u0000"
          + "mo\u0003\u0019\f\u0000nm\u0001\u0000\u0000\u0000or\u0001\u0000\u0000\u0000"
          + "pn\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000qs\u0001\u0000\u0000"
          + "\u0000rp\u0001\u0000\u0000\u0000su\u0005\'\u0000\u0000td\u0001\u0000\u0000"
          + "\u0000tl\u0001\u0000\u0000\u0000u\u0016\u0001\u0000\u0000\u0000vy\b\u0000"
          + "\u0000\u0000wy\u0003\u001b\r\u0000xv\u0001\u0000\u0000\u0000xw\u0001\u0000"
          + "\u0000\u0000y\u0018\u0001\u0000\u0000\u0000z}\b\u0001\u0000\u0000{}\u0003"
          + "\u001b\r\u0000|z\u0001\u0000\u0000\u0000|{\u0001\u0000\u0000\u0000}\u001a"
          + "\u0001\u0000\u0000\u0000~\u0086\u0005\\\u0000\u0000\u007f\u0087\u0003"
          + "3\u0019\u0000\u0080\u0087\u00031\u0018\u0000\u0081\u0087\u0007\u0002\u0000"
          + "\u0000\u0082\u0083\u0005x\u0000\u0000\u0083\u0084\u0003#\u0011\u0000\u0084"
          + "\u0085\u0003#\u0011\u0000\u0085\u0087\u0001\u0000\u0000\u0000\u0086\u007f"
          + "\u0001\u0000\u0000\u0000\u0086\u0080\u0001\u0000\u0000\u0000\u0086\u0081"
          + "\u0001\u0000\u0000\u0000\u0086\u0082\u0001\u0000\u0000\u0000\u0087\u001c"
          + "\u0001\u0000\u0000\u0000\u0088\u0090\u0003%\u0012\u0000\u0089\u008d\u0005"
          + ".\u0000\u0000\u008a\u008c\u0003\'\u0013\u0000\u008b\u008a\u0001\u0000"
          + "\u0000\u0000\u008c\u008f\u0001\u0000\u0000\u0000\u008d\u008b\u0001\u0000"
          + "\u0000\u0000\u008d\u008e\u0001\u0000\u0000\u0000\u008e\u0091\u0001\u0000"
          + "\u0000\u0000\u008f\u008d\u0001\u0000\u0000\u0000\u0090\u0089\u0001\u0000"
          + "\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u0093\u0001\u0000"
          + "\u0000\u0000\u0092\u0094\u0003)\u0014\u0000\u0093\u0092\u0001\u0000\u0000"
          + "\u0000\u0093\u0094\u0001\u0000\u0000\u0000\u0094\u00a6\u0001\u0000\u0000"
          + "\u0000\u0095\u0097\u0005.\u0000\u0000\u0096\u0098\u0003\'\u0013\u0000"
          + "\u0097\u0096\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000"
          + "\u0099\u0097\u0001\u0000\u0000\u0000\u0099\u009a\u0001\u0000\u0000\u0000"
          + "\u009a\u009c\u0001\u0000\u0000\u0000\u009b\u009d\u0003)\u0014\u0000\u009c"
          + "\u009b\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000\u0000\u009d"
          + "\u00a6\u0001\u0000\u0000\u0000\u009e\u009f\u00050\u0000\u0000\u009f\u00a1"
          + "\u0007\u0003\u0000\u0000\u00a0\u00a2\u0003#\u0011\u0000\u00a1\u00a0\u0001"
          + "\u0000\u0000\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001"
          + "\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4\u00a6\u0001"
          + "\u0000\u0000\u0000\u00a5\u0088\u0001\u0000\u0000\u0000\u00a5\u0095\u0001"
          + "\u0000\u0000\u0000\u00a5\u009e\u0001\u0000\u0000\u0000\u00a6\u001e\u0001"
          + "\u0000\u0000\u0000\u00a7\u00a9\u0005#\u0000\u0000\u00a8\u00aa\u0003#\u0011"
          + "\u0000\u00a9\u00a8\u0001\u0000\u0000\u0000\u00aa\u00ab\u0001\u0000\u0000"
          + "\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000\u0000"
          + "\u0000\u00ac \u0001\u0000\u0000\u0000\u00ad\u00ae\u0007\u0004\u0000\u0000"
          + "\u00ae\"\u0001\u0000\u0000\u0000\u00af\u00b0\u0007\u0005\u0000\u0000\u00b0"
          + "$\u0001\u0000\u0000\u0000\u00b1\u00ba\u00050\u0000\u0000\u00b2\u00b6\u0007"
          + "\u0006\u0000\u0000\u00b3\u00b5\u0003\'\u0013\u0000\u00b4\u00b3\u0001\u0000"
          + "\u0000\u0000\u00b5\u00b8\u0001\u0000\u0000\u0000\u00b6\u00b4\u0001\u0000"
          + "\u0000\u0000\u00b6\u00b7\u0001\u0000\u0000\u0000\u00b7\u00ba\u0001\u0000"
          + "\u0000\u0000\u00b8\u00b6\u0001\u0000\u0000\u0000\u00b9\u00b1\u0001\u0000"
          + "\u0000\u0000\u00b9\u00b2\u0001\u0000\u0000\u0000\u00ba&\u0001\u0000\u0000"
          + "\u0000\u00bb\u00bc\u0007\u0007\u0000\u0000\u00bc(\u0001\u0000\u0000\u0000"
          + "\u00bd\u00bf\u0007\b\u0000\u0000\u00be\u00c0\u0003!\u0010\u0000\u00bf"
          + "\u00be\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0"
          + "\u00c2\u0001\u0000\u0000\u0000\u00c1\u00c3\u0003\'\u0013\u0000\u00c2\u00c1"
          + "\u0001\u0000\u0000\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4\u00c2"
          + "\u0001\u0000\u0000\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000\u00c5*\u0001"
          + "\u0000\u0000\u0000\u00c6\u00ca\u0003-\u0016\u0000\u00c7\u00c9\u0003/\u0017"
          + "\u0000\u00c8\u00c7\u0001\u0000\u0000\u0000\u00c9\u00cc\u0001\u0000\u0000"
          + "\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001\u0000\u0000"
          + "\u0000\u00cb,\u0001\u0000\u0000\u0000\u00cc\u00ca\u0001\u0000\u0000\u0000"
          + "\u00cd\u00d1\u0007\t\u0000\u0000\u00ce\u00cf\u0005\\\u0000\u0000\u00cf"
          + "\u00d1\u00031\u0018\u0000\u00d0\u00cd\u0001\u0000\u0000\u0000\u00d0\u00ce"
          + "\u0001\u0000\u0000\u0000\u00d1.\u0001\u0000\u0000\u0000\u00d2\u00d5\u0003"
          + "-\u0016\u0000\u00d3\u00d5\u0003\'\u0013\u0000\u00d4\u00d2\u0001\u0000"
          + "\u0000\u0000\u00d4\u00d3\u0001\u0000\u0000\u0000\u00d50\u0001\u0000\u0000"
          + "\u0000\u00d6\u00d7\u0005u\u0000\u0000\u00d7\u00d8\u0003#\u0011\u0000\u00d8"
          + "\u00d9\u0003#\u0011\u0000\u00d9\u00da\u0003#\u0011\u0000\u00da\u00db\u0003"
          + "#\u0011\u0000\u00db2\u0001\u0000\u0000\u0000\u00dc\u00de\u0005\r\u0000"
          + "\u0000\u00dd\u00dc\u0001\u0000\u0000\u0000\u00dd\u00de\u0001\u0000\u0000"
          + "\u0000\u00de\u00df\u0001\u0000\u0000\u0000\u00df\u00e2\u0005\n\u0000\u0000"
          + "\u00e0\u00e2\u0005\r\u0000\u0000\u00e1\u00dd\u0001\u0000\u0000\u0000\u00e1"
          + "\u00e0\u0001\u0000\u0000\u0000\u00e24\u0001\u0000\u0000\u0000\u00e3\u00e5"
          + "\u0007\n\u0000\u0000\u00e4\u00e3\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001"
          + "\u0000\u0000\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e6\u00e7\u0001"
          + "\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000\u00e8\u00e9\u0006"
          + "\u001a\u0000\u0000\u00e96\u0001\u0000\u0000\u0000\u001b\u0000Mbhptx|\u0086"
          + "\u008d\u0090\u0093\u0099\u009c\u00a3\u00a5\u00ab\u00b6\u00b9\u00bf\u00c4"
          + "\u00ca\u00d0\u00d4\u00dd\u00e1\u00e6\u0001\u0006\u0000\u0000";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
