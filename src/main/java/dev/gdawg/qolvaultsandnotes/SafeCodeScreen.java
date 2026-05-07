package dev.gdawg.qolvaultsandnotes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class SafeCodeScreen extends Screen {

    private static final Identifier GUI_PINCODE = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/container/pincode_screen.png");
    private static final Identifier GUI_SERIAL = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/container/serialcode_screen.png");
    private static final Identifier SEGMENT_ACTIVE = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/sprites/serial_code_screen/xp_filled.png");

    private final BlockPos blockPos;
    private final boolean isKeycard; // false = key = serial screen, true = keycard = pincode screen
    private String enteredCode = "";

    // Serial layout segment state — 18 toggleable segments
    private final boolean[] segments = new boolean[18];

    private static int IMAGE_WIDTH  = 180;
    private static int IMAGE_HEIGHT = 211;
    private int leftPos;
    private int topPos;

    // Segment dimensions
    private static final int SEGMENT_WIDTH  = 11;
    private static final int SEGMENT_HEIGHT = 5;
    private static final int SEGMENT_GAP    = -1;
    private static final int SEGMENT_START_X = 37;
    private static final int SEGMENT_START_Y = 51;

    public SafeCodeScreen(BlockPos blockPos, boolean isKeycard) {
        super(Component.translatable("container.qolvaultsandnotes.safe_code"));
        this.blockPos = blockPos;
        this.isKeycard = isKeycard;
    }

    @Override
    protected void init() {
        if (!isKeycard) {
            initSerialLayout();
        } else {
            initPincodeLayout();
        }
    }

    private void initSerialLayout() {
        IMAGE_WIDTH = 256;
        IMAGE_HEIGHT = 95;
        this.leftPos = (this.width  - IMAGE_WIDTH)  / 2;
        this.topPos  = (this.height - IMAGE_HEIGHT) / 2;

        // 18 segments are rendered and clicked manually — see render() and mouseClicked()
        // Only a confirm button is added as a widget
        addRenderableWidget(Button.builder(
                Component.literal("OK"),
                btn -> onConfirmPressed()
        ).bounds(leftPos + IMAGE_WIDTH / 2 - 15, topPos + 160, 30, 20).build());

    }

    private void initPincodeLayout() {
        IMAGE_WIDTH = 180;
        IMAGE_HEIGHT = 211;
        this.leftPos = (this.width  - IMAGE_WIDTH)  / 2;
        this.topPos  = (this.height - IMAGE_HEIGHT) / 2;

        for (int i = 1; i <= 9; i++) {
            final int digit = i;
            int col = (i - 1) % 3;
            int row = (i - 1) / 3;
            addRenderableWidget(Button.builder(
                    Component.literal(String.valueOf(digit)),
                    btn -> enteredCode += digit
            ).bounds(leftPos + 20 + col * 24, topPos + 50 + row * 24, 20, 20).build());
        }

        addRenderableWidget(Button.builder(
                Component.literal("0"),
                btn -> enteredCode += "0"
        ).bounds(leftPos + 44, topPos + 122, 20, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("<"),
                btn -> {
                    if (!enteredCode.isEmpty())
                        enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
                }
        ).bounds(leftPos + 20, topPos + 122, 20, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("x"),
                btn -> enteredCode = ""
        ).bounds(leftPos + 20, topPos + 140, 20, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("OK"),
                btn -> onConfirmPressed()
        ).bounds(leftPos + 68, topPos + 122, 30, 20).build());
    }

    private void onConfirmPressed() {
        String code;
        if (!isKeycard) {
            // Build dot/dash string from segment states
            StringBuilder sb = new StringBuilder();
            for (boolean segment : segments) {
                sb.append(segment ? '.' : '-');
            }
            code = sb.toString();
        } else {
            code = enteredCode;
        }
        ClientPacketDistributor.sendToServer(new SafeCodePacket(blockPos, code));

        this.onClose();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (!isKeycard) {
            for (int i = 0; i < 18; i++) {
                int segX = leftPos + SEGMENT_START_X + i * (SEGMENT_WIDTH + SEGMENT_GAP);
                int segY = topPos + SEGMENT_START_Y;
                if(i > 8) segX++;
                if (mouseX >= segX && mouseX < segX + SEGMENT_WIDTH
                        && mouseY >= segY && mouseY < segY + SEGMENT_HEIGHT) {
                    segments[i] = !segments[i]; // toggle
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        // isKeycard false = key = serial screen, isKeycard true = keycard = pincode screen
        if(isKeycard) topPos = (this.width - 128) / 2;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                isKeycard ? GUI_PINCODE : GUI_SERIAL,
                leftPos, topPos,
                0.0f, 0.0f,
                IMAGE_WIDTH, IMAGE_HEIGHT,
                256, 256
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (!isKeycard) {
            // Draw the 18 segments for serial layout
            for (int i = 0; i < 18; i++) {
                int segX = leftPos + SEGMENT_START_X + i * (SEGMENT_WIDTH + SEGMENT_GAP);
                int segY = topPos + SEGMENT_START_Y;
                if(i > 8) segX++;
                if (segments[i]) {
                    // Draw filled segment using active_small texture
                    guiGraphics.blit(
                            RenderPipelines.GUI_TEXTURED,
                            SEGMENT_ACTIVE,
                            segX, segY,
                            0.0f, 0.0f,
                            SEGMENT_WIDTH, SEGMENT_HEIGHT,
                            SEGMENT_WIDTH, SEGMENT_HEIGHT
                    );
                }
                // Unfilled segments show through from the background PNG
            }
        } else {
            // Draw entered digits for pincode screen
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