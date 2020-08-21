// Generated from P4.g4 by ANTLR 4.8
package parser.p4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link P4Parser}.
 */
public interface P4Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link P4Parser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(P4Parser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(P4Parser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(P4Parser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(P4Parser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(P4Parser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(P4Parser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(P4Parser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(P4Parser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#preprocessorLine}.
	 * @param ctx the parse tree
	 */
	void enterPreprocessorLine(P4Parser.PreprocessorLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#preprocessorLine}.
	 * @param ctx the parse tree
	 */
	void exitPreprocessorLine(P4Parser.PreprocessorLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#ppIncludeFileName}.
	 * @param ctx the parse tree
	 */
	void enterPpIncludeFileName(P4Parser.PpIncludeFileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#ppIncludeFileName}.
	 * @param ctx the parse tree
	 */
	void exitPpIncludeFileName(P4Parser.PpIncludeFileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#nonTypeName}.
	 * @param ctx the parse tree
	 */
	void enterNonTypeName(P4Parser.NonTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#nonTypeName}.
	 * @param ctx the parse tree
	 */
	void exitNonTypeName(P4Parser.NonTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(P4Parser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(P4Parser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#nonTableKwName}.
	 * @param ctx the parse tree
	 */
	void enterNonTableKwName(P4Parser.NonTableKwNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#nonTableKwName}.
	 * @param ctx the parse tree
	 */
	void exitNonTableKwName(P4Parser.NonTableKwNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#optCONST}.
	 * @param ctx the parse tree
	 */
	void enterOptCONST(P4Parser.OptCONSTContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#optCONST}.
	 * @param ctx the parse tree
	 */
	void exitOptCONST(P4Parser.OptCONSTContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#optAnnotations}.
	 * @param ctx the parse tree
	 */
	void enterOptAnnotations(P4Parser.OptAnnotationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#optAnnotations}.
	 * @param ctx the parse tree
	 */
	void exitOptAnnotations(P4Parser.OptAnnotationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#annotations}.
	 * @param ctx the parse tree
	 */
	void enterAnnotations(P4Parser.AnnotationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#annotations}.
	 * @param ctx the parse tree
	 */
	void exitAnnotations(P4Parser.AnnotationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(P4Parser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(P4Parser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#annotationBody}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationBody(P4Parser.AnnotationBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#annotationBody}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationBody(P4Parser.AnnotationBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#annotationToken}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationToken(P4Parser.AnnotationTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#annotationToken}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationToken(P4Parser.AnnotationTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#kvList}.
	 * @param ctx the parse tree
	 */
	void enterKvList(P4Parser.KvListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#kvList}.
	 * @param ctx the parse tree
	 */
	void exitKvList(P4Parser.KvListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#kvPair}.
	 * @param ctx the parse tree
	 */
	void enterKvPair(P4Parser.KvPairContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#kvPair}.
	 * @param ctx the parse tree
	 */
	void exitKvPair(P4Parser.KvPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(P4Parser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(P4Parser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#nonEmptyParameterList}.
	 * @param ctx the parse tree
	 */
	void enterNonEmptyParameterList(P4Parser.NonEmptyParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#nonEmptyParameterList}.
	 * @param ctx the parse tree
	 */
	void exitNonEmptyParameterList(P4Parser.NonEmptyParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(P4Parser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(P4Parser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#direction}.
	 * @param ctx the parse tree
	 */
	void enterDirection(P4Parser.DirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#direction}.
	 * @param ctx the parse tree
	 */
	void exitDirection(P4Parser.DirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#packageTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPackageTypeDeclaration(P4Parser.PackageTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#packageTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPackageTypeDeclaration(P4Parser.PackageTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#instantiation}.
	 * @param ctx the parse tree
	 */
	void enterInstantiation(P4Parser.InstantiationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#instantiation}.
	 * @param ctx the parse tree
	 */
	void exitInstantiation(P4Parser.InstantiationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#objInitializer}.
	 * @param ctx the parse tree
	 */
	void enterObjInitializer(P4Parser.ObjInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#objInitializer}.
	 * @param ctx the parse tree
	 */
	void exitObjInitializer(P4Parser.ObjInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#objDeclarations}.
	 * @param ctx the parse tree
	 */
	void enterObjDeclarations(P4Parser.ObjDeclarationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#objDeclarations}.
	 * @param ctx the parse tree
	 */
	void exitObjDeclarations(P4Parser.ObjDeclarationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#objDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterObjDeclaration(P4Parser.ObjDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#objDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitObjDeclaration(P4Parser.ObjDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#optConstructorParameters}.
	 * @param ctx the parse tree
	 */
	void enterOptConstructorParameters(P4Parser.OptConstructorParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#optConstructorParameters}.
	 * @param ctx the parse tree
	 */
	void exitOptConstructorParameters(P4Parser.OptConstructorParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#dotPrefix}.
	 * @param ctx the parse tree
	 */
	void enterDotPrefix(P4Parser.DotPrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#dotPrefix}.
	 * @param ctx the parse tree
	 */
	void exitDotPrefix(P4Parser.DotPrefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParserDeclaration(P4Parser.ParserDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParserDeclaration(P4Parser.ParserDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserLocalElements}.
	 * @param ctx the parse tree
	 */
	void enterParserLocalElements(P4Parser.ParserLocalElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserLocalElements}.
	 * @param ctx the parse tree
	 */
	void exitParserLocalElements(P4Parser.ParserLocalElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserLocalElement}.
	 * @param ctx the parse tree
	 */
	void enterParserLocalElement(P4Parser.ParserLocalElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserLocalElement}.
	 * @param ctx the parse tree
	 */
	void exitParserLocalElement(P4Parser.ParserLocalElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParserTypeDeclaration(P4Parser.ParserTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParserTypeDeclaration(P4Parser.ParserTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserStates}.
	 * @param ctx the parse tree
	 */
	void enterParserStates(P4Parser.ParserStatesContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserStates}.
	 * @param ctx the parse tree
	 */
	void exitParserStates(P4Parser.ParserStatesContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserState}.
	 * @param ctx the parse tree
	 */
	void enterParserState(P4Parser.ParserStateContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserState}.
	 * @param ctx the parse tree
	 */
	void exitParserState(P4Parser.ParserStateContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserStatements}.
	 * @param ctx the parse tree
	 */
	void enterParserStatements(P4Parser.ParserStatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserStatements}.
	 * @param ctx the parse tree
	 */
	void exitParserStatements(P4Parser.ParserStatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserStatement}.
	 * @param ctx the parse tree
	 */
	void enterParserStatement(P4Parser.ParserStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserStatement}.
	 * @param ctx the parse tree
	 */
	void exitParserStatement(P4Parser.ParserStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserBlockStatement}.
	 * @param ctx the parse tree
	 */
	void enterParserBlockStatement(P4Parser.ParserBlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserBlockStatement}.
	 * @param ctx the parse tree
	 */
	void exitParserBlockStatement(P4Parser.ParserBlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#transitionStatement}.
	 * @param ctx the parse tree
	 */
	void enterTransitionStatement(P4Parser.TransitionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#transitionStatement}.
	 * @param ctx the parse tree
	 */
	void exitTransitionStatement(P4Parser.TransitionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#stateExpression}.
	 * @param ctx the parse tree
	 */
	void enterStateExpression(P4Parser.StateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#stateExpression}.
	 * @param ctx the parse tree
	 */
	void exitStateExpression(P4Parser.StateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#selectExpression}.
	 * @param ctx the parse tree
	 */
	void enterSelectExpression(P4Parser.SelectExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#selectExpression}.
	 * @param ctx the parse tree
	 */
	void exitSelectExpression(P4Parser.SelectExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#selectCaseList}.
	 * @param ctx the parse tree
	 */
	void enterSelectCaseList(P4Parser.SelectCaseListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#selectCaseList}.
	 * @param ctx the parse tree
	 */
	void exitSelectCaseList(P4Parser.SelectCaseListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#selectCase}.
	 * @param ctx the parse tree
	 */
	void enterSelectCase(P4Parser.SelectCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#selectCase}.
	 * @param ctx the parse tree
	 */
	void exitSelectCase(P4Parser.SelectCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#keysetExpression}.
	 * @param ctx the parse tree
	 */
	void enterKeysetExpression(P4Parser.KeysetExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#keysetExpression}.
	 * @param ctx the parse tree
	 */
	void exitKeysetExpression(P4Parser.KeysetExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#tupleKeysetExpression}.
	 * @param ctx the parse tree
	 */
	void enterTupleKeysetExpression(P4Parser.TupleKeysetExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#tupleKeysetExpression}.
	 * @param ctx the parse tree
	 */
	void exitTupleKeysetExpression(P4Parser.TupleKeysetExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#simpleExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpressionList(P4Parser.SimpleExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#simpleExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpressionList(P4Parser.SimpleExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#simpleKeysetExpression}.
	 * @param ctx the parse tree
	 */
	void enterSimpleKeysetExpression(P4Parser.SimpleKeysetExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#simpleKeysetExpression}.
	 * @param ctx the parse tree
	 */
	void exitSimpleKeysetExpression(P4Parser.SimpleKeysetExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#valueSetDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterValueSetDeclaration(P4Parser.ValueSetDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#valueSetDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitValueSetDeclaration(P4Parser.ValueSetDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#controlDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterControlDeclaration(P4Parser.ControlDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#controlDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitControlDeclaration(P4Parser.ControlDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#controlTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterControlTypeDeclaration(P4Parser.ControlTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#controlTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitControlTypeDeclaration(P4Parser.ControlTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#controlLocalDeclarations}.
	 * @param ctx the parse tree
	 */
	void enterControlLocalDeclarations(P4Parser.ControlLocalDeclarationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#controlLocalDeclarations}.
	 * @param ctx the parse tree
	 */
	void exitControlLocalDeclarations(P4Parser.ControlLocalDeclarationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#controlLocalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterControlLocalDeclaration(P4Parser.ControlLocalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#controlLocalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitControlLocalDeclaration(P4Parser.ControlLocalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#controlBody}.
	 * @param ctx the parse tree
	 */
	void enterControlBody(P4Parser.ControlBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#controlBody}.
	 * @param ctx the parse tree
	 */
	void exitControlBody(P4Parser.ControlBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#externDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterExternDeclaration(P4Parser.ExternDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#externDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitExternDeclaration(P4Parser.ExternDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#methodPrototypes}.
	 * @param ctx the parse tree
	 */
	void enterMethodPrototypes(P4Parser.MethodPrototypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#methodPrototypes}.
	 * @param ctx the parse tree
	 */
	void exitMethodPrototypes(P4Parser.MethodPrototypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#functionPrototype}.
	 * @param ctx the parse tree
	 */
	void enterFunctionPrototype(P4Parser.FunctionPrototypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#functionPrototype}.
	 * @param ctx the parse tree
	 */
	void exitFunctionPrototype(P4Parser.FunctionPrototypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#methodPrototype}.
	 * @param ctx the parse tree
	 */
	void enterMethodPrototype(P4Parser.MethodPrototypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#methodPrototype}.
	 * @param ctx the parse tree
	 */
	void exitMethodPrototype(P4Parser.MethodPrototypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typeRef}.
	 * @param ctx the parse tree
	 */
	void enterTypeRef(P4Parser.TypeRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typeRef}.
	 * @param ctx the parse tree
	 */
	void exitTypeRef(P4Parser.TypeRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#namedType}.
	 * @param ctx the parse tree
	 */
	void enterNamedType(P4Parser.NamedTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#namedType}.
	 * @param ctx the parse tree
	 */
	void exitNamedType(P4Parser.NamedTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#prefixedType}.
	 * @param ctx the parse tree
	 */
	void enterPrefixedType(P4Parser.PrefixedTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#prefixedType}.
	 * @param ctx the parse tree
	 */
	void exitPrefixedType(P4Parser.PrefixedTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(P4Parser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(P4Parser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#tupleType}.
	 * @param ctx the parse tree
	 */
	void enterTupleType(P4Parser.TupleTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#tupleType}.
	 * @param ctx the parse tree
	 */
	void exitTupleType(P4Parser.TupleTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#headerStackType}.
	 * @param ctx the parse tree
	 */
	void enterHeaderStackType(P4Parser.HeaderStackTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#headerStackType}.
	 * @param ctx the parse tree
	 */
	void exitHeaderStackType(P4Parser.HeaderStackTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#specializedType}.
	 * @param ctx the parse tree
	 */
	void enterSpecializedType(P4Parser.SpecializedTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#specializedType}.
	 * @param ctx the parse tree
	 */
	void exitSpecializedType(P4Parser.SpecializedTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#baseType}.
	 * @param ctx the parse tree
	 */
	void enterBaseType(P4Parser.BaseTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#baseType}.
	 * @param ctx the parse tree
	 */
	void exitBaseType(P4Parser.BaseTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typeOrVoid}.
	 * @param ctx the parse tree
	 */
	void enterTypeOrVoid(P4Parser.TypeOrVoidContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typeOrVoid}.
	 * @param ctx the parse tree
	 */
	void exitTypeOrVoid(P4Parser.TypeOrVoidContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#optTypeParameters}.
	 * @param ctx the parse tree
	 */
	void enterOptTypeParameters(P4Parser.OptTypeParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#optTypeParameters}.
	 * @param ctx the parse tree
	 */
	void exitOptTypeParameters(P4Parser.OptTypeParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void enterTypeParameterList(P4Parser.TypeParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typeParameterList}.
	 * @param ctx the parse tree
	 */
	void exitTypeParameterList(P4Parser.TypeParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typeArg}.
	 * @param ctx the parse tree
	 */
	void enterTypeArg(P4Parser.TypeArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typeArg}.
	 * @param ctx the parse tree
	 */
	void exitTypeArg(P4Parser.TypeArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typeArgumentList}.
	 * @param ctx the parse tree
	 */
	void enterTypeArgumentList(P4Parser.TypeArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typeArgumentList}.
	 * @param ctx the parse tree
	 */
	void exitTypeArgumentList(P4Parser.TypeArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#realTypeArg}.
	 * @param ctx the parse tree
	 */
	void enterRealTypeArg(P4Parser.RealTypeArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#realTypeArg}.
	 * @param ctx the parse tree
	 */
	void exitRealTypeArg(P4Parser.RealTypeArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#realTypeArgumentList}.
	 * @param ctx the parse tree
	 */
	void enterRealTypeArgumentList(P4Parser.RealTypeArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#realTypeArgumentList}.
	 * @param ctx the parse tree
	 */
	void exitRealTypeArgumentList(P4Parser.RealTypeArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypeDeclaration(P4Parser.TypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypeDeclaration(P4Parser.TypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#derivedTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterDerivedTypeDeclaration(P4Parser.DerivedTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#derivedTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitDerivedTypeDeclaration(P4Parser.DerivedTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#headerTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterHeaderTypeDeclaration(P4Parser.HeaderTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#headerTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitHeaderTypeDeclaration(P4Parser.HeaderTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#structTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterStructTypeDeclaration(P4Parser.StructTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#structTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitStructTypeDeclaration(P4Parser.StructTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#headerUnionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterHeaderUnionDeclaration(P4Parser.HeaderUnionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#headerUnionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitHeaderUnionDeclaration(P4Parser.HeaderUnionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#structFieldList}.
	 * @param ctx the parse tree
	 */
	void enterStructFieldList(P4Parser.StructFieldListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#structFieldList}.
	 * @param ctx the parse tree
	 */
	void exitStructFieldList(P4Parser.StructFieldListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#structField}.
	 * @param ctx the parse tree
	 */
	void enterStructField(P4Parser.StructFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#structField}.
	 * @param ctx the parse tree
	 */
	void exitStructField(P4Parser.StructFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterEnumDeclaration(P4Parser.EnumDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#enumDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitEnumDeclaration(P4Parser.EnumDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#specifiedIdentifierList}.
	 * @param ctx the parse tree
	 */
	void enterSpecifiedIdentifierList(P4Parser.SpecifiedIdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#specifiedIdentifierList}.
	 * @param ctx the parse tree
	 */
	void exitSpecifiedIdentifierList(P4Parser.SpecifiedIdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#specifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterSpecifiedIdentifier(P4Parser.SpecifiedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#specifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitSpecifiedIdentifier(P4Parser.SpecifiedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#errorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterErrorDeclaration(P4Parser.ErrorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#errorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitErrorDeclaration(P4Parser.ErrorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#matchKindDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMatchKindDeclaration(P4Parser.MatchKindDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#matchKindDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMatchKindDeclaration(P4Parser.MatchKindDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(P4Parser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(P4Parser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#typedefDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypedefDeclaration(P4Parser.TypedefDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#typedefDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypedefDeclaration(P4Parser.TypedefDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#assignmentOrMethodCallStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOrMethodCallStatement(P4Parser.AssignmentOrMethodCallStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#assignmentOrMethodCallStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOrMethodCallStatement(P4Parser.AssignmentOrMethodCallStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(P4Parser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(P4Parser.EmptyStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#exitStatement}.
	 * @param ctx the parse tree
	 */
	void enterExitStatement(P4Parser.ExitStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#exitStatement}.
	 * @param ctx the parse tree
	 */
	void exitExitStatement(P4Parser.ExitStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(P4Parser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(P4Parser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#conditionalStatement}.
	 * @param ctx the parse tree
	 */
	void enterConditionalStatement(P4Parser.ConditionalStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#conditionalStatement}.
	 * @param ctx the parse tree
	 */
	void exitConditionalStatement(P4Parser.ConditionalStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#directApplication}.
	 * @param ctx the parse tree
	 */
	void enterDirectApplication(P4Parser.DirectApplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#directApplication}.
	 * @param ctx the parse tree
	 */
	void exitDirectApplication(P4Parser.DirectApplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(P4Parser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(P4Parser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(P4Parser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(P4Parser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#statOrDeclList}.
	 * @param ctx the parse tree
	 */
	void enterStatOrDeclList(P4Parser.StatOrDeclListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#statOrDeclList}.
	 * @param ctx the parse tree
	 */
	void exitStatOrDeclList(P4Parser.StatOrDeclListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchStatement(P4Parser.SwitchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchStatement(P4Parser.SwitchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#switchCases}.
	 * @param ctx the parse tree
	 */
	void enterSwitchCases(P4Parser.SwitchCasesContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#switchCases}.
	 * @param ctx the parse tree
	 */
	void exitSwitchCases(P4Parser.SwitchCasesContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#switchCase}.
	 * @param ctx the parse tree
	 */
	void enterSwitchCase(P4Parser.SwitchCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#switchCase}.
	 * @param ctx the parse tree
	 */
	void exitSwitchCase(P4Parser.SwitchCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void enterSwitchLabel(P4Parser.SwitchLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#switchLabel}.
	 * @param ctx the parse tree
	 */
	void exitSwitchLabel(P4Parser.SwitchLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#statementOrDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterStatementOrDeclaration(P4Parser.StatementOrDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#statementOrDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitStatementOrDeclaration(P4Parser.StatementOrDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#tableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTableDeclaration(P4Parser.TableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#tableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTableDeclaration(P4Parser.TableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#tablePropertyList}.
	 * @param ctx the parse tree
	 */
	void enterTablePropertyList(P4Parser.TablePropertyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#tablePropertyList}.
	 * @param ctx the parse tree
	 */
	void exitTablePropertyList(P4Parser.TablePropertyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#tableProperty}.
	 * @param ctx the parse tree
	 */
	void enterTableProperty(P4Parser.TablePropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#tableProperty}.
	 * @param ctx the parse tree
	 */
	void exitTableProperty(P4Parser.TablePropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#keyElementList}.
	 * @param ctx the parse tree
	 */
	void enterKeyElementList(P4Parser.KeyElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#keyElementList}.
	 * @param ctx the parse tree
	 */
	void exitKeyElementList(P4Parser.KeyElementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#keyElement}.
	 * @param ctx the parse tree
	 */
	void enterKeyElement(P4Parser.KeyElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#keyElement}.
	 * @param ctx the parse tree
	 */
	void exitKeyElement(P4Parser.KeyElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#actionList}.
	 * @param ctx the parse tree
	 */
	void enterActionList(P4Parser.ActionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#actionList}.
	 * @param ctx the parse tree
	 */
	void exitActionList(P4Parser.ActionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#actionRef}.
	 * @param ctx the parse tree
	 */
	void enterActionRef(P4Parser.ActionRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#actionRef}.
	 * @param ctx the parse tree
	 */
	void exitActionRef(P4Parser.ActionRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#entry}.
	 * @param ctx the parse tree
	 */
	void enterEntry(P4Parser.EntryContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#entry}.
	 * @param ctx the parse tree
	 */
	void exitEntry(P4Parser.EntryContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#actionBinding}.
	 * @param ctx the parse tree
	 */
	void enterActionBinding(P4Parser.ActionBindingContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#actionBinding}.
	 * @param ctx the parse tree
	 */
	void exitActionBinding(P4Parser.ActionBindingContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#entriesList}.
	 * @param ctx the parse tree
	 */
	void enterEntriesList(P4Parser.EntriesListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#entriesList}.
	 * @param ctx the parse tree
	 */
	void exitEntriesList(P4Parser.EntriesListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#actionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterActionDeclaration(P4Parser.ActionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#actionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitActionDeclaration(P4Parser.ActionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(P4Parser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(P4Parser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#constantDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstantDeclaration(P4Parser.ConstantDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#constantDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstantDeclaration(P4Parser.ConstantDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#optInitializer}.
	 * @param ctx the parse tree
	 */
	void enterOptInitializer(P4Parser.OptInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#optInitializer}.
	 * @param ctx the parse tree
	 */
	void exitOptInitializer(P4Parser.OptInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#initializer}.
	 * @param ctx the parse tree
	 */
	void enterInitializer(P4Parser.InitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#initializer}.
	 * @param ctx the parse tree
	 */
	void exitInitializer(P4Parser.InitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(P4Parser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(P4Parser.FunctionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(P4Parser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(P4Parser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#nonEmptyArgList}.
	 * @param ctx the parse tree
	 */
	void enterNonEmptyArgList(P4Parser.NonEmptyArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#nonEmptyArgList}.
	 * @param ctx the parse tree
	 */
	void exitNonEmptyArgList(P4Parser.NonEmptyArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#argument}.
	 * @param ctx the parse tree
	 */
	void enterArgument(P4Parser.ArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#argument}.
	 * @param ctx the parse tree
	 */
	void exitArgument(P4Parser.ArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(P4Parser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(P4Parser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#prefixedNonTypeName}.
	 * @param ctx the parse tree
	 */
	void enterPrefixedNonTypeName(P4Parser.PrefixedNonTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#prefixedNonTypeName}.
	 * @param ctx the parse tree
	 */
	void exitPrefixedNonTypeName(P4Parser.PrefixedNonTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterLvalue(P4Parser.LvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitLvalue(P4Parser.LvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(P4Parser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(P4Parser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#type_or_id}.
	 * @param ctx the parse tree
	 */
	void enterType_or_id(P4Parser.Type_or_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#type_or_id}.
	 * @param ctx the parse tree
	 */
	void exitType_or_id(P4Parser.Type_or_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link P4Parser#parserStateCondition}.
	 * @param ctx the parse tree
	 */
	void enterParserStateCondition(P4Parser.ParserStateConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link P4Parser#parserStateCondition}.
	 * @param ctx the parse tree
	 */
	void exitParserStateCondition(P4Parser.ParserStateConditionContext ctx);
}