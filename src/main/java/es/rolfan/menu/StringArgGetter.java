package es.rolfan.menu;

import java.util.Scanner;

public class StringArgGetter implements ArgGetter<String> {

    @Override
    public String get(String param, String name) {
        System.out.print(param.isEmpty() ? "Introduce el string \"" + name + "\": " : param);
        return new Scanner(System.in).nextLine();
    }
}
