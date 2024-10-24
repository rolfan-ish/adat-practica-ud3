package es.rolfan.menu;

import java.io.FileReader;

public class LazyFileArgGetter extends LazyArgGetter<FileReader> {

    @Override
    public ArgGetter<FileReader> instanceGetter() {
        return new FileArgGetter();
    }
}
