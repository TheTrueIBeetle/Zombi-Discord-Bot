package me.zombibot.command.commands.music;

import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.util.Globals;
import me.zombibot.util.LinkConverter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")

public class PlayCommand implements ICommand {


    @Override
    public void handle(CommandContext ctx) {

        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final Member member = ctx.getMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        final VoiceChannel memberChannel = memberVoiceState.getChannel();
        AudioManager audioManager = ctx.getGuild().getAudioManager();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());

        //Check permission first
        if (!self.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("I don't have permission to join voice channels in this server. You must enable me to do so").queue();
        }

        //Join channel if possible (must not be in another voice channel currently)
        if(self.hasPermission(Permission.VOICE_CONNECT) && !self.getVoiceState().inVoiceChannel() && member.getVoiceState().inVoiceChannel()){
            audioManager.openAudioConnection(memberChannel);
            audioManager.setSelfDeafened(true);
            Globals.isSelfInVoice = true;
            Globals.currentVoiceChannel = memberChannel.getName(); //RECENT: Fixes bug with bot leaving when another channel empties
            channel.sendMessageFormat("Connecting to \uD83D\uDD0A `%s`", memberChannel.getName()).queue();
        }
        else if (self.hasPermission(Permission.VOICE_CONNECT) && !member.getVoiceState().inVoiceChannel()) {
            channel.sendMessage("Please join yourself to a voice channel first before queuing up music").queue();
            return;
        }

        if (self.hasPermission(Permission.VOICE_CONNECT) && self.getVoiceState().inVoiceChannel() && member.getVoiceState()
                .getChannel() != self.getVoiceState().getChannel()) {
            channel.sendMessage("Sorry, I'm currently in another voice channel at the moment").queue();
            return;
        }



        //Check for pauses
        if (args.isEmpty() && !musicManager.player.isPaused()){
            channel.sendMessage("Nothing is currently paused. Please enter a song title or link for me to play").queue();
            return;
        }
        else if (args.isEmpty() && musicManager.player.isPaused()) {
            musicManager.player.setPaused(false);
            channel.sendMessage("Continuing the paused track...").queue();
            return;
        }


        String input = String.join(" ", args);

        //Check if input is a spotify link
        ArrayList<String> possibleTracks = new ArrayList<>();
        if (input.contains("spotify")) {
            channel.sendMessage("Unfortunately, spotify links will never work with lavaplayer natively because spotify does not " +
                    "allow robots and other programs to play spotify sound which is beyond my control." +
                    "Sorry for the inconvenience.").queue();
            return;
        }

        if (!isUrl(input)) {
            input = "ytsearch:" + input + " audio" + " explicit"; //These addons can be changed accordingly. For this we are using "audio" and "explicit" as a filter.
																  //"ytsearch" is reequired for lavaplayer to recognize
        }

        PlayerManager.getInstance().loadAndPlay(channel, input, false);

    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        }
        catch (URISyntaxException ignored){
            return false;
        }
    }


    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getHelp() {
        return "Plays a song\n" +
                "Usage: `zplay [song or playlist URL OR name of song]` OR `zplay` to just continue from a paused song";
    }
}
