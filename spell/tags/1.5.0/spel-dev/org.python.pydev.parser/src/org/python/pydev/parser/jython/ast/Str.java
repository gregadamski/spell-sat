// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public class Str extends exprType implements str_typeType {
    public String s;
    public int type;
    public boolean unicode;
    public boolean raw;
    public boolean binary;

    public Str(String s, int type, boolean unicode, boolean raw, boolean
    binary) {
        this.s = s;
        this.type = type;
        this.unicode = unicode;
        this.raw = raw;
        this.binary = binary;
    }

    public Str(String s, int type, boolean unicode, boolean raw, boolean
    binary, SimpleNode parent) {
        this(s, type, unicode, raw, binary);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Str[");
        sb.append("s=");
        sb.append(dumpThis(this.s));
        sb.append(", ");
        sb.append("type=");
        sb.append(dumpThis(this.type, str_typeType.str_typeTypeNames));
        sb.append(", ");
        sb.append("unicode=");
        sb.append(dumpThis(this.unicode));
        sb.append(", ");
        sb.append("raw=");
        sb.append(dumpThis(this.raw));
        sb.append(", ");
        sb.append("binary=");
        sb.append(dumpThis(this.binary));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitStr(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
    }

}
