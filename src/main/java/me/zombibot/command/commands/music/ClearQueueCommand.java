package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ClearQueueCommand implements ICommand {


    @Override
    public void handle(CommandContext ctx) {
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());

        if (musicManager.scheduler.getQueue().isEmpty()){
            ctx.getChannel().sendMessage("There is currently nothing in the queue. Nothing to clear.").queue();
        }
        else {
            ctx.getChannel().sendMessage("Clearing queue..").queue();
            musicManager.scheduler.getQueue().clear();
            ctx.getChannel().sendMessage("Successfully Cleared the queue").queue();
        }

    }

    @Override
    public String getName() {
        return "clearqueue";
    }

    @Override
    public String getHelp() {
        return "Clears the entire queue";
    }

    @Override
    public List<String> getAliases() {
        //return List.of("clearq", "clear", "queueclear", "qclear");
        return Arrays.asList("clearq", "clear", "queueclear", "qclear", "cler", "clar", "claer"); //Java 8
    }
}
