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

        let userId
        let target = message.mentions.members.first() || message.guild.members.cache.get(args[0]);
        if (!target) {
            userId = message.author.id
        } else {
            userId = target.user.id
        };

        let users = us_read();

        if (users[userId]) {

            function progress_bar(currentXP, maxXP, barLength = 20, barCharacter = '█', emptyCharacter = '░') {
                const progress = Math.floor((currentXP / maxXP) * barLength);
                const emptyProgress = barLength - progress;

                const progressBar = barCharacter.repeat(progress) + emptyCharacter.repeat(emptyProgress);

                return progressBar
            };

            function comma(x) {
                return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
            };

            let placeholder_level = ``
            let placeholder_full_xp = ``
            let placeholder_color = ``
            let placeholder_xp = users[userId].XP
            let placeholder_max_xp = Number(Math.floor((users[userId].Level * config.us.xp_per) * Math.pow(config.us.xp_growth, users[userId].Level)));
            placeholder_full_xp = `${ShortNumber(Number(users[userId].XP.toFixed(2)))}/${ShortNumber(placeholder_max_xp)}`

            if (users[userId].Level >= config.us.max_level) {
                placeholder_level = `${users[userId].Level} (MAX)`
                placeholder_xp = 100
                placeholder_max_xp = 100
                //placeholder_full_xp = `${ShortNumber(Number(users[userId].XP.toFixed(2)))}`
            } else {
                placeholder_level = users[userId].Level
            };

            let XP_Bar = ""
            if (placeholder_xp >= placeholder_max_xp) {
                XP_Bar = progress_bar(100, 100)
            } else {
                XP_Bar = progress_bar(placeholder_xp, placeholder_max_xp)
            };

            if (users[userId].Level >= 0 && users[userId].Level <= 49) {
                placeholder_color = '#FFFFFF'
            } else {
                if (users[userId].Level >= 50 && users[userId].Level <= 99) {
                    placeholder_color = '#2ECC71'
                } else {
                    if (users[userId].Level >= 100 && users[userId].Level <= 149) {
                        placeholder_color = '#6495ED'
                    } else {
                        if (users[userId].Level >= 150 && users[userId].Level <= 199) {
                            placeholder_color = '#A569BD'
                        } else {
                            if (users[userId].Level >= 200 && users[userId].Level <= 249) {
                                placeholder_color = '#F5B041'
                            } else {
                                if (users[userId].Level >= 250 && users[userId].Level <= 299) {
                                    placeholder_color = '#FF00FF'
                                } else {
                                    if (users[userId].Level >= 300 && users[userId].Level <= 349) {
                                        placeholder_color = '#00FFFF'
                                    } else {
                                        if (users[userId].Level >= 350 && users[userId].Level <= 399) {
                                            placeholder_color = '#FF5555'
                                        } else {
                                            if (users[userId].Level >= 400 && users[userId].Level <= 449) {
                                                placeholder_color = '#AA0000'
                                            } else {
                                                if (users[userId].Level >= 450 && users[userId].Level <= 499) {
                                                    placeholder_color = '#AA0000'
                                                } else {
                                                    if (users[userId].Level >= 500 && users[userId].Level <= 549) {
                                                        placeholder_color = '#AA0000'
                                                    } else {
                                                        placeholder_color = '#000000'
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };

            const UserInfo = new EmbedBuilder()
                .addFields(
                    { name: '**Profile Stats**', value: `Level: **💠 ${placeholder_level}**\nCoins: **🪙 ${ShortNumber(users[userId].Coins)}**`, inline: true },
                    { name: `XP: ${placeholder_full_xp} (x${users[userId].Multiplier})`, value: `${XP_Bar}` },
                    { name: '**Tools**', value: `Fishing Rod: \`${users[userId].Tools.Rod}\`\nWatering Can: \`${users[userId].Tools.WateringCan}\`` },
                    { name: '**Inventory**', value: `Bait: \`🪱 ${comma(users[userId].Tools.Bait)}\`\nFish: \`🐟 ${comma(users[userId].Inventory.Fish)}\`\nWheat: \`🌾 ${comma(users[userId].Inventory.Wheat)}\`\nPotato: \`🥔 ${comma(users[userId].Inventory.Potato)}\`\nCarrot: \`🥕 ${comma(users[userId].Inventory.Carrot)}\`\nCorn: \`🌽 ${comma(users[userId].Inventory.Corn)}\`` })
                .setColor(placeholder_color)
                .setTimestamp()

            if (target) {
                UserInfo.setAuthor({ name: `${target.user.tag}`, iconURL: `${target.user.displayAvatarURL()}` })
                UserInfo.setThumbnail(target.user.displayAvatarURL())
                return message.reply({ embeds: [UserInfo] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete();
                            }
                        }, 15000);
                    })
                    .catch(console.error);
            } else {
                UserInfo.setAuthor({ name: `${message.author.tag}`, iconURL: `${message.author.displayAvatarURL()}` })
                UserInfo.setThumbnail(message.author.displayAvatarURL())
                return message.reply({ embeds: [UserInfo] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete();
                            }
                        }, 15000);
                    })
                    .catch(console.error);
            };

        } else {

            return message.reply(`ไม่มีข้อมูลของมึง ไอต่างด้าว`)
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 3000);
                })
                .catch(console.error);

        };

    } catch (error) {
        console.log(error)
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);;
    }

};

exports.help = {
    name: 'user',
    aliases: ['users', 'profile', 'view']
};