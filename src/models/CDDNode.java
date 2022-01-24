package models;

import lib.CDDLib;

public class CDDNode {
    private long pointer;

    CDDNode(long pointer){
        this.pointer = pointer;
    }

    public long getPointer() {
        return pointer;
    }

    public int getLevel(){
        return CDDLib.getNodeLevel(pointer);
    }

    public ElemIterable getElemIterable(){
        return new ElemIterable(this);
    }

    public boolean isEndOfElems(int index){
        return CDDLib.isElemArrayNullTerminator(pointer,index);
    }

    public boolean isTrueTerminal(){
        return CDDLib.isTrue(pointer);
    }

    public boolean isFalseTerminal(){
        return CDDLib.isFalse(pointer);
    }

    public Elem getElemAtIndex(int index){
        int bound = CDDLib.getBoundFromElemArray(pointer, index);
        long cddNodePointer = CDDLib.getChildFromElemArray(pointer, index);
        return new Elem(new CDDNode(cddNodePointer), bound);
    }
}
