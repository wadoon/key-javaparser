class Ghost {
    public /*@helper@*/ int size() {
        //@ ghost \locset pcDep = \empty;
        final int x;
        //@ set ArrayList.resultDep = \set_union(pcDep, sizeDep);
        return size;
    }
}