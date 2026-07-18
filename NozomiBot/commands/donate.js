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

        if (!args[0]) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!donate <@user> <amount>\``)
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

        if (!args[1]) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!donate <@user> <amount>\``)
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

        let TARGET_USER = message.mentions.members.first() || message.guild.members.cache.get(args[0]);
        let AMOUNT_COIN = Number(args[1])

        let userId = message.author.id
        let users = us_read();

        if (users[userId]) {

            if (Number(AMOUNT_COIN) <= 0) {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Error Occurred!`)
                    .setDescription(`❌ Invalid Number Value!`)
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

            users = us_read();

            if (users[userId].Coins >= Number(AMOUNT_COIN)) { } else {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Error Occurred!`)
                    .setDescription(`❌ คุณมี Coins ไม่เพียงพอ!`)
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

            let mention = `<@${message.author.id}>`;

            updateUserData(userId, { Coins: users[userId].Coins -= AMOUNT_COIN });
            updateUserData(TARGET_USER.id, { Coins: users[TARGET_USER.id].Coins += AMOUNT_COIN });

            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mdonated \x1b[90m[\x1b[37m🪙 ${comma(AMOUNT_COIN)}\x1b[90m] \x1b[35mto \x1b[90m[\x1b[37m${TARGET_USER.user.tag}\x1b[90m] \x1b[90m[\x1b[37m${message.guild.name}\x1b[90m]`);

            const EventEmbed_Completed = new EmbedBuilder()
                .setTitle(`Interaction Completed!`)
                .setDescription(`✅ โอนเงินเรียบร้อย!`)
                .setColor('#00FF00')
                .setTimestamp()

            return message.reply({ content: mention, embeds: [EventEmbed_Completed] })
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

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'donate',
    aliases: ['donates', 'pay', 'pays']
};