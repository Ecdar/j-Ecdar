package models;

import jdk.jshell.spi.ExecutionControl;
import lib.CDDLib;

public class CDDNode {
    private final long pointer;

    CDDNode(long pointer) {
        this.pointer = pointer;
    }

    public long getPointer() {
        return pointer;
    }

    public int getLevel() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("CDDLib.getNodeLevel call crashes");
        // TODO: Fix this function call crash
        // return CDDLib.getNodeLevel(pointer);
    }

    public SegmentIterable getElemIterable() {
        return new SegmentIterable(this);
    }

    public boolean isEndOfSegments(int index) {
        return CDDLib.isElemArrayNullTerminator(pointer, index);
    }

    public boolean isTrueTerminal() {
        return CDDLib.isTrue(pointer);
    }

    public boolean isFalseTerminal() {
        return CDDLib.isFalse(pointer);
    }

    public Segment getSegmentAtIndex(int index) {
        int bound = CDDLib.getBoundFromElemArray(pointer, index);
        long cddNodePointer = CDDLib.getChildFromElemArray(pointer, index);
        return new Segment(new CDDNode(cddNodePointer), bound);
    }
}
