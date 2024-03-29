/*
 * Created on 24/09/2005
 */
package org.python.pydev.core;

import java.io.Serializable;

/**
 * Defines a tuple of some object, adding equals and hashCode operations
 * 
 * @author Fabio
 */
public class TupleN implements Serializable{

    public Object[] o1;

    public TupleN(Object ... o1) {
        this.o1 = o1;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof TupleN)){
            return false;
        }
        
        TupleN t2 = (TupleN) obj;
        if(t2.o1.length != this.o1.length){
            return false;
        }
        for(int i=0;i<o1.length;i++){
            if(!o1[i].equals(t2.o1[i])){
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int ret = 1;
        for(int i=0;i<o1.length;i++){
            ret *= o1[i].hashCode();
        }
        return 7 * ret;
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Tuple [");
        for(int i=0;i<o1.length;i++){
            buffer.append(o1[i].toString());
            if(i != o1.length-1){
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }
}
