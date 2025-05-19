package de.lucalabs.ziplines.curves;

public interface SegmentIterator extends SegmentView {
    boolean hasNext();
    boolean next();
}
