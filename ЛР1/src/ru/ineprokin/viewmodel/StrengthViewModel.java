package ru.ineprokin.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.ineprokin.Config;
import ru.ineprokin.model.Alphabet;
import ru.ineprokin.model.AlphabetOption;
import ru.ineprokin.model.PasswordGenerator;
import ru.ineprokin.model.SpeedUnit;
import ru.ineprokin.model.StrengthEstimator;
import ru.ineprokin.model.StrengthResult;
import ru.ineprokin.model.StrengthTask;
import ru.ineprokin.model.TimeUnit;
import ru.ineprokin.util.Fmt;

import java.math.BigDecimal;

public class StrengthViewModel {

    private final PasswordGenerator generator = new PasswordGenerator();

    private final StringProperty p = new SimpleStringProperty(Config.DEFAULT_P);
    private final StringProperty speed = new SimpleStringProperty(Config.DEFAULT_SPEED);
    private final StringProperty time = new SimpleStringProperty(Config.DEFAULT_TIME);
    private final ObjectProperty<SpeedUnit> speedUnit = new SimpleObjectProperty<>(SpeedUnit.PER_MINUTE);
    private final ObjectProperty<TimeUnit> timeUnit = new SimpleObjectProperty<>(TimeUnit.WEEKS);
    private final ObjectProperty<AlphabetOption> selected = new SimpleObjectProperty<>();
    private final IntegerProperty count = new SimpleIntegerProperty(Config.DEFAULT_PASSWORD_COUNT);

    private final ObservableList<AlphabetOption> options = FXCollections.observableArrayList();
    private final ObservableList<String> passwords = FXCollections.observableArrayList();
    private final ReadOnlyStringWrapper attemptsText = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper boundText = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper choiceText = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper errorText = new ReadOnlyStringWrapper("");

    public StrengthViewModel() {
        selected.addListener((obs, old, value) -> updateChoice(value));
        calculate();
    }

    public StringProperty pProperty() {
        return p;
    }

    public StringProperty speedProperty() {
        return speed;
    }

    public StringProperty timeProperty() {
        return time;
    }

    public ObjectProperty<SpeedUnit> speedUnitProperty() {
        return speedUnit;
    }

    public ObjectProperty<TimeUnit> timeUnitProperty() {
        return timeUnit;
    }

    public ObjectProperty<AlphabetOption> selectedProperty() {
        return selected;
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public ObservableList<AlphabetOption> options() {
        return options;
    }

    public ObservableList<String> passwords() {
        return passwords;
    }

    public ReadOnlyStringProperty attemptsTextProperty() {
        return attemptsText.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty boundTextProperty() {
        return boundText.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty choiceTextProperty() {
        return choiceText.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty errorTextProperty() {
        return errorText.getReadOnlyProperty();
    }

    public void calculate() {
        try {
            StrengthTask task = new StrengthTask(number(p.get()), number(speed.get()), speedUnit.get(),
                    number(time.get()), timeUnit.get());
            StrengthResult result = StrengthEstimator.evaluate(task);
            Alphabet previous = selected.get() == null ? Alphabet.LETTERS_DIGITS : selected.get().alphabet();
            options.setAll(result.options());
            attemptsText.set("Число попыток за срок T:  V · T = "
                    + Fmt.group(result.attempts().toBigInteger()) + " паролей");
            boundText.set("Нижняя граница числа паролей:  S* = [V · T / P] = "
                    + Fmt.group(result.lowerBound()) + "  ≈ " + Fmt.sci(result.lowerBound()));
            errorText.set("");
            options.stream().filter(o -> o.alphabet() == previous).findFirst().ifPresent(selected::set);
            updateChoice(selected.get());
        } catch (RuntimeException e) {
            errorText.set("Некорректные исходные данные: " + e.getMessage());
        }
    }

    public void generate() {
        AlphabetOption option = selected.get();
        if (option == null) {
            return;
        }
        passwords.clear();
        for (int i = 0; i < count.get(); i++) {
            passwords.add(generator.generate(option.alphabet(), option.length()));
        }
    }

    private void updateChoice(AlphabetOption option) {
        choiceText.set(option == null ? "" : "Выбрано: A = " + option.alphabet().power()
                + ", L = " + option.length() + ", S = " + Fmt.sci(option.total())
                + " ≥ S*, фактическая P = " + Fmt.sci(option.probability()));
    }

    private BigDecimal number(String text) {
        return new BigDecimal(text.trim().replace(',', '.'));
    }
}
