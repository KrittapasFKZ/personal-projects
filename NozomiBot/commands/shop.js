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
                .setDescription(`❌ ตอนนี้คุณมี Shop เปิดอยู่!`)
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

        let userId = message.author.id
        let users = us_read();

        let query = ``
        if (args[0]) {
            query = args[0]
        } else {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!shop <stats/bait/rod/water>\``)
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

        let ShopData_Page = 1
        let ShopData_Type = 1

        if (!["stats", "bait", "rod", "water"].includes(query.toLowerCase())) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Error Occurred!`)
                .setDescription(`❌ Unknown Shop Type!`)
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
            ShopData_Type = query
        };

        if (users[userId]) {

            let mention = `<@${message.author.id}>`;
            message.delete()

            if (ShopData_Type == `all`) {

            } else {
                if (ShopData_Type == `stats`) {

                    let debounce = false
                    let ItemCost_Multiplier = Math.floor((users[userId].Multiplier.toFixed(1) * 1000) + Math.floor(((users[userId].Multiplier * 500) * 150) / 100))
                    let ItemCost_CropDrop = Math.floor((users[userId].Farming.CropDrop * 4000) + Math.floor(((users[userId].Farming.CropDrop * 4000) * 50) / 100))
                    let ItemCost_CropSlot = Math.floor((users[userId].Farming.Slot * 700) + Math.floor(((users[userId].Farming.Slot * 700) * 150) / 100))

                    if (users[userId].Multiplier >= 20) {
                        ItemCost_Multiplier = 9999999999999
                    };

                    if (users[userId].Farming.Slot >= 16) {
                        ItemCost_CropSlot = 9999999999999
                    };

                    if (users[userId].Farming.CropDrop >= 10) {
                        ItemCost_CropDrop = 9999999999999
                    };

                    const ShopEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Stats Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                        .setColor("#3498DB")
                        .addFields(
                            { name: '**Upgrade XP Multiplier**', value: `Current: \`x${users[userId].Multiplier.toFixed(1)}\`\nNext Upgrade: \`x${(users[userId].Multiplier + 0.5).toFixed(1)}\`\nPrice: \`🪙 ${comma(ItemCost_Multiplier)} Coins\`` },
                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                        )
                        .setTimestamp()

                    talkedRecently.add(message.author.id);

                    const MainEmbed = await message.channel.send({ content: mention, embeds: [ShopEmbed] });
                    await MainEmbed.react('❌');
                    await MainEmbed.react(`◀`);
                    await MainEmbed.react('💰');
                    await MainEmbed.react(`▶`);

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

                        let item_cost = 0
 
                        if (ShopData_Page == 1) {
                            item_cost = ItemCost_Multiplier
                        } else {
                            if (ShopData_Page == 2) {
                                item_cost = ItemCost_CropDrop
                            } else {
                                if (ShopData_Page == 3) {
                                    item_cost = ItemCost_CropSlot
                                } else {
                                    item_cost = 9999999999999
                                };
                            };
                        };

                        if (users[userId].Coins >= item_cost) {

                            if (ShopData_Page == 1) {
                                users = us_read();
                                updateUserData(userId, { Multiplier: users[userId].Multiplier = Number((users[userId].Multiplier + 0.5).toFixed(1)) });
                                updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                us_write(users)
                                bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37mx${users[userId].Multiplier.toFixed(1)} XP Multiplier\x1b[90m]`);
                            } else {
                                if (ShopData_Page == 2) {
                                    users = us_read();
                                    updateUserData(userId, { Farming: { ...users[userId].Farming, CropDrop: users[userId].Farming.CropDrop += 1 } });
                                    updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                    us_write(users)
                                    bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37mx${users[userId].Farming.CropDrop} Crop Drop\x1b[90m]`);
                                } else {
                                    if (ShopData_Page == 3) {
                                        users = us_read();
                                        updateUserData(userId, { Farming: { ...users[userId].Farming, CropDrop: users[userId].Farming.Slot += 1 } });
                                        updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                        us_write(users)
                                        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m1 Extra Crop Slot\x1b[90m]`);
                                    } else {

                                    };
                                };
                            };

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
                            case "◀":
                                if (user.tag === message.author.tag) {

                                    ShopData_Page -= 1
                                    if (ShopData_Page < 1) { ShopData_Page = 3 };
                                    CheckPage();

                                    reaction.users.remove(user);
                                    break;
                                };
                            case "▶":
                                if (user.tag === message.author.tag) {

                                    ShopData_Page += 1
                                    if (ShopData_Page > 3) { ShopData_Page = 1 };
                                    CheckPage();

                                    reaction.users.remove(user);
                                    break;
                                };
                        };
                    });

                    async function CheckPage() {
                        if (ShopData_Page == 1) {
                            const EditEmbed = new EmbedBuilder()
                                .setAuthor({ name: `${message.author.tag}'s Stats Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                .setColor("#3498DB")
                                .addFields(
                                    { name: '**Upgrade XP Multiplier**', value: `Current: \`x${users[userId].Multiplier.toFixed(1)}\`\nNext Upgrade: \`x${(users[userId].Multiplier + 0.5).toFixed(1)}\`\nPrice: \`🪙 ${comma(ItemCost_Multiplier)} Coins\`` },
                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                )
                                .setTimestamp()
                            await MainEmbed.edit({ embeds: [EditEmbed] })
                        } else {
                            if (ShopData_Page == 2) {
                                const EditEmbed = new EmbedBuilder()
                                    .setAuthor({ name: `${message.author.tag}'s Stats Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                    .setColor("#3498DB")
                                    .addFields(
                                        { name: '**Upgrade Crop Drops**', value: `Current: \`x${users[userId].Farming.CropDrop}\`\nNext Upgrade: \`x${users[userId].Farming.CropDrop + 1}\`\nPrice: \`🪙 ${comma(ItemCost_CropDrop)} Coins\`` },
                                        { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                        { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                    )
                                    .setTimestamp()
                                await MainEmbed.edit({ embeds: [EditEmbed] })
                            } else {
                                if (ShopData_Page == 3) {
                                    const EditEmbed = new EmbedBuilder()
                                        .setAuthor({ name: `${message.author.tag}'s Stats Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                        .setColor("#3498DB")
                                        .addFields(
                                            { name: '**Upgrade Crop Slot**', value: `Current: \`${users[userId].Farming.Slot} Slots\`\nNext Upgrade: \`${users[userId].Farming.Slot + 1} Slots\`\nPrice: \`🪙 ${comma(ItemCost_CropSlot)} Coins\`` },
                                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                        )
                                        .setTimestamp()
                                    await MainEmbed.edit({ embeds: [EditEmbed] })
                                } else {

                                };
                            };
                        };
                    };

                } else {
                    if (ShopData_Type == `bait`) {

                        let debounce = false
                        let ItemCost_Bait01 = 10
                        let ItemCost_Bait02 = 50
                        let ItemCost_Bait03 = 150
                        let ItemCost_Bait04 = 500
                        let ItemCost_Bait05 = 1500
                        let ItemCost_Bait06 = 5000

                        const ShopEmbed = new EmbedBuilder()
                            .setAuthor({ name: `${message.author.tag}'s Bait Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                            .setColor("#3498DB")
                            .addFields(
                                { name: '**🪱 Bait x 20**', value: `Current: \`x${comma(users[userId].Tools.Bait)}\`\nPrice: \`🪙 ${comma(ItemCost_Bait01)} Coins\`` },
                                { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                            )
                            .setTimestamp()

                        talkedRecently.add(message.author.id);

                        const MainEmbed = await message.channel.send({ content: mention, embeds: [ShopEmbed] });
                        await MainEmbed.react('❌');
                        await MainEmbed.react(`◀`);
                        await MainEmbed.react('💰');
                        await MainEmbed.react(`▶`);

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

                            let item_cost = 0

                            if (ShopData_Page == 1) {
                                item_cost = ItemCost_Bait01
                            } else {
                                if (ShopData_Page == 2) {
                                    item_cost = ItemCost_Bait02
                                } else {
                                    if (ShopData_Page == 3) {
                                        item_cost = ItemCost_Bait03
                                    } else {
                                        if (ShopData_Page == 4) {
                                            item_cost = ItemCost_Bait04
                                        } else {
                                            if (ShopData_Page == 5) {
                                                item_cost = ItemCost_Bait05
                                            } else {
                                                if (ShopData_Page == 6) {
                                                    item_cost = ItemCost_Bait06
                                                } else {
                                                    item_cost = 9999999999999
                                                };
                                            };
                                        };
                                    };
                                };
                            };

                            if (users[userId].Coins >= item_cost) {

                                if (ShopData_Page == 1) {
                                    users = us_read();
                                    updateUserData(userId, { Tools: { ...users[userId].Tools, Bait: users[userId].Tools.Bait += 20 } });
                                    updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                    us_write(users)
                                    bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🪱 Bait x 20\x1b[90m]`);
                                } else {
                                    if (ShopData_Page == 2) {
                                        users = us_read();
                                        updateUserData(userId, { Tools: { ...users[userId].Tools, Bait: users[userId].Tools.Bait += 100 } });
                                        updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                        us_write(users)
                                        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🪱 Bait x 100\x1b[90m]`);
                                    } else {
                                        if (ShopData_Page == 3) {
                                            users = us_read();
                                            updateUserData(userId, { Tools: { ...users[userId].Tools, Bait: users[userId].Tools.Bait += 300 } });
                                            updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                            us_write(users)
                                            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🪱 Bait x 300\x1b[90m]`);
                                        } else {
                                            if (ShopData_Page == 4) {
                                                users = us_read();
                                                updateUserData(userId, { Tools: { ...users[userId].Tools, Bait: users[userId].Tools.Bait += 1000 } });
                                                updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                us_write(users)
                                                bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🪱 Bait x 1,000\x1b[90m]`);
                                            } else {
                                                if (ShopData_Page == 5) {
                                                    users = us_read();
                                                    updateUserData(userId, { Tools: { ...users[userId].Tools, Bait: users[userId].Tools.Bait += 3000 } });
                                                    updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                    us_write(users)
                                                    bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🪱 Bait x 3,000\x1b[90m]`);
                                                } else {
                                                    if (ShopData_Page == 6) {
                                                        users = us_read();
                                                        updateUserData(userId, { Tools: { ...users[userId].Tools, Bait: users[userId].Tools.Bait += 10000 } });
                                                        updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                        us_write(users)
                                                        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🪱 Bait x 10,000\x1b[90m]`);
                                                    } else {
    
                                                    };
                                                };
                                            };
                                        };
                                    };
                                };

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
                                case "◀":
                                    if (user.tag === message.author.tag) {

                                        ShopData_Page -= 1
                                        if (ShopData_Page < 1) { ShopData_Page = 6 };
                                        CheckPage();

                                        reaction.users.remove(user);
                                        break;
                                    };
                                case "▶":
                                    if (user.tag === message.author.tag) {

                                        ShopData_Page += 1
                                        if (ShopData_Page > 6) { ShopData_Page = 1 };
                                        CheckPage();

                                        reaction.users.remove(user);
                                        break;
                                    };
                            };
                        });

                        async function CheckPage() {
                            if (ShopData_Page == 1) {
                                const EditEmbed = new EmbedBuilder()
                                    .setAuthor({ name: `${message.author.tag}'s Bait Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                    .setColor("#3498DB")
                                    .addFields(
                                        { name: '**🪱 Bait x 20**', value: `Current: \`x${comma(users[userId].Tools.Bait)}\`\nPrice: \`🪙 ${comma(ItemCost_Bait01)} Coins\`` },
                                        { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                        { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                    )
                                    .setTimestamp()
                                await MainEmbed.edit({ embeds: [EditEmbed] })
                            } else {
                                if (ShopData_Page == 2) {
                                    const EditEmbed = new EmbedBuilder()
                                        .setAuthor({ name: `${message.author.tag}'s Bait Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                        .setColor("#3498DB")
                                        .addFields(
                                            { name: '**🪱 Bait x 100**', value: `Current: \`x${comma(users[userId].Tools.Bait)}\`\nPrice: \`🪙 ${comma(ItemCost_Bait02)} Coins\`` },
                                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                        )
                                        .setTimestamp()
                                    await MainEmbed.edit({ embeds: [EditEmbed] })
                                } else {
                                    if (ShopData_Page == 3) {
                                        const EditEmbed = new EmbedBuilder()
                                            .setAuthor({ name: `${message.author.tag}'s Bait Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                            .setColor("#3498DB")
                                            .addFields(
                                                { name: '**🪱 Bait x 300**', value: `Current: \`x${comma(users[userId].Tools.Bait)}\`\nPrice: \`🪙 ${comma(ItemCost_Bait03)} Coins\`` },
                                                { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                            )
                                            .setTimestamp()
                                        await MainEmbed.edit({ embeds: [EditEmbed] })
                                    } else {
                                        if (ShopData_Page == 4) {
                                            const EditEmbed = new EmbedBuilder()
                                                .setAuthor({ name: `${message.author.tag}'s Bait Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                .setColor("#3498DB")
                                                .addFields(
                                                    { name: '**🪱 Bait x 1,000**', value: `Current: \`x${comma(users[userId].Tools.Bait)}\`\nPrice: \`🪙 ${comma(ItemCost_Bait04)} Coins\`` },
                                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                )
                                                .setTimestamp()
                                            await MainEmbed.edit({ embeds: [EditEmbed] })
                                        } else {
                                            if (ShopData_Page == 5) {
                                                const EditEmbed = new EmbedBuilder()
                                                    .setAuthor({ name: `${message.author.tag}'s Bait Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                    .setColor("#3498DB")
                                                    .addFields(
                                                        { name: '**🪱 Bait x 3,000**', value: `Current: \`x${comma(users[userId].Tools.Bait)}\`\nPrice: \`🪙 ${comma(ItemCost_Bait05)} Coins\`` },
                                                        { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                        { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                    )
                                                    .setTimestamp()
                                                await MainEmbed.edit({ embeds: [EditEmbed] })
                                            } else {
                                                if (ShopData_Page == 6) {
                                                    const EditEmbed = new EmbedBuilder()
                                                        .setAuthor({ name: `${message.author.tag}'s Bait Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                        .setColor("#3498DB")
                                                        .addFields(
                                                            { name: '**🪱 Bait x 10,000**', value: `Current: \`x${comma(users[userId].Tools.Bait)}\`\nPrice: \`🪙 ${comma(ItemCost_Bait06)} Coins\`` },
                                                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                        )
                                                        .setTimestamp()
                                                    await MainEmbed.edit({ embeds: [EditEmbed] })
                                                } else {
    
                                                };
                                            };
                                        };
                                    };
                                };
                            };
                        };

                    } else {
                        if (ShopData_Type == `rod`) {

                            let debounce = false
                            let ItemCost_Rod01 = 100
                            let ItemCost_Rod02 = 200
                            let ItemCost_Rod03 = 400
                            let ItemCost_Rod04 = 700
                            let ItemCost_Rod05 = 1000
                            let ItemCost_Rod06 = 1300
                            let ItemCost_Rod07 = 1600

                            const ShopEmbed = new EmbedBuilder()
                                .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                .setColor("#3498DB")
                                .addFields(
                                    { name: '**🎣 Basic Rod**', value: `Catch Speed: \`⌛ 40s\`\nCatch Chance: \`50%\`\nDouble Chance: \`0%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod01)} Coins\`` },
                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                )
                                .setTimestamp()

                            talkedRecently.add(message.author.id);

                            const MainEmbed = await message.channel.send({ content: mention, embeds: [ShopEmbed] });
                            await MainEmbed.react('❌');
                            await MainEmbed.react(`◀`);
                            await MainEmbed.react('💰');
                            await MainEmbed.react(`▶`);

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

                                let item_cost = 0

                                if (ShopData_Page == 1) {
                                    item_cost = ItemCost_Rod01
                                } else {
                                    if (ShopData_Page == 2) {
                                        item_cost = ItemCost_Rod02
                                    } else {
                                        if (ShopData_Page == 3) {
                                            item_cost = ItemCost_Rod03
                                        } else {
                                            if (ShopData_Page == 4) {
                                                item_cost = ItemCost_Rod04
                                            } else {
                                                if (ShopData_Page == 5) {
                                                    item_cost = ItemCost_Rod05
                                                } else {
                                                    if (ShopData_Page == 6) {
                                                        item_cost = ItemCost_Rod06
                                                    } else {
                                                        if (ShopData_Page == 7) {
                                                            item_cost = ItemCost_Rod07
                                                        } else {
                                                            item_cost = 9999999999999
                                                        };
                                                    };
                                                };
                                            };
                                        };
                                    };
                                };

                                if (users[userId].Coins >= item_cost) {

                                    if (ShopData_Page == 1) {
                                        users = us_read();
                                        updateUserData(userId, { Tools: { ...users[userId].Tools, Rod: users[userId].Tools.Rod = "🎣 Basic Rod" } });
                                        updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                        us_write(users)
                                        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🎣 Basic Rod\x1b[90m]`);
                                    } else {
                                        if (ShopData_Page == 2) {
                                            users = us_read();
                                            updateUserData(userId, { Tools: { ...users[userId].Tools, Rod: users[userId].Tools.Rod = "🎣 Advanced Rod" } });
                                            updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                            us_write(users)
                                            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🎣 Advanced Rod\x1b[90m]`);
                                        } else {
                                            if (ShopData_Page == 3) {
                                                users = us_read();
                                                updateUserData(userId, { Tools: { ...users[userId].Tools, Rod: users[userId].Tools.Rod = "🎣 Expert Rod" } });
                                                updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                us_write(users)
                                                bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🎣 Expert Rod\x1b[90m]`);
                                            } else {
                                                if (ShopData_Page == 4) {
                                                    users = us_read();
                                                    updateUserData(userId, { Tools: { ...users[userId].Tools, Rod: users[userId].Tools.Rod = "🎣 Legendary Rod" } });
                                                    updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                    us_write(users)
                                                    bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🎣 Legendary Rod\x1b[90m]`);
                                                } else {
                                                    if (ShopData_Page == 5) {
                                                        users = us_read();
                                                        updateUserData(userId, { Tools: { ...users[userId].Tools, Rod: users[userId].Tools.Rod = "🎣 Mythical Rod" } });
                                                        updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                        us_write(users)
                                                        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🎣 Mythical Rod\x1b[90m]`);
                                                    } else {
                                                        if (ShopData_Page == 6) {
                                                            users = us_read();
                                                            updateUserData(userId, { Tools: { ...users[userId].Tools, Rod: users[userId].Tools.Rod = "🎣 Ultimate Rod" } });
                                                            updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                            us_write(users)
                                                            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🎣 Ultimate Rod\x1b[90m]`);
                                                        } else {
                                                            if (ShopData_Page == 7) {
                                                                users = us_read();
                                                                updateUserData(userId, { Tools: { ...users[userId].Tools, Rod: users[userId].Tools.Rod = "🎣 Divine Rod" } });
                                                                updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                                us_write(users)
                                                                bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🎣 Divine Rod\x1b[90m]`);
                                                            } else {

                                                            };
                                                        };
                                                    };
                                                };
                                            };
                                        };
                                    };

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
                                    case "◀":
                                        if (user.tag === message.author.tag) {

                                            ShopData_Page -= 1
                                            if (ShopData_Page < 1) { ShopData_Page = 7 };
                                            CheckPage();

                                            reaction.users.remove(user);
                                            break;
                                        };
                                    case "▶":
                                        if (user.tag === message.author.tag) {

                                            ShopData_Page += 1
                                            if (ShopData_Page > 7) { ShopData_Page = 1 };
                                            CheckPage();

                                            reaction.users.remove(user);
                                            break;
                                        };
                                };
                            });

                            async function CheckPage() {
                                if (ShopData_Page == 1) {
                                    const EditEmbed = new EmbedBuilder()
                                        .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                        .setColor("#3498DB")
                                        .addFields(
                                            { name: '**🎣 Basic Rod**', value: `Catch Speed: \`⌛ 40s\`\nCatch Chance: \`50%\`\nDouble Chance: \`0%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod01)} Coins\`` },
                                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                        )
                                        .setTimestamp()
                                    await MainEmbed.edit({ embeds: [EditEmbed] })
                                } else {
                                    if (ShopData_Page == 2) {
                                        const EditEmbed = new EmbedBuilder()
                                            .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                            .setColor("#3498DB")
                                            .addFields(
                                                { name: '**🎣 Advanced Rod**', value: `Catch Speed: \`⌛ 35s\`\nCatch Chance: \`55%\`\nDouble Chance: \`5%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod02)} Coins\`` },
                                                { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                            )
                                            .setTimestamp()
                                        await MainEmbed.edit({ embeds: [EditEmbed] })
                                    } else {
                                        if (ShopData_Page == 3) {
                                            const EditEmbed = new EmbedBuilder()
                                                .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                .setColor("#3498DB")
                                                .addFields(
                                                    { name: '**🎣 Expert Rod**', value: `Catch Speed: \`⌛ 30s\`\nCatch Chance: \`60%\`\nDouble Chance: \`10%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod03)} Coins\`` },
                                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                )
                                                .setTimestamp()
                                            await MainEmbed.edit({ embeds: [EditEmbed] })
                                        } else {
                                            if (ShopData_Page == 4) {
                                                const EditEmbed = new EmbedBuilder()
                                                    .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                    .setColor("#3498DB")
                                                    .addFields(
                                                        { name: '**🎣 Legendary Rod**', value: `Catch Speed: \`⌛ 25s\`\nCatch Chance: \`65%\`\nDouble Chance: \`15%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod04)} Coins\`` },
                                                        { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                        { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                    )
                                                    .setTimestamp()
                                                await MainEmbed.edit({ embeds: [EditEmbed] })
                                            } else {
                                                if (ShopData_Page == 5) {
                                                    const EditEmbed = new EmbedBuilder()
                                                        .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                        .setColor("#3498DB")
                                                        .addFields(
                                                            { name: '**🎣 Mythical Rod**', value: `Catch Speed: \`⌛ 20s\`\nCatch Chance: \`70%\`\nDouble Chance: \`20%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod05)} Coins\`` },
                                                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                        )
                                                        .setTimestamp()
                                                    await MainEmbed.edit({ embeds: [EditEmbed] })
                                                } else {
                                                    if (ShopData_Page == 6) {
                                                        const EditEmbed = new EmbedBuilder()
                                                            .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                            .setColor("#3498DB")
                                                            .addFields(
                                                                { name: '**🎣 Ultimate Rod**', value: `Catch Speed: \`⌛ 20s\`\nCatch Chance: \`75%\`\nDouble Chance: \`25%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod06)} Coins\`` },
                                                                { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                            )
                                                            .setTimestamp()
                                                        await MainEmbed.edit({ embeds: [EditEmbed] })
                                                    } else {
                                                        if (ShopData_Page == 7) {
                                                            const EditEmbed = new EmbedBuilder()
                                                                .setAuthor({ name: `${message.author.tag}'s Rod Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                                .setColor("#3498DB")
                                                                .addFields(
                                                                    { name: '**🎣 Divine Rod**', value: `Catch Speed: \`⌛ 20s\`\nCatch Chance: \`80%\`\nDouble Chance: \`30%\`\n\nCurrent: \`${users[userId].Tools.Rod}\`\nPrice: \`🪙 ${comma(ItemCost_Rod07)} Coins\`` },
                                                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                                )
                                                                .setTimestamp()
                                                            await MainEmbed.edit({ embeds: [EditEmbed] })
                                                        } else {

                                                        };
                                                    };
                                                };
                                            };
                                        };
                                    };
                                };
                            };

                        } else {
                            if (ShopData_Type == `water`) {

                                let debounce = false
                                let ItemCost_WC01 = 5000
                                let ItemCost_WC02 = 10000
                                let ItemCost_WC03 = 15000
                                let ItemCost_WC04 = 20000
                                let ItemCost_WC05 = 25000
                                let ItemCost_WC06 = 30000
                                let ItemCost_WC07 = 45000
                                let ItemCost_WC08 = 55000
                                let ItemCost_WC09 = 65000
                                let ItemCost_WC10 = 80000

                                const ShopEmbed = new EmbedBuilder()
                                    .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                    .setColor("#3498DB")
                                    .addFields(
                                        { name: '**🚿 Basic Watering Can**', value: `Grow Speed: \`⌛ -3%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC01)} Coins\`` },
                                        { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                        { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                    )
                                    .setTimestamp()

                                talkedRecently.add(message.author.id);

                                const MainEmbed = await message.channel.send({ content: mention, embeds: [ShopEmbed] });
                                await MainEmbed.react('❌');
                                await MainEmbed.react(`◀`);
                                await MainEmbed.react('💰');
                                await MainEmbed.react(`▶`);

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

                                    let item_cost = 0

                                    if (ShopData_Page == 1) {
                                        item_cost = ItemCost_WC01
                                    } else {
                                        if (ShopData_Page == 2) {
                                            item_cost = ItemCost_WC02
                                        } else {
                                            if (ShopData_Page == 3) {
                                                item_cost = ItemCost_WC03
                                            } else {
                                                if (ShopData_Page == 4) {
                                                    item_cost = ItemCost_WC04
                                                } else {
                                                    if (ShopData_Page == 5) {
                                                        item_cost = ItemCost_WC05
                                                    } else {
                                                        if (ShopData_Page == 6) {
                                                            item_cost = ItemCost_WC06
                                                        } else {
                                                            if (ShopData_Page == 7) {
                                                                item_cost = ItemCost_WC07
                                                            } else {
                                                                if (ShopData_Page == 8) {
                                                                    item_cost = ItemCost_WC08
                                                                } else {
                                                                    if (ShopData_Page == 9) {
                                                                        item_cost = ItemCost_WC09
                                                                    } else {
                                                                        if (ShopData_Page == 10) {
                                                                            item_cost = ItemCost_WC10
                                                                        } else {
                                                                            item_cost = 9999999999999
                                                                        };
                                                                    };
                                                                };
                                                            };
                                                        };
                                                    };
                                                };
                                            };
                                        };
                                    };

                                    if (users[userId].Coins >= item_cost) {

                                        if (ShopData_Page == 1) {
                                            users = us_read();
                                            updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Basic Watering Can" } });
                                            updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                            us_write(users)
                                            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Basic Watering Can\x1b[90m]`);
                                        } else {
                                            if (ShopData_Page == 2) {
                                                users = us_read();
                                                updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Advanced Watering Can" } });
                                                updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                us_write(users)
                                                bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Advanced Watering Can\x1b[90m]`);
                                            } else {
                                                if (ShopData_Page == 3) {
                                                    users = us_read();
                                                    updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Expert Watering Can" } });
                                                    updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                    us_write(users)
                                                    bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Expert Watering Can\x1b[90m]`);
                                                } else {
                                                    if (ShopData_Page == 4) {
                                                        users = us_read();
                                                        updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Legendary Watering Can" } });
                                                        updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                        us_write(users)
                                                        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Legendary Watering Can\x1b[90m]`);
                                                    } else {
                                                        if (ShopData_Page == 5) {
                                                            users = us_read();
                                                            updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Mythical Watering Can" } });
                                                            updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                            us_write(users)
                                                            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Mythical Watering Can\x1b[90m]`);
                                                        } else {
                                                            if (ShopData_Page == 6) {
                                                                users = us_read();
                                                                updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Ultimate Watering Can" } });
                                                                updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                                us_write(users)
                                                                bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Ultimate Watering Can\x1b[90m]`);
                                                            } else {
                                                                if (ShopData_Page == 7) {
                                                                    users = us_read();
                                                                    updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Divine Watering Can" } });
                                                                    updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                                    us_write(users)
                                                                    bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Divine Watering Can\x1b[90m]`);
                                                                } else {
                                                                    if (ShopData_Page == 8) {
                                                                        users = us_read();
                                                                        updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Eternal Watering Can" } });
                                                                        updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                                        us_write(users)
                                                                        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Eternal Watering Can\x1b[90m]`);
                                                                    } else {
                                                                        if (ShopData_Page == 9) {
                                                                            users = us_read();
                                                                            updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Transcendent Watering Can" } });
                                                                            updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                                            us_write(users)
                                                                            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Transcendent Watering Can\x1b[90m]`);
                                                                        } else {
                                                                            if (ShopData_Page == 10) {
                                                                                users = us_read();
                                                                                updateUserData(userId, { Tools: { ...users[userId].Tools, WateringCan: users[userId].Tools.WateringCan = "🚿 Celestial Watering Can" } });
                                                                                updateUserData(userId, { Coins: users[userId].Coins -= item_cost });
                                                                                us_write(users)
                                                                                bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas purchased \x1b[90m[\x1b[37m🚿 Celestial Watering Can\x1b[90m]`);
                                                                            } else {
                                                                                
                                                                            };
                                                                        };
                                                                    };
                                                                };
                                                            };
                                                        };  
                                                    };
                                                };
                                            };
                                        };

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
                                        case "◀":
                                            if (user.tag === message.author.tag) {

                                                ShopData_Page -= 1
                                                if (ShopData_Page < 1) { ShopData_Page = 10 };
                                                CheckPage();

                                                reaction.users.remove(user);
                                                break;
                                            };
                                        case "▶":
                                            if (user.tag === message.author.tag) {

                                                ShopData_Page += 1
                                                if (ShopData_Page > 10) { ShopData_Page = 1 };
                                                CheckPage();

                                                reaction.users.remove(user);
                                                break;
                                            };
                                    };
                                });

                                async function CheckPage() {
                                    if (ShopData_Page == 1) {
                                        const EditEmbed = new EmbedBuilder()
                                            .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                            .setColor("#3498DB")
                                            .addFields(
                                                { name: '**🚿 Basic Watering Can**', value: `Grow Speed: \`⌛ -3%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC01)} Coins\`` },
                                                { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                            )
                                            .setTimestamp()
                                        await MainEmbed.edit({ embeds: [EditEmbed] })
                                    } else {
                                        if (ShopData_Page == 2) {
                                            const EditEmbed = new EmbedBuilder()
                                                .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                .setColor("#3498DB")
                                                .addFields(
                                                    { name: '**🚿 Advanced Watering Can**', value: `Grow Speed: \`⌛ -6%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC02)} Coins\`` },
                                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                )
                                                .setTimestamp()
                                            await MainEmbed.edit({ embeds: [EditEmbed] })
                                        } else {
                                            if (ShopData_Page == 3) {
                                                const EditEmbed = new EmbedBuilder()
                                                    .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                    .setColor("#3498DB")
                                                    .addFields(
                                                        { name: '**🚿 Expert Watering Can**', value: `Grow Speed: \`⌛ -9%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC03)} Coins\`` },
                                                        { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                        { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                    )
                                                    .setTimestamp()
                                                await MainEmbed.edit({ embeds: [EditEmbed] })
                                            } else {
                                                if (ShopData_Page == 4) {
                                                    const EditEmbed = new EmbedBuilder()
                                                        .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                        .setColor("#3498DB")
                                                        .addFields(
                                                            { name: '**🚿 Legendary Watering Can**', value: `Grow Speed: \`⌛ -12%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC04)} Coins\`` },
                                                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                        )
                                                        .setTimestamp()
                                                    await MainEmbed.edit({ embeds: [EditEmbed] })
                                                } else {
                                                    if (ShopData_Page == 5) {
                                                        const EditEmbed = new EmbedBuilder()
                                                            .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                            .setColor("#3498DB")
                                                            .addFields(
                                                                { name: '**🚿 Mythical Watering Can**', value: `Grow Speed: \`⌛ -15%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC05)} Coins\`` },
                                                                { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                            )
                                                            .setTimestamp()
                                                        await MainEmbed.edit({ embeds: [EditEmbed] })
                                                    } else {
                                                        if (ShopData_Page == 6) {
                                                            const EditEmbed = new EmbedBuilder()
                                                                .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                                .setColor("#3498DB")
                                                                .addFields(
                                                                    { name: '**🚿 Ultimate Watering Can**', value: `Grow Speed: \`⌛ -18%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC06)} Coins\`` },
                                                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                                )
                                                                .setTimestamp()
                                                            await MainEmbed.edit({ embeds: [EditEmbed] })
                                                        } else {
                                                            if (ShopData_Page == 7) {
                                                                const EditEmbed = new EmbedBuilder()
                                                                    .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                                    .setColor("#3498DB")
                                                                    .addFields(
                                                                        { name: '**🚿 Divine Watering Can**', value: `Grow Speed: \`⌛ -21%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC07)} Coins\`` },
                                                                        { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                        { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                                    )
                                                                    .setTimestamp()
                                                                await MainEmbed.edit({ embeds: [EditEmbed] })
                                                            } else {
                                                                if (ShopData_Page == 8) {
                                                                    const EditEmbed = new EmbedBuilder()
                                                                        .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                                        .setColor("#3498DB")
                                                                        .addFields(
                                                                            { name: '**🚿 Eternal Watering Can**', value: `Grow Speed: \`⌛ -24%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC08)} Coins\`` },
                                                                            { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                            { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                                        )
                                                                        .setTimestamp()
                                                                    await MainEmbed.edit({ embeds: [EditEmbed] })
                                                                } else {
                                                                    if (ShopData_Page == 9) {
                                                                        const EditEmbed = new EmbedBuilder()
                                                                            .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                                            .setColor("#3498DB")
                                                                            .addFields(
                                                                                { name: '**🚿 Transcendent Watering Can**', value: `Grow Speed: \`⌛ -27%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC09)} Coins\`` },
                                                                                { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                                { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                                            )
                                                                            .setTimestamp()
                                                                        await MainEmbed.edit({ embeds: [EditEmbed] })
                                                                    } else {
                                                                        if (ShopData_Page == 10) {
                                                                            const EditEmbed = new EmbedBuilder()
                                                                                .setAuthor({ name: `${message.author.tag}'s Watering Can Shop | Page ${ShopData_Page}`, iconURL: `${message.author.displayAvatarURL()}` })
                                                                                .setColor("#3498DB")
                                                                                .addFields(
                                                                                    { name: '**🚿 Celestial Watering Can**', value: `Grow Speed: \`⌛ -30%\`\n\nCurrent: \`${users[userId].Tools.WateringCan}\`\nPrice: \`🪙 ${comma(ItemCost_WC10)} Coins\`` },
                                                                                    { name: `Your Stats`, value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`` },
                                                                                    { name: `Options`, value: `Click 💰 to purchase.\nClick ❌ to close.\nClick ◀ or ▶ to change page.` },
                                                                                )
                                                                                .setTimestamp()
                                                                            await MainEmbed.edit({ embeds: [EditEmbed] })
                                                                        } else {
                                                                            
                                                                        };
                                                                    };
                                                                };
                                                            };
                                                        };
                                                    };
                                                };
                                            };
                                        };
                                    };
                                };

                            } else {

                            }
                        }
                    }
                }
            }

        };

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[90m[\x1b[37m${error}" \x1b[31mfrom \x1b[90m[\x1b[37m${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'shop',
    aliases: ['store', 'upgrade']
};