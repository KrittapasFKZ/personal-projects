const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");
const assert = require('assert');

let talkedRecently = new Set();

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {

        if (config.farming_status == false && message.author.id != "605361556297089035") {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Interaction Denied!`)
                .setDescription(`❌ ตอนนี้ระบบฟาร์มถูกปิดใช้งาน!`)
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

        if (talkedRecently.has(message.author.id)) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Interaction Denied!`)
                .setDescription(`❌ ตอนนี้คุณกำลังเปิดฟาร์มอยู่!`)
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

        function convertMsToTime(ms) {
            let hours = Math.floor(ms / 3600000);
            let remainingMs = ms % 3600000;
            let minutes = Math.floor(remainingMs / 60000);
            let seconds = Math.round((remainingMs % 60000) / 1000);

            let timeString = '';

            if (hours > 0) {
                timeString += hours + 'h';
            }

            if (minutes > 0 || timeString === '') {
                timeString += minutes + 'm';
            }

            if (seconds > 0 || timeString === '') {
                timeString += seconds + 's';
            }

            return timeString;
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

        function generateUUID() {
            let dt = new Date().getTime();
            const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                const r = (dt + Math.random() * 16) % 16 | 0;
                dt = Math.floor(dt / 16);
                return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
            });
            return uuid;
        };

        let userId = message.author.id
        let users = us_read();

        if (users[userId]) {

            message.delete()
            let mention = `<@${message.author.id}>`;

            let Farming_WateringCan = users[userId].Tools.WateringCan
            let Farming_CropDrop = users[userId].Farming.CropDrop
            let Farming_GrowSpeed = 0
            let Farming_Farmlands = users[userId].Farming.Farmlands
            let Farming_MaxSlot = users[userId].Farming.Slot

            if (Farming_WateringCan == "🚿 Noob Watering Can") {
                Farming_GrowSpeed = 0
            } else {
                if (Farming_WateringCan == "🚿 Basic Watering Can") {
                    Farming_GrowSpeed = 3
                } else {
                    if (Farming_WateringCan == "🚿 Advanced Watering Can") {
                        Farming_GrowSpeed = 6
                    } else {
                        if (Farming_WateringCan == "🚿 Expert Watering Can") {
                            Farming_GrowSpeed = 9
                        } else {
                            if (Farming_WateringCan == "🚿 Legendary Watering Can") {
                                Farming_GrowSpeed = 12
                            } else {
                                if (Farming_WateringCan == "🚿 Mythical Watering Can") {
                                    Farming_GrowSpeed = 15
                                } else {
                                    if (Farming_WateringCan == "🚿 Ultimate Watering Can") {
                                        Farming_GrowSpeed = 18
                                    } else {
                                        if (Farming_WateringCan == "🚿 Divine Watering Can") {
                                            Farming_GrowSpeed = 21
                                        } else {
                                            if (Farming_WateringCan == "🚿 Eternal Watering Can") {
                                                Farming_GrowSpeed = 24
                                            } else {
                                                if (Farming_WateringCan == "🚿 Transcendent Watering Can") {
                                                    Farming_GrowSpeed = 27
                                                } else {
                                                    if (Farming_WateringCan == "🚿 Celestial Watering Can") {
                                                        Farming_GrowSpeed = 30
                                                    } else {
                                                        Farming_GrowSpeed = 0
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

            talkedRecently.add(message.author.id);

            const EventEmbed_Load = new EmbedBuilder()
                .setTitle(`Loading...`)
                .setColor('#FFFF00')
                .setTimestamp()

            const MainEmbed = await message.channel.send({ content: mention, embeds: [EventEmbed_Load] });

            async function FarmingPage_Main() {
                const StartEmbed = new EmbedBuilder()
                    .setAuthor({ name: `${message.author.tag}'s Farmland - Main Page`, iconURL: `${message.author.displayAvatarURL()}` })
                    .setThumbnail(`https://cdnb.artstation.com/p/assets/images/images/031/016/295/original/reecion-farminggame.gif?1602336749`)
                    .addFields(
                        { name: '**Your Stats**', value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`\nWatering Can: \`${Farming_WateringCan}\`\nGrow Speed: \`⌛ -${Farming_GrowSpeed}%\`\nCrop Drops: \`x${Farming_CropDrop}\`\nFarmland Slot: \`🌱 ${Farming_Farmlands.length}/${Farming_MaxSlot}\`` },
                    )
                    .setColor("#2ECC71")
                    .setTimestamp()
                    .setFooter({ text: `Click ❌ to hide this message.\nClick 🌱 to plant crop.\nClick 🎒 to harvest crop.` })

                if (Farming_Farmlands.length <= 0) {
                    StartEmbed.addFields({ name: '**No Active Farmlands!**', value: ` ` })
                } else {
                    StartEmbed.addFields({ name: `**${Farming_Farmlands.length} Active Farmlands**`, value: ` ` })
                    Farming_Farmlands.forEach(farm => {

                        let FARM_START = farm.CROP_START
                        let FARM_END = farm.CROP_END
                        let FARM_TIME_LEFT = (FARM_END / 1000).toFixed(0)
                        let FARM_PROGRESS = Math.min(100, ((Date.now() - FARM_START) / (FARM_END - FARM_START)) * 100);

                        if (FARM_PROGRESS >= 100) {
                            StartEmbed.addFields({ name: `**${farm.CROP_NAME}**`, value: `Status: \`✅ READY\`\nProgress: \`${FARM_PROGRESS.toFixed(1)}%\`` });
                        } else {
                            StartEmbed.addFields({ name: `**${farm.CROP_NAME}**`, value: `Status: \`❌ NOT READY\`\nProgress: \`${FARM_PROGRESS.toFixed(1)}%\`\nFully Grow: <t:${FARM_TIME_LEFT}:R>` });
                        };

                    });
                };

                MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                await MainEmbed.edit({ embeds: [StartEmbed] })
                await MainEmbed.react('❌');
                await MainEmbed.react('🌱');
                await MainEmbed.react('🎒');

                async function HarvestPlant() {

                    let HARVEST_AMOUNT = 0
                    let HARVEST_PRODUCT = 3

                    const EventEmbed_Completed = new EmbedBuilder()
                        .setTitle(`Interaction Completed!`)
                        .setColor('#00FF00')
                        .setTimestamp()

                    const EventEmbed_Failed = new EmbedBuilder()
                        .setTitle(`Error Occurred!`)
                        .setDescription(`❌ ไม่มี Crops ชนิดไหนที่สามารถเก็บเกี่ยวได้ในตอนนี้!`)
                        .setColor('#FF0000')
                        .setTimestamp()

                    async function GetCrop(farm, index) {

                        return new Promise((resolve, reject) => {

                            let newData = null
                            HARVEST_PRODUCT = (HARVEST_PRODUCT * Farming_CropDrop);

                            HARVEST_AMOUNT += 1;

                            if (farm.CROP_ID == "WHEAT") {
                                users = us_read();
                                newData = { Inventory: { ...users[userId].Inventory, Wheat: users[userId].Inventory.Wheat += HARVEST_PRODUCT } };
                            } else {
                                if (farm.CROP_ID == "POTATO") {
                                    users = us_read();
                                    newData = { Inventory: { ...users[userId].Inventory, Potato: users[userId].Inventory.Potato += HARVEST_PRODUCT } };
                                } else {
                                    if (farm.CROP_ID == "CARROT") {
                                        users = us_read();
                                        newData = { Inventory: { ...users[userId].Inventory, Carrot: users[userId].Inventory.Carrot += HARVEST_PRODUCT } };
                                    } else {
                                        if (farm.CROP_ID == "CORN") {
                                            users = us_read();
                                            newData = { Inventory: { ...users[userId].Inventory, Corn: users[userId].Inventory.Corn += HARVEST_PRODUCT } };
                                        } else {

                                        };
                                    };
                                };
                            };

                            updateUserData(userId, newData);
                            us_write(users)
                            resolve(newData);

                        });
                    };

                    let HARVEST_NOW_WHEAT = 0
                    let HARVEST_NOW_POTATO = 0
                    let HARVEST_NOW_CARROT = 0
                    let HARVEST_NOW_CORN = 0

                    let HARVEST_NOW_XP = 0

                    for (let index = 0; index < Farming_Farmlands.length; index++) {
                        let farm = Farming_Farmlands[index];
                        let time_now = Date.now();
                        HARVEST_PRODUCT = 3;

                        if (time_now >= farm.CROP_END) {

                            if (farm.CROP_ID == "WHEAT") {
                                HARVEST_NOW_WHEAT += 3
                                HARVEST_NOW_XP += 60
                            } else {
                                if (farm.CROP_ID == "POTATO") {
                                    HARVEST_NOW_POTATO += 3
                                    HARVEST_NOW_XP += 120
                                } else {
                                    if (farm.CROP_ID == "CARROT") {
                                        HARVEST_NOW_CARROT += 3
                                        HARVEST_NOW_XP += 120
                                    } else {
                                        if (farm.CROP_ID == "CORN") {
                                            HARVEST_NOW_CORN += 3
                                            HARVEST_NOW_XP += 180
                                        } else {

                                        };
                                    };
                                };
                            };

                            await GetCrop(farm, index);
                            Farming_Farmlands.splice(index, 1);
                            index--;
                        };
                    };

                    if (HARVEST_NOW_WHEAT >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mharvested \x1b[37m"🌾 Wheat x ${HARVEST_NOW_WHEAT * Farming_CropDrop}"`);
                        EventEmbed_Completed.addFields({ name: `**🌾 Wheat** x ${HARVEST_NOW_WHEAT * Farming_CropDrop}`, value: ` ` });
                    };
                    if (HARVEST_NOW_POTATO >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mharvested \x1b[37m"🥔 Potato x ${HARVEST_NOW_POTATO * Farming_CropDrop}"`);
                        EventEmbed_Completed.addFields({ name: `**🥔 Potato** x ${HARVEST_NOW_POTATO * Farming_CropDrop}`, value: ` ` });
                    };
                    if (HARVEST_NOW_CARROT >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mharvested \x1b[37m"🥕 Carrot x ${HARVEST_NOW_CARROT * Farming_CropDrop}"`);
                        EventEmbed_Completed.addFields({ name: `**🥕 Carrot** x ${HARVEST_NOW_CARROT * Farming_CropDrop}`, value: ` ` });
                    };
                    if (HARVEST_NOW_CORN >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mharvested \x1b[37m"🌽 Corn x ${HARVEST_NOW_CORN * Farming_CropDrop}"`);
                        EventEmbed_Completed.addFields({ name: `**🌽 Corn** x ${HARVEST_NOW_CORN * Farming_CropDrop}`, value: ` ` });
                    };

                    EventEmbed_Completed.addFields({ name: `**✨ XP** x ${comma(Number((HARVEST_NOW_XP * users[userId].Multiplier).toFixed(2)))}`, value: ` ` });

                    users = us_read();
                    updateUserData(userId, { Farming: { ...users[userId].Farming, Farmlands: users[userId].Farming.Farmlands = Farming_Farmlands } });
                    us_write(users);

                    users = us_read();
                    let xpToAdd = Number((HARVEST_NOW_XP * users[userId].Multiplier).toFixed(2));
                    updateUserData(userId, { XP: users[userId].XP += xpToAdd });
                    us_write(users);

                    EventEmbed_Completed.setDescription(`✅ เก็บเกี่ยวเรียบร้อยแล้ว!`)

                    if (HARVEST_AMOUNT >= 1) {

                        now = new Date(Date.now());
                        now_hours = now.getHours().toString().padStart(2, '0');
                        now_mins = now.getMinutes().toString().padStart(2, '0');
                        now_seconds = now.getSeconds().toString().padStart(2, '0');

                        MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                        await MainEmbed.edit({ embeds: [EventEmbed_Completed] });
                        setTimeout(async () => {
                            await FarmingPage_Main();
                        }, 2000);

                    } else {

                        MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                        await MainEmbed.edit({ embeds: [EventEmbed_Failed] });
                        setTimeout(async () => {
                            await FarmingPage_Main();
                        }, 2000);

                    };

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

                collector.on('collect', async (reaction, user) => {
                    switch (reaction.emoji.name) {
                        case "❌":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                if (talkedRecently.has(message.author.id)) {
                                    talkedRecently.delete(message.author.id);
                                };
                                collector.stop()
                                MainEmbed.delete();
                                break;
                            };
                        case "🌱":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                collector.stop()
                                await FarmingPage_Plant();
                                break;
                            };
                        case "🎒":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                collector.stop()
                                await HarvestPlant();
                                break;
                            };
                    }
                });
            };

            async function FarmingPage_Plant() {

                const StartEmbed = new EmbedBuilder()
                    .setAuthor({ name: `${message.author.tag}'s Farmland - Plant Crops`, iconURL: `${message.author.displayAvatarURL()}` })
                    .setThumbnail(`https://cdnb.artstation.com/p/assets/images/images/031/016/295/original/reecion-farminggame.gif?1602336749`)
                    .addFields(
                        { name: '**Your Stats**', value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`\nWatering Can: \`${Farming_WateringCan}\`\nGrow Speed: \`⌛ -${Farming_GrowSpeed}%\`\nCrop Drops: \`x${Farming_CropDrop}\`\nFarmland Slot: \`🌱 ${Farming_Farmlands.length}/${Farming_MaxSlot}\`` },
                    )
                    .setColor("#2ECC71")
                    .setTimestamp()
                    .setFooter({ text: `Click ◀ to go back.\nClick 🌾 to select crop.\nClick 🥔 to select crop.\nClick 🥕 to select crop.\nClick 🌽 to select crop.` })

                let UsedFarmLands_Amount = 0

                let UsedFarmLands_Wheat = 0
                let UsedFarmLands_Potato = 0
                let UsedFarmLands_Carrot = 0
                let UsedFarmLands_Corn = 0

                if (Farming_Farmlands.length <= 0) {
                    StartEmbed.addFields({ name: `**${Farming_MaxSlot - Farming_Farmlands.length} Slots Available!**`, value: ` ` })
                } else {
                    StartEmbed.addFields({ name: `**${Farming_MaxSlot - Farming_Farmlands.length} Slots Available!**`, value: ` ` })
                    Farming_Farmlands.forEach(farm => {
                        UsedFarmLands_Amount += 1
                        if (farm.CROP_ID == "WHEAT") {
                            UsedFarmLands_Wheat += 1
                        } else {
                            if (farm.CROP_ID == "POTATO") {
                                UsedFarmLands_Potato += 1
                            } else {
                                if (farm.CROP_ID == "CARROT") {
                                    UsedFarmLands_Carrot += 1
                                } else {
                                    if (farm.CROP_ID == "CORN") {
                                        UsedFarmLands_Corn += 1
                                    } else {

                                    };
                                };
                            };
                        };
                    });
                    StartEmbed.addFields({ name: `**Current Farmlands**`, value: `**🌾 Wheat x ${UsedFarmLands_Wheat}**\n**🥔 Potato x ${UsedFarmLands_Potato}**\n**🥕 Carrot x ${UsedFarmLands_Carrot}**\n**🌽 Corn x ${UsedFarmLands_Corn}**` })
                };

                let CROP_TIME_WHEAT = 3600000 - Math.floor((3600000 * Farming_GrowSpeed) / 100)
                let CROP_TIME_POTATO = 6000000 - Math.floor((6000000 * Farming_GrowSpeed) / 100)
                let CROP_TIME_CARROT = 6000000 - Math.floor((6000000 * Farming_GrowSpeed) / 100)
                let CROP_TIME_CORN = 12000000 - Math.floor((12000000 * Farming_GrowSpeed) / 100)

                StartEmbed.addFields(
                    { name: `**Seed Price**`, value: `**🌾 Wheat**: **🪙 100** **(${convertMsToTime(CROP_TIME_WHEAT)})**\n**🥔 Potato**: **🪙 300** **(${convertMsToTime(CROP_TIME_POTATO)})**\n**🥕 Carrot**: **🪙 300** **(${convertMsToTime(CROP_TIME_CARROT)})**\n**🌽 Corn**: **🪙 500** **(${convertMsToTime(CROP_TIME_CORN)})**` }
                );

                MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                await MainEmbed.edit({ embeds: [StartEmbed] })
                await MainEmbed.react(`◀`);
                await MainEmbed.react(`🌾`);
                await MainEmbed.react(`🥔`);
                await MainEmbed.react(`🥕`);
                await MainEmbed.react(`🌽`);

                async function PurchasePlant(CROP_NAME) {

                    let BUY_CROP_COST = 0
                    let BUY_CROP_ID = CROP_NAME
                    let BUY_CROP_NAME = ""
                    let BUY_CROP_TIME = 0

                    if (BUY_CROP_ID == "WHEAT") {
                        BUY_CROP_COST = 100
                        BUY_CROP_NAME = "🌾 Wheat"
                        BUY_CROP_TIME = 3600000
                    } else {
                        if (BUY_CROP_ID == "POTATO") {
                            BUY_CROP_COST = 300
                            BUY_CROP_NAME = "🥔 Potato"
                            BUY_CROP_TIME = 6000000
                        } else {
                            if (BUY_CROP_ID == "CARROT") {
                                BUY_CROP_COST = 300
                                BUY_CROP_NAME = "🥕 Carrot"
                                BUY_CROP_TIME = 6000000
                            } else {
                                if (BUY_CROP_ID == "CORN") {
                                    BUY_CROP_COST = 500
                                    BUY_CROP_NAME = "🌽 Corn"
                                    BUY_CROP_TIME = 12000000
                                } else {
                                    BUY_CROP_COST = 999999999
                                    BUY_CROP_NAME = "UNKNOWN"
                                    BUY_CROP_TIME = 0
                                };
                            };
                        };
                    };

                    if (users[userId].Coins >= BUY_CROP_COST) {

                        if (Farming_Farmlands.length >= Farming_MaxSlot) {
                            return
                        };

                        now = new Date(Date.now());
                        now_hours = now.getHours().toString().padStart(2, '0');
                        now_mins = now.getMinutes().toString().padStart(2, '0');
                        now_seconds = now.getSeconds().toString().padStart(2, '0');

                        BUY_CROP_TIME = BUY_CROP_TIME - Math.floor((BUY_CROP_TIME * Farming_GrowSpeed) / 100)

                        let BUY_CROP_AT = Date.now()
                        let BUY_CROP_END = (BUY_CROP_AT + BUY_CROP_TIME)

                        const newFarmland = {
                            "UUID": generateUUID(),
                            "CROP_ID": BUY_CROP_ID,
                            "CROP_NAME": BUY_CROP_NAME,
                            "CROP_WATERING_CAN": Farming_WateringCan,
                            "CROP_START": BUY_CROP_AT,
                            "CROP_END": BUY_CROP_END
                        };

                        Farming_Farmlands.push(newFarmland);

                        users = us_read();
                        updateUserData(userId, { Farming: { ...users[userId].Farming, Farmlands: users[userId].Farming.Farmlands = Farming_Farmlands } });
                        updateUserData(userId, { Coins: users[userId].Coins -= BUY_CROP_COST });
                        us_write(users)

                    };

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

                async function UpdateEmbed() {
                    const StartEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Farmland - Plant Crops`, iconURL: `${message.author.displayAvatarURL()}` })
                        .setThumbnail(`https://cdnb.artstation.com/p/assets/images/images/031/016/295/original/reecion-farminggame.gif?1602336749`)
                        .addFields(
                            { name: '**Your Stats**', value: `Coins: \`🪙 ${comma(users[userId].Coins)}\`\nWatering Can: \`${Farming_WateringCan}\`\nGrow Speed: \`⌛ -${Farming_GrowSpeed}%\`\nCrop Drops: \`x${Farming_CropDrop}\`\nFarmland Slot: \`🌱 ${Farming_Farmlands.length}/${Farming_MaxSlot}\`` },
                        )
                        .setColor("#2ECC71")
                        .setTimestamp()
                        .setFooter({ text: `Click ◀ to go back.\nClick 🌾 to select crop.\nClick 🥔 to select crop.\nClick 🥕 to select crop.\nClick 🌽 to select crop.` })

                    let UsedFarmLands_Amount = 0

                    let UsedFarmLands_Wheat = 0
                    let UsedFarmLands_Potato = 0
                    let UsedFarmLands_Carrot = 0
                    let UsedFarmLands_Corn = 0

                    if (Farming_Farmlands.length <= 0) {
                        StartEmbed.addFields({ name: `**${Farming_MaxSlot - Farming_Farmlands.length} Slots Available!**`, value: ` ` })
                    } else {
                        StartEmbed.addFields({ name: `**${Farming_MaxSlot - Farming_Farmlands.length} Slots Available!**`, value: ` ` })
                        Farming_Farmlands.forEach(farm => {
                            UsedFarmLands_Amount += 1
                            if (farm.CROP_ID == "WHEAT") {
                                UsedFarmLands_Wheat += 1
                            } else {
                                if (farm.CROP_ID == "POTATO") {
                                    UsedFarmLands_Potato += 1
                                } else {
                                    if (farm.CROP_ID == "CARROT") {
                                        UsedFarmLands_Carrot += 1
                                    } else {
                                        if (farm.CROP_ID == "CORN") {
                                            UsedFarmLands_Corn += 1
                                        } else {

                                        };
                                    };
                                };
                            };
                        });
                        StartEmbed.addFields({ name: `**Current Farmlands**`, value: `**🌾 Wheat x ${UsedFarmLands_Wheat}**\n**🥔 Potato x ${UsedFarmLands_Potato}**\n**🥕 Carrot x ${UsedFarmLands_Carrot}**\n**🌽 Corn x ${UsedFarmLands_Corn}**` })
                    };

                    if (UsedFarmLands_Wheat >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mplanted \x1b[37m"🌾 Wheat x ${UsedFarmLands_Wheat}" \x1b[32min their Farm! \x1b[37m[${Farming_Farmlands.length}/${Farming_MaxSlot}]`);
                    };
                    if (UsedFarmLands_Potato >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mplanted \x1b[37m"🥔 Potato x ${UsedFarmLands_Potato}" \x1b[32min their Farm! \x1b[37m[${Farming_Farmlands.length}/${Farming_MaxSlot}]`);
                    };
                    if (UsedFarmLands_Carrot >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mplanted \x1b[37m"🥕 Carrot x ${UsedFarmLands_Carrot}" \x1b[32min their Farm! \x1b[37m[${Farming_Farmlands.length}/${Farming_MaxSlot}]`);
                    };
                    if (UsedFarmLands_Corn >= 1) {
                        bot.BotLogs(message.guild.name, `\x1b[32m\x1b[37m"${message.author.tag}" \x1b[32mplanted \x1b[37m"🌽 Corn x ${UsedFarmLands_Corn}" \x1b[32min their Farm! \x1b[37m[${Farming_Farmlands.length}/${Farming_MaxSlot}]`);
                    };

                    let CROP_TIME_WHEAT = 3600000 - Math.floor((3600000 * Farming_GrowSpeed) / 100)
                    let CROP_TIME_POTATO = 6000000 - Math.floor((6000000 * Farming_GrowSpeed) / 100)
                    let CROP_TIME_CARROT = 6000000 - Math.floor((6000000 * Farming_GrowSpeed) / 100)
                    let CROP_TIME_CORN = 12000000 - Math.floor((12000000 * Farming_GrowSpeed) / 100)

                    StartEmbed.addFields(
                        { name: `**Seed Price**`, value: `**🌾 Wheat**: **🪙 100** **(${convertMsToTime(CROP_TIME_WHEAT)})**\n**🥔 Potato**: **🪙 300** **(${convertMsToTime(CROP_TIME_POTATO)})**\n**🥕 Carrot**: **🪙 300** **(${convertMsToTime(CROP_TIME_CARROT)})**\n**🌽 Corn**: **🪙 500** **(${convertMsToTime(CROP_TIME_CORN)})**` }
                    );
                    await MainEmbed.edit({ embeds: [StartEmbed] })
                };

                collector.on('collect', async (reaction, user) => {
                    switch (reaction.emoji.name) {
                        case "◀":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                collector.stop()
                                await FarmingPage_Main();
                                break;
                            };
                        case "🌾":
                            if (user.tag === message.author.tag) {
                                reaction.users.remove(user);
                                for (let index = 0; index < Farming_MaxSlot; index++) {
                                    await PurchasePlant("WHEAT");
                                };
                                UpdateEmbed();
                                break;
                            };
                        case "🥔":
                            if (user.tag === message.author.tag) {
                                reaction.users.remove(user);
                                for (let index = 0; index < Farming_MaxSlot; index++) {
                                    await PurchasePlant("POTATO");
                                };
                                UpdateEmbed();
                                break;
                            };
                        case "🥕":
                            if (user.tag === message.author.tag) {
                                reaction.users.remove(user);
                                for (let index = 0; index < Farming_MaxSlot; index++) {
                                    await PurchasePlant("CARROT");
                                };
                                UpdateEmbed();
                                break;
                            };
                        case "🌽":
                            if (user.tag === message.author.tag) {
                                reaction.users.remove(user);
                                for (let index = 0; index < Farming_MaxSlot; index++) {
                                    await PurchasePlant("CORN");
                                };
                                UpdateEmbed();
                                break;
                            };
                    }
                });
            };

            await FarmingPage_Main();

        };

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'farm',
    aliases: ['farms', 'farming', 'crop', 'crops']
};