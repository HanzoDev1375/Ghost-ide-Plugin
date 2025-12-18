// Generated from /storage/emulated/0/AndroidIDEProjects/My pl/app/src/main/java/ir/ninjacoder/json5/editor/JSON5.g4 by ANTLR 4.13.2
package ir.ninjacoder.json5.editor;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JSON5Parser}.
 */
public interface JSON5Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JSON5Parser#json5}.
	 * @param ctx the parse tree
	 */
	void enterJson5(JSON5Parser.Json5Context ctx);
	/**
	 * Exit a parse tree produced by {@link JSON5Parser#json5}.
	 * @param ctx the parse tree
	 */
	void exitJson5(JSON5Parser.Json5Context ctx);
	/**
	 * Enter a parse tree produced by {@link JSON5Parser#obj}.
	 * @param ctx the parse tree
	 */
	void enterObj(JSON5Parser.ObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSON5Parser#obj}.
	 * @param ctx the parse tree
	 */
	void exitObj(JSON5Parser.ObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSON5Parser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(JSON5Parser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSON5Parser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(JSON5Parser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSON5Parser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(JSON5Parser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSON5Parser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(JSON5Parser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSON5Parser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(JSON5Parser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSON5Parser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(JSON5Parser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSON5Parser#arr}.
	 * @param ctx the parse tree
	 */
	void enterArr(JSON5Parser.ArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSON5Parser#arr}.
	 * @param ctx the parse tree
	 */
	void exitArr(JSON5Parser.ArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSON5Parser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(JSON5Parser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSON5Parser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(JSON5Parser.NumberContext ctx);
}