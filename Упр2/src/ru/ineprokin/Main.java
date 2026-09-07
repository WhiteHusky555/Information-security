package ru.ineprokin;

import ru.ineprokin.model.AccessMatrix;
import ru.ineprokin.view.ConsoleView;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new ConsoleView(AccessMatrix.ofVariant()).run();
    }
}
