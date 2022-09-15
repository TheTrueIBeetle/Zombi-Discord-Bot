package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ShuffleCommand implements ICommand {


    @Override
    public void handle(CommandContext ctx) {
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());

        if (musicManager.scheduler.getQueue().isEmpty()){
            ctx.getChannel().sendMessage("There is currently nothing in the queue. Nothing to shuffle.").queue();
        }
        else {
            BlockingQueue<AudioTrack> bq = musicManager.scheduler.getQueue();

            List<AudioTrack> tempList = new ArrayList<>(bq); //Adds all elements from blocking queue to thee list
            Collections.shuffle(tempList);

            musicManager.scheduler.getQueue().clear();
            musicManager.scheduler.getQueue().addAll(tempList);

            ctx.getChannel().sendMessage("\uD83D\uDD00 Shuffling queue..").queue();
        }
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getHelp() {
        return "Shuffles the current queue";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("mix", "shuffl", "shuff", "shuffel", "shufle", "shullfe", "shufl");
    }
}
