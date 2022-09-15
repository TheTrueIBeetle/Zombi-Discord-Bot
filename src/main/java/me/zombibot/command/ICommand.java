/*
 * Project Name: ZombiBot
 * File Name: ICommand.java
 * Type: Interface
 * Author: Luke Bas
 * Date Created: 2021-08-29
 */

package me.zombibot.command;

import java.util.Arrays;
import java.util.List;

public interface ICommand {

    void handle(CommandContext ctx);

    String getName();

    String getHelp();

    default List<String> getAliases() {
        return Arrays.asList();
        //return List.of(); //Arrays.asList if using java 8
    }
}
