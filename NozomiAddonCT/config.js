import { @Vigilant, @TextProperty, @ColorProperty, @ButtonProperty, @SwitchProperty, Color, @CheckboxProperty } from 'Vigilance';

/// Empty UI
var myGui = new Gui();
let VERSION = "0.8.5"
let mainText = new Text(" ", 5, 5).setScale(1).setShadow(true).setAlign("CENTER");
let subText = new Text(" ", 5, 5).setScale(1).setShadow(true).setAlign("CENTER");
myGui.registerDraw(() => {
    let x = Renderer.screen.getWidth() / 2
    let y = 20
    mainText.setScale(1);
    mainText.setString(`&b&k&lA&r &d&lGUI Editing Mode &b&k&lA&r`);
    mainText.draw(x, y);
    subText.setScale(1);
    subText.setString(`&eLC+Drag &f= &aMove &eLC+Scroll &f= &aSize &eLC+RC &f= &aReset`);
    subText.draw(x, y + 10);
});
///

@Vigilant("NozomiAddon", `§d§lNozomiAddon §bv${VERSION}`, {
    getCategoryComparator: () => (a, b) => {
        const categories = ["General", "Skills", "Dungeon", "MaskTimer", "Kuudra", "Slayer", "Rift", "Performance", "GUI & Other"];
        return categories.indexOf(a.name) - categories.indexOf(b.name);
    }
})
class Settings {

    prefix = "&d&lNozomiAddon &f&l»";
    editUI = false
    editUIName = ""
    version = `v${VERSION}`

    @ButtonProperty({
        name: "Edit GUI",
        description: "&r",
        category: "GUI & Other",
        placeholder: "Edit"
    })
    editgui() {
        Client.currentGui.close()
        myGui.open()
        this.editUI = true
        let checkTurnoff = register("guiClosed", () => {
            this.editUI = false
            checkTurnoff.unregister()
        });
    }

    @SwitchProperty({
        name: "Short Prefix Style",
        description: "Change &d&lNozomiAddon&r to &d&lNA",
        category: "GUI & Other",
    })
    prefixsettings = false;

    @SwitchProperty({
        name: "Hide Damage in Dungeon",
        description: `Hide damage splash while in &cDungeon&r`,
        category: "GUI & Other",
        subcategory: "Damage Hider",
    })
    dungeonhidedamage = false;

    @SwitchProperty({
        name: "Hide Damage in Kuudra",
        description: `Hide damage splash while in &cKuudra&r`,
        category: "GUI & Other",
        subcategory: "Damage Hider",
    })
    kuudrahidedamage = false;

    @SwitchProperty({
        name: "Hide Damage in Overworld",
        description: `hide damage splash while in &aOverworld&r`,
        category: "GUI & Other",
        subcategory: "Damage Hider",
    })
    generalhidedamage = false;

    @SwitchProperty({
        name: "No Blindness",
        description: `stop blindness from rendering`,
        category: "GUI & Other",
        subcategory: "No Debuff",
    })
    nodebuff_blindness = false;

    @SwitchProperty({
        name: "Background Blur",
        description: "render blur as background in ui",
        category: "GUI & Other",
    })
    backgroundBlur = false;

    /////

    @SwitchProperty({
        name: "Re-heated Gummy Polar Bear Alert",
        description: "Alert you when the buff has run out",
        category: "General",
        subcategory: "General",
    })
    gummy_bear = false;

    @SwitchProperty({
        name: "Abbreviate Damage Splash",
        description: "make the damage splash shorter",
        category: "General",
        subcategory: "General",
    })
    damagesplash = false;

    @SwitchProperty({
        name: "Bazaar Notify",
        description: "Alert when bazaar filled",
        category: "General",
        subcategory: "General",
    })
    bazaarnotify = false;

    @SwitchProperty({
        name: "Auction Notify",
        description: "Alert when Auction filled",
        category: "General",
        subcategory: "General",
    })
    auctionnotify = false;

    @SwitchProperty({
        name: "Chat Emotes",
        description: "Allow you to use MVP++ emotes",
        category: "General",
        subcategory: "General",
    })
    chatEmotes = false;

    @SwitchProperty({
        name: "Recipe Total Cost",
        description: "Calculate the item's ingredients",
        category: "General",
        subcategory: "General",
    })
    recipetotalcost = false;

    @SwitchProperty({
        name: "Lootchest Powder",
        description: "Display powder",
        category: "Skills",
        subcategory: "Mining",
    })
    miningpowder = false;

    @SwitchProperty({
        name: "Trophy Fish Diamond Alert",
        description: "Show Title when u fish up diamond!",
        category: "Skills",
        subcategory: "Fishing",
    })
    fishingtrophyfish = false;

    @SwitchProperty({
        name: "Frozen Corpse Helper",
        description: "locate the corpse for you",
        category: "Skills",
        subcategory: "Mining",
    })
    frozencorpsehelper = false;

    @SwitchProperty({
        name: "Trapper Animal Helper",
        description: "try it bro :]",
        category: "Skills",
        subcategory: "Trapper",
    })
    trapperanimalhelper = false;

    /////

    @SwitchProperty({
        name: "UbixTimer",
        description: "Ubix Time Overlay",
        category: "Rift",
        subcategory: "Ubix",
    })
    riftubix = false;

    @TextProperty({
        name: "RiftUbix X",
        category: "Rift",
        subcategory: "Ubix",
        description: "Position X of HUD",
        hidden: true
    })
    riftubix_pos_x = "5";

    @TextProperty({
        name: "RiftUbix Y",
        category: "Rift",
        subcategory: "Ubix",
        description: "Position Y of HUD",
        hidden: true
    })
    riftubix_pos_y = "120";

    @TextProperty({
        name: "RiftUbix Scale",
        category: "Rift",
        subcategory: "Ubix",
        description: "Scale of HUD",
        hidden: true
    })
    riftubix_scale = "1";

    /////

    /////

    @SwitchProperty({
        name: "ThunderBottle Overlay",
        description: "Render Thunder/Storm/Hurricane Bottle Charge Overlay",
        category: "General",
        subcategory: "ThunderBottle",
    })
    thunderbottle = false;

    @TextProperty({
        name: "ThunderBottle X",
        category: "General",
        subcategory: "ThunderBottle",
        description: "Position X of HUD",
        hidden: true
    })
    thunderbottle_pos_x = "5";

    @TextProperty({
        name: "ThunderBottle Y",
        category: "General",
        subcategory: "ThunderBottle",
        description: "Position Y of HUD",
        hidden: true
    })
    thunderbottle_pos_y = "120";

    @TextProperty({
        name: "ThunderBottle Scale",
        category: "General",
        subcategory: "ThunderBottle",
        description: "Scale of HUD",
        hidden: true
    })
    thunderbottle_scale = "1";

    /////

    /////

    @SwitchProperty({
        name: "Realtime Clock",
        description: "Realtime Clock Overlay",
        category: "GUI & Other",
        subcategory: "Realtime Clock",
    })
    realtimeclock = false;

    @TextProperty({
        name: "Realtime Clock X",
        category: "GUI & Other",
        subcategory: "Realtime Clock",
        description: "Position X of HUD",
        hidden: true
    })
    realtimeclock_pos_x = "5";

    @TextProperty({
        name: "Realtime Clock Y",
        category: "GUI & Other",
        subcategory: "Realtime Clock",
        description: "Position Y of HUD",
        hidden: true
    })
    realtimeclock_pos_y = "120";

    @TextProperty({
        name: "Realtime Clock Scale",
        category: "GUI & Other",
        subcategory: "Realtime Clock",
        description: "Scale of HUD",
        hidden: true
    })
    realtimeclock_scale = "1";

    ///// awdawdaw dwadawdawdawdawdawd

    @SwitchProperty({
        name: "Rare Drop Alert",
        description: "Alert you when you got rare drop",
        category: "General",
        subcategory: "Rare Drop Alert",
    })
    raredropalert = false;

    @SwitchProperty({
        name: "RNG Only",
        description: "Only show &d&lCRAZY RARE DROP &rand &c&lINSANE DROP",
        category: "General",
        subcategory: "Rare Drop Alert",
    })
    raredropalert_onlyrng = false;

    @SwitchProperty({
        name: "Slayer Boss Spawned Alert",
        description: "Alert you when the slayer boss has spawned",
        category: "Slayer",
        subcategory: "Slayer",
    })
    slayer_boss_spawn_alert = false;

    @SwitchProperty({
        name: "Dungeon Shadow Fury Highlight",
        description: `Draw box around the mobs when holding &7Shadow Fury`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    shadowfuryhighlight = false;

    @SwitchProperty({
        name: "Dungeon Starred Mob Highlight",
        description: `Draw box around the starred mob`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    starredmobhighlight = false;

    @ColorProperty({
        name: "Dungeon Starred Mob Color",
        description: "The color of the overlay for starred mob",
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    starredmobhighlight_color = new Color(0.33, 1, 1, 1);

    @SwitchProperty({
        name: "Dungeon Wither Lords Highlight",
        description: `Draw box around the wither lords boss &c&lMAXOR, STORM, GOLDOR, NECRON&r`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    witherlordhighlight = false;

    @ColorProperty({
        name: "Dungeon Wither Lords Color",
        description: "The color of the overlay for Wither Lords",
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    witherlordhighlight_color = new Color(1, 0.33, 0.33, 1);

    @SwitchProperty({
        name: "Dungeon Livid Highlight",
        description: `Draw box around the real livid`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    lividhighlight = false;

    @SwitchProperty({
        name: "Dungeon Class Highlight",
        description: `Draw box around the player that has specific class`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    classhighlight = false;

    @SwitchProperty({
        name: "Dungeon Class Highlight Line",
        description: `Draw line from you to other class`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    classhighlight_line = false;

    @TextProperty({
        name: "Dungeon Class Highlight Class",
        description: `class name ex: "mage", "MAGE", "Mage"`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    classhighlight_class = "Mage";

    @SwitchProperty({
        name: "Dungeon Fel Highlight",
        description: `Draw box around Fel`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    felhighlight = false;

    @SwitchProperty({
        name: "Dungeon Bat Highlight",
        description: `Draw box around Bat`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    bathighlight = false;

    @SwitchProperty({
        name: "Dungeon Bat Highlight Line",
        description: `Draw line from you to Bat`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    bathighlight_line = false;

    @ColorProperty({
        name: "Dungeon Bat Highlight Color",
        description: "The color of the overlay for Bat",
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    bathighlight_color = new Color(0.33, 0.33, 1, 1);

    @SwitchProperty({
        name: "Dungeon Blood Room",
        description: `Blood room stuff`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    bloodroom = false;

    /////

    @SwitchProperty({
        name: "Dungeon RunOverview",
        description: `Overview of dungeon time`,
        category: "Dungeon",
        subcategory: "Split Timer",
    })
    runoverview = false;

    @TextProperty({
        name: "RunOverview X",
        category: "Rift",
        subcategory: "Split Timer",
        description: "Position X of HUD",
        hidden: true
    })
    runoverview_pos_x = "5";

    @TextProperty({
        name: "RunOverview Y",
        category: "Rift",
        subcategory: "Split Timer",
        description: "Position Y of HUD",
        hidden: true
    })
    runoverview_pos_y = "120";

    @TextProperty({
        name: "RunOverview Scale",
        category: "Rift",
        subcategory: "Split Timer",
        description: "Scale of HUD",
        hidden: true
    })
    runoverview_scale = "1";

    /////

    @SwitchProperty({
        name: "Dungeon Death Message",
        description: `Send message on player death`,
        category: "Dungeon",
        subcategory: "General",
    })
    dungeondeathMessage = false;

    @TextProperty({
        name: "Custom Death Message Text",
        description: "The text sent on dungeon death.\nUse {name} to use the dead player's name.\nUse a comma to use many messages.",
        category: "Dungeon",
        subcategory: "General",
        placeholder: "Stupid"
    })
    dungeondeathMessageText = "Stupid";

    @SwitchProperty({
        name: "Dungeon Crypt Reminder",
        description: `Remind to get crypt`,
        category: "Dungeon",
        subcategory: "General",
    })
    cryptreminder = false;

    @SwitchProperty({
        name: "Dungeon Auto Get Draft",
        description: `auto get draft from your sack when failed puzzle`,
        category: "Dungeon",
        subcategory: "General",
    })
    autodraft = false;

    @SwitchProperty({
        name: "Dungeon Storm Freak",
        description: `idk`,
        category: "Dungeon",
        subcategory: "General",
    })
    stormfreak = false;

    @SwitchProperty({
        name: "Dungeon Blessing",
        description: `blessing stuff`,
        category: "Dungeon",
        subcategory: "General",
    })
    dungeonblessing = false;

    @SwitchProperty({
        name: "Dungeon Leap Announce",
        description: `Announce the party chat when leap`,
        category: "Dungeon",
        subcategory: "General",
    })
    leapannounce = false;

    @SwitchProperty({
        name: "Hide Player after Leap",
        description: `stop rendering player after leap for a few seconds`,
        category: "Dungeon",
        subcategory: "General",
    })
    leaphideplayer = false;

    @SwitchProperty({
        name: "Dungeon Mob Drop",
        description: `Detect mob drop such as &9Ice Spray Wand&r, &6Skeleton Master Chestplate&r`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    detectarmorstanddrop = false;

    ///

    @SwitchProperty({
        name: "Hide Falling Blocks",
        description: `Stop rendering Falling Blocks`,
        category: "Performance",
        subcategory: "General",
    })
    p5hidefallingBlock = false;

    @SwitchProperty({
        name: "Hide Firework Spark Particles",
        description: "Stop rendering FIREWORKS_SPARK particles",
        category: "Performance",
        subcategory: "General",
    })
    hidefireworksparkparticles = false;

    @SwitchProperty({
        name: "Hide Enchantment Table Particles",
        description: "Stop rendering ENCHANTMENT_TABLE particles",
        category: "Performance",
        subcategory: "General",
    })
    hideenchantmenttableparticles = false;

    @SwitchProperty({
        name: "Hide Heart Particles",
        description: "Stop rendering HEART particles",
        category: "Performance",
        subcategory: "General",
    })
    hideheartparticles = false;

    @SwitchProperty({
        name: "Hide Angry Villager Particles",
        description: "Stop rendering VILLAGER_ANGRY",
        category: "Performance",
        subcategory: "General",
    })
    hideangryvillagerparticles = false;

    @SwitchProperty({
        name: "Dungeon Wither BossBar",
        description: `bossbar stuff for wither bosses`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    witherbossbar = false;

    @TextProperty({
        name: "Dungeon Wither BossBar X",
        category: "Dungeon",
        subcategory: "Object Entity",
        description: "Position X of HUD",
        hidden: true
    })
    witherbossbar_pos_x = "5";

    @TextProperty({
        name: "Dungeon Wither BossBar Y",
        category: "Dungeon",
        subcategory: "Object Entity",
        description: "Position Y of HUD",
        hidden: true
    })
    witherbossbar_pos_y = "120";

    @TextProperty({
        name: "Dungeon Wither BossBar Scale",
        category: "Dungeon",
        subcategory: "Object Entity",
        description: "Scale of HUD",
        hidden: true
    })
    witherbossbar_scale = "1";

    ///

    @SwitchProperty({
        name: "Dungeon Key Highlight",
        description: `Highlight wither/blood key`,
        category: "Dungeon",
        subcategory: "Object Entity",
    })
    detectkeydrop = false;

    @SwitchProperty({
        name: "Phase 3 Start Timer",
        description: "Showing timer when goldor phase is about to start",
        category: "Dungeon",
        subcategory: "Goldor Phase",
    })
    p3timer = false;

    @SwitchProperty({
        name: "Position Message",
        description: "Announce in party when in the position",
        category: "Dungeon",
        subcategory: "Goldor Phase",
    })
    positionmessage = false;

    @SwitchProperty({
        name: "Rag Axe Alert",
        description: "Alert when to rag axe on M7",
        category: "Dungeon",
        subcategory: "M7 Dragon Phase",
    })
    dragphaseragtimer = false;

    @SwitchProperty({
        name: "Slow Simon Says",
        description: "Hit the emerald of Simon Says to announce 'Slow SS'",
        category: "Dungeon",
        subcategory: "Goldor Phase",
    })
    slowSS = false;

    @SwitchProperty({
        name: "Dungeon Chest Profit",
        description: "Show profit of dungeon chest items",
        category: "Dungeon",
        subcategory: "Dungeon Chest",
    })
    dungeonchestprofit = false;

    @SwitchProperty({
        name: "Dungeon Party Finder Addon",
        description: "Alert you when your party is ready to start dungeon",
        category: "Dungeon",
        subcategory: "Party Finder",
    })
    dungeonpfinder = false;

    @TextProperty({
        name: "Dungeon Party Finder Addon X",
        category: "Rift",
        subcategory: "Split Timer",
        description: "Position X of HUD",
        hidden: true
    })
    dungeonpfinder_pos_x = "5";

    @TextProperty({
        name: "Dungeon Party Finder Addon Y",
        category: "Rift",
        subcategory: "Split Timer",
        description: "Position Y of HUD",
        hidden: true
    })
    dungeonpfinder_pos_y = "120";

    @TextProperty({
        name: "Dungeon Party Finder Addon Scale",
        category: "Rift",
        subcategory: "Split Timer",
        description: "Scale of HUD",
        hidden: true
    })
    dungeonpfinder_scale = "1";

    @SwitchProperty({
        name: "Melody Terminal",
        description: "Show button while doing melody terminals on F7/M7",
        category: "Dungeon",
        subcategory: "Goldor Phase",
    })
    dungeonmelody = false;

    @SwitchProperty({
        name: "Melody Warning",
        description: "Alert you when teammate has melody terminal F7/M7",
        category: "Dungeon",
        subcategory: "Goldor Phase",
    })
    dungeonmelodywarning = false;

    @SwitchProperty({
        name: "Notify P3 Gate",
        description: "Alert if P3 gate isn't destroyed",
        category: "Dungeon",
        subcategory: "Goldor Phase",
    })
    notifyp3gate = false;

    @SwitchProperty({
        name: "Notify P3 Gate Show Title",
        description: "Show the title",
        category: "Dungeon",
        subcategory: "Goldor Phase",
    })
    notifyp3gate_showtitle = false;

    /////

    @SwitchProperty({
        name: "MaskTimer",
        category: "MaskTimer",
        subcategory: "MaskTimer",
        description: "MaskTimer Notify & HUD",
    })
    masktimer = false;

    @TextProperty({
        name: "EditingMaskTimer",
        category: "General",
        subcategory: "MaskTimer",
        hidden: true
    })
    masktimer_edit = false;

    @SwitchProperty({
        name: "MaskTimer Title",
        category: "MaskTimer",
        subcategory: "MaskTimer",
        description: "Show MaskTimer Title",
    })
    masktimer_showtitle = false;

    @TextProperty({
        name: "Position X",
        category: "MaskTimer",
        subcategory: "MaskTimer",
        description: "Position X of HUD",
        hidden: true
    })
    masktimer_pos_x = "5";

    @TextProperty({
        name: "Position Y",
        category: "MaskTimer",
        subcategory: "MaskTimer",
        description: "Position Y of HUD",
        hidden: true
    })
    masktimer_pos_y = "120";

    @TextProperty({
        name: "Scale",
        category: "MaskTimer",
        subcategory: "MaskTimer",
        description: "Scale of HUD",
        hidden: true
    })
    masktimer_scale = "1";

    /////

    /////

    @SwitchProperty({
        name: "InvincibleTimer",
        category: "MaskTimer",
        subcategory: "Invincible Timer",
        description: "InvincibleTimer Notify & HUD",
    })
    invincibletimer = false;

    @TextProperty({
        name: "EditingInvincibleTimer",
        category: "MaskTimer",
        subcategory: "Invincible Timer",
        hidden: true
    })
    invincibletimer_edit = false;

    @TextProperty({
        name: "Position X",
        category: "MaskTimer",
        subcategory: "Invincible Timer",
        description: "Position X of HUD",
        hidden: true
    })
    invincibletimer_pos_x = "5";

    @TextProperty({
        name: "Position Y",
        category: "MaskTimer",
        subcategory: "Invincible Timer",
        description: "Position Y of HUD",
        hidden: true
    })
    invincibletimer_pos_y = "120";

    @TextProperty({
        name: "Scale",
        category: "MaskTimer",
        subcategory: "Invincible Timer",
        description: "Scale of HUD",
        hidden: true
    })
    invincibletimer_scale = "1";

    /////

    /////

    @SwitchProperty({
        name: "Kuudra RunOverview",
        description: `Overview of Kuudra time`,
        category: "Kuudra",
        subcategory: "General",
    })
    kuudrarunoverview = false;

    @TextProperty({
        name: "Kuudra RunOverview X",
        category: "Kuudra",
        subcategory: "General",
        description: "Position X of HUD",
        hidden: true
    })
    kuudrarunoverview_pos_x = "5";

    @TextProperty({
        name: "Kuudra RunOverview Y",
        category: "Kuudra",
        subcategory: "General",
        description: "Position Y of HUD",
        hidden: true
    })
    kuudrarunoverview_pos_y = "120";

    @TextProperty({
        name: "Kuudra RunOverview Scale",
        category: "Kuudra",
        subcategory: "General",
        description: "Scale of HUD",
        hidden: true
    })
    kuudrarunoverview_scale = "1";

    /////

    @SwitchProperty({
        name: "Kuudra Chest Profit",
        description: "Show profit of kuudra chest items",
        category: "Kuudra",
        subcategory: "Kuudra Chest",
    })
    kuudrachestprofit = false;

    @SwitchProperty({
        name: "Kuudra Chest Open Announce",
        description: "Announce chest opened in party chat",
        category: "Kuudra",
        subcategory: "Kuudra Chest",
    })
    kuudrachestopen = false;

    @SwitchProperty({
        name: "Kuudra Key Price",
        description: "Calculate the price of each keys",
        category: "Kuudra",
        subcategory: "Kuudra Chest",
    })
    kuudrakeyprice = false;

    @SwitchProperty({
        name: "Kuudra Supplies Collected",
        description: "Show title supplies amount",
        category: "Kuudra",
        subcategory: "General",
    })
    kuudrasupplies = false;

    @TextProperty({
        name: "KuudraSupplies X",
        category: "General",
        subcategory: "General",
        description: "Position X of HUD",
        hidden: true
    })
    kuudrasupplies_pos_x = "5";

    @TextProperty({
        name: "KuudraSupplies Y",
        category: "General",
        subcategory: "General",
        description: "Position Y of HUD",
        hidden: true
    })
    kuudrasupplies_pos_y = "120";

    @TextProperty({
        name: "KuudraSupplies Scale",
        category: "General",
        subcategory: "General",
        description: "Scale of HUD",
        hidden: true
    })
    kuudrasupplies_scale = "1";

    /////

    @SwitchProperty({
        name: "Pet Overlay",
        category: "General",
        subcategory: "Pet Overlay",
        description: "Render Pet Overlay on screen",
    })
    petOverlay = false;

    @SwitchProperty({
        name: "Pet Title",
        category: "General",
        subcategory: "Pet Overlay",
        description: "Show title when pet changes",
    })
    petOverlay_title = false;

    @SwitchProperty({
        name: "Pet Alert Party",
        category: "General",
        subcategory: "Pet Overlay",
        description: "Send pet message to party chat",
    })
    petOverlay_chat = false;

    @TextProperty({
        name: "EditingPetOverlay",
        category: "General",
        subcategory: "Pet Overlay",
        hidden: true
    })
    petOverlay_edit = false;

    @TextProperty({
        name: "Pet X",
        category: "General",
        subcategory: "Pet Overlay",
        description: "Position X of HUD",
        hidden: true
    })
    petOverlay_pos_x = "5";

    @TextProperty({
        name: "Pet Y",
        category: "General",
        subcategory: "Pet Overlay",
        description: "Position Y of HUD",
        hidden: true
    })
    petOverlay_pos_y = "120";

    @TextProperty({
        name: "Pet Scale",
        category: "General",
        subcategory: "Pet Overlay",
        description: "Scale of HUD",
        hidden: true
    })
    petOverlay_scale = "1";

    /////

    constructor() {
        this.initialize(this)
        this.addDependency("MaskTimer Title", "MaskTimer")
        this.addDependency("Custom Death Message Text", "Dungeon Death Message")
        this.addDependency("Pet Title", "Pet Overlay")
        this.addDependency("Pet Alert Party", "Pet Overlay")
        this.addDependency("Notify P3 Gate Show Title", "Notify P3 Gate")
        this.addDependency("RNG Only", "Rare Drop Alert")
        this.addDependency("Dungeon Bat Highlight Line", "Dungeon Bat Highlight")
        this.addDependency("Dungeon Bat Highlight Color", "Dungeon Bat Highlight")
        this.addDependency("Dungeon Starred Mob Color", "Dungeon Starred Mob Highlight")
        this.addDependency("Dungeon Wither Lords Color", "Dungeon Wither Lords Highlight")
        this.addDependency("Dungeon Class Highlight Line", "Dungeon Class Highlight")
        this.addDependency("Dungeon Class Highlight Class", "Dungeon Class Highlight")
        this.addDependency("Hide Player after Leap", "Dungeon Leap Announce")
    }

}

export default new Settings();