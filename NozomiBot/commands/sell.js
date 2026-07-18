const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");
const talkedRecently = new Set();

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {

        if (talkedRecently.has(message.author.id)) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Interaction Denied!`)
                .setDescription(`❌ ตอนนี้คุณกำลังขายอยู่!`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
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

        function capitalizeFirstLetter(str) {
            return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
        };

        let ITEM_NAME = `none`
        let ITEM_AMOUNT

        if (args[0]) {
            ITEM_NAME = args[0]
        } else {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!sell <item_name> <amount>\``)
                .setColor('#FFFF00')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);
        };

        if (args[1]) {
            ITEM_AMOUNT = args[1]
        } else {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!sell <item_name> <amount>\``)
                .setColor('#FFFF00')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);
        };

        if (!["fish", "wheat", "potato", "carrot", "corn"].includes(ITEM_NAME.toLowerCase())) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Error Occurred!`)
                .setDescription(`❌ Unknown Item!`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
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

        let userId = message.author.id
        let users = us_read();
        let Target_Item = capitalizeFirstLetter(ITEM_NAME);
        let Target_Icon = '';
        let Target_Price = 0

        if (Target_Item == "Fish") {
            Target_Icon = "🐟"
            Target_Price = 16
        } else {
            if (Target_Item == "Wheat") {
                Target_Icon = "🌾"
                Target_Price = 45
            } else {
                if (Target_Item == "Potato") {
                    Target_Icon = "🥔"
                    Target_Price = 95
                } else {
                    if (Target_Item == "Carrot") {
                        Target_Icon = "🥕"
                        Target_Price = 95
                    } else {
                        if (Target_Item == "Corn") {
                            Target_Icon = "🌽"
                            Target_Price = 185
                        } else {
                            Target_Icon = "?"
                            Target_Price = 0
                        }
                    }
                }
            }
        }

        if (users[userId]) {

            if (ITEM_AMOUNT == "all") {
                ITEM_AMOUNT = users[userId].Inventory[Target_Item]
            } else {
                if (Number(ITEM_AMOUNT) <= 0) {
                    const EventEmbed_Failed = new EmbedBuilder()
                        .setTitle(`Error Occurred!`)
                        .setDescription(`❌ กรุณาใส่จำนวนที่ต้องการขาย!`)
                        .setColor('#FF0000')
                        .setTimestamp()
                    return message.reply({ embeds: [EventEmbed_Failed] })
                        .then(msg => {
                            setTimeout(() => {
                                if (msg) {
                                    msg.delete().catch(console.error);
                                    message.delete();
                                }
                            }, 3000);
                        })
                        .catch(console.error);
                } else {
                    if (Number(ITEM_AMOUNT) >= 1) { } else {
                        const EventEmbed_Failed = new EmbedBuilder()
                            .setTitle(`Error Occurred!`)
                            .setDescription(`❌ กรุณาใส่จำนวนที่ต้องการขาย!`)
                            .setColor('#FF0000')
                            .setTimestamp()
                        return message.reply({ embeds: [EventEmbed_Failed] })
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
                };
            };

            users = us_read();

            if (users[userId].Inventory[Target_Item] >= Number(ITEM_AMOUNT)) { } else {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Error Occurred!`)
                    .setDescription(`❌ คุณมี Item ไม่เพียงพอ!`)
                    .setColor('#FF0000')
                    .setTimestamp()
                return message.reply({ embeds: [EventEmbed_Failed] })
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

            message.delete()
            let mention = `<@${message.author.id}>`;

            let total_coins = Number(ITEM_AMOUNT) * Target_Price

            const StartEmbed = new EmbedBuilder()
                .setAuthor({ name: `${message.author.tag}`, iconURL: `${message.author.displayAvatarURL()}` })
                .addFields(
                    { name: '**Selling Info**', value: `Item: \`${Target_Icon} ${Target_Item} x ${comma(ITEM_AMOUNT)}\`\nTotal: \`🪙 ${comma(total_coins)} Coins\``, inline: true },
                    { name: `**Option**`, value: `Click ✅ to confirm.\nClick ❌ to cancel.` })
                .setColor('#0099FF')
                .setTimestamp()

            talkedRecently.add(message.author.id);

            const MainEmbed = await message.channel.send({ content: mention, embeds: [StartEmbed] });
            await MainEmbed.react('✅');
            await MainEmbed.react('❌');

            let MessageTimeOut = setTimeout(async () => {
                if (talkedRecently.has(message.author.id)) {
                    talkedRecently.delete(message.author.id);
                };
                collector.stop()
                MainEmbed.delete();
            }, 20000);

            const filter = (reaction, user) => {
                return user.id === message.author.id;
            };
            const collector = MainEmbed.createReactionCollector({
                filter
            });

            collector.on('collect', async (reaction, user) => {
                switch (reaction.emoji.name) {
                    case "✅":
                        if (user.tag === message.author.tag) {

                            users = us_read();
                            const updatedInventory = { ...users[userId].Inventory };
                            updatedInventory[Target_Item] -= Number(ITEM_AMOUNT);

                            let newData = { Inventory: updatedInventory };
                            updateUserData(userId, newData);
                            updateUserData(userId, { Coins: users[userId].Coins += total_coins });

                            const EventEmbed_Completed = new EmbedBuilder()
                                .setTitle(`Interaction Completed!`)
                                .setDescription(`✅ ขายเรียบร้อยแล้ว!`)
                                .setColor('#00FF00')
                                .setTimestamp()

                            if (talkedRecently.has(message.author.id)) {
                                talkedRecently.delete(message.author.id);
                            };

                            clearTimeout(MessageTimeOut);
                            MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                            bot.BotLogs(message.guild.name, `\x1b[36m\x1b[37m"${message.author.tag}" \x1b[36mhas sold \x1b[37m"${Target_Icon} ${Target_Item} x ${comma(ITEM_AMOUNT)}" \x1b[36mfor \x1b[37m"🪙 ${comma(total_coins)} Coins"\x1b[36m!`);
                            await MainEmbed.edit({ embeds: [EventEmbed_Completed] })
                            setTimeout(async () => {
                                MainEmbed.delete();
                            }, 4000);
                            break;
                        };
                    case "❌":
                        if (user.tag === message.author.tag) {
                            if (talkedRecently.has(message.author.id)) {
                                talkedRecently.delete(message.author.id);
                            };
                            clearTimeout(MessageTimeOut);
                            MainEmbed.delete()
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

exports.help = {
    name: 'sell'
};