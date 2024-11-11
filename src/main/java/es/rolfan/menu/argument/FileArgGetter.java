package es.rolfan.menu.argument;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

public class FileArgGetter implements ArgGetter<InputStream> {
    @Override
    public InputStream get(String param, String name) throws Exception {
        System.out.print(param.isEmpty() ? "Introduce el archivo \"" + name + "\": " : param);
        return new FileInputStream(new Scanner(System.in).nextLine());
    }
}
