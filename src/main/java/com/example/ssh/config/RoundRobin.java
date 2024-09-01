package com.example.ssh.config;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin<T> implements Iterable<T> {
    private final List<T> items;

    public RoundRobin(final List<T> coll) {
        this.items = Collections.unmodifiableList(coll);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private final AtomicInteger index = new AtomicInteger(0);

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public synchronized T next() {
                int idx = index.getAndUpdate(x -> {
                    // handle overflow
                    if (x == Integer.MAX_VALUE) {
                        return (x % items.size()) + 1;
                    } else {
                        return ++x;
                    }
                });
                return items.get(idx % items.size());
            }

            @Override
            public void remove() {
                throw new IllegalArgumentException("remove not allowed");
            }
        };
    }
}