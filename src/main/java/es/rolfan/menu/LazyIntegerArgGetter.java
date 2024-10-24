package es.rolfan.menu;

public class LazyIntegerArgGetter extends LazyArgGetter<Integer> {
    @Override
    public ArgGetter<Integer> instanceGetter() {
        return new IntegerArgGetter();
    }
}
