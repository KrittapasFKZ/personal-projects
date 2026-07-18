package net.otsutsukimiho.nozomiaddon.utils;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.otsutsukimiho.nozomiaddon.features.RenderMode;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class Renderer implements ClientModInitializer {
    private static Renderer instance;

    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(786432);
    private BufferBuilder buffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private MappableRingBuffer vertexBuffer;

    public enum BoxStyle {FILLED, OUTLINE}

    public record RenderBox(AABB box, BoxStyle style, float r, float g, float b, float a, float thickness) {}
    public record RenderLine(Vec3 start, Vec3 end, float r, float g, float b, float a) {}

    private static final List<RenderBox> BOXES = new ArrayList<>();
    private static final List<RenderLine> LINES = new ArrayList<>();

    public static void clearRenderQueue() {
        BOXES.clear();
        LINES.clear();
    }

    public static void addBox(AABB box, float r, float g, float b) {
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

    public static void addLine(Vec3 start, Vec3 end, float r, float g, float b, float a) {
        LINES.add(new RenderLine(start, end, r, g, b, a));
    }

    public static void addTracer(Vec3 targetEnd, float r, float g, float b, float a) {
        LINES.add(new RenderLine(null, targetEnd, r, g, b, a));
    }

    public static Renderer getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::extractAndDrawWaypoint);

        IrisCompatibility.registerPipeline(FILLED_THROUGH_WALLS, IrisCompatibility.ShaderType.BASIC);
        IrisCompatibility.registerPipeline(FILLED_WITH_DEPTH, IrisCompatibility.ShaderType.BASIC);
        IrisCompatibility.registerPipeline(LINES_THROUGH_WALLS, IrisCompatibility.ShaderType.LINES);
        IrisCompatibility.registerPipeline(LINES_WITH_DEPTH, IrisCompatibility.ShaderType.LINES);
    }

    static final RenderPipeline FILLED_THROUGH_WALLS =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath("nozomiaddon", "pipeline_no_depth"))
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                            .withDepthStencilState(Optional.empty())
                            .build()
            );

    static final RenderPipeline FILLED_WITH_DEPTH =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath("nozomiaddon", "pipeline_with_depth"))
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                            .build()
            );

    static final RenderPipeline LINES_THROUGH_WALLS =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath("nozomiaddon", "pipeline_lines_no_depth"))
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                            .withPrimitiveTopology(PrimitiveTopology.LINES)
                            .withDepthStencilState(Optional.empty())
                            .build()
            );

    static final RenderPipeline LINES_WITH_DEPTH =
            RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath("nozomiaddon", "pipeline_lines_with_depth"))
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                            .withPrimitiveTopology(PrimitiveTopology.LINES)
                            .build()
            );

    private void extractAndDrawWaypoint(LevelRenderContext context) {
        if (BOXES.isEmpty() && LINES.isEmpty()) return;
        renderWaypoint(context);
        clearRenderQueue();
    }

    private void renderWaypoint(LevelRenderContext context) {
        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (!BOXES.isEmpty()) {
            RenderPipeline pipeline = RenderMode.depthTest.isEnabled() ? FILLED_WITH_DEPTH : FILLED_THROUGH_WALLS;
            buffer = new BufferBuilder(allocator, pipeline.getPrimitiveTopology(), Objects.requireNonNull(pipeline.getVertexFormatBinding(0)));

            for (RenderBox rb : BOXES) {
                AABB box = rb.box();
                if (rb.style() == BoxStyle.FILLED) {
                    drawFilledBox(matrices, buffer, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, rb.r(), rb.g(), rb.b(), rb.a());
                } else {
                    drawThickOutline(matrices, buffer, box.inflate(0.02), rb.thickness(), rb.r(), rb.g(), rb.b(), rb.a());
                }
            }
            drawFilledThroughWalls(Minecraft.getInstance(), pipeline, matrices);
        }

        if (!LINES.isEmpty()) {
            RenderPipeline linePipeline = RenderMode.depthTest.isEnabled() ? LINES_WITH_DEPTH : LINES_THROUGH_WALLS;
            buffer = new BufferBuilder(allocator, linePipeline.getPrimitiveTopology(), Objects.requireNonNull(linePipeline.getVertexFormatBinding(0)));
            PoseStack.Pose entry = matrices.last();

            Vec3 defaultStart = camera.add(0, -0.2, 0);

            for (RenderLine rl : LINES) {
                Vec3 start = (rl.start() != null) ? rl.start() : defaultStart;
                Vec3 end = rl.end();
                int color = ((int)(rl.a() * 255) << 24) | ((int)(rl.r() * 255) << 16) | ((int)(rl.g() * 255) << 8) | ((int)(rl.b() * 255));

                buffer.addVertex(entry, (float) start.x, (float) start.y, (float) start.z).setColor(color);
                buffer.addVertex(entry, (float) end.x, (float) end.y, (float) end.z).setColor(color);
            }
            drawFilledThroughWalls(Minecraft.getInstance(), linePipeline, matrices);
        }

        matrices.popPose();
    }

    private void drawFilledThroughWalls(Minecraft client, RenderPipeline pipeline, PoseStack matrices) {
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBufferSlice vertices = upload(drawParameters, format, builtBuffer);

        draw(client, pipeline, builtBuffer, drawParameters, vertices, format, matrices);

        vertexBuffer.rotate();
        buffer = null;
    }

    private static void addQuad(PoseStack.Pose entry, BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int color) {
        buffer.addVertex(entry, x1, y1, z1).setColor(color);
        buffer.addVertex(entry, x2, y2, z2).setColor(color);
        buffer.addVertex(entry, x3, y3, z3).setColor(color);
        buffer.addVertex(entry, x1, y1, z1).setColor(color);
        buffer.addVertex(entry, x3, y3, z3).setColor(color);
        buffer.addVertex(entry, x4, y4, z4).setColor(color);
    }

    private static void drawFilledBox(PoseStack matrices, BufferBuilder buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        PoseStack.Pose entry = matrices.last();
        int color = ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);

        addQuad(entry, buffer, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, color);
        addQuad(entry, buffer, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
        addQuad(entry, buffer, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, color);
        addQuad(entry, buffer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        addQuad(entry, buffer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, color);
        addQuad(entry, buffer, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
    }

    private void drawThickOutline(PoseStack matrices, BufferBuilder buffer, AABB box, float thickness, float r, float g, float b, float a) {
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

    private GpuBufferSlice upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();
        int requiredSize = vertexBufferSize * 4;

        if (vertexBuffer == null || vertexBuffer.size() < requiredSize) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(() -> "nozomiaddon render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_COPY_DST, requiredSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBufferSlice slice = vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining());

        commandEncoder.writeToBuffer(slice, builtBuffer.vertexBuffer());
        return slice;
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBufferSlice vertices, VertexFormat format, PoseStack matrices) {
        GpuBuffer indices;
        var IndexType = drawParameters.indexType();

        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getPrimitiveTopology());
        indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, new Vector3f(), new org.joml.Matrix4f().identity());
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "nozomiaddon" + " pipeline rendering", client.gameRenderer.mainRenderTarget().getColorTextureView(), Optional.empty(), client.gameRenderer.mainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, IndexType);

            renderPass.drawIndexed(drawParameters.indexCount(), 1,0, 0, 0);
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