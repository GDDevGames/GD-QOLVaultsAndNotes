/// ----- SafeCodeScreen -----
/// Code for the PIN code GUI.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
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
    private static final Identifier KEY_ = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/sprites/serial_code_screen/key.png");
    private static final Identifier ARROW_BUTTON = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/sprites/pin_code_screen/arrow.png");
    private static final Identifier CLEAR_BUTTON = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/sprites/pin_code_screen/x.png");
    private static final Identifier OK_BUTTON = Identifier.fromNamespaceAndPath(
            QOLVaultsAndNotes.MODID, "textures/gui/sprites/pin_code_screen/confirm.png");

    private final BlockPos blockPos;
    private final boolean isKeycard;
    // False = key = serial screen
    // True = keycard = PIN screen
    private String enteredCode = "";

    // Amount of segments in the serial code
    private final boolean[] segments = new boolean[18];

    private static int IMAGE_WIDTH  = 180;
    private static int IMAGE_HEIGHT = 211;
    private int leftPos;
    private int topPos;

    private static final int SEGMENT_WIDTH  = 11;
    private static final int SEGMENT_HEIGHT = 5;
    private static final int SEGMENT_GAP    = -1;
    private static final int SEGMENT_START_X = 37;
    private static final int SEGMENT_START_Y = 51;

    private static int TITLE_START_X;
    private static int TITLE_START_Y;

    // --- CONSTRUCTOR ---
    public SafeCodeScreen(BlockPos blockPos, boolean isKeycard) {
        super(Component.translatable("container.qolvaultsandnotes.safe"));
        this.blockPos = blockPos;
        this.isKeycard = isKeycard;
    }

    @Override
    protected void init() {
        TITLE_START_X = width / 2;
        if (!isKeycard) {
            initSerialLayout();
        } else {
            initPincodeLayout();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        var key = InputConstants.getKey(keyEvent);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    private void initSerialLayout() {
        IMAGE_WIDTH = 256;
        IMAGE_HEIGHT = 95;
        this.leftPos = (this.width  - IMAGE_WIDTH)  / 2;
        this.topPos  = (this.height - IMAGE_HEIGHT) / 2;

        TITLE_START_Y = topPos + 20;

        // 18 segments are rendered and clicked manually in render() and mouseClicked()
        // Only the confirm button is added as a widget

        addRenderableWidget(new AbstractButton(leftPos + IMAGE_WIDTH / 2 - 8, topPos + 63, 16, 16, Component.empty()) {
            @Override
            public void onPress(InputWithModifiers input) {
                onConfirmPressed();
            }

            @Override
            public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Identifier id = isHoveredOrFocused()
                        ? Identifier.withDefaultNamespace("container/beacon/button_highlighted")
                        : Identifier.withDefaultNamespace("container/beacon/button");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
                renderIcon(guiGraphics);
            }

            protected void renderIcon(GuiGraphics g) {
                g.blit(RenderPipelines.GUI_TEXTURED, KEY_, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }

            @Override
            public void updateWidgetNarration(NarrationElementOutput output) {
                defaultButtonNarrationText(output);
            }
        });
    }

    private void initPincodeLayout() {
        IMAGE_WIDTH = 180;
        IMAGE_HEIGHT = 211;
        this.leftPos = (this.width  - IMAGE_WIDTH)  / 2;
        this.topPos  = (this.height - IMAGE_HEIGHT) / 2;

        TITLE_START_Y = topPos + 30;

        // Buttons 1-9
        for (int i = 1; i <= 9; i++) {
            final int digit = i;
            int col = (i - 1) % 3;
            int row = (i - 1) / 3;
            addRenderableWidget(new AbstractButton(leftPos + 23 + col * 32, topPos + 64 + row * 32, 32, 32, Component.empty()) {
                @Override
                public void onPress(InputWithModifiers input) {
                    enteredCode += digit;
                }
                // TODO: Replace with custom PIN button sprites (with numbers on them, not empty ones)
                @Override
                public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    Identifier id = isHoveredOrFocused()
                            ? Identifier.withDefaultNamespace("container/beacon/button_highlighted")
                            : Identifier.withDefaultNamespace("container/beacon/button");
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
                    guiGraphics.drawCenteredString(font, Component.literal(String.valueOf(digit)), getX() + 16, getY() + 12, -1);
                }

                @Override
                public void updateWidgetNarration(NarrationElementOutput output) {
                    defaultButtonNarrationText(output);
                }
            });
        }

        // Button 0
        addRenderableWidget(new AbstractButton(leftPos + 23 + 32, topPos + 64 + 3 * 32, 32, 32, Component.empty()) {
            @Override
            public void onPress(InputWithModifiers input) {
                enteredCode += 0;
            }

            // TODO: Replace with custom PIN button sprites (with numbers on them, not empty ones)
            @Override
            public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Identifier id = isHoveredOrFocused()
                        ? Identifier.withDefaultNamespace("container/beacon/button_highlighted")
                        : Identifier.withDefaultNamespace("container/beacon/button");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
                guiGraphics.drawCenteredString(font, Component.literal(String.valueOf(0)), getX() + 16, getY() + 12, -1);
            }

            @Override
            public void updateWidgetNarration(NarrationElementOutput output) {
                defaultButtonNarrationText(output);
            }
        });

        // Arrow button
        addRenderableWidget(new AbstractButton(leftPos + 23 + 3 * 32 + 4, topPos + 64 + 32, 32, 32, Component.empty()) {
            @Override
            public void onPress(InputWithModifiers input) {
                if (!enteredCode.isEmpty())
                    enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
            }

            // TODO: Replace with custom PIN button sprites (with the symbols on them)
            @Override
            public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Identifier id = isHoveredOrFocused()
                        ? Identifier.withDefaultNamespace("container/beacon/button_highlighted")
                        : Identifier.withDefaultNamespace("container/beacon/button");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
                renderIcon(guiGraphics);
            }

            protected void renderIcon(GuiGraphics g) {
                g.blit(RenderPipelines.GUI_TEXTURED, ARROW_BUTTON, getX(), getY(), 0, 0, 32, 32, 32, 32);
            }

            @Override
            public void updateWidgetNarration(NarrationElementOutput output) {
                defaultButtonNarrationText(output);
            }
        });

        // Clear button
        addRenderableWidget(new AbstractButton(leftPos + 23 + 3 * 32 + 4, topPos + 64, 32, 32, Component.empty()) {
            @Override
            public void onPress(InputWithModifiers input) {
                enteredCode = "";
            }

            // TODO: Replace with custom PIN button sprites (with the symbols on them)
            @Override
            public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Identifier id = isHoveredOrFocused()
                        ? Identifier.withDefaultNamespace("container/beacon/button_highlighted")
                        : Identifier.withDefaultNamespace("container/beacon/button");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
                renderIcon(guiGraphics);
            }

            protected void renderIcon(GuiGraphics g) {
                g.blit(RenderPipelines.GUI_TEXTURED, CLEAR_BUTTON, getX(), getY(), 0, 0, 32, 32, 32, 32);
            }

            @Override
            public void updateWidgetNarration(NarrationElementOutput output) {
                defaultButtonNarrationText(output);
            }
        });
        // Confirm button
        addRenderableWidget(new AbstractButton(leftPos + 23 + 3 * 32 + 4, topPos + 64 + 2 * 32, 32, 32, Component.empty()) {
            @Override
            public void onPress(InputWithModifiers input) {
                onConfirmPressed();
            }

            // TODO: Replace with custom PIN button sprites (with the symbols on them)
            @Override
            public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Identifier id = isHoveredOrFocused()
                        ? Identifier.withDefaultNamespace("container/beacon/button_highlighted")
                        : Identifier.withDefaultNamespace("container/beacon/button");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
                renderIcon(guiGraphics);
            }

            protected void renderIcon(GuiGraphics g) {
                g.blit(RenderPipelines.GUI_TEXTURED, OK_BUTTON, getX(), getY(), 0, 0, 32 ,32, 32, 32);
            }

            @Override
            public void updateWidgetNarration(NarrationElementOutput output) {
                defaultButtonNarrationText(output);
            }
        });

        // Empty keypad buttons
        for (int j = 0; j < 2; j++)
        {
            addRenderableWidget(new AbstractButton(leftPos + 23 + j * 64, topPos + 64 + 3 * 32, 32, 32, Component.empty()) {
                @Override
                public void onPress(InputWithModifiers input) {
                    return;
                }

                // TODO: Replace with custom PIN button sprites (the empty ones)
                @Override
                public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    Identifier id = Identifier.withDefaultNamespace("container/beacon/button");
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
                }

                @Override
                public void updateWidgetNarration(NarrationElementOutput output) {
                    return;
                }
            });
        }

        // Empty menu button
        addRenderableWidget(new AbstractButton(leftPos + 23 + 3 * 32 + 4, topPos + 64 + 3 * 32, 32, 32, Component.empty()) {
            @Override
            public void onPress(InputWithModifiers input) {
                return;
            }

            // TODO: Replace with custom PIN button sprites (the empty ones)
            @Override
            public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                Identifier id = Identifier.withDefaultNamespace("container/beacon/button");
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
            }

            @Override
            public void updateWidgetNarration(NarrationElementOutput output) {
                return;
            }
        });
    }

    private void onConfirmPressed() {
        String code;
        if (!isKeycard) {
            // Build string from segment states
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
        // If we're on the serial code screen, handle clicking the segments
        if (!isKeycard) {
            for (int i = 0; i < 18; i++) {
                int segX = leftPos + SEGMENT_START_X + i * (SEGMENT_WIDTH + SEGMENT_GAP);
                int segY = topPos + SEGMENT_START_Y;
                if(i > 8) segX++;
                if (mouseX >= segX && mouseX < segX + SEGMENT_WIDTH
                        && mouseY >= segY && mouseY < segY + SEGMENT_HEIGHT) {
                    segments[i] = !segments[i];
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

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
        }

        // Render the title of the safe code screen
        guiGraphics.drawString(this.font, this.title, TITLE_START_X - font.width(title) / 2, TITLE_START_Y, -12566464, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}