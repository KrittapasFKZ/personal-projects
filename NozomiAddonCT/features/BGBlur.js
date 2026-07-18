import Settings from "../config";
import { registerWhen } from "../utils/utils";

const ResourceLocation = Java.type('net.minecraft.util.ResourceLocation')
let applied = false

registerWhen(register('guiClosed', () => {
    if (!Settings.backgroundBlur) return
    if (!applied) return
    Client.getMinecraft().field_71460_t.func_181022_b()
    applied = false
}), () => Settings.backgroundBlur);

registerWhen(register('postGuiRender', (mx, my, gui) => {
    if (!Settings.backgroundBlur) return
    if (applied) return
    if (gui.class == 'class net.optifine.gui.GuiChatOF' || gui.class == 'class gg.essential.vigilance.gui.SettingsGui') return
    Client.getMinecraft().field_71460_t.func_181022_b()
    Client.getMinecraft().field_71460_t.func_175069_a(new ResourceLocation('shaders/post/blur.json'))
    applied = true
}), () => Settings.backgroundBlur);