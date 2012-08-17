// Autogenerated AST node
package com.astra.ses.spell.language.model.ast;

import com.astra.ses.spell.language.model.SimpleNode;

public class Global extends stmtType
{
	public NameTokType[]	names;
	public exprType	     value;

	public Global(NameTokType[] names, exprType value)
	{
		this.names = names;
		this.value = value;
	}

	public Global(NameTokType[] names, exprType value, SimpleNode parent)
	{
		this(names, value);
		this.beginLine = parent.beginLine;
		this.beginColumn = parent.beginColumn;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("Global[");
		sb.append("names=");
		sb.append(dumpThis(this.names));
		sb.append(", ");
		sb.append("value=");
		sb.append(dumpThis(this.value));
		sb.append("]");
		return sb.toString();
	}

	public Object accept(VisitorIF visitor) throws Exception
	{
		return visitor.visitGlobal(this);
	}

	public void traverse(VisitorIF visitor) throws Exception
	{
		if (names != null)
		{
			for (int i = 0; i < names.length; i++)
			{
				if (names[i] != null) names[i].accept(visitor);
			}
		}
		if (value != null) value.accept(visitor);
	}

}
