// Autogenerated AST node
package com.astra.ses.spell.language.model.ast;

import com.astra.ses.spell.language.model.SimpleNode;

public class IfExp extends exprType
{
	public exprType	test;
	public exprType	body;
	public exprType	orelse;

	public IfExp(exprType test, exprType body, exprType orelse)
	{
		this.test = test;
		this.body = body;
		this.orelse = orelse;
	}

	public IfExp(exprType test, exprType body, exprType orelse,
	        SimpleNode parent)
	{
		this(test, body, orelse);
		this.beginLine = parent.beginLine;
		this.beginColumn = parent.beginColumn;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("IfExp[");
		sb.append("test=");
		sb.append(dumpThis(this.test));
		sb.append(", ");
		sb.append("body=");
		sb.append(dumpThis(this.body));
		sb.append(", ");
		sb.append("orelse=");
		sb.append(dumpThis(this.orelse));
		sb.append("]");
		return sb.toString();
	}

	public Object accept(VisitorIF visitor) throws Exception
	{
		return visitor.visitIfExp(this);
	}

	public void traverse(VisitorIF visitor) throws Exception
	{
		if (test != null) test.accept(visitor);
		if (body != null) body.accept(visitor);
		if (orelse != null) orelse.accept(visitor);
	}

}