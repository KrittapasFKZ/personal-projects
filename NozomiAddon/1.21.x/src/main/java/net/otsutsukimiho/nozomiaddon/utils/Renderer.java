package net.otsutsukimiho.nozomiaddon.utils;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import net.otsutsukimiho.nozomiaddon.features.RenderMode;

import org.joml.Vector3f;
import org.joml.Vector4f;

import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Renderer implements ClientModInitializer {
    private static Renderer instance;

    private static final BufferAllocator allocator = new BufferAllocator(786432);
    private BufferBuilder buffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private MappableRingBuffer vertexBuffer;

    public enum BoxStyle {FILLED, OUTLINE}

    public record RenderBox(Box box, BoxStyle style, float r, float g, float b, float a, float thickness) {}
    public record RenderLine(Vec3d start, Vec3d end, float r, float g, float b, float a) {}

    private static final List<RenderBox> BOXES = new ArrayList<>();
    private static final List<RenderLine> LINES = new ArrayList<>();

    public static void clearRenderQueue() {
        BOXES.clear();
        LINES.clear();
    }

    public static void addBox(Box box, float r, float g, float b) {
        String mode = RenderMode.renderType.getMode();
        float thickness = RenderMode.outlineThickness.getValue() * 0.05f;

        if (mode.equals("Filled")) {
            BOXES.add(new RenderBox(box, BoxStyle.FILLED, r, g, b, 0.25f, 0f));
        } else if (mode.equals("Outline")) {
            BOXES.add(new RenderBox(box, BoxStyle.OUTLINE, r, g, b, 1f, thickness));
        } else {
            BOXES.add(new RenderBox(box, BoxStyle.FILLED, r, g, b, 0.25f, 0f));
            BOXES.add(new RenderBox(box, BoxStyle.OUTLINE, r, g, b, 1f, thickness));
        }
    }

    public static void addLine(Vec3d start, Vec3d end, float r, float g, float b, float a) {
        LINES.add(new RenderLine(start, end, r, g, b, a));
    }

    public static void addTracer(Vec3d targetEnd, float r, float g, float b, float a) {
        LINES.add(new RenderLine(null, targetEnd, r, g, b, a));
    }

    public static Renderer getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        WorldRenderEvents.AFTER_ENTITIES.register(this::extractAndDrawWaypoint);
    }

    static final RenderPipeline FILLED_THROUGH_WALLS =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                            .withLocation(Identifier.of("nozomiaddon", "pipeline_no_depth"))
                            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                            .build()
            );

    static final RenderPipeline FILLED_WITH_DEPTH =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                            .withLocation(Identifier.of("nozomiaddon", "pipeline_with_depth"))
                            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                            .build()
            );

    static final RenderPipeline LINES_THROUGH_WALLS =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                            .withLocation(Identifier.of("nozomiaddon", "pipeline_lines_no_depth"))
                            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
                            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                            .build()
            );

    static final RenderPipeline LINES_WITH_DEPTH =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                            .withLocation(Identifier.of("nozomiaddon", "pipeline_lines_with_depth"))
                            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
                            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                            .build()
            );

    private void extractAndDrawWaypoint(WorldRenderContext context) {
        if (BOXES.isEmpty() && LINES.isEmpty()) return;
        renderWaypoint(context);
        clearRenderQueue();
    }

    private void renderWaypoint(WorldRenderContext context) {
        MatrixStack matrices = context.matrices();
        Vec3d camera = context.worldState().cameraRenderState.pos;

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (!BOXES.isEmpty()) {
            RenderPipeline pipeline = RenderMode.depthTest.isEnabled() ? FILLED_WITH_DEPTH : FILLED_THROUGH_WALLS;
            buffer = new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());

            for (RenderBox rb : BOXES) {
                Box box = rb.box();
                if (rb.style() == BoxStyle.FILLED) {
                    drawFilledBox(matrices, buffer, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, rb.r(), rb.g(), rb.b(), rb.a());
                } else {
                    drawThickOutline(matrices, buffer, box.expand(0.02), rb.thickness(), rb.r(), rb.g(), rb.b(), rb.a());
                }
            }
            drawFilledThroughWalls(MinecraftClient.getInstance(), pipeline);
        }

        if (!LINES.isEmpty()) {
            RenderPipeline linePipeline = RenderMode.depthTest.isEnabled() ? LINES_WITH_DEPTH : LINES_THROUGH_WALLS;
            buffer = new BufferBuilder(allocator, linePipeline.getVertexFormatMode(), linePipeline.getVertexFormat());
            MatrixStack.Entry entry = matrices.peek();

            Vec3d defaultStart = camera.add(0, -0.2, 0);

            for (RenderLine rl : LINES) {
                Vec3d start = (rl.start() != null) ? rl.start() : defaultStart;
                Vec3d end = rl.end();
                int color = ((int)(rl.a() * 255) << 24) | ((int)(rl.r() * 255) << 16) | ((int)(rl.g() * 255) << 8) | ((int)(rl.b() * 255));

                buffer.vertex(entry, (float) start.x, (float) start.y, (float) start.z).color(color);
                buffer.vertex(entry, (float) end.x, (float) end.y, (float) end.z).color(color);
            }
            drawFilledThroughWalls(MinecraftClient.getInstance(), linePipeline);
        }

        matrices.pop();
    }

    private void drawFilledThroughWalls(MinecraftClient client, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {
        BuiltBuffer builtBuffer = buffer.end();
        BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
        VertexFormat format = drawParameters.format();

        if (drawParameters.vertexCount() > 0) {
            GpuBuffer vertices = upload(drawParameters, format, builtBuffer);
            draw(client, pipeline, builtBuffer, drawParameters, vertices, format);
            vertexBuffer.rotate();
        } else {
            builtBuffer.close();
        }

        buffer = null;
    }

    private static void drawFilledBox(MatrixStack matrices, BufferBuilder buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        int color = ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);

        buffer.vertex(entry, minX, minY, minZ).color(color);
        buffer.vertex(entry, maxX, minY, minZ).color(color);
        buffer.vertex(entry, maxX, minY, maxZ).color(color);
        buffer.vertex(entry, minX, minY, maxZ).color(color);

        buffer.vertex(entry, minX, maxY, minZ).color(color);
        buffer.vertex(entry, minX, maxY, maxZ).color(color);
        buffer.vertex(entry, maxX, maxY, maxZ).color(color);
        buffer.vertex(entry, maxX, maxY, minZ).color(color);

        buffer.vertex(entry, minX, minY, minZ).color(color);
        buffer.vertex(entry, minX, maxY, minZ).color(color);
        buffer.vertex(entry, maxX, maxY, minZ).color(color);
        buffer.vertex(entry, maxX, minY, minZ).color(color);

        buffer.vertex(entry, minX, minY, maxZ).color(color);
        buffer.vertex(entry, maxX, minY, maxZ).color(color);
        buffer.vertex(entry, maxX, maxY, maxZ).color(color);
        buffer.vertex(entry, minX, maxY, maxZ).color(color);

        buffer.vertex(entry, minX, minY, minZ).color(color);
        buffer.vertex(entry, minX, minY, maxZ).color(color);
        buffer.vertex(entry, minX, maxY, maxZ).color(color);
        buffer.vertex(entry, minX, maxY, minZ).color(color);

        buffer.vertex(entry, maxX, minY, minZ).color(color);
        buffer.vertex(entry, maxX, maxY, minZ).color(color);
        buffer.vertex(entry, maxX, maxY, maxZ).color(color);
        buffer.vertex(entry, maxX, minY, maxZ).color(color);
    }

    private void drawThickOutline(MatrixStack matrices, BufferBuilder buffer, Box box, float thickness, float r, float g, float b, float a) {
        float t = thickness / 2.0f;

        float minX = (float) box.minX; float minY = (float) box.minY; float minZ = (float) box.minZ;
        float maxX = (float) box.maxX; float maxY = (float) box.maxY; float maxZ = (float) box.maxZ;

        drawFilledBox(matrices, buffer, minX - t, minY - t, minZ - t, maxX + t, minY + t, minZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, minX - t, minY - t, maxZ - t, maxX + t, minY + t, maxZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, minX - t, minY - t, minZ - t, minX + t, minY + t, maxZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, maxX - t, minY - t, minZ - t, maxX + t, minY + t, maxZ + t, r, g, b, a);

        drawFilledBox(matrices, buffer, minX - t, maxY - t, minZ - t, maxX + t, maxY + t, minZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, minX - t, maxY - t, maxZ - t, maxX + t, maxY + t, maxZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, minX - t, maxY - t, minZ - t, minX + t, maxY + t, maxZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, maxX - t, maxY - t, minZ - t, maxX + t, maxY + t, maxZ + t, r, g, b, a);

        drawFilledBox(matrices, buffer, minX - t, minY - t, minZ - t, minX + t, maxY + t, minZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, maxX - t, minY - t, minZ - t, maxX + t, maxY + t, minZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, minX - t, minY - t, maxZ - t, minX + t, maxY + t, maxZ + t, r, g, b, a);
        drawFilledBox(matrices, buffer, maxX - t, minY - t, maxZ - t, maxX + t, maxY + t, maxZ + t, r, g, b, a);
    }

    private GpuBuffer upload(BuiltBuffer.DrawParameters drawParameters, VertexFormat format, BuiltBuffer builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();
        int requiredSize = vertexBufferSize * 4;

        if (vertexBuffer == null || vertexBuffer.size() < requiredSize) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(() -> "nozomiaddon" + " render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, requiredSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
        }

        return vertexBuffer.getBlocking();
    }

    private static void draw(MinecraftClient client, RenderPipeline pipeline, BuiltBuffer builtBuffer, BuiltBuffer.DrawParameters drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.DrawMode.QUADS) {
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().getVertexSorter());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.getSortedBuffer());
            indexType = builtBuffer.getDrawParameters().indexType();
        } else {
            RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getIndexBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.getIndexType();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, new Vector3f(), new org.joml.Matrix4f().identity());
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "nozomiaddon" + " pipeline rendering", client.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), client.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        allocator.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}