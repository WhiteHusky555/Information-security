package ru.ineprokin.view;

import ru.ineprokin.Config;
import ru.ineprokin.model.AccessMatrix;
import ru.ineprokin.model.OperationResult;
import ru.ineprokin.model.Permission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class ConsoleView {

    private final AccessMatrix matrix;
    private final BufferedReader reader;
    private final PrintStream out;

    public ConsoleView(AccessMatrix matrix) {
        Charset charset = Charset.forName(System.getProperty("stdout.encoding", "UTF-8"));
        this.matrix = matrix;
        this.reader = new BufferedReader(new InputStreamReader(System.in, charset));
        this.out = new PrintStream(System.out, true, charset);
    }

    public void run() throws IOException {
        out.println("Дискреционная модель политики безопасности. Вариант " + Config.VARIANT
                + ": пользователей — " + matrix.users().size() + ", объектов — " + matrix.objects().size());
        printMatrix();
        while (true) {
            out.print("\nUser: ");
            String login = reader.readLine();
            if (login == null) {
                return;
            }
            login = login.trim();
            if (login.equalsIgnoreCase("exit")) {
                out.println("Завершение работы программы.");
                return;
            }
            if (!matrix.hasUser(login)) {
                out.println("Идентификация не пройдена: пользователь " + login + " не зарегистрирован в системе");
                continue;
            }
            session(login);
        }
    }

    private void session(String user) throws IOException {
        out.println("Идентификация прошла успешно, добро пожаловать в систему"
                + (matrix.isAdmin(user) ? " (администратор)" : ""));
        printRights(user);
        while (true) {
            out.print("Жду ваших указаний > ");
            String command = reader.readLine();
            if (command == null) {
                return;
            }
            command = command.trim().toLowerCase();
            switch (command) {
                case "" -> {
                }
                case "quit" -> {
                    out.println("Работа пользователя " + user + " завершена. До свидания.");
                    return;
                }
                case "rights" -> printRights(user);
                case "matrix" -> printMatrix();
                case "help" -> printHelp();
                case "read", "write" -> operate(user, Permission.byCommand(command));
                case "grant" -> grant(user);
                default -> out.println("Неизвестная команда. Доступны: read, write, grant, rights, matrix, help, quit");
            }
        }
    }

    private void operate(String user, Permission permission) throws IOException {
        int object = askObject("Над каким объектом производится операция? ");
        OperationResult result = matrix.perform(user, object, permission);
        out.println(result.message());
    }

    private void grant(String user) throws IOException {
        int object = askObject("Право на какой объект передается? ");
        if (!matrix.validObject(object)) {
            out.println("Объекта с таким номером не существует");
            return;
        }
        out.print("Какое право передается? ");
        String value = reader.readLine();
        Permission permission = value == null ? null : Permission.byCommand(value);
        if (permission == null) {
            out.println("Неизвестное право. Доступны: read, write, grant");
            return;
        }
        out.print("Какому пользователю передается право? ");
        String target = reader.readLine();
        if (target == null) {
            return;
        }
        OperationResult result = matrix.grant(user, target.trim(), object, permission);
        out.println(result.message());
        if (result.success()) {
            printMatrix();
        }
    }

    private int askObject(String prompt) throws IOException {
        out.print(prompt);
        String value = reader.readLine();
        try {
            return Integer.parseInt(value == null ? "" : value.trim()) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void printRights(String user) {
        out.println("Перечень Ваших прав:");
        for (int i = 0; i < matrix.objects().size(); i++) {
            out.printf("  %d. %-10s %s%n", i + 1, matrix.objects().get(i), matrix.rightsText(user, i));
        }
    }

    private void printMatrix() {
        out.println("\nМатрица доступа:");
        out.printf("%-12s", "Субъект");
        for (String object : matrix.objects()) {
            out.printf("| %-22s", object);
        }
        out.println();
        for (String user : matrix.users()) {
            out.printf("%-12s", user + (matrix.isAdmin(user) ? " *" : ""));
            for (int i = 0; i < matrix.objects().size(); i++) {
                out.printf("| %-22s", matrix.rightsText(user, i));
            }
            out.println();
        }
        out.println("* — администратор системы");
    }

    private void printHelp() {
        out.println("read   — чтение объекта");
        out.println("write  — запись в объект");
        out.println("grant  — передача права другому пользователю");
        out.println("rights — перечень своих прав");
        out.println("matrix — вся матрица доступа");
        out.println("quit   — выход из системы (exit в приглашении User: — выход из программы)");
    }
}
