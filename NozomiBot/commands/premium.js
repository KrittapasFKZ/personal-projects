const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const fs = require("fs");
const path = require('path');

exports.run = async (bot, message, args) => {

    try {

        if (message.author.id == "605361556297089035" || message.author.id == "852653820566831145" || message.author.id == "371505406578524160") { } else {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Access Denied`)
                .setDescription(`You don't have permission to use this.`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete()
                        }
                    }, 5000);
                })
                .catch(console.error);
        };

        function us_read() {
            const rawData = fs.readFileSync('./database/premium.json');
            return JSON.parse(rawData);
        };

        let userId;
        let database = us_read();

        function createNewMonthDatabase() {
            const date = new Date();
            const month = date.getMonth() + 1;
            const year = date.getFullYear() + 543;

            const monthKey = `${month}/${year}`;

            if (!database[monthKey]) {
                database[monthKey] = {
                    "Month": date.toLocaleString('en-US', { month: 'long' }),
                    "Year": year,
                    "Users": []
                };
                bot.BotLogs(message.guild.name, `\x1b[91mNew Premium Subscription Month created for \x1b[90m[\x1b[37m${monthKey}\x1b[90m]`);

                fs.writeFileSync('./database/premium.json', JSON.stringify(database, null, 2));
            };
        };

        function getCurrentMonthUsers() {
            const date = new Date();
            const month = date.getMonth() + 1;
            const year = date.getFullYear() + 543;
            const monthKey = `${month}/${year}`;

            if (database[monthKey]) {
                return database[monthKey].Users;
            } else {
                return null;
            }
        }

        createNewMonthDatabase();

        let target = message.mentions.members.first() || message.guild.members.cache.get(args[0]);
        let paid_amount = args[1];

        if (target) {

            userId = target.user.id
            if (!paid_amount) {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Command Usage`)
                    .setDescription(`\`!premium <@user> <amount>\``)
                    .setColor('#FFFF00')
                    .setTimestamp()

                return message.reply({ embeds: [EventEmbed_Failed] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete()
                            }
                        }, 5000);
                    })
                    .catch(console.error);
            };

            if (paid_amount <= 0) {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Error Occurred`)
                    .setDescription(`Invalid amount.`)
                    .setColor('#FF0000')
                    .setTimestamp()

                return message.reply({ embeds: [EventEmbed_Failed] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete()
                            }
                        }, 5000);
                    })
                    .catch(console.error);
            };

            let date = new Date();
            let month = date.getMonth() + 1;
            let year = date.getFullYear() + 543;
            let monthKey = `${month}/${year}`;

            if (!database[monthKey]) {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Error Occurred`)
                    .setDescription(`This Month doesn't exist!`)
                    .setColor('#FF0000')
                    .setTimestamp()

                return message.reply({ embeds: [EventEmbed_Failed] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete()
                            }
                        }, 5000);
                    })
                    .catch(console.error);
            }

            const existingUser = database[monthKey].Users.find(user => user.ID === userId);

            if (existingUser) {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Error Occurred`)
                    .setDescription(`This User has already paid!`)
                    .setColor('#FF0000')
                    .setTimestamp()

                return message.reply({ embeds: [EventEmbed_Failed] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete()
                            }
                        }, 5000);
                    })
                    .catch(console.error);
            } else {
                database[monthKey].Users.push({
                    "ID": userId,
                    "Name": `${target.user.tag}`,
                    "Paid_Amount": Number(paid_amount)
                });

                fs.writeFileSync('./database/premium.json', JSON.stringify(database, null, 2));
                bot.BotLogs(message.guild.name, `\x1b[91mNew Subscription Paid by \x1b[90m[\x1b[37m${target.user.tag}\x1b[90m] \x1b[91mwith \x1b[90m[\x1b[37m${paid_amount} THB\x1b[90m]`);

                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Action Completed`)
                    .setDescription(`Added ${target} to the list! Amount: **${paid_amount} THB**`)
                    .setColor('#00FF00')
                    .setTimestamp()
                    .setThumbnail(target.user.displayAvatarURL())

                return message.reply({ embeds: [EventEmbed_Failed] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete()
                            }
                        }, 5000);
                    })
                    .catch(console.error);

            };

        } else {

            message.delete();
            let CurrentPage = "Main"
            let MainTimeOut

            const EventEmbed_Load = new EmbedBuilder()
                .setTitle(`Loading...`)
                .setColor('#FFFF00')
                .setTimestamp()

            const MainEmbed = await message.channel.send({ embeds: [EventEmbed_Load] })

            const Page_Main = async () => {
                let users = getCurrentMonthUsers();
                let current_amount = 0
                let expected_amount = 60 * 4
                let calculate = ``

                let place = 0

                let date = new Date();
                let month = date.getMonth() + 1;
                let year = date.getFullYear() + 543;

                if (!users || users.length === 0) {
                    const Board = new EmbedBuilder()
                        .setTitle(`Premium Subscription for ${month}/${year}`)
                        .setDescription('No payments have been made this month.')
                        .setColor('#ff6666')
                        .setThumbnail("https://static-00.iconduck.com/assets.00/youtube-icon-2048x2048-oa03jx3h.png")

                    await MainEmbed.edit({ embeds: [Board] })
                    clearTimeout(MainTimeOut)
                    MainTimeOut = setTimeout(() => {
                        MainEmbed.delete()
                    }, 15000);
                    return;
                };

                const Board = new EmbedBuilder()
                    .setTitle(`Premium Subscription for ${month}/${year}`)
                    .setColor('#ff6666')
                    .setThumbnail("https://static-00.iconduck.com/assets.00/youtube-icon-2048x2048-oa03jx3h.png")

                let userList = await Promise.all(users.map(async (user) => {
                    try {
                        let member = await bot.users.fetch(user.ID);
                        place += 1
                        Board.addFields({ name: `\`\`\`${place}.) ${member.username} (${member.displayName}) | 💵 ${user.Paid_Amount.toFixed(2)} THB\`\`\``, value: ` ` })
                        current_amount += Number(user.Paid_Amount)
                    } catch (error) {
                        place += 1
                        Board.addFields({ name: `\`\`\`${place}.) ไอเชี่ยนี่ใครวะ ไม่รู้ว่ะ | 💵 ${user.Paid_Amount.toFixed(2)} THB\`\`\``, value: ` ` })
                        current_amount += Number(user.Paid_Amount)
                    }
                }));

                if (current_amount >= expected_amount) {
                    let profit = (current_amount - expected_amount)
                    calculate = `**Profit:** \`\`\`💵 +${profit.toFixed(2)} THB\`\`\``
                } else {
                    let loss = (current_amount - expected_amount)
                    calculate = `**Loss:** \`\`\`💵 ${loss.toFixed(2)} THB\`\`\``
                };

                Board.addFields({ name: "**Status Report**", value: `**Users:** \`\`\`👥 ${users.length}/4\`\`\`\n**Amount:** \`\`\`💵 ${current_amount.toFixed(2)} / ${expected_amount.toFixed(2)} THB\`\`\`\n${calculate}` })

                await MainEmbed.edit({ embeds: [Board] })
                clearTimeout(MainTimeOut)
                MainTimeOut = setTimeout(() => {
                    MainEmbed.delete()
                }, 15000);
                return;
            };

            const History_Main = async () => {
                let historyData = us_read();
                let currentDate = new Date();
                let Board = new EmbedBuilder()
                    .setTitle(`Premium Subscription History (Last 3 Months)`)
                    .setColor('#ff6666')
                    .setThumbnail("https://static-00.iconduck.com/assets.00/youtube-icon-2048x2048-oa03jx3h.png");

                for (let i = 0; i < 6; i++) {
                    let targetDate = new Date(currentDate);
                    targetDate.setMonth(targetDate.getMonth() - i);

                    let month = targetDate.getMonth() + 1;
                    let year = targetDate.getFullYear() + 543;
                    let key = `${month}/${year}`;

                    let monthData = historyData[key] || { Users: [] };
                    let users = monthData.Users;
                    let current_amount = users.reduce((sum, user) => sum + Number(user.Paid_Amount), 0);
                    let expected_amount = 60 * 4;
                    let place = 0;
                    let userText = "";

                    if (users.length === 0) {
                        userText = "No payments this month.";
                    } else {
                        await Promise.all(users.map(async (user) => {
                            try {
                                let member = await bot.users.fetch(user.ID);
                                place += 1;
                                userText += `\`${place}.) ${member.username} (${user.Name || member.displayName}) | 💵 ${user.Paid_Amount.toFixed(2)} THB\`\n`;
                            } catch (error) {
                                place += 1;
                                userText += `\`${place}.) ไม่ทราบชื่อ | 💵 ${user.Paid_Amount.toFixed(2)} THB\`\n`;
                            }
                        }));
                    }

                    let calculate = (current_amount >= expected_amount)
                        ? `**Profit:** \`\`\`💵 +${(current_amount - expected_amount).toFixed(2)} THB\`\`\``
                        : `**Loss:** \`\`\`💵 ${(current_amount - expected_amount).toFixed(2)} THB\`\`\``;

                    Board.addFields({
                        name: `📅 ${monthData.Month || month}/${monthData.Year || year}`,
                        value: `**Users:** \`\`\`👥 ${users.length}/4\`\`\`\n**Amount:** \`\`\`💵 ${current_amount.toFixed(2)} / ${expected_amount.toFixed(2)} THB\`\`\`\n${calculate}\n${userText}`
                    });
                }

                await MainEmbed.edit({ embeds: [Board] });
                clearTimeout(MainTimeOut)
                MainTimeOut = setTimeout(() => {
                    MainEmbed.delete()
                }, 15000);
                return;
            };

            await Page_Main();
            await MainEmbed.react(`▶`);

            const collector = MainEmbed.createReactionCollector();

            collector.on('collect', async (reaction, user) => {
                switch (reaction.emoji.name) {
                    case "▶":
                        if (user.id === "887531368836370483") return
                        if (user.id == message.author.id) {
                            if (CurrentPage == "Main") {
                                CurrentPage == "History"
                                await History_Main();
                            } else {
                                CurrentPage == "Main"
                                await Page_Main();
                            }
                        }
                        reaction.users.remove(user);
                        break;
                };
            });



        };

    } catch (error) {
        console.log(error)
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'premium',
    aliases: ['yt_premium', 'ytpremium', 'debt']
};