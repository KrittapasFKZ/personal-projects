import Settings from "../config";
import { registerWhen } from "../utils/utils";

const RenderFogEvent = net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity
var GL11 = Java.type("org.lwjgl.opengl.GL11");

registerWhen(register(RenderFogEvent, (event) => {
    event.setCanceled(true);
    GL11.glFogf(GL11.GL_FOG_START, 998);
    GL11.glFogf(GL11.GL_FOG_END, 999);
    GL11.glFogf(GL11.GL_FOG_DENSITY, 0);
}), () => Settings.nodebuff_blindness);