// Autogenerated AST node
package com.astra.ses.spell.language.model.ast;
import com.astra.ses.spell.language.model.SimpleNode;

public class Slice extends sliceType {
    public exprType lower;
    public exprType upper;
    public exprType step;

    public Slice(exprType lower, exprType upper, exprType step) {
        this.lower = lower;
        this.upper = upper;
        this.step = step;
    }

    public Slice(exprType lower, exprType upper, exprType step, SimpleNode
    parent) {
        this(lower, upper, step);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Slice[");
        sb.append("lower=");
        sb.append(dumpThis(this.lower));
        sb.append(", ");
        sb.append("upper=");
        sb.append(dumpThis(this.upper));
        sb.append(", ");
        sb.append("step=");
        sb.append(dumpThis(this.step));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitSlice(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (lower != null)
            lower.accept(visitor);
        if (upper != null)
            upper.accept(visitor);
        if (step != null)
            step.accept(visitor);
    }

}
