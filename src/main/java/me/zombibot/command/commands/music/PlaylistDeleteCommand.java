package me.zombibot.command.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaylistDeleteCommand implements ICommand {

    private EventWaiter waiter = null;

    public PlaylistDeleteCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            channel.sendMessage("Please provide a playlist name to delete").queue();
            return;
        }

        String playlistName = args.get(0);

        //Make certain the user wants to delete the playlist
        channel.sendMessage("Are you sure you want to delete the playlist: " + playlistName + "?").setActionRow(Button.danger("btn_delete", "Delete"),
                Button.secondary("btn_cancel_delete", "Cancel")).queue((message) -> {
					
            this.waiter.waitForEvent(
                    ButtonClickEvent.class, (e) -> e.getMessageIdLong() == message.getIdLong() && !e.getUser().isBot(),
                    (e) -> {
                        try {
                            switch (e.getComponentId()) {
                                case "btn_delete":
                                    try
                                    {
                                        File playlistFile = new File(Config.get("playlist_files_init_path") + playlistName + ".txt");
                                        if (playlistFile.delete()) {
                                            channel.sendMessage("Successfully deleted playlist: " + playlistName).queue();
                                        }
                                        else {
                                            channel.sendMessage("Something went wrong when trying to delete the playlist. Please try again").queue();
                                            return;
                                        }

                                        e.reply("Successfully deleted the playlist: " + playlistName).queue();
                                    }
                                    catch(Exception ioe)
                                    {
                                        System.err.println("IOException: " + ioe.getMessage());
                                        e.reply("Something went wrong when trying to delete the playlist. Please try again").queue();
                                    }
                                    break;
                                case "btn_cancel_delete":
                                    e.reply("Ok, I canceled the delete request").queue();
                                    break;
                                default:
                                    break;
                            }
                        }
                        catch (IllegalStateException ex) {
                            System.out.println("Cancel btn has been selected");
                        }
                    },
                    1L, TimeUnit.MINUTES,
                    () -> channel.sendMessage("You waited too long to respond so I canceled the request..").queue()
            );
        });

    }

    @Override
    public String getName() {
        return "playlistdelete";
    }

    @Override
    public String getHelp() {
        return "Deletes a specified playlist" +
                "\nUsage: `zplaylistdelete [playlist name]`";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pld", "playlistd");
    }
}
