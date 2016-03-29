package clegoues.genprog4java.mut;

import java.util.List;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaMethodReplacer extends JavaEditOperation {

	public JavaMethodReplacer(ClassInfo fileName, JavaStatement location) {
		super(Mutation.METHODREPLACE, fileName, location);
	}

	@Override
	public void edit(final ASTRewrite rewriter, AST ast, final CompilationUnit cu) {


		ASTNode locationNode = this.getLocation().getASTNode();
		locationNode.accept((new ASTVisitor() {

			private ASTNode getParent(MethodInvocation node) {
				ASTNode parent = node.getParent();
				while(!(parent instanceof MethodDeclaration)){
					parent = parent.getParent();
				}
				return ((MethodDeclaration)parent).getBody();
			}	

			public boolean visit(final MethodInvocation node) {
				final String originalMethodName = node.getName().toString();
				final ITypeBinding returnType = node.resolveTypeBinding();
				String typename = "null";
				if(returnType != null){
					typename = returnType.getName();
				}
				final int numParams = node.arguments().size();
				final String params = node.arguments().toString();
				ASTNode parentNode = getParent(node);
				// going from method invocation up to method declaration level (highest in-scope)
				// find other method invocations with same return type and parameters
				parentNode.accept(new ASTVisitor() {

					public boolean visit(MethodInvocation mnode) {
						final String newMethodName = mnode.getName().toString();
						ITypeBinding returnType2 = mnode.resolveTypeBinding();
						final String typename2;
						if(returnType2 != null){
							typename2 = returnType2.getName();
						}
						int paramscount = mnode.arguments().size();
						String paramsvalues = mnode.arguments().toString();

						if(	((returnType != null && returnType.equals(returnType2))||(returnType == null && returnType2 == null)) && 
								numParams == paramscount && !originalMethodName.equalsIgnoreCase(newMethodName)){

							// find if the matched methods have compatible parameters
							cu.accept(new ASTVisitor() {

								List<VariableDeclaration> reqdparams = null,givenparams = null;
								Boolean match = false;
								public boolean visit(MethodDeclaration md) {

									String reqdparamstype = md.parameters().toString();
									String givenparamstype = md.parameters().toString();

									if(md.getName().toString().equalsIgnoreCase(originalMethodName)){
										reqdparamstype =  md.parameters().toString();
										reqdparams = md.parameters();												
									}
									if(md.getName().toString().equalsIgnoreCase(newMethodName)){
										givenparamstype =  md.parameters().toString();
										givenparams = md.parameters();
									}

									if(reqdparams!=null && givenparams!=null && reqdparams.size() >= givenparams.size()){
										match =  matchParameters(reqdparams, givenparams);
										if(match == true){													
											MethodInvocation newnode = node.getAST().newMethodInvocation();
											SimpleName newMethodNode = node.getAST().newSimpleName(newMethodName);
											newnode.setName(newMethodNode);
											List<ASTNode> paramNodes = node.arguments();
											for(int i=0; i<paramNodes.size(); i++){
												ASTNode param = paramNodes.get(i);
												int paramType = param.getNodeType();
												String paramName = param.toString();
												switch (paramType){

												case ASTNode.SIMPLE_NAME:
													SimpleName paramnode = param.getAST().newSimpleName(paramName);
													newnode.arguments().add(paramnode);
													break;

												case ASTNode.NUMBER_LITERAL:
													NumberLiteral paramnode1 = param.getAST().newNumberLiteral(paramName);
													newnode.arguments().add(paramnode1);
													break;

												case ASTNode.STRING_LITERAL:
													StringLiteral paramnode2 = param.getAST().newStringLiteral();
													paramnode2.setLiteralValue(paramName);
													newnode.arguments().add(paramnode2);
													break;

												case ASTNode.ARRAY_ACCESS:
													ArrayAccess paramnode3 = param.getAST().newArrayAccess();
													SimpleName aexp = (SimpleName) ((ArrayAccess)param).getArray();
													SimpleName paramname = aexp.getAST().newSimpleName(aexp.toString());
													paramnode3.setArray(paramname);	
													Expression iexp = ((ArrayAccess)param).getIndex();
													if(iexp instanceof SimpleName){
														iexp = (SimpleName) ((ArrayAccess)param).getIndex();
														SimpleName indexname = iexp.getAST().newSimpleName(iexp.toString());
														paramnode3.setIndex(indexname);
													}else if (iexp instanceof NumberLiteral){
														iexp = (NumberLiteral)((ArrayAccess)param).getIndex();
														NumberLiteral indexliteral = iexp.getAST().newNumberLiteral(iexp.toString());
														paramnode3.setIndex(indexliteral);
													}
													newnode.arguments().add(paramnode3);
													break;	
												}
											}
											rewriter.replace(node, newnode, null);

										}else{
											givenparams = null;
										}
										return false;
									}
									return false;
								}

								private boolean matchParameters(List<VariableDeclaration> reqdparams, List<VariableDeclaration> givenparams) {
									Boolean found = false;
									Object[] tempgivenparams = givenparams.toArray();
									for(int i=0; i<reqdparams.size(); i++){
										String reqdparam = reqdparams.get(i).toString();
										String reqdparamtype = reqdparam.split(" ")[0];
										found = false;
										for(int j=0; j<tempgivenparams.length; j++){
											String givenparam = givenparams.get(j).toString();
											String givenparamtype = givenparam.split(" ")[0];
											if(tempgivenparams[j] != null && reqdparamtype.equals(givenparamtype)){
												found = true;
												tempgivenparams[j] = null;
												break;
											}else{
												found = false;
											}
										}
										if(found == false){
											return false;
										}
									}
									return found;
								}
							});	
						} 					
						return false;
					}	
				});
				return false;
			}
		}));
	}
}
