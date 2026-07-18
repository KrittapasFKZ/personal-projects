const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs")

let talkedRecently = new Set(); 
let fishing_ponds = new Map();

exports.run = async (bot, message, args) => {

    const fetchChannel = await bot.channels.fetch(message.channel.id);
    let tempGuild = bot.guilds.cache.get(message.channel.guild);

    try {

        if (message.channel.id == "470516112443047950") { } else {
            bot.BotLogs("SYSTEM", `\x1b[31mCancelled Fishing Event by \x1b[37m"${message.author.tag}"`);
            return
        };

        if (talkedRecently.has(message.author.id)) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Interaction Denied!`)
                .setDescription(`❌ ตอนนี้คุณกำลังตกปลาอยู่!`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            try { message.delete() } catch (error) { };
                        }
                    }, 3000);
                })
                .catch(console.error);

        };

        function formatTime(milliseconds) {
            const seconds = Math.floor(milliseconds / 1000);
            const minutes = Math.floor(seconds / 60);
            const remainingSeconds = seconds % 60;

            let formattedTime = '';
            if (minutes > 0) {
                formattedTime += `${minutes}m `;
            }
            if (remainingSeconds > 0) {
                formattedTime += `${remainingSeconds}s`;
            }

            return formattedTime.trim();
        }

        function comma(x) {
            return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        };

        function us_read() {
            const rawData = fs.readFileSync('./database/users.json');
            return JSON.parse(rawData);
        };

        function us_write(users) {
            fs.writeFileSync('./database/users.json', JSON.stringify(users, null, 4));
        };

        function updateUserData(userId, newData) {
            let users = us_read();
            if (!users[userId]) return;

            for (const key in newData) {
                if (Object.hasOwnProperty.call(newData, key)) {
                    users[userId][key] = newData[key];
                }
            }

            us_write(users);
        };

        let userId = message.author.id
        let users = us_read();

        if (users[userId]) {

            if (users[userId].Tools.Bait <= 0) {
                return
            };

            try { message.delete() } catch (error) { }
            let mention = `<@${message.author.id}>`;

            let FishingStatus = true
            let FishingRod = users[userId].Tools.Rod
            let FishingSpeed = 1000
            let FishingLuck = 0
            let FishingDouble = 0
            let FishingBaitUsed = 0
            let FishingCatched = 0
            let FishingEscaped = 0
            let FishingCoins = 0
            let FishingCoinsStatus = "Profit"

            if (FishingRod == "🎣 Noob Rod") {
                FishingSpeed = 45000
                FishingLuck = 0.5
                FishingDouble = 0.0
            } else {
                if (FishingRod == "🎣 Basic Rod") {
                    FishingSpeed = 40000
                    FishingLuck = 0.5
                    FishingDouble = 0.0
                } else {
                    if (FishingRod == "🎣 Advanced Rod") {
                        FishingSpeed = 35000
                        FishingLuck = 0.55
                        FishingDouble = 0.05
                    } else {
                        if (FishingRod == "🎣 Expert Rod") {
                            FishingSpeed = 30000
                            FishingLuck = 0.6
                            FishingDouble = 0.1
                        } else {
                            if (FishingRod == "🎣 Legendary Rod") {
                                FishingSpeed = 25000
                                FishingLuck = 0.65
                                FishingDouble = 0.15
                            } else {
                                if (FishingRod == "🎣 Mythical Rod") {
                                    FishingSpeed = 20000
                                    FishingLuck = 0.7
                                    FishingDouble = 0.2
                                } else {
                                    if (FishingRod == "🎣 Ultimate Rod") {
                                        FishingSpeed = 20000
                                        FishingLuck = 0.75
                                        FishingDouble = 0.25
                                    } else {
                                        if (FishingRod == "🎣 Divine Rod") {
                                            FishingSpeed = 20000
                                            FishingLuck = 0.80
                                            FishingDouble = 0.3
                                        } else {
                                            FishingSpeed = 1
                                            FishingLuck = 0
                                            FishingDouble = 0
                                        };
                                    };
                                };
                            };
                        };
                    };
                };
            };

            let FishingStart = Date.now()
            let FishingFinish = Number((FishingStart / 1000).toFixed(0)) + ((FishingSpeed * users[userId].Tools.Bait) / 1000);

            const StartEmbed = new EmbedBuilder()
                .setAuthor({ name: `${message.author.tag}'s Fishing`, iconURL: `${message.author.displayAvatarURL}` })
                .setThumbnail('https://i.imgur.com/p1IvjCG.gif')
                .addFields(
                    { name: '**Your Stats**', value: `Fishing Rod: \`${users[userId].Tools.Rod}\`\nBait Stock: \`🪱 ${comma(users[userId].Tools.Bait)}\`\nCatch Speed: \`⌛ ${formatTime(Number(FishingSpeed))}\`\nCatch Chance: \`${Number(FishingLuck * 100).toFixed(0)}%\`\nDouble Chance: \`${Number(FishingDouble * 100).toFixed(0)}%\``, inline: true },
                    { name: `**Records**`, value: `Finish Time: <t:${FishingFinish}:R>\nBait Used: \`🪱 ${Number(FishingBaitUsed)}\`\nCatched: \`🐟 ${Number(FishingCatched)}\`\nEscaped: \`${Number(FishingEscaped)}\`\nStatus: \`...\`` })
                .setColor('#0099FF')
                .setTimestamp()
                .setFooter({ text: 'Click ❌ to stop.\n🕒 Update every 60s' })

            talkedRecently.add(message.author.id);

            const MainEmbed = await fetchChannel.send({ content: mention, embeds: [StartEmbed] });
            await MainEmbed.react('❌');

            bot.BotLogs(tempGuild.name, `\x1b[36mRecreating Fishing Event for \x1b[37m"${message.author.tag}"!`);

            const FishEvent = setInterval(async () => {

                if (FishingStatus === true) {

                    FishingFinish = Number((FishingStart / 1000).toFixed(0)) + ((FishingSpeed * users[userId].Tools.Bait) / 1000);

                    users = us_read();
                    let newData = { Tools: { ...users[userId].Tools, Bait: users[userId].Tools.Bait - 1 } };
                    updateUserData(userId, newData);
                    FishingBaitUsed += 1

                    let catch_random = Math.random();

                    if (catch_random < FishingLuck) {

                        users = us_read();
                        let xpToAdd = Number((10 * users[userId].Multiplier).toFixed(2));
                        updateUserData(userId, { XP: users[userId].XP += xpToAdd });

                        let double_random = Math.random();
                        if (double_random < FishingDouble) {
                            FishingCatched += 2
                            users = us_read();
                            let newData = { Inventory: { ...users[userId].Inventory, Fish: users[userId].Inventory.Fish + 2 } };
                            updateUserData(userId, newData);
                        } else {
                            FishingCatched += 1
                            users = us_read();
                            let newData = { Inventory: { ...users[userId].Inventory, Fish: users[userId].Inventory.Fish + 1 } };
                            updateUserData(userId, newData);
                        };
                    } else {
                        FishingEscaped += 1
                    };

                    fishing_ponds.set(userId, {
                        messageId: MainEmbed.id,
                        channelId: message.channel.id,
                        message: {
                            author: message.author,
                            channel: message.channel,
                        },
                        startTime: FishingStart,
                        finishTime: FishingFinish,
                        procFish: Date.now(),
                        status: true
                    });

                    fs.writeFileSync('./database/fishing_ponds.json', JSON.stringify(Array.from(fishing_ponds.entries()), null, 4));

                };

                if (users[userId].Tools.Bait <= 0) {
                    StopFishing();
                };

            }, FishingSpeed)

            const UpdateEvent = setInterval(async () => {

                if (FishingStatus === true) {

                    users = us_read();

                    FishingStart = Date.now()
                    FishingFinish = Number((FishingStart / 1000).toFixed(0)) + ((FishingSpeed * users[userId].Tools.Bait) / 1000);

                    FishingCoins = (FishingCatched * 16) - (FishingBaitUsed * 2)
                    if (FishingCoins >= 0) {
                        FishingCoinsStatus = `Profit 🪙 ${comma(FishingCoins)} Coins`
                    } else {
                        FishingCoinsStatus = `Loss 🪙 ${comma(FishingCoins * -1)} Coins`
                    };

                    const EditEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Fishing`, iconURL: `${message.author.displayAvatarURL}` })
                        .setThumbnail('https://i.imgur.com/p1IvjCG.gif')
                        .addFields(
                            { name: '**Your Stats**', value: `Fishing Rod: \`${users[userId].Tools.Rod}\`\nBait Stock: \`🪱 ${comma(users[userId].Tools.Bait)}\`\nCatch Speed: \`⌛ ${formatTime(Number(FishingSpeed))}\`\nCatch Chance: \`${Number(FishingLuck * 100).toFixed(0)}%\`\nDouble Chance: \`${Number(FishingDouble * 100).toFixed(0)}%\``, inline: true },
                            { name: `**Records**`, value: `Finish Time: <t:${FishingFinish}:R>\nBait Used: \`🪱 ${Number(FishingBaitUsed)}\`\nCatched: \`🐟 ${Number(FishingCatched)}\`\nEscaped: \`${Number(FishingEscaped)}\`\nStatus: \`${FishingCoinsStatus}\`` })
                        .setColor('#0099FF')
                        .setTimestamp()
                        .setFooter({ text: 'Click ❌ to stop.\n🕒 Update every 60s' })

                    await MainEmbed.edit({ embeds: [EditEmbed] });

                };

            }, 60000)

            fishing_ponds.set(userId, {
                messageId: MainEmbed.id,
                channelId: message.channel.id,
                message: {
                    author: message.author,
                    channel: message.channel,
                },
                startTime: Date.now(),
                finishTime: FishingFinish,
                status: true
            });

            fs.writeFileSync('./database/fishing_ponds.json', JSON.stringify(Array.from(fishing_ponds.entries()), null, 4));

            const filter = (reaction, user) => {
                return user.id === message.author.id;
            };
            const collector = MainEmbed.createReactionCollector({
                filter
            });

            async function StopFishing() {
                clearInterval(FishEvent);
                clearInterval(UpdateEvent);
                now = new Date(Date.now());
                now_hours = now.getHours().toString().padStart(2, '0');
                now_mins = now.getMinutes().toString().padStart(2, '0');
                now_seconds = now.getSeconds().toString().padStart(2, '0');
                FishingStatus = false;
                if (talkedRecently.has(message.author.id)) {
                    talkedRecently.delete(message.author.id);
                };
                fishing_ponds.delete(userId);
                const StopEmbed = new EmbedBuilder()
                    .setAuthor({ name: `${message.author.tag}'s Fishing`, iconURL: `${message.author.displayAvatarURL}` })
                    .addFields(
                        { name: `**Records**`, value: `Bait Used: \`🪱 ${Number(FishingBaitUsed)}\`\nCatched: \`🐟 ${Number(FishingCatched)}\`\nEscaped: \`${Number(FishingEscaped)}\`` })
                    .setColor('#00FF00')
                    .setTimestamp()
                fs.writeFileSync(`./database/fishing_ponds.json`, JSON.stringify(Array.from(fishing_ponds.entries()), null, 4));
                MainEmbed.delete();
                const RecordsEmbed = await fetchChannel.send({ content: mention, embeds: [StopEmbed] });
                setTimeout(async () => {
                    RecordsEmbed.delete();
                }, 6000);
                bot.BotLogs(tempGuild.name, `\x1b[36m\x1b[37m"${message.author.tag}" \x1b[36mstopped Fishing Simulator!`);
            };

            collector.on('collect', async (reaction, user) => {
                switch (reaction.emoji.name) {
                    case "❌":
                        if (user.tag === message.author.tag) {
                            clearInterval(FishEvent);
                            StopFishing()
                            break;
                        };
                }
            });

        };

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.fishing_ponds = fishing_ponds
exports.talkedRecently = talkedRecently

exports.help = {
    name: 'force_fish'
};