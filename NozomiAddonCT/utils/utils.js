export const GuiContainer = Java.type("net.minecraft.client.gui.inventory.GuiContainer")
export const MCItemStack = Java.type("net.minecraft.item.ItemStack")
export const BossStatus = Java.type("net.minecraft.entity.boss.BossStatus")

const guiContainerLeftField = GuiContainer.class.getDeclaredField("field_147003_i")
const guiContainerTopField = GuiContainer.class.getDeclaredField("field_147009_r")
guiContainerLeftField.setAccessible(true)
guiContainerTopField.setAccessible(true)

/**
 * 
 * @param {Number} slotNumber 
 * @param {GuiContainer} mcGuiContainer
 * @returns {[Number, Number]}
 */
export const getSlotRenderPosition = (slotNumber, mcGuiContainer) => {
    const guiLeft = guiContainerLeftField.get(mcGuiContainer)
    const guiTop = guiContainerTopField.get(mcGuiContainer)

    const slot = mcGuiContainer.field_147002_h.func_75139_a(slotNumber)

    return [guiLeft + slot.field_75223_e, guiTop + slot.field_75221_f]
}

/**
 * 
 * @param {GuiContainer} gui - The GuiContainer to render inside of
 * @param {Number} slotIndex - The slot index 
 * @param {Number} r - 0-1
 * @param {Number} g - 0-1
 * @param {Number} b - 0-1 
 * @param {Number} a - 0-1
 * @param {Boolean} aboveItem - Hightlight in front of the item in the slot
 * @param {Number} z - The z position for the highlight to be rendered. Will override the aboveItem parameter if used.
 */
export const highlightSlot = (gui, slotIndex, r, g, b, a, aboveItem = false, z = null) => {
    if (!(gui instanceof GuiContainer)) return

    const [x, y] = getSlotRenderPosition(slotIndex, gui)

    let zPosition = 245
    if (aboveItem) zPosition = 241
    if (z !== null) zPosition = z 

    Renderer.translate(x, y, zPosition)
    Renderer.drawRect(Renderer.color(r * 255, g * 255, b * 255, a * 255), 0, 0, 16, 16)
    Renderer.finishDraw()
}

const checkingTriggers = [] // [[trigger, func]]
/**
 * Registers and unregisters the trigger depending on the result of the checkFunc. Use with render triggers to reduce lag when they are not being used.
 * @param {() => void} trigger 
 * @param {Function} checkFunc 
 * @returns 
 */
export const registerWhen = (trigger, checkFunc) => checkingTriggers.push([trigger.unregister(), checkFunc])

register("tick", () => {
    for (let i = 0; i < checkingTriggers.length; i++) {
        let [trigger, func] = checkingTriggers[i]
        if (func()) trigger.register()
        else trigger.unregister()
    }; 
});

/**
 * Gets the texture property of the skull item.
 * @param {Item} skullItem 
 * @returns {String|null}
 */
export const getSkullTexture = (skullItem) => {
    if (skullItem instanceof MCItemStack) skullItem = new Item(skullItem)
    if (!(skullItem instanceof Item)) return null
    const textures = skullItem.getNBT()?.toObject()?.tag?.SkullOwner?.Properties?.textures
    if (!textures || !textures.length) return
    return textures[0].Value
}

export function isPlayerInArea(x1, x2, y1, y2, z1, z2, entity = Player) {
    const x = entity.getX();
    const y = entity.getY();
    const z = entity.getZ();
    return (
        x >= Math.min(x1, x2) &&
        x <= Math.max(x1, x2) &&
        y >= Math.min(y1, y2) &&
        y <= Math.max(y1, y2) &&
        z >= Math.min(z1, z2) &&
        z <= Math.max(z1, z2)
    );
}

export const getSecs = (ms) => !ms ? "0s" : Math.floor(ms / 10) / 100 + "s"
export const getTime = (ms) => !ms ? "?" : Math.floor(ms / 60000) !== 0 ? `${Math.floor(ms / 60000)}m ${Math.floor(ms / 1000) % 60}s` : `${Math.floor(ms / 1000) % 60}s`
export const stripRank = (rankedPlayer) => rankedPlayer.replace(/\[[\w+\+-]+] /, "").trim()