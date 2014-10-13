// Autogenerated AST node
package com.astra.ses.spell.language.model.ast;

import com.astra.ses.spell.language.model.SimpleNode;

public class TryExcept extends stmtType
{
	public stmtType[]	       body;
	public excepthandlerType[]	handlers;
	public suiteType	       orelse;

	public TryExcept(stmtType[] body, excepthandlerType[] handlers,
	        suiteType orelse)
	{
		this.body = body;
		this.handlers = handlers;
		this.orelse = orelse;
	}

	public TryExcept(stmtType[] body, excepthandlerType[] handlers,
	        suiteType orelse, SimpleNode parent)
	{
		this(body, handlers, orelse);
		this.beginLine = parent.beginLine;
		this.beginColumn = parent.beginColumn;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("TryExcept[");
		sb.append("body=");
		sb.append(dumpThis(this.body));
		sb.append(", ");
		sb.append("handlers=");
		sb.append(dumpThis(this.handlers));
		sb.append(", ");
		sb.append("orelse=");
		sb.append(dumpThis(this.orelse));
		sb.append("]");
		return sb.toString();
	}

	public Object accept(VisitorIF visitor) throws Exception
	{
		return visitor.visitTryExcept(this);
	}

	public void traverse(VisitorIF visitor) throws Exception
	{
		if (body != null)
		{
			for (int i = 0; i < body.length; i++)
			{
				if (body[i] != null) body[i].accept(visitor);
			}
		}
		if (handlers != null)
		{
			for (int i = 0; i < handlers.length; i++)
			{
				if (handlers[i] != null) handlers[i].accept(visitor);
			}
		}
		if (orelse != null) orelse.accept(visitor);
	}

}
