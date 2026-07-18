const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {

        function ShortNumber(number) {
            if (number < 1000) {
                return number.toString();
            };

            const units = ["K", "M", "B", "T"];
            let unitIndex = Math.floor((number.toString().length - 1) / 3) - 1;
            let abbreviatedNumber = (number / Math.pow(1000, unitIndex + 1)).toFixed(1);

            if (abbreviatedNumber < 1 && unitIndex > 0) {
                unitIndex--;
                abbreviatedNumber = (number / Math.pow(1000, unitIndex + 1)).toFixed(1);
            };

            return `${abbreviatedNumber}${units[unitIndex]}`;
        };

        function us_read() {
            const rawData = fs.readFileSync('./database/users.json');
            return JSON.parse(rawData);
        };

        function progress_bar(currentXP, maxXP, barLength = 20, barCharacter = '█', emptyCharacter = '░') {
            const progress = Math.floor((currentXP / maxXP) * barLength);
            const emptyProgress = barLength - progress;

            const progressBar = barCharacter.repeat(progress) + emptyCharacter.repeat(emptyProgress);

            return progressBar
        };

        function comma(x) {
            return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        };

        const EventEmbed_Load = new EmbedBuilder()
            .setTitle(`Loading...`)
            .setColor('#FFFF00')
            .setTimestamp()

        let BoardPage = 1
        let mention = `<@${message.author.id}>`;

        const MainEmbed = await message.channel.send({ content: mention, embeds: [EventEmbed_Load] });

        async function LoadBoard() {

            if (BoardPage == 1) {

                try {
                    let readuser = us_read();
                    const Board = new EmbedBuilder()
                        .setAuthor({ name: `Top 10 XP Leaderboard`, iconURL: `${bot.user.displayAvatarURL()}` })
                        .setThumbnail(bot.user.displayAvatarURL())
                        .setColor('#E4FF00')

                    const userArray = Object.keys(readuser).map(userId => ({
                        id: userId,
                        ...readuser[userId]
                    }));

                    userArray.sort((a, b) => {
                        const aScore = a.Level * 100000 + a.XP;
                        const bScore = b.Level * 100000 + b.XP;
                        return bScore - aScore;
                    });

                    for (let i = 0; i < Math.min(userArray.length, 10); i++) {
                        let member = await bot.users.fetch(userArray[i].ID);
                        let maxXP = Number(Math.floor((userArray[i].Level * config.us.xp_per) * Math.pow(config.us.xp_growth, userArray[i].Level)));
                        let placeholder_level = ``
                        let placeholder_xp = userArray[i].XP
                        let placeholder_max_xp = Number(Math.floor((userArray[i].Level * config.us.xp_per) * Math.pow(config.us.xp_growth, userArray[i].Level)));

                        if (userArray[i].Level >= config.us.max_level) {
                            placeholder_level = `${userArray[i].Level} (MAX)`
                            placeholder_xp = 100
                            placeholder_max_xp = 100
                        } else {
                            placeholder_level = userArray[i].Level
                        };

                        let XP_Bar = ""
                        if (placeholder_xp >= placeholder_max_xp) {
                            XP_Bar = progress_bar(100, 100)
                        } else {
                            XP_Bar = progress_bar(placeholder_xp, placeholder_max_xp)
                        };

                        if (userArray[i].ID == message.author.id) {
                            if (userArray[i].Level >= config.us.max_level) {
                                Board.addFields({ name: `${i + 1}. ${member.username} (${member.displayName}) | <- YOU ARE HERE`, value: `Level: **💠 ${placeholder_level}**\nXP: **${ShortNumber(Math.floor(userArray[i].XP))}/${ShortNumber(maxXP)} (x${userArray[i].Multiplier.toFixed(1)})**\n${XP_Bar}` });
                            } else {
                                Board.addFields({ name: `${i + 1}. ${member.username} (${member.displayName}) | <- YOU ARE HERE`, value: `Level: **💠 ${placeholder_level}**\nXP: **${ShortNumber(Math.floor(userArray[i].XP))}/${ShortNumber(maxXP)} (x${userArray[i].Multiplier.toFixed(1)})**\n${XP_Bar}` });
                            }
                        } else {
                            if (userArray[i].Level >= config.us.max_level) {
                                Board.addFields({ name: `${i + 1}. ${member.username} (${member.displayName})`, value: `Level: **💠 ${placeholder_level}**\nXP: **${ShortNumber(Math.floor(userArray[i].XP))}/${ShortNumber(maxXP)} (x${userArray[i].Multiplier.toFixed(1)})**\n${XP_Bar}` });
                            } else {
                                Board.addFields({ name: `${i + 1}. ${member.username} (${member.displayName})`, value: `Level: **💠 ${placeholder_level}**\nXP: **${ShortNumber(Math.floor(userArray[i].XP))}/${ShortNumber(maxXP)} (x${userArray[i].Multiplier.toFixed(1)})**\n${XP_Bar}` });
                            }
                        }
                    };

                    await MainEmbed.edit({ embeds: [Board] })
                } catch (error) {
                    console.log(error)
                }

            } else {
                if (BoardPage == 2) {

                    try { 
                        let readuser = us_read();
                        const Board = new EmbedBuilder()
                            .setAuthor({ name: `Top 10 Coins Leaderboard`, iconURL: `${bot.user.displayAvatarURL()}` })
                            .setThumbnail(bot.user.displayAvatarURL())
                            .setColor('#E4FF00')

                        const userArray = Object.keys(readuser).map(userId => ({
                            id: userId,
                            ...readuser[userId]
                        }));

                        userArray.sort((a, b) => b.Coins - a.Coins);

                        for (let i = 0; i < Math.min(userArray.length, 10); i++) {
                            let member = await bot.users.fetch(userArray[i].ID);
                            if (userArray[i].ID == message.author.id) {
                                Board.addFields({ name: `${i + 1}. ${member.username} (${member.displayName}) | <- YOU ARE HERE`, value: `Coins: **🪙 ${ShortNumber(userArray[i].Coins)}**` });
                            } else {
                                Board.addFields({ name: `${i + 1}. ${member.username} (${member.displayName})`, value: `Coins: **🪙 ${ShortNumber(userArray[i].Coins)}**` });
                            }
                        };

                        await MainEmbed.edit({ embeds: [Board] })
                    } catch (error) {
                        console.log(error)
                    }

                } else {

                };
            };

        };

        message.delete();
        LoadBoard();
        await MainEmbed.react(`▶`);

        //bot.BotLogs(message.guild.name, `\x1b[35mDisplaying Global Leaderboard for \x1b[37m"${message.author.tag}"`)

        const collector = MainEmbed.createReactionCollector();

        collector.on('collect', async (reaction, user) => {
            switch (reaction.emoji.name) {
                case "▶":
                    if (user.id === "887531368836370483") return
                    BoardPage += 1
                    if (BoardPage > 2) { BoardPage = 1 }
                    LoadBoard()
                    reaction.users.remove(user);
                    break;
            };
        });

        setTimeout(async () => {
            MainEmbed.delete();
        }, 25000);

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);;
    }

};

exports.help = {
    name: 'leaderboard',
    aliases: ['board', 'top']
};