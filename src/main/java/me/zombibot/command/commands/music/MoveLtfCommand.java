package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class MoveLtfCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        channel.sendMessage("Give me a second..").queue();
        ArrayList<AudioTrack> listToManip = new ArrayList<>(queue);
        moveEle(listToManip, listToManip.size() - 1, 0);

        queue.clear();
        queue.addAll(listToManip);

        channel.sendMessage("Successfully moved last track to first / next up").queue();
    }

    @Override
    public String getName() {
        return "moveltf";
    }

    @Override
    public String getHelp() {
        return "Moves last track to the first position";
    }

    private void moveEle(List<?> collection, int indexToMoveFrom,
                         int indexToMoveAt) {
        if (indexToMoveAt >= indexToMoveFrom) {
            Collections.rotate(
                    collection.subList(indexToMoveFrom, indexToMoveAt + 1),
                    -1);
        } else {
            Collections.rotate(
                    collection.subList(indexToMoveAt, indexToMoveFrom + 1),
                    1);
        }
    }
}
