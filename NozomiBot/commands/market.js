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
                .setDescription(`❌ ตอนนี้คุณมี Market เปิดอยู่!`)
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

        function uu_read() {
            const rawData = fs.readFileSync('./database/market.json');
            return JSON.parse(rawData);
        }

        function uu_write(unitUsers) {
            fs.writeFileSync('./database/market.json', JSON.stringify(unitUsers, null, 4));
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

        function checkLevel(unit) {
            unit.Attributes.Health_Value = Math.floor(Number((unit.Base.Base_Health) + (((unit.Base.Base_Health * unit.Unit_Level) * 55) / 100)));
            unit.Attributes.Physical_Value = Math.floor(Number((unit.Base.Base_Physical) + (((unit.Base.Base_Physical * unit.Unit_Level) * 20) / 100)));
            unit.Attributes.Magical_Value = Math.floor(Number((unit.Base.Base_Magical) + (((unit.Base.Base_Magical * unit.Unit_Level) * 20) / 100)));
            return unit;
        };

        let userId = message.author.id
        let users = us_read();
        let unitUsers = uu_read();

        let unit_list = [];
        let rawData = fs.readFileSync('./global/unit_list.json');
        unit_list = JSON.parse(rawData);

        if (!unitUsers[userId]) {
            unitUsers[userId] = {
                current_market: [],
                next_reset: 0
            };
        }

        function resetMarketForUser() {
            unitUsers[userId].current_market = [];
            if (users[userId].Dungeon.Unit.length >= unit_list.length) return;
            for (let i = 0; i < 1; i++) {
                const randomCrop = unit_list[Math.floor(Math.random() * unit_list.length)];
                checkLevel(randomCrop);
                randomCrop.Unit_Level = Math.floor(Math.random() * 3) + 1;
                unitUsers[userId].current_market.push(randomCrop);
            }

            let first_reset = Date.now();
            unitUsers[userId].next_reset = Number((first_reset / 1000).toFixed(0)) + ((1800000) / 1000);

            uu_write(unitUsers);

            if (users[userId].Dungeon.Unit.some(unit => unit.Unit_Name === unitUsers[userId].current_market[0].Unit_Name)) {
                resetMarketForUser();
            };
        }

        if (Date.now() / 1000 >= unitUsers[userId].next_reset) {
            resetMarketForUser();
        };

        if (users[userId]) {

            let mention = `<@${message.author.id}>`;
            message.delete()

            let debounce = false

            let MarketTitle = ``
            let MarketText = ``

            let UnitPrice = 0
            let MarketIcon = ``

            if (unitUsers[userId].current_market.length >= 1) {
                if (unitUsers[userId].current_market[0].Unit_Class == 'Warrior') {
                    MarketIcon = `⚔️`
                } else {
                    if (unitUsers[userId].current_market[0].Unit_Class == 'Gunner') {
                        MarketIcon = `🔫`
                    } else {
                        if (unitUsers[userId].current_market[0].Unit_Class == 'Wizard') {
                            MarketIcon = `🪄`
                        } else {
                            if (unitUsers[userId].current_market[0].Unit_Class == 'Priest') {
                                MarketIcon = `💉`
                            } else {
                                
                            }
                        }
                    }
                };
                UnitPrice = Number(unitUsers[userId].current_market[0].Unit_Level * 50000)
                MarketTitle = `** ${MarketIcon} ${unitUsers[userId].current_market[0].Unit_Name} (Lv. ${unitUsers[userId].current_market[0].Unit_Level})**`
                MarketText = `Health: \`💕️ ${unitUsers[userId].current_market[0].Attributes.Health_Value}\`\nPhysical Power: \`💪 ${unitUsers[userId].current_market[0].Attributes.Physical_Value}\`\nMagical Power: \`💫 ${unitUsers[userId].current_market[0].Attributes.Magical_Value}\`\nPrice: \`🪙 ${comma(UnitPrice)} Coins\``
            } else {
                MarketTitle = `**No Unit Available**`
                MarketText = ` `
                UnitPrice = 0
            };

            const ShopEmbed = new EmbedBuilder()
                .setAuthor({ name: `${message.author.tag}'s Unit Market`, iconURL: `${message.author.displayAvatarURL()}` })
                .setColor("#800080")
                .addFields(
                    { name: `${MarketTitle}`, value: `${MarketText}` },
                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                    { name: `Next Reset`, value: `<t:${unitUsers[userId].next_reset}:R>` })
                .setTimestamp()
                .setFooter({ text: `Click 💰 to purchase.\nClick ❌ to close.\n` })

            talkedRecently.add(message.author.id);

            const MainEmbed = await message.channel.send({ content: mention, embeds: [ShopEmbed] });
            await MainEmbed.react('❌');
            if (unitUsers[userId].current_market.length >= 1) {
                await MainEmbed.react('💰');
            };

            let MessageTimeOut = setTimeout(async () => {
                if (talkedRecently.has(message.author.id)) {
                    talkedRecently.delete(message.author.id);
                };
                collector.stop()
                MainEmbed.delete();
            }, 40000);

            const filter = (reaction, user) => {
                return user.id === message.author.id;
            };

            const collector = MainEmbed.createReactionCollector({
                filter
            });

            async function PurchaseItem() {

                let item_cost = UnitPrice
                let DUNGEON_UNIT_LIST = users[userId].Dungeon.Unit;

                if (users[userId].Coins >= item_cost) {

                    DUNGEON_UNIT_LIST.push(unitUsers[userId].current_market[0])

                    bot.BotLogs(message.guild.name, `\x1b[35m\x1b[37m"${message.author.tag}" \x1b[35mhas purchased \x1b[37m"${unitUsers[userId].current_market[0].Unit_Name} (Lv. ${unitUsers[userId].current_market[0].Unit_Level})"`);

                    users = us_read();
                    updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                    us_write(users)

                    users = us_read();
                    updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Unit: users[userId].Dungeon.Unit = DUNGEON_UNIT_LIST } });
                    us_write(users);

                    unitUsers = uu_read();
                    unitUsers[userId].current_market = [];
                    uu_write(unitUsers);

                    const EventEmbed_Completed = new EmbedBuilder()
                        .setTitle(`Interaction Completed!`)
                        .setDescription(`✅ ซื้อเรียบร้อยแล้ว!`)
                        .setColor('#00FF00')
                        .setTimestamp()

                    MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                    await MainEmbed.edit({ embeds: [EventEmbed_Completed] });
                    setTimeout(async () => {
                        MainEmbed.delete();
                    }, 4000);

                    resetMarketForUser();

                } else {

                    const EventEmbed_Failed = new EmbedBuilder()
                        .setTitle(`Error Occurred!`)
                        .setDescription(`❌ คุณมี Coins ไม่เพียงพอ!\nต้องการอีก \`${(item_cost - users[userId].Coins)}\` Coins!`)
                        .setColor('#FF0000')
                        .setTimestamp()

                    MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                    await MainEmbed.edit({ embeds: [EventEmbed_Failed] })
                    setTimeout(async () => {
                        MainEmbed.delete();
                    }, 4000);
                };
            };

            collector.on('collect', async (reaction, user) => {
                switch (reaction.emoji.name) {
                    case "💰":
                        if (user.tag === message.author.tag) {
                            if (unitUsers[userId].current_market.length >= 1) {} else return
                            if (debounce) return
                            debounce = true
                            clearTimeout(MessageTimeOut);
                            PurchaseItem();
                            if (talkedRecently.has(message.author.id)) {
                                talkedRecently.delete(message.author.id);
                            };
                            break;
                        };

                    case "❌":
                        if (user.tag === message.author.tag) {
                            if (talkedRecently.has(message.author.id)) {
                                talkedRecently.delete(message.author.id);
                            };
                            clearTimeout(MessageTimeOut);
                            MainEmbed.delete();
                            break;
                        };
                };
            });

        };

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'market',
    aliases: ['mk', 'units']
};