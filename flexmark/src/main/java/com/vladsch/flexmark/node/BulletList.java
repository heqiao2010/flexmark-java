package com.vladsch.flexmark.node;

import com.vladsch.flexmark.internal.BlockContent;
import com.vladsch.flexmark.internal.util.BasedSequence;

import java.util.List;

public class BulletList extends ListBlock {
    private char bulletMarker;

    public BulletList() {
    }

    public BulletList(BasedSequence chars) {
        super(chars);
    }

    public BulletList(BasedSequence chars, List<BasedSequence> segments) {
        super(chars, segments);
    }

    public BulletList(BlockContent blockContent) {
        super(blockContent);
    }

    public char getBulletMarker() {
        return bulletMarker;
    }

    public void setBulletMarker(char bulletMarker) {
        this.bulletMarker = bulletMarker;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
