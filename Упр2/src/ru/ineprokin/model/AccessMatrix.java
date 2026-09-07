package ru.ineprokin.model;

import ru.ineprokin.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class AccessMatrix {

    private final List<String> users;
    private final List<String> objects;
    private final List<List<EnumSet<Permission>>> rights = new ArrayList<>();

    public AccessMatrix(List<String> users, List<String> objects, Random random) {
        this.users = List.copyOf(users);
        this.objects = List.copyOf(objects);
        for (int u = 0; u < users.size(); u++) {
            List<EnumSet<Permission>> row = new ArrayList<>();
            for (int o = 0; o < objects.size(); o++) {
                row.add(u == Config.ADMIN_INDEX ? EnumSet.allOf(Permission.class) : randomRights(random));
            }
            rights.add(row);
        }
    }

    public static AccessMatrix ofVariant() {
        return new AccessMatrix(
                Arrays.asList(Config.USER_NAMES).subList(0, Config.USER_COUNT),
                Arrays.asList(Config.OBJECT_NAMES).subList(0, Config.OBJECT_COUNT),
                new Random());
    }

    public List<String> users() {
        return users;
    }

    public List<String> objects() {
        return objects;
    }

    public boolean hasUser(String user) {
        return users.contains(user);
    }

    public boolean isAdmin(String user) {
        return users.indexOf(user) == Config.ADMIN_INDEX;
    }

    public EnumSet<Permission> rights(String user, int objectIndex) {
        return EnumSet.copyOf(rights.get(users.indexOf(user)).get(objectIndex));
    }

    public boolean allowed(String user, int objectIndex, Permission permission) {
        return rights.get(users.indexOf(user)).get(objectIndex).contains(permission);
    }

    public String rightsText(String user, int objectIndex) {
        return describe(rights.get(users.indexOf(user)).get(objectIndex));
    }

    public OperationResult perform(String user, int objectIndex, Permission permission) {
        if (!validObject(objectIndex)) {
            return OperationResult.denied("Объекта с таким номером не существует");
        }
        if (!allowed(user, objectIndex, permission)) {
            return OperationResult.denied("Отказ в выполнении операции. У Вас нет прав для ее осуществления");
        }
        return OperationResult.ok("Операция прошла успешно");
    }

    public OperationResult grant(String from, String to, int objectIndex, Permission permission) {
        if (!validObject(objectIndex)) {
            return OperationResult.denied("Объекта с таким номером не существует");
        }
        if (!hasUser(to)) {
            return OperationResult.denied("Отказ в выполнении операции. Пользователь " + to + " не найден");
        }
        if (from.equals(to)) {
            return OperationResult.denied("Отказ в выполнении операции. Права передаются другому пользователю");
        }
        if (!allowed(from, objectIndex, Permission.GRANT)) {
            return OperationResult.denied("Отказ в выполнении операции. У Вас нет прав для ее осуществления");
        }
        if (!allowed(from, objectIndex, permission)) {
            return OperationResult.denied("Отказ в выполнении операции. Вы не владеете передаваемым правом");
        }
        rights.get(users.indexOf(to)).get(objectIndex).add(permission);
        return OperationResult.ok("Операция прошла успешно");
    }

    public boolean validObject(int objectIndex) {
        return objectIndex >= 0 && objectIndex < objects.size();
    }

    private EnumSet<Permission> randomRights(Random random) {
        EnumSet<Permission> set = EnumSet.noneOf(Permission.class);
        for (Permission permission : Permission.values()) {
            if (random.nextBoolean()) {
                set.add(permission);
            }
        }
        return set;
    }

    private String describe(EnumSet<Permission> set) {
        if (set.isEmpty()) {
            return "Запрет";
        }
        if (set.size() == Permission.values().length) {
            return "Полные права";
        }
        StringBuilder sb = new StringBuilder();
        for (Permission permission : set) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(permission.title());
        }
        return sb.toString();
    }
}
