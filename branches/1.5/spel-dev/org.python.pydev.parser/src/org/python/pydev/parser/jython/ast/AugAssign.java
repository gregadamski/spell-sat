// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public class AugAssign extends stmtType implements operatorType {
    public exprType target;
    public int op;
    public exprType value;

    public AugAssign(exprType target, int op, exprType value) {
        this.target = target;
        this.op = op;
        this.value = value;
    }

    public AugAssign(exprType target, int op, exprType value, SimpleNode
    parent) {
        this(target, op, value);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("AugAssign[");
        sb.append("target=");
        sb.append(dumpThis(this.target));
        sb.append(", ");
        sb.append("op=");
        sb.append(dumpThis(this.op, operatorType.operatorTypeNames));
        sb.append(", ");
        sb.append("value=");
        sb.append(dumpThis(this.value));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitAugAssign(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (target != null)
            target.accept(visitor);
        if (value != null)
            value.accept(visitor);
    }

}
