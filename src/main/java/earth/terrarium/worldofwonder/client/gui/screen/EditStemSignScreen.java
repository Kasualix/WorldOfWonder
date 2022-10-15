package earth.terrarium.worldofwonder.client.gui.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import earth.terrarium.worldofwonder.WorldOfWonder;
import earth.terrarium.worldofwonder.block.StemStandingSignBlock;
import earth.terrarium.worldofwonder.client.renderer.tileentity.StemSignTileEntityRenderer;
import earth.terrarium.worldofwonder.network.UpdateSignPacket;
import earth.terrarium.worldofwonder.tileentity.StemSignTileEntity;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.stream.IntStream;

public class EditStemSignScreen extends Screen {
    private final SignRenderer.SignModel signModel = new SignRenderer.SignModel();
    private final StemSignTileEntity tileSign;
    private int updateCounter;
    private int editLine;
    private TextFieldHelper textInputUtil;
    private final String[] text;

    public EditStemSignScreen(StemSignTileEntity teSign) {
        super(new TranslatableComponent("sign.edit"));
        this.text = IntStream.range(0, 4).mapToObj(teSign::getText).map(Component::getString).toArray(String[]::new);
        this.tileSign = teSign;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, new TranslatableComponent("gui.done"), p_214266_1_ -> this.close()));
        this.tileSign.setEditable(false);
        this.textInputUtil = new TextFieldHelper(() -> this.text[this.editLine], text -> {
            this.text[this.editLine] = text;
            this.tileSign.setText(this.editLine, new TextComponent(text));
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), text -> this.minecraft.font.width(text) <= 90);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        WorldOfWonder.NETWORK.sendToServer(new UpdateSignPacket(this.tileSign.getBlockPos(), this.tileSign.signText));
        this.tileSign.setEditable(true);
    }

    @Override
    public void tick() {
        ++this.updateCounter;
        if (!this.tileSign.getType().isValid(this.tileSign.getBlockState().getBlock())) {
            this.close();
        }
    }

    private void close() {
        this.tileSign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        this.textInputUtil.charTyped(p_charTyped_1_);
        return true;
    }

    @Override
    public void onClose() {
        this.close();
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 265) {
            this.editLine = this.editLine - 1 & 3;
            this.textInputUtil.setCursorToEnd();
            return true;
        } else if (p_keyPressed_1_ != 264 && p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
            return this.textInputUtil.keyPressed(p_keyPressed_1_) || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        } else {
            this.editLine = this.editLine + 1 & 3;
            this.textInputUtil.setCursorToEnd();
            return true;
        }
    }

    @Override
    public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        Lighting.setupForFlatItems();
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        matrixStack.pushPose();
        matrixStack.translate((this.width / 2.0), 0.0D, 50.0D);
        float f = 93.75F;
        matrixStack.scale(f, -f, f);
        matrixStack.translate(0.0D, -1.3125D, 0.0D);
        BlockState blockstate = this.tileSign.getBlockState();
        boolean flag = blockstate.getBlock() instanceof StemStandingSignBlock;
        if (!flag) {
            matrixStack.translate(0.0D, -0.3125D, 0.0D);
        }

        boolean flag1 = this.updateCounter / 6 % 2 == 0;
        float f1 = 2f / 3f;
        matrixStack.pushPose();
        matrixStack.scale(f1, -f1, -f1);
        MultiBufferSource.BufferSource irendertypebuffer$impl = this.minecraft.renderBuffers().bufferSource();
        VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(this.signModel.renderType(StemSignTileEntityRenderer.TEXTURE));
        this.signModel.sign.render(matrixStack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY);
        if (flag) {
            this.signModel.stick.render(matrixStack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY);
        }

        matrixStack.popPose();
        float f2 = 0.010416667F;
        matrixStack.translate(0.0D, 0.33333334, 0.046666667);
        matrixStack.scale(f2, -f2, f2);
        int i = this.tileSign.getTextColor().getTextColor();
        int j = this.textInputUtil.getCursorPos();
        int k = this.textInputUtil.getSelectionPos();
        int l = this.editLine * 10 - this.text.length * 5;
        Matrix4f matrix4f = matrixStack.last().pose();

        for(int i1 = 0; i1 < this.text.length; ++i1) {
            String s = this.text[i1];
            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }

                float f3 = (float)(-this.minecraft.font.width(s) / 2);
                this.minecraft.font.drawInBatch(s, f3, (float)(i1 * 10 - this.text.length * 5), i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880, false);
                if (i1 == this.editLine && j >= 0 && flag1) {
                    int j1 = this.minecraft.font.width(s.substring(0, Math.min(j, s.length())));
                    int k1 = j1 - this.minecraft.font.width(s) / 2;
                    if (j >= s.length()) {
                        this.minecraft.font.drawInBatch("_", (float)k1, (float)l, i, false, matrix4f, irendertypebuffer$impl, false, 0, 15728880, false);
                    }
                }
            }
        }

        irendertypebuffer$impl.endBatch();

        for (int k3 = 0; k3 < text.length; ++k3) {
            String s1 = text[k3];
            if (s1 != null && k3 == this.editLine && k >= 0) {
                int l3 = this.minecraft.font.width(s1.substring(0, Math.min(k, s1.length())));
                int i4 = l3 - this.minecraft.font.width(s1) / 2;
                if (flag1 && k < s1.length()) {
                    fill(matrixStack, i4, l - 1, i4 + 1, l + 9, -16777216 | i);
                }

                if (j != k) {
                    int j4 = Math.min(k, j);
                    int j2 = Math.max(k, j);
                    int k2 = this.minecraft.font.width(s1.substring(0, j4)) - this.minecraft.font.width(s1) / 2;
                    int l2 = this.minecraft.font.width(s1.substring(0, j2)) - this.minecraft.font.width(s1) / 2;
                    int i3 = Math.min(k2, l2);
                    int j3 = Math.max(k2, l2);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferbuilder = tesselator.getBuilder();
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    bufferbuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
                    bufferbuilder.vertex(matrix4f, (float) i3, (float) (l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) j3, (float) (l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) j3, (float) l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) i3, (float) l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.end();
                    BufferUploader.end(bufferbuilder);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        matrixStack.popPose();
        Lighting.setupFor3DItems();
        super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
    }
}