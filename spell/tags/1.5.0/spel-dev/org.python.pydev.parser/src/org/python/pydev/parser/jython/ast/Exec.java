// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public class Exec extends stmtType {
    public exprType body;
    public exprType globals;
    public exprType locals;

    public Exec(exprType body, exprType globals, exprType locals) {
        this.body = body;
        this.globals = globals;
        this.locals = locals;
    }

    public Exec(exprType body, exprType globals, exprType locals,
    SimpleNode parent) {
        this(body, globals, locals);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Exec[");
        sb.append("body=");
        sb.append(dumpThis(this.body));
        sb.append(", ");
        sb.append("globals=");
        sb.append(dumpThis(this.globals));
        sb.append(", ");
        sb.append("locals=");
        sb.append(dumpThis(this.locals));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitExec(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (body != null)
            body.accept(visitor);
        if (globals != null)
            globals.accept(visitor);
        if (locals != null)
            locals.accept(visitor);
    }

}
