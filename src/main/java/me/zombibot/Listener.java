/*
 * Project Name: ZombiBot
 * File Name: Listener.java
 * Type: Class
 * Author: Luke Bas
 * Date Created: 2021-08-29
 */

package me.zombibot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.botcommons.BotCommons;
import me.zombibot.command.CommandContext;
import me.zombibot.command.commands.games.PlayConnectFourCommand;
import me.zombibot.command.commands.music.GuildMusicManager;
import me.zombibot.command.commands.music.PlayerManager;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Listener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final CommandManager manager;

    public Listener(EventWaiter waiter) {
        manager = new CommandManager(waiter);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event){
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        User user = event.getAuthor();

        if (user.isBot() || event.isWebhookMessage()){
            return;
        }

        String prefix = Config.get("prefix");
        String raw = event.getMessage().getContentRaw();

        if (raw.equalsIgnoreCase(prefix + "shutdown") && user.getId().equals(Config.get("owner_id"))){
            LOGGER.info("Shutting down");
            event.getJDA().shutdown();
            BotCommons.shutdown(event.getJDA());
        }


        if (raw.startsWith(Config.get("prefix"))){
            manager.handle(event);
        }
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event){
        final List<TextChannel> channelList = event.getGuild().getTextChannelsByName("general", true);

        if (channelList.isEmpty()){
            return;
        }

        final TextChannel defaultChannel = channelList.get(0);
        final String useGuildSpecificSettingsInstead = String.format("Welcome, %s to %s.",
                event.getMember().getUser().getAsTag(), event.getGuild().getName());

        defaultChannel.sendMessage(useGuildSpecificSettingsInstead + "\nIf you want to learn how to use me, use 'zhelp'").queue();
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel vc = event.getChannelLeft();
        String channelName = vc.getName();
        if (event.getChannelLeft().getMembers().isEmpty() && channelName.equals(Globals.currentVoiceChannel)) {
            Globals.isAutoplayToggledOn = false;
            Globals.isNewTrackPlaying = false;
            Globals.isSelfInVoice = false;
            Globals.currentVoiceChannel = null;
            audioManager.closeAudioConnection();
        }
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        switch (event.getComponentId()) {
            case "queue_playlist" :
                TextChannel channel = event.getTextChannel();
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();
                VoiceChannel memberChannel = memberVoiceState.getChannel();

                AudioManager audioManager = event.getGuild().getAudioManager();
                PlayerManager playerManager = PlayerManager.getInstance();
                GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
                AudioPlayer player = musicManager.player;

                //Join channel if possible (must not be in another voice channel currently)
                if(member.getVoiceState().inVoiceChannel()){
                    audioManager.openAudioConnection(memberChannel);
                    audioManager.setSelfDeafened(true);
                    Globals.isSelfInVoice = true;
                    Globals.currentVoiceChannel = memberChannel.getName(); //RECENT: Fixes bug with bot leaving when another channel empties
                    channel.sendMessageFormat("Connecting to \uD83D\uDD0A `%s`", memberChannel.getName()).queue();
                }
                else if (!member.getVoiceState().inVoiceChannel()) {
                    channel.sendMessage("Please join yourself to a voice channel first before queuing up music").queue();
                    return;
                }

                event.reply("Queuing up playlist: " + Globals.currentPlaylistName).queue();
                //Queue up playlist
                Scanner fileReader = null;
                try {
                    File songFile = new File(Config.get("playlist_files_init_path") + Globals.currentPlaylistName + ".txt");
                    fileReader = new Scanner(songFile);
                    ArrayList<String> songList = new ArrayList<String>();
                    ArrayList<String> noDuplicateSongList = new ArrayList<String>();
                    Set<String> set = new LinkedHashSet<>();

                    while (fileReader.hasNext()) {
                        String theLine = fileReader.nextLine();
                        songList.add(theLine);
                    }

                    for (String str : songList) {
                        String input = "ytsearch:" + str;
                        PlayerManager.getInstance().loadAndPlay(channel, input, true);
                    }
                }
                catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                finally {
                    if (fileReader != null) {
                        fileReader.close();
                    }
                }
                break;
            case "btn_shuffle":
                PlayerManager playerManager2 = PlayerManager.getInstance();
                GuildMusicManager musicManager2 = playerManager2.getGuildMusicManager(event.getGuild());
                if (musicManager2.scheduler.getQueue().isEmpty()){
                    event.reply("There is currently nothing in the queue. Nothing to shuffle.").queue();
                }
                else {
                    event.reply("\uD83D\uDD00 Shuffling queue..").queue();
                    BlockingQueue<AudioTrack> bq = musicManager2.scheduler.getQueue();

                    List<AudioTrack> tempList = new ArrayList<>(bq); //Adds all elements from blocking queue to thee list
                    Collections.shuffle(tempList);

                    musicManager2.scheduler.getQueue().clear();
                    musicManager2.scheduler.getQueue().addAll(tempList);
                }
                break;

            case "btn_moveltf":
                event.reply("Moving last element to next up..").queue();
                PlayerManager playerManager3 = PlayerManager.getInstance();
                GuildMusicManager musicManager3 = playerManager3.getGuildMusicManager(event.getGuild());
                BlockingQueue<AudioTrack> queue = musicManager3.scheduler.getQueue();

                ArrayList<AudioTrack> listToManip = new ArrayList<>(queue);
                moveEle(listToManip, listToManip.size() - 1, 0);

                queue.clear();
                queue.addAll(listToManip);

                break;
            case "btn_clear":
                PlayerManager playerManager4 = PlayerManager.getInstance();
                GuildMusicManager musicManager4 = playerManager4.getGuildMusicManager(event.getGuild());

                if (musicManager4.scheduler.getQueue().isEmpty()){
                    event.reply("There is currently nothing in the queue. Nothing to clear.").queue();
                }
                else {
                    event.reply("Clearing queue..").queue();
                    musicManager4.scheduler.getQueue().clear();
                }
                break;
            default:
                break;

        }
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
