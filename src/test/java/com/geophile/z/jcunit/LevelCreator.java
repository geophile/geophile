package com.geophile.z.jcunit;

import java.io.Serializable;

abstract class LevelCreator<T> implements Serializable {
    public abstract T create();

    public abstract int levelId();

    public abstract String factorName();

    @Override
    public int hashCode() {
        return levelId();
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof LevelCreator)) return false;
        LevelCreator another;
        another = (LevelCreator) anotherObject;
        return this.factorName().equals(another.factorName()) && this.levelId() == another.levelId();
    }
}