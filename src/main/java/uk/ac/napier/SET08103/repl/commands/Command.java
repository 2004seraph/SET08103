package uk.ac.napier.SET08103.repl.commands;

import uk.ac.napier.SET08103.repl.commands.implementations.*;

/**
 * This is where a command name (like "leaderboard ...") is mapped to its implementation class,
 * by using Command.valueOf(...).
 * <p>
 * This means the enum value will be what the command name is.
 */
public enum Command {
    // Linking to a new instance of the implementation
    HELP(new Help()),
    LEADERBOARD(new Leaderboard()),
    INFO(new RichPopulationInfo()),
    TOTAL(new PopulationOf()),
    LANGUAGES(new LanguageStats());

    @SuppressWarnings("unused")
    public static final Command[] asArray = values();

    private final ICommand command;

    Command(final ICommand command) {
        this.command = command;
    }

    // Singleton behaviour
    public ICommand Instance() {
        return this.command;
    }
}
