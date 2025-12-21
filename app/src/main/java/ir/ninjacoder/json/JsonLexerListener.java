// Generated from /storage/emulated/0/apk/JsonLexer.g4 by ANTLR 4.13.2
package ir.ninjacoder.json;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JsonLexerParser}.
 */
public interface JsonLexerListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JsonLexerParser#json}.
	 * @param ctx the parse tree
	 */
	void enterJson(JsonLexerParser.JsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLexerParser#json}.
	 * @param ctx the parse tree
	 */
	void exitJson(JsonLexerParser.JsonContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLexerParser#obj}.
	 * @param ctx the parse tree
	 */
	void enterObj(JsonLexerParser.ObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLexerParser#obj}.
	 * @param ctx the parse tree
	 */
	void exitObj(JsonLexerParser.ObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLexerParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(JsonLexerParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLexerParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(JsonLexerParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLexerParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(JsonLexerParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLexerParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(JsonLexerParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLexerParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(JsonLexerParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLexerParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(JsonLexerParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLexerParser#arr}.
	 * @param ctx the parse tree
	 */
	void enterArr(JsonLexerParser.ArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLexerParser#arr}.
	 * @param ctx the parse tree
	 */
	void exitArr(JsonLexerParser.ArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link JsonLexerParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(JsonLexerParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link JsonLexerParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(JsonLexerParser.NumberContext ctx);
}