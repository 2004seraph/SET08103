package com.napier.SET08103.repl.commands;

import com.napier.SET08103.repl.commands.implementations.Leaderboard;
import com.napier.SET08103.repl.commands.implementations.PopulationOf;

/**
 * This is where a command name (like "leaderboard ...") is mapped to its implementation class,
 * by using Command.valueOf(...).
 *
 * This means the enum value will be what the command name is.
 */
public enum Command {
    // Linking to a new instance of the implementation
    LEADERBOARD(new Leaderboard()),
    TOTAL(new PopulationOf());

    private final ICommand command;

    public static final Command[] asArray = values();

    private Command(ICommand command) {
        this.command = command;
    }

    // Singleton behaviour
    public ICommand Instance() {
        return this.command;
    }
}
