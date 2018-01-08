
package com.win.app.map;

import java.util.*;

/**
 * Created by 17010125 on 2017/12/18.
 */
public abstract class CopyAbstractSet<E> extends AbstractCollection<E> implements Set<E> {
    protected CopyAbstractSet() {
    }

    @Override
    public int hashCode() {
        int h = 0;
        Iterator<E> i   = iterator();

        while (i.hasNext()){
            E next = i.next();
            if (next != null){
                h+= next.hashCode();
            }
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        }
        if (!(obj instanceof Set)){
            return false;
        }
        Collection<?> c = (Collection<?>) obj;
        if (c.size() != size()){
            return false;
        }
        return containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modifyied = false;
        if (size()>c.size()){

            for (Iterator<?> iterator =c.iterator(); iterator.hasNext();){
                modifyied |=remove(iterator.next());
            }
        }else {

            for (Iterator<?> iterator =c.iterator();iterator.hasNext();){
                if (c.contains(iterator.next())){
                    iterator.remove();
                    modifyied =true;
                }
            }
        }
        return modifyied;
    }


}
