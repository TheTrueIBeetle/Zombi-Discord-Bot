package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class RemoveCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        ArrayList<AudioTrack> listToManip = new ArrayList<>(queue);

        int posToRemove = 0;
        try {
            posToRemove = Integer.parseInt(args.get(0));
            posToRemove--;
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
            channel.sendMessage("Oops, you tried to use something other than a number. Try using a number").queue();
            return;
        }


        try {
            listToManip.remove(posToRemove);
        }
        catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            channel.sendMessage("Oops, try providing me a valid position").queue();
            return;
        }

        queue.clear();
        queue.addAll(listToManip);

        channel.sendMessage("Successfully removed specified track").queue();
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getHelp() {
        return "Removes specified track within the queue. " +
                "\nUsage: `zremove [position]`";
    }
}
