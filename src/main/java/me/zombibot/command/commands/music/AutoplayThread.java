package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.zombibot.Config;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class AutoplayThread implements Runnable {

    private final AudioPlayer player;
    private final TextChannel channel;

    public AutoplayThread(PlayerManager playerManager, Guild guild, TextChannel channel) {
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(guild);
        this.player = musicManager.player;
        this.channel = channel;
    }

    @Override
    public void run() {

            ArrayList<String> songList = new ArrayList<String>();
            ArrayList<String> noDuplicateSongList = new ArrayList<String>();
            Set<String> set = new LinkedHashSet<>();
            String input = null;
            String songSelected = null;
            Scanner fileReader = null;

            try {
                File songFile = new File(Config.get("resource_file_song_history"));
                fileReader = new Scanner(songFile);

                while (fileReader.hasNext()) {
                    String theLine = fileReader.nextLine();
                    songList.add(theLine);
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

            Collections.shuffle(songList);
            Collections.shuffle(songList);
            set.addAll(songList); //Makes everything unique no duplicates
            noDuplicateSongList.addAll(set);

            int count = -1;
            while (true) {
                try {
                    Thread.sleep(2000);
                    if (Globals.isAutoplayToggledOn && this.player.getPlayingTrack() == null) {
                        count++;

                        //Take 1 song and load it
                        if (count == noDuplicateSongList.size() -1) {
                            channel.sendMessage("I have played everything I have to offer from your history. You may restart autoplay to reset this").queue();
                            break;
                        }
                        songSelected = noDuplicateSongList.get(count);
                        input = "ytsearch:" + songSelected;

                        PlayerManager.getInstance().loadAndPlay(this.channel, input, true);
                    }
                    else if (!Globals.isAutoplayToggledOn || Globals.isSelfInVoice == false) {
                        break;
                    }
                }
                catch (InterruptedException e) {
                    if (count == -1) {
                        count = 0;
                    }
                    e.printStackTrace();
                }
            }//end while
    }
}
