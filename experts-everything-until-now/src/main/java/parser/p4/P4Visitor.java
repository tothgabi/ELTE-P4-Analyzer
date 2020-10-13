// Generated from P4.g4 by ANTLR 4.8
package parser.p4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link P4Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface P4Visitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link P4Parser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(P4Parser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(P4Parser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput(P4Parser.InputContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(P4Parser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#preprocessorLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreprocessorLine(P4Parser.PreprocessorLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#ppIncludeFileName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPpIncludeFileName(P4Parser.PpIncludeFileNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#nonTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonTypeName(P4Parser.NonTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(P4Parser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#nonTableKwName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonTableKwName(P4Parser.NonTableKwNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#optCONST}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptCONST(P4Parser.OptCONSTContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#optAnnotations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptAnnotations(P4Parser.OptAnnotationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#annotations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotations(P4Parser.AnnotationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(P4Parser.AnnotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#annotationBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationBody(P4Parser.AnnotationBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#annotationToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationToken(P4Parser.AnnotationTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#kvList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKvList(P4Parser.KvListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#kvPair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKvPair(P4Parser.KvPairContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(P4Parser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#nonEmptyParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonEmptyParameterList(P4Parser.NonEmptyParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(P4Parser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#direction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirection(P4Parser.DirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#packageTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageTypeDeclaration(P4Parser.PackageTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#instantiation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstantiation(P4Parser.InstantiationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#objInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjInitializer(P4Parser.ObjInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#objDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjDeclarations(P4Parser.ObjDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#objDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjDeclaration(P4Parser.ObjDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#optConstructorParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptConstructorParameters(P4Parser.OptConstructorParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#dotPrefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDotPrefix(P4Parser.DotPrefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserDeclaration(P4Parser.ParserDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserLocalElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserLocalElements(P4Parser.ParserLocalElementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserLocalElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserLocalElement(P4Parser.ParserLocalElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserTypeDeclaration(P4Parser.ParserTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserStates}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserStates(P4Parser.ParserStatesContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserState}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserState(P4Parser.ParserStateContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserStatements(P4Parser.ParserStatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserStatement(P4Parser.ParserStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserBlockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserBlockStatement(P4Parser.ParserBlockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#transitionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitionStatement(P4Parser.TransitionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#stateExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStateExpression(P4Parser.StateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#selectExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExpression(P4Parser.SelectExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#selectCaseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectCaseList(P4Parser.SelectCaseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#selectCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectCase(P4Parser.SelectCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#keysetExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeysetExpression(P4Parser.KeysetExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#tupleKeysetExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleKeysetExpression(P4Parser.TupleKeysetExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#simpleExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpressionList(P4Parser.SimpleExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#simpleKeysetExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleKeysetExpression(P4Parser.SimpleKeysetExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#valueSetDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueSetDeclaration(P4Parser.ValueSetDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#controlDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlDeclaration(P4Parser.ControlDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#controlTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlTypeDeclaration(P4Parser.ControlTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#controlLocalDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlLocalDeclarations(P4Parser.ControlLocalDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#controlLocalDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlLocalDeclaration(P4Parser.ControlLocalDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#controlBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlBody(P4Parser.ControlBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#externDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternDeclaration(P4Parser.ExternDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#methodPrototypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodPrototypes(P4Parser.MethodPrototypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#functionPrototype}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionPrototype(P4Parser.FunctionPrototypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#methodPrototype}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodPrototype(P4Parser.MethodPrototypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typeRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeRef(P4Parser.TypeRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#namedType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedType(P4Parser.NamedTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#prefixedType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixedType(P4Parser.PrefixedTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(P4Parser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#tupleType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleType(P4Parser.TupleTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#headerStackType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHeaderStackType(P4Parser.HeaderStackTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#specializedType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecializedType(P4Parser.SpecializedTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#baseType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseType(P4Parser.BaseTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typeOrVoid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeOrVoid(P4Parser.TypeOrVoidContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#optTypeParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptTypeParameters(P4Parser.OptTypeParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typeParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParameterList(P4Parser.TypeParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typeArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArg(P4Parser.TypeArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typeArgumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeArgumentList(P4Parser.TypeArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#realTypeArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealTypeArg(P4Parser.RealTypeArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#realTypeArgumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealTypeArgumentList(P4Parser.RealTypeArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(P4Parser.TypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#derivedTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDerivedTypeDeclaration(P4Parser.DerivedTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#headerTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHeaderTypeDeclaration(P4Parser.HeaderTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#structTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructTypeDeclaration(P4Parser.StructTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#headerUnionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHeaderUnionDeclaration(P4Parser.HeaderUnionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#structFieldList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructFieldList(P4Parser.StructFieldListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#structField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructField(P4Parser.StructFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#enumDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDeclaration(P4Parser.EnumDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#specifiedIdentifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecifiedIdentifierList(P4Parser.SpecifiedIdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#specifiedIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecifiedIdentifier(P4Parser.SpecifiedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#errorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorDeclaration(P4Parser.ErrorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#matchKindDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchKindDeclaration(P4Parser.MatchKindDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(P4Parser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#typedefDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedefDeclaration(P4Parser.TypedefDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#assignmentOrMethodCallStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOrMethodCallStatement(P4Parser.AssignmentOrMethodCallStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#emptyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement(P4Parser.EmptyStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#exitStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExitStatement(P4Parser.ExitStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(P4Parser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#conditionalStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalStatement(P4Parser.ConditionalStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#directApplication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectApplication(P4Parser.DirectApplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(P4Parser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#blockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatement(P4Parser.BlockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#statOrDeclList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatOrDeclList(P4Parser.StatOrDeclListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#switchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchStatement(P4Parser.SwitchStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#switchCases}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchCases(P4Parser.SwitchCasesContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#switchCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchCase(P4Parser.SwitchCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#switchLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchLabel(P4Parser.SwitchLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#statementOrDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementOrDeclaration(P4Parser.StatementOrDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#tableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableDeclaration(P4Parser.TableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#tablePropertyList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablePropertyList(P4Parser.TablePropertyListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#tableProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableProperty(P4Parser.TablePropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#keyElementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyElementList(P4Parser.KeyElementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#keyElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyElement(P4Parser.KeyElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#actionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionList(P4Parser.ActionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#actionRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionRef(P4Parser.ActionRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#entry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntry(P4Parser.EntryContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#actionBinding}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionBinding(P4Parser.ActionBindingContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#entriesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntriesList(P4Parser.EntriesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#actionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionDeclaration(P4Parser.ActionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(P4Parser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#constantDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDeclaration(P4Parser.ConstantDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#optInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptInitializer(P4Parser.OptInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#initializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitializer(P4Parser.InitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(P4Parser.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(P4Parser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#nonEmptyArgList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonEmptyArgList(P4Parser.NonEmptyArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument(P4Parser.ArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(P4Parser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#prefixedNonTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixedNonTypeName(P4Parser.PrefixedNonTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalue(P4Parser.LvalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(P4Parser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#type_or_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_or_id(P4Parser.Type_or_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link P4Parser#parserStateCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParserStateCondition(P4Parser.ParserStateConditionContext ctx);
}