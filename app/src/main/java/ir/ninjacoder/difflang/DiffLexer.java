// Generated from /storage/emulated/0/apk/DiffLexer.g4 by ANTLR 4.13.2
package ir.ninjacoder.difflang;

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
public class DiffLexer extends Lexer {
  static {
    RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION);
  }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
  public static final int DIFF = 1,
      INDEX = 2,
      OLD_FILE = 3,
      NEW_FILE = 4,
      HUNK_HEADER = 5,
      ADDED_LINE = 6,
      REMOVED_LINE = 7,
      NO_NEWLINE = 8,
      ID = 9,
      NEWLINE = 10,
      WS = 11;
  public static String[] channelNames = {"DEFAULT_TOKEN_CHANNEL", "HIDDEN"};

  public static String[] modeNames = {"DEFAULT_MODE"};

  private static String[] makeRuleNames() {
    return new String[] {
      "DIFF",
      "INDEX",
      "OLD_FILE",
      "NEW_FILE",
      "HUNK_HEADER",
      "ADDED_LINE",
      "REMOVED_LINE",
      "NO_NEWLINE",
      "HEX",
      "INT",
      "ID",
      "NEWLINE",
      "WS"
    };
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[] {null, null, null, "'---'", "'+++'", null, "'+'", "'-'"};
  }

  private static final String[] _LITERAL_NAMES = makeLiteralNames();

  private static String[] makeSymbolicNames() {
    return new String[] {
      null,
      "DIFF",
      "INDEX",
      "OLD_FILE",
      "NEW_FILE",
      "HUNK_HEADER",
      "ADDED_LINE",
      "REMOVED_LINE",
      "NO_NEWLINE",
      "ID",
      "NEWLINE",
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

  public DiffLexer(CharStream input) {
    super(input);
    _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  @Override
  public String getGrammarFileName() {
    return "DiffLexer.g4";
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
      "\u0004\u0000\u000b\u008d\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"
          + "\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"
          + "\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"
          + "\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"
          + "\u000b\u0007\u000b\u0002\f\u0007\f\u0001\u0000\u0001\u0000\u0001\u0000"
          + "\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"
          + "\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"
          + "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"
          + "\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"
          + "\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"
          + "\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"
          + "\u0001\u0004\u0001\u0004\u0003\u0004E\b\u0004\u0001\u0004\u0001\u0004"
          + "\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004L\b\u0004\u0001\u0004"
          + "\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006"
          + "\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"
          + "\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"
          + "\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"
          + "\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"
          + "\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0004"
          + "\bs\b\b\u000b\b\f\bt\u0001\t\u0004\tx\b\t\u000b\t\f\ty\u0001\n\u0001\n"
          + "\u0004\n~\b\n\u000b\n\f\n\u007f\u0001\u000b\u0003\u000b\u0083\b\u000b"
          + "\u0001\u000b\u0001\u000b\u0001\f\u0004\f\u0088\b\f\u000b\f\f\f\u0089\u0001"
          + "\f\u0001\f\u0000\u0000\r\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004"
          + "\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\u0000\u0013\u0000\u0015\t\u0017"
          + "\n\u0019\u000b\u0001\u0000\u0005\u0003\u000009AFaf\u0001\u000009\u0001"
          + "\u0000az\u0004\u0000--09AZaz\u0002\u0000\t\t  \u0091\u0000\u0001\u0001"
          + "\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001"
          + "\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000"
          + "\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000"
          + "\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000"
          + "\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000"
          + "\u0000\u0001\u001b\u0001\u0000\u0000\u0000\u0003\'\u0001\u0000\u0000\u0000"
          + "\u00054\u0001\u0000\u0000\u0000\u00078\u0001\u0000\u0000\u0000\t<\u0001"
          + "\u0000\u0000\u0000\u000bQ\u0001\u0000\u0000\u0000\rS\u0001\u0000\u0000"
          + "\u0000\u000fU\u0001\u0000\u0000\u0000\u0011r\u0001\u0000\u0000\u0000\u0013"
          + "w\u0001\u0000\u0000\u0000\u0015{\u0001\u0000\u0000\u0000\u0017\u0082\u0001"
          + "\u0000\u0000\u0000\u0019\u0087\u0001\u0000\u0000\u0000\u001b\u001c\u0005"
          + "d\u0000\u0000\u001c\u001d\u0005i\u0000\u0000\u001d\u001e\u0005f\u0000"
          + "\u0000\u001e\u001f\u0005f\u0000\u0000\u001f \u0001\u0000\u0000\u0000 "
          + "!\u0005 \u0000\u0000!\"\u0005-\u0000\u0000\"#\u0005-\u0000\u0000#$\u0005"
          + "g\u0000\u0000$%\u0005i\u0000\u0000%&\u0005t\u0000\u0000&\u0002\u0001\u0000"
          + "\u0000\u0000\'(\u0005i\u0000\u0000()\u0005n\u0000\u0000)*\u0005d\u0000"
          + "\u0000*+\u0005e\u0000\u0000+,\u0005x\u0000\u0000,-\u0001\u0000\u0000\u0000"
          + "-.\u0005 \u0000\u0000./\u0003\u0011\b\u0000/0\u0005.\u0000\u000001\u0005"
          + ".\u0000\u000012\u0001\u0000\u0000\u000023\u0003\u0011\b\u00003\u0004\u0001"
          + "\u0000\u0000\u000045\u0005-\u0000\u000056\u0005-\u0000\u000067\u0005-"
          + "\u0000\u00007\u0006\u0001\u0000\u0000\u000089\u0005+\u0000\u00009:\u0005"
          + "+\u0000\u0000:;\u0005+\u0000\u0000;\b\u0001\u0000\u0000\u0000<=\u0005"
          + "@\u0000\u0000=>\u0005@\u0000\u0000>?\u0001\u0000\u0000\u0000?@\u0005 "
          + "\u0000\u0000@A\u0003\r\u0006\u0000AD\u0003\u0013\t\u0000BC\u0005,\u0000"
          + "\u0000CE\u0003\u0013\t\u0000DB\u0001\u0000\u0000\u0000DE\u0001\u0000\u0000"
          + "\u0000EF\u0001\u0000\u0000\u0000FG\u0005 \u0000\u0000GH\u0003\u000b\u0005"
          + "\u0000HK\u0003\u0013\t\u0000IJ\u0005,\u0000\u0000JL\u0003\u0013\t\u0000"
          + "KI\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000"
          + "\u0000MN\u0005 \u0000\u0000NO\u0005@\u0000\u0000OP\u0005@\u0000\u0000"
          + "P\n\u0001\u0000\u0000\u0000QR\u0005+\u0000\u0000R\f\u0001\u0000\u0000"
          + "\u0000ST\u0005-\u0000\u0000T\u000e\u0001\u0000\u0000\u0000UV\u0005\\\u0000"
          + "\u0000VW\u0005 \u0000\u0000WX\u0005N\u0000\u0000XY\u0005o\u0000\u0000"
          + "YZ\u0005 \u0000\u0000Z[\u0005n\u0000\u0000[\\\u0005e\u0000\u0000\\]\u0005"
          + "w\u0000\u0000]^\u0005l\u0000\u0000^_\u0005i\u0000\u0000_`\u0005n\u0000"
          + "\u0000`a\u0005e\u0000\u0000ab\u0005 \u0000\u0000bc\u0005a\u0000\u0000"
          + "cd\u0005t\u0000\u0000de\u0005 \u0000\u0000ef\u0005e\u0000\u0000fg\u0005"
          + "n\u0000\u0000gh\u0005d\u0000\u0000hi\u0005 \u0000\u0000ij\u0005o\u0000"
          + "\u0000jk\u0005f\u0000\u0000kl\u0005 \u0000\u0000lm\u0005f\u0000\u0000"
          + "mn\u0005i\u0000\u0000no\u0005l\u0000\u0000op\u0005e\u0000\u0000p\u0010"
          + "\u0001\u0000\u0000\u0000qs\u0007\u0000\u0000\u0000rq\u0001\u0000\u0000"
          + "\u0000st\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000\u0000tu\u0001\u0000"
          + "\u0000\u0000u\u0012\u0001\u0000\u0000\u0000vx\u0007\u0001\u0000\u0000"
          + "wv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000yw\u0001\u0000\u0000"
          + "\u0000yz\u0001\u0000\u0000\u0000z\u0014\u0001\u0000\u0000\u0000{}\u0007"
          + "\u0002\u0000\u0000|~\u0007\u0003\u0000\u0000}|\u0001\u0000\u0000\u0000"
          + "~\u007f\u0001\u0000\u0000\u0000\u007f}\u0001\u0000\u0000\u0000\u007f\u0080"
          + "\u0001\u0000\u0000\u0000\u0080\u0016\u0001\u0000\u0000\u0000\u0081\u0083"
          + "\u0005\r\u0000\u0000\u0082\u0081\u0001\u0000\u0000\u0000\u0082\u0083\u0001"
          + "\u0000\u0000\u0000\u0083\u0084\u0001\u0000\u0000\u0000\u0084\u0085\u0005"
          + "\n\u0000\u0000\u0085\u0018\u0001\u0000\u0000\u0000\u0086\u0088\u0007\u0004"
          + "\u0000\u0000\u0087\u0086\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000"
          + "\u0000\u0000\u0089\u0087\u0001\u0000\u0000\u0000\u0089\u008a\u0001\u0000"
          + "\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000\u008b\u008c\u0006\f\u0000"
          + "\u0000\u008c\u001a\u0001\u0000\u0000\u0000\b\u0000DKty\u007f\u0082\u0089"
          + "\u0001\u0006\u0000\u0000";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
