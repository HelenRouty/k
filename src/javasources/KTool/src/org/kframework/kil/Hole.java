package org.kframework.kil;

import org.kframework.kil.loader.Constants;
import org.kframework.kil.visitors.Visitor;
import org.w3c.dom.Element;

/**
 * Represents {@code HOLE} in context declarations.
 * See {@link FreezerHole} for holes in freezers.
 */
public class Hole extends Term {

    public static final Hole KITEM_HOLE = new Hole(KSorts.KITEM);

    private Hole(Element element) {
        super(element);
        this.sort = element.getAttribute(Constants.SORT_sort_ATTR);
    }

    private Hole(Hole hole) {
        super(hole);
    }

    private Hole(String sort) {
        super(sort);

        assert sort.equals(KSorts.KITEM);
    }

    public String toString() {
        return "[]:" + sort + " ";
    }

    @Override
    public Hole shallowCopy() {
        return new Hole(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (!(obj instanceof Hole))
            return false;
        Hole hole = (Hole)obj;

        return this.sort.equals(hole.getSort());
    }

    @Override
    public int hashCode() {
        return sort.hashCode();
    }

    @Override
    public <P, R> R accept(Visitor<P, R> visitor, P p) {
        return visitor.visit(this, p);
    }
}
