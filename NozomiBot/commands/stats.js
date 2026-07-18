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

        return;

        if (message.author.id == "605361556297089035") {

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

            let users = us_read();

            message.guild.members.cache.forEach(member => {

                let userId = member.id

                if (users[userId]) {

                    const NewStats = {
                        "Fish": users[userId].Inventory.Fish,
                        "Wheat": 0,
                        "Potato": 0,
                        "Carrot": 0,
                        "Corn": 0
                    };

                    users = us_read();
                    updateUserData(userId, { Inventory: users[userId].Inventory = NewStats });

                    bot.BotLogs(message.guild.name, `\x1b[36mNew Stats applied to \x1b[37m"${member.user.username}"`);

                };
            });

        } else {
            return message.reply(`**No Permission.**`)
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete()
                        }
                    }, 3000);
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
    name: 'stats'
};