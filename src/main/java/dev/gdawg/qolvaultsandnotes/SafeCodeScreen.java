package dev.gdawg.qolvaultsandnotes;
import dev.gdawg.qolvaultsandnotes.QOLVaultsAndNotes;
import dev.gdawg.qolvaultsandnotes.SafeCodePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class SafeCodeScreen extends Screen {

    private static final Identifier GUI_PINCODE = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/container/safe_pincode.png");
    private static final Identifier GUI_SERIAL = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/container/safe_serialcode.png");

    private final BlockPos blockPos;
    private final boolean isKeycard; // true = serial screen, false = pincode screen
    private String enteredCode = "";

    private static final int IMAGE_WIDTH  = 256;
    private static final int IMAGE_HEIGHT = 256;
    private int leftPos;
    private int topPos;

    public SafeCodeScreen(BlockPos blockPos, boolean isKeycard) {
        super(Component.translatable("screen.qolvaultsandnotes.safe_code"));
        this.blockPos = blockPos;
        this.isKeycard = isKeycard;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width  - IMAGE_WIDTH)  / 2;
        this.topPos  = (this.height - IMAGE_HEIGHT) / 2;

        if (isKeycard) {
            initSerialLayout();
        } else {
            initPincodeLayout();
        }
    }

    private void initPincodeLayout() {
        // Number buttons 1-9
        for (int i = 1; i <= 9; i++) {
            final int digit = i;
            int col = (i - 1) % 3;
            int row = (i - 1) / 3;
            addRenderableWidget(Button.builder(
                    Component.literal(String.valueOf(digit)),
                    btn -> enteredCode += digit
            ).bounds(leftPos + 20 + col * 24, topPos + 50 + row * 24, 20, 20).build());
        }

        // 0 button
        addRenderableWidget(Button.builder(
                Component.literal("0"),
                btn -> enteredCode += "0"
        ).bounds(leftPos + 44, topPos + 122, 20, 20).build());

        // Backspace
        addRenderableWidget(Button.builder(
                Component.literal("<"),
                btn -> {
                    if (!enteredCode.isEmpty())
                        enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
                }
        ).bounds(leftPos + 20, topPos + 122, 20, 20).build());

        // Confirm
        addRenderableWidget(Button.builder(
                Component.literal("OK"),
                btn -> onConfirmPressed()
        ).bounds(leftPos + 68, topPos + 122, 30, 20).build());
    }

    private void initSerialLayout() {
        // Serial code uses a text field instead of number buttons
        // since keycard codes have no length limit and can be any characters
        net.minecraft.client.gui.components.EditBox codeField =
                new net.minecraft.client.gui.components.EditBox(
                        this.font,
                        leftPos + 20, topPos + 60,
                        IMAGE_WIDTH - 40, 20,
                        Component.translatable("screen.qolvaultsandnotes.safe_code")
                );
        codeField.setResponder(text -> enteredCode = text);
        addRenderableWidget(codeField);

        // Confirm
        addRenderableWidget(Button.builder(
                Component.literal("OK"),
                btn -> onConfirmPressed()
        ).bounds(leftPos + IMAGE_WIDTH / 2 - 15, topPos + 100, 30, 20).build());
    }

    private void onConfirmPressed() {
        ClientPacketDistributor.sendToServer(new SafeCodePacket(blockPos, enteredCode));
        this.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                isKeycard ? GUI_SERIAL : GUI_PINCODE,
                leftPos, topPos,
                0.0f, 0.0f,
                IMAGE_WIDTH, IMAGE_HEIGHT,
                256, 256
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // Only draw the entered code display for pincode — serial uses EditBox
        if (!isKeycard) {
            guiGraphics.drawString(this.font,
                    Component.literal(enteredCode),
                    leftPos + 20, topPos + 30,
                    0x404020, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}