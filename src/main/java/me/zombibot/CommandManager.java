/*
 * Project Name: ZombiBot
 * File Name: Bot.java
 * Type: Class
 * Author: Luke Bas
 * Date Created: 2021-08-29
 */

package me.zombibot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.command.commands.*;
import me.zombibot.command.commands.games.*;
import me.zombibot.command.commands.music.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager {

    private final List<ICommand> commands = new ArrayList<>(); //Should use hashmap

    public CommandManager(EventWaiter waiter){
        addCommand(new HelpCommand(this));
        addCommand(new LeaveCommand());
        addCommand(new PlayCommand());
        addCommand(new PauseCommand());
        addCommand(new SkipCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new ClearQueueCommand());
        addCommand(new QueueCommand());
        addCommand(new RepeatCommand());
        addCommand(new VolumeCommand());
        addCommand(new SeekCommand());
        addCommand(new ForwardCommand());
        addCommand(new RewindCommand());
        addCommand(new ShuffleCommand());
        addCommand(new AutoplayCommand());
        addCommand(new BulkCommand());
        addCommand(new MoveCommand());
        addCommand(new MoveLtfCommand());
        addCommand(new RemoveCommand());
        addCommand(new PadCommand());
        addCommand(new PlaylistCreateCommand());
        addCommand(new PlaylistAddCommand(waiter));
        addCommand(new PlaylistDeleteCommand(waiter));
        addCommand(new PlaylistRenameCommand());
        addCommand(new PlaylistShowCommand());
        addCommand(new PlaylistPlayCommand());
        addCommand(new PlaylistRemoveCommand());
    }

    private void addCommand(ICommand cmd){
        boolean commandFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (commandFound){
            throw new IllegalArgumentException("A command with this name is already present");
        }
        commands.add(cmd);
    }

    @Nullable
    public ICommand getCommand(String search){
        String searchLower = search.toLowerCase();
        for (ICommand c : this.commands){
            if (c.getName().equals(searchLower) || c.getAliases().contains(searchLower)){
                return c;
            }
        }
        return null;
    }

    public List<ICommand> getCommands(){
        return this.commands;
    }

    void handle(GuildMessageReceivedEvent event) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(Config.get("prefix")), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null){ //Needs this for possible null pointer exception
            event.getChannel().sendTyping().queue();
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);

            cmd.handle(ctx);
        }
    }
}
