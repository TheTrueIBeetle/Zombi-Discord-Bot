package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MoveCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        if (args.isEmpty()) {
            channel.sendMessage("Please provide 2 spots. To move from and new position").queue();
            return;
        }
        else if (args.size() == 1) {
            channel.sendMessage("Please provide 2 spots. To move from and new position").queue();
        }

        int posFrom = 0;
        int posTo = 0;
        try {
            posFrom = Integer.parseInt(args.get(0)) - 1;
            posTo = Integer.parseInt(args.get(1)) - 1;
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
            channel.sendMessage("Oops, you tried to use something other than a number. Try using a number").queue();
            return;
        }

        channel.sendMessage("Give me a second..").queue();

        ArrayList<AudioTrack> listToManip = new ArrayList<>(queue);

        //Attempt to move track
        if (moveEle(listToManip, posFrom, posTo, channel)) {
            queue.clear();
            queue.addAll(listToManip);

            channel.sendMessage("Successfully moved track").queue();
        }

    }

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public String getHelp() {
        return "Moves a specified track to a new spot in the queue. " +
                "\nUsage: `zmove [position from] [position to]`";
    }

    private boolean moveEle(List<?> collection, int indexToMoveFrom,
                         int indexToMoveAt, TextChannel channel) {
        try {
            if (indexToMoveAt >= indexToMoveFrom) {
                Collections.rotate(
                        collection.subList(indexToMoveFrom, indexToMoveAt + 1),
                        -1);
            } else {
                Collections.rotate(
                        collection.subList(indexToMoveAt, indexToMoveFrom + 1),
                        1);
            }
            return true;
        }
        catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            channel.sendMessage("Oops, try providing me a valid position").queue();
            return false;
        }
    }
}
