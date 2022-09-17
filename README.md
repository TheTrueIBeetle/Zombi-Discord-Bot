# Zombi-Discord-Bot
A Discord bot capable of taking commands related to music

This bot is able to parse incomming commands using a prefix. The commands are for the most part, music based meaning the bot
will join a voice channel within a Discord server and play music. Commands are capable of maniuplating the tracks, playlists, etc.

!NOTE: 
Some of the code and designs decisions made in this project should
never be done in a professional environment. The two main bad practises within this project are the use of makeshift global variables using static, and saving data to text files
instead of an organized database. The only reason this project uses these bad practises is because it was intended to be for one private server.
If you are interested in using this as a template to create your own Discord bot, I would recommend not using some of the strategies that were used within this project. 
Also, the .env file is a template that you can change to fit your own project.
