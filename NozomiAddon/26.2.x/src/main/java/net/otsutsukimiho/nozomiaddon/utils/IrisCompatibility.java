package net.otsutsukimiho.nozomiaddon.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.loader.api.FabricLoader;

public class IrisCompatibility {
    private static final boolean IS_IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    public enum ShaderType {
        LINES, BASIC
    }

    public static void registerPipeline(RenderPipeline pipeline, ShaderType type) {
        if (IS_IRIS_LOADED) {
            IrisCompatImpl.register(pipeline, type);
        }
    }

    private static class IrisCompatImpl {
        static void register(RenderPipeline pipeline, ShaderType type) {
            try {
                net.irisshaders.iris.api.v0.IrisApi irisApi = net.irisshaders.iris.api.v0.IrisApi.getInstance();

                net.irisshaders.iris.api.v0.IrisProgram program = (type == ShaderType.LINES) ?
                        net.irisshaders.iris.api.v0.IrisProgram.LINES :
                        net.irisshaders.iris.api.v0.IrisProgram.BASIC;

                irisApi.assignPipeline(pipeline, program);
            } catch (Exception e) {
                System.err.println("[NozomiAddon] Failed to register Iris Pipeline: " + e.getMessage());
            }
        }
    }
}