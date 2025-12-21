// Generated from /storage/emulated/0/apk/JsonLexer.g4 by ANTLR 4.13.2
package ir.ninjacoder.json;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JsonLexerParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JsonLexerVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JsonLexerParser#json}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson(JsonLexerParser.JsonContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLexerParser#obj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObj(JsonLexerParser.ObjContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLexerParser#pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPair(JsonLexerParser.PairContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLexerParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(JsonLexerParser.KeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLexerParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(JsonLexerParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLexerParser#arr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArr(JsonLexerParser.ArrContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonLexerParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(JsonLexerParser.NumberContext ctx);
}