package me.zombibot.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.zombibot.command.commands.games.ConnectFourService;
import me.zombibot.command.commands.games.HangmanService;
import me.zombibot.command.commands.games.Piece;
import me.zombibot.command.commands.games.TicTacToeService;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

public class Globals {

    //Autoplay stuff
    public static boolean isNewTrackPlaying = false;
    public static boolean isAutoplayToggledOn = false;

    //Util
    public static boolean isSelfInVoice = false;
    public static String currentVoiceChannel = null;
    public static String currentPlaylistName = null;
    public static AudioTrackInfo currentTrackToAdd = null;

    //Public util methods
    public synchronized static String getRandomLine(String path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Random random = new Random();
        return lines.get(random.nextInt(lines.size()));
    }
}
