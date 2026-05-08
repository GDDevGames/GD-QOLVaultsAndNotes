package dev.gdawg.qolvaultsandnotes;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class BulletinBoardScreen extends AbstractContainerScreen<BulletinBoardMenu> {

    // --- Sprites ---
    static final Identifier BUTTON_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/beacon/button_disabled");
    static final Identifier BUTTON_SELECTED_SPRITE = Identifier.withDefaultNamespace("container/beacon/button_selected");
    static final Identifier BUTTON_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("container/beacon/button_highlighted");
    static final Identifier BUTTON_SPRITE = Identifier.withDefaultNamespace("container/beacon/button");
    static final Identifier RECIPEBOOK_BUTTON_SPRITE = Identifier.withDefaultNamespace("recipe_book/button");
    static final Identifier RECIPEBOOK_BUTTON_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("recipe_book/button_highlighted");
    static final Identifier TEXT_FIELD_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field");
    static final Identifier TEXT_FIELD_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field_disabled");
    static final Identifier LIGHT_BLUE_DYE_SPRITE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/light_blue_dye.png");
    static final Identifier LIME_DYE_SPRITE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/lime_dye.png");
    static final Identifier PINK_DYE_SPRITE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/pink_dye.png");
    static final Identifier YELLOW_DYE_SPRITE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/yellow_dye.png");

    // --- Textures ---
    static final Identifier MAIN_BULLETIN_BOARD_LOCATION = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/bulletin_board/main_panel.png");
    static final Identifier SIDE_BULLETIN_BOARD_LOCATION = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/bulletin_board/side_panel.png");

    // Note textures — index matches colour int (0=yellow,1=green,2=blue,3=pink)
    static final Identifier[] NOTE_TEXTURES = {
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/yellow_note.png"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/green_note.png"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/lightblue_note.png"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/pink_note.png")
    };
    static final Identifier[] BIG_NOTE_TEXTURES = {
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/big_yellow_note.png"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/big_green_note.png"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/big_blue_note.png"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/sprites/bulletin_board/big_pink_note.png")
    };

    // --- State ---
    public boolean extendedMenu = false;
    private int selectedNoteSlot = -1;  // which note slot is selected (-1 = new note)
    private boolean isNewNote = true;   // true = writing new note, false = viewing existing
    private int currentColour = 0;      // 0=yellow,1=green,2=blue,3=pink
    private String originalTitle = "";  // for reverting changes
    private String originalBody = "";

    // --- Widgets ---
    private BulletinBoardSpriteScreenButton toggleExtendedButton;
    private BulletinBoardScreenButton pinButton;
    private BulletinBoardScreenButton unpinButton;
    private BulletinBoardScreenButton colourButton;
    private EditBox titleField;
    // Book-style body lines
    private final List<String> bodyLines = new ArrayList<>();
    private int bodyCaretLine = 0;
    private int bodyCaretPos = 0;
    private boolean editingBody = false;

    private final List<BulletinBoardScreenButton> sidePanelButtons = new ArrayList<>();
    private final BulletinBoardBlockEntity be;

    // Note slot positions relative to main panel (adjust to match your PNG)
    private static final int[] NOTE_SLOT_X = {16, 68, 121, 174, 17, 68, 121, 174};
    private static final int[] NOTE_SLOT_Y = {13, 13, 13, 13, 65, 65, 65, 65};
    private static final int NOTE_SLOT_W = 50;
    private static final int NOTE_SLOT_H = 50;

    // Max amount of lines you can write in the body of a note
    private static final int maxLines = 6; //7? or..

    public BulletinBoardScreen(BulletinBoardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 128;
        this.be = menu.blockEntity;
        // Init body lines
        for (int i = 0; i < 10; i++) bodyLines.add("");
    }

    // -----------------------------------------------------------------------
    // INIT
    // -----------------------------------------------------------------------

    @Override
    protected void init() {
        super.init();
        sidePanelButtons.clear();

        // Title text field (side panel)
        titleField = new EditBox(this.font,
                leftPos + 210, topPos + 16,
                71, 12,
                Component.literal("Title"));
        titleField.setCanLoseFocus(true);
        titleField.setTextColor(-1);
        titleField.setTextColorUneditable(-1);
        titleField.setInvertHighlightedTextColor(false);
        titleField.setBordered(false);
        titleField.setMaxLength(12);
        titleField.setValue("");
        titleField.visible = true;
        titleField.active = true;
        addRenderableWidget(titleField);

        // Toggle extended button (always visible)
        BulletinBoardSpriteScreenButton toggleBtn = new BulletinBoardSpriteScreenButton(
                this.leftPos + 231, this.topPos + 54,
                RECIPEBOOK_BUTTON_SPRITE, Component.literal("")
        ) {
            @Override
            public void onPress(InputWithModifiers input) {
                toggleExtendedMenu();
                setSelected(!isSelected());
            }
            @Override
            public void renderContents(GuiGraphics g, int mx, int my, float pt) {
                Identifier id = isHoveredOrFocused()
                        ? RECIPEBOOK_BUTTON_HIGHLIGHTED_SPRITE
                        : RECIPEBOOK_BUTTON_SPRITE;
                g.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), 20, 18);
            }
            @Override
            protected void renderIcon(GuiGraphics g) {}
        };
        toggleExtendedButton = toggleBtn;
        addRenderableWidget(toggleBtn);

        // Pin button (redstone torch)
        pinButton = new BulletinBoardScreenButton(0, 0, 18, 18, Component.literal("Pin")) {
            @Override
            public void onPress(InputWithModifiers input) {
                onPinClicked();
            }
            @Override
            protected void renderIcon(GuiGraphics g) {
                g.renderFakeItem(new ItemStack(Items.REDSTONE_TORCH), getX() + 1, getY());
            }

            @Override
            public void playDownSound(SoundManager soundManager) {
                //super.playDownSound(soundManager);
            }

        };
        addSidePanelButton(pinButton, 12, 129);

        // Unpin button (copper torch)
        unpinButton = new BulletinBoardScreenButton(0, 0, 18, 18, Component.literal("Unpin")) {
            @Override
            public void onPress(InputWithModifiers input) {
                onUnpinClicked();
            }
            @Override
            protected void renderIcon(GuiGraphics g) {
                g.renderFakeItem(new ItemStack(Items.COPPER_TORCH), getX() + 1, getY());
            }
            @Override
            public void playDownSound(SoundManager soundManager) {
                // super.playDownSound(soundManager);
            }
        };
        addSidePanelButton(unpinButton, 33, 129);

        // Colour cycle button
        colourButton = new BulletinBoardScreenButton(0, 0, 14, 14, Component.literal("Colour")) {
            @Override
            public void onPress(InputWithModifiers input) {
                currentColour = (currentColour + 1) % 4;
            }
            @Override
            protected void renderIcon(GuiGraphics g) {
                // Show the current colour dye item
                switch (currentColour) {
                    case 1 -> g.blit(RenderPipelines.GUI_TEXTURED, LIME_DYE_SPRITE, getX() + 1, getY(), 0, 0, 12, 12, 16, 16, 16, 16);
                    case 2 -> g.blit(RenderPipelines.GUI_TEXTURED, LIGHT_BLUE_DYE_SPRITE, getX() + 1, getY(), 0, 0, 12, 12, 16, 16, 16, 16);
                    case 3 -> g.blit(RenderPipelines.GUI_TEXTURED, PINK_DYE_SPRITE, getX() + 1, getY(), 0, 0, 12, 12, 16, 16, 16, 16);
                    default -> g.blit(RenderPipelines.GUI_TEXTURED, YELLOW_DYE_SPRITE, getX() + 1, getY(), 0, 0, 12, 12, 16, 16, 16, 16);
                }
            }

            @Override
            public void playDownSound(SoundManager soundManager) {
                super.playDownSound(soundManager);
            }
        };
        addSidePanelButton(colourButton, 99, 13); // top right near title field

        updateSidePanelVisibility();
    }

    // -----------------------------------------------------------------------
    // NOTE ACTIONS
    // -----------------------------------------------------------------------

    private void onPinClicked() {
        String title = titleField.getValue();
        String body = String.join("\n", bodyLines).stripTrailing();

        if (title.isEmpty() && body.isEmpty()) return;

        int slot = selectedNoteSlot >= 0 ? selectedNoteSlot : be.getNextFreeSlot();
        if (slot < 0) return; // no free slots

        // Send packet to server to save note and consume resources
        ClientPacketDistributor.sendToServer(new BulletinBoardPinPacket(
                be.getBlockPos(), slot, title, body, currentColour, isNewNote));

        // Optimistic client update
        be.setNote(slot, title, body, currentColour);
        originalTitle = title;
        originalBody = body;
        selectedNoteSlot = slot;
        isNewNote = false;

        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F)
        );

        // No button selected after pinning
        pinButton.setSelected(false);
        unpinButton.setSelected(false);
    }

    private void onUnpinClicked() {
        // Clear editor for a new note
        selectedNoteSlot = -1;
        isNewNote = true;
        titleField.setValue("");
        clearBody();
        currentColour = 0;
        pinButton.setSelected(false);
        unpinButton.setSelected(false);

        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.SHIELD_BLOCK, 2.0F)
        );
    }

    private void onNoteSlotClicked(int slot) {
        if (!be.isNoteOccupied(slot)) {
            // Empty slot — treat as new note
            onUnpinClicked();
            return;
        }
        // Load note into editor
        selectedNoteSlot = slot;
        isNewNote = false;
        String title = be.getNoteTitle(slot);
        String body = be.getNoteBody(slot);
        currentColour = be.getNoteColour(slot);
        originalTitle = title;
        originalBody = body;
        titleField.setValue(title);
        loadBody(body);
        // No button selected when viewing
        pinButton.setSelected(false);
        unpinButton.setSelected(false);
    }

    private void clearBody() {
        bodyLines.clear();
        for (int i = 0; i < 10; i++) bodyLines.add("");
        bodyCaretLine = 0;
        bodyCaretPos = 0;
        editingBody = false;
    }

    private void loadBody(String body) {
        clearBody();
        String[] lines = body.split("\n", -1);
        for (int i = 0; i < Math.min(lines.length, 10); i++) {
            bodyLines.set(i, lines[i]);
        }
    }

    // -----------------------------------------------------------------------
    // INPUT
    // -----------------------------------------------------------------------

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        // Check note slot clicks in main panel
        int panelX = extendedMenu
                ? (this.width - (256 + 128)) / 2
                : (this.width - 256) / 2;
        int panelY = (this.height - 128) / 2;

        for (int i = 0; i < 8; i++) {
            int nx = panelX + NOTE_SLOT_X[i];
            int ny = panelY + NOTE_SLOT_Y[i];
            if (mouseX >= nx && mouseX < nx + NOTE_SLOT_W
                    && mouseY >= ny && mouseY < ny + NOTE_SLOT_H) {
                if (!extendedMenu) toggleExtendedMenu();
                onNoteSlotClicked(i);
                return super.mouseClicked(event, doubleClick);
            }
        }

        // Check if clicking body area
        if (extendedMenu) {
            int sidePanelX = (this.width - (256 + 128)) / 2 + 256;
            int sidePanelY = (this.height - 128) / 2;
            int bodyX = sidePanelX + 10;
            int bodyY = sidePanelY + 40;
            int bodyW = 108;
            int bodyH = 80;
            if (mouseX >= bodyX && mouseX < bodyX + bodyW
                    && mouseY >= bodyY && mouseY < bodyY + bodyH) {
                editingBody = true;
                titleField.setFocused(false);
                return super.mouseClicked(event, doubleClick);
            } else {
                editingBody = false;
            }

            //everything below is an ugly solution to something internal we have to fish out and use naturally.
            int titleX = titleField.getX();
            int titleY = titleField.getY();
            int titleW = 80;
            int titleH = 14;

            if (mouseX >= titleX && mouseX < titleX + titleW
                    && mouseY >= titleY && mouseY < titleY + titleH) {
                this.setFocused(titleField);
                /*if(titleField.mouseClicked(event, doubleClick)) {

                }*/
                /*titleField.setFocused(true);
                titleField.setEditable(true);
                titleField.active = true;
                titleField.mouseClicked(event, doubleClick);*/
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (editingBody) {
            return handleBodyKeyPress(keyEvent);
        }
        if (titleField.isFocused()) {
            System.out.println("trying to type");
            return titleField.keyPressed(keyEvent);
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (editingBody) {
            int codepoint = event.codepoint();
            if (event.isAllowedChatCharacter()) {
                String c = String.valueOf(Character.toChars(codepoint));
                String line = bodyLines.get(bodyCaretLine);
                String newLine = line.substring(0, bodyCaretPos) + c + line.substring(bodyCaretPos);
                boolean lastLine = false;


                // Check if the new line fits within the note width (adjust 70 to match your note's text area)
                if (font.width(newLine) <= 74) {
                    bodyLines.set(bodyCaretLine, newLine);
                    bodyCaretPos++;
                } else if (bodyCaretLine < maxLines) {
                    // Auto wrap — commit current line and move character to next line
                    bodyLines.set(bodyCaretLine, line.substring(0, bodyCaretPos));
                    bodyCaretLine++;
                    String rest = c + line.substring(bodyCaretPos);
                    bodyLines.set(bodyCaretLine, rest);
                    bodyCaretPos = 1;
                }
            }
            return true;
        }
        return super.charTyped(event);
    }

    private boolean handleBodyKeyPress(KeyEvent keyEvent) {
        int key = keyEvent.key();
        String line = bodyLines.get(bodyCaretLine);

        if (key == 259) { // Backspace
            if (bodyCaretPos > 0) {
                bodyLines.set(bodyCaretLine,
                        line.substring(0, bodyCaretPos - 1) + line.substring(bodyCaretPos));
                bodyCaretPos--;
            } else if (bodyCaretLine > 0) {
                String prev = bodyLines.get(bodyCaretLine - 1);
                bodyCaretPos = prev.length();
                bodyLines.set(bodyCaretLine - 1, prev + line);
                bodyLines.remove(bodyCaretLine);
                bodyLines.add("");
                bodyCaretLine--;
            }
            return true;
        }
        if (key == 257 || key == 335) { // Enter
            if (bodyCaretLine < maxLines) {
                String rest = line.substring(bodyCaretPos);
                bodyLines.set(bodyCaretLine, line.substring(0, bodyCaretPos));
                bodyCaretLine++;
                bodyLines.add(bodyCaretLine, rest);
                if (bodyLines.size() > maxLines + 1) bodyLines.remove(maxLines + 1);
                bodyCaretPos = 0;
            }
            return true;
        }
        if (key == 263) { // Left
            if (bodyCaretPos > 0) bodyCaretPos--;
            return true;
        }
        if (key == 262) { // Right
            if (bodyCaretPos < line.length()) bodyCaretPos++;
            return true;
        }
        if (key == 265 && bodyCaretLine > 0) { // Up
            bodyCaretLine--;
            bodyCaretPos = Math.min(bodyCaretPos, bodyLines.get(bodyCaretLine).length());
            return true;
        }
        if (key == 264 && bodyCaretLine < maxLines) { // Down
            bodyCaretLine++;
            bodyCaretPos = Math.min(bodyCaretPos, bodyLines.get(bodyCaretLine).length());
            return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // RENDERING
    // -----------------------------------------------------------------------

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (extendedMenu) {
            int totalWidth = 256 + 128;
            int startX = (this.width - totalWidth) / 2;
            int startY = (this.height - 128) / 2;
            g.blit(RenderPipelines.GUI_TEXTURED, MAIN_BULLETIN_BOARD_LOCATION,
                    startX, startY, 0, 0, 256, 128, 256, 256);
            g.blit(RenderPipelines.GUI_TEXTURED, SIDE_BULLETIN_BOARD_LOCATION,
                    startX + 256, startY, 0, 0, 128, 151, 256, 256);

            // Text field background behind title
            g.blitSprite(RenderPipelines.GUI_TEXTURED, TEXT_FIELD_SPRITE,
                    startX + 256 + 15, startY + 13, 80, 14);

            // Ink and paper counts
            g.renderFakeItem(new ItemStack(Items.INK_SAC), startX + 256 + 77, startY + 130);
            g.renderFakeItem(new ItemStack(Items.PAPER), startX + 256 + 98, startY + 130);
            ItemStack ink = be.getItem(0);
            ItemStack paper = be.getItem(1);
            g.renderItemDecorations(minecraft.font, ink.isEmpty()
                            ? new ItemStack(Items.INK_SAC) : ink,
                    startX + 256 + 77, startY + 130,
                    ink.isEmpty() ? "0" : null);
            g.renderItemDecorations(minecraft.font, paper.isEmpty()
                            ? new ItemStack(Items.PAPER) : paper,
                    startX + 256 + 98, startY + 130,
                    paper.isEmpty() ? "0" : null);

        } else {
            int startX = (this.width - 256) / 2;
            int startY = (this.height - 128) / 2;
            g.blit(RenderPipelines.GUI_TEXTURED, MAIN_BULLETIN_BOARD_LOCATION,
                    startX, startY, 0, 0, 256, 128, 256, 256);
        }

        // Draw notes in main panel slots
        int panelX = extendedMenu
                ? (this.width - (256 + 128)) / 2
                : (this.width - 256) / 2;
        int panelY = (this.height - 128) / 2;
        for (int i = 0; i < 8; i++) {
            if (be.isNoteOccupied(i)) {
                int nx = panelX + NOTE_SLOT_X[i];
                int ny = panelY + NOTE_SLOT_Y[i];
                g.blit(RenderPipelines.GUI_TEXTURED,
                        NOTE_TEXTURES[be.getNoteColour(i)],
                        nx, ny, 0, 0, NOTE_SLOT_W, NOTE_SLOT_H, NOTE_SLOT_W, NOTE_SLOT_H);
                // Draw title on note
                String title = be.getNoteTitle(i);
                if (!title.isEmpty()) {
                    String display = title.length() > 7 ? title.substring(0, 7) : title;
                    g.drawString(font, display, (nx + 25) - font.width(display) / 2 , ny + 4, -16777216, false);
                }
            }
        }

        // Draw big note preview in side panel
        if (extendedMenu) {
            int totalWidth = 256 + 128;

            int sidePanelX = (this.width / 2) + 90;
            int sidePanelY = (this.height / 2) - 32;
            int noteX = sidePanelX;
            int noteY = sidePanelY;
            g.blit(RenderPipelines.GUI_TEXTURED,
                    BIG_NOTE_TEXTURES[currentColour],
                    noteX, noteY, 0, 0, 80, 80, 150, 150, 150, 150);


            // Draw title on big note
            String title = titleField.getValue();
            if (!title.isEmpty()) {
                g.drawString(font, title, (noteX + 40) - font.width(title) / 2, noteY + 4, -16777216, false);
            }

            // Draw body lines on big note
            for (int i = 0; i < bodyLines.size(); i++) {
                String line = bodyLines.get(i);
                if (!line.isEmpty()) {
                    g.drawString(font, line, noteX + 4, noteY + 16 + i * 9, -12303310, false);
                }
                // Draw caret
                if (editingBody && i == bodyCaretLine) {
                    int caretX = noteX + 4 + font.width(line.substring(0, bodyCaretPos));
                    int caretY = noteY + 16 + i * 9;
                    g.fill(caretX, caretY, caretX + 1, caretY + 9, -12303310);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Suppress default labels
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    public void toggleExtendedMenu() {
        extendedMenu = !extendedMenu;
        updateToggleButtonPosition();
        updateSidePanelVisibility();
    }

    private void updateToggleButtonPosition() {
        if (extendedMenu) {
            int totalWidth = 256 + 128;
            int startX = (this.width - totalWidth) / 2;
            int startY = (this.height - 128) / 2;
            toggleExtendedButton.setX(startX + 230);
            toggleExtendedButton.setY(startY + 54);
        } else {
            toggleExtendedButton.setX(this.leftPos + 230);
            toggleExtendedButton.setY(this.topPos + 54);
        }
    }

    private void addSidePanelButton(BulletinBoardScreenButton button, int offsetX, int offsetY) {
        int totalWidth = 256 + 128;
        int sidePanelX = (this.width - totalWidth) / 2 + 256;
        int sidePanelY = (this.height - 128) / 2;
        button.setX(sidePanelX + offsetX);
        button.setY(sidePanelY + offsetY);
        sidePanelButtons.add(button);
        addRenderableWidget(button);
    }

    private void updateSidePanelVisibility() {
        for (BulletinBoardScreenButton btn : sidePanelButtons) {
            btn.visible = extendedMenu;
        }
        titleField.visible = extendedMenu;
    }

    // -----------------------------------------------------------------------
    // BUTTON CLASSES
    // -----------------------------------------------------------------------

    abstract static class BulletinBoardScreenButton extends AbstractButton {
        protected boolean selected;

        protected BulletinBoardScreenButton(int x, int y, Component message) {
            super(x, y, 20, 20, message);
        }
        protected BulletinBoardScreenButton(int x, int y, int sizeX, int sizeY, Component message) {
            super(x, y, sizeX, sizeY, message);
        }
        public void renderContents(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            Identifier id;
            if (!this.active) {
                id = BUTTON_DISABLED_SPRITE;
            } else if (this.selected) {
                id = BUTTON_SELECTED_SPRITE;
            } else if (this.isHoveredOrFocused()) {
                id = BUTTON_HIGHLIGHTED_SPRITE;
            } else {
                id = BUTTON_SPRITE;
            }
            g.blitSprite(RenderPipelines.GUI_TEXTURED, id, getX(), getY(), width, height);
            renderIcon(g);
        }

        protected abstract void renderIcon(GuiGraphics g);

        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }

        @Override
        public void updateWidgetNarration(NarrationElementOutput out) {
            defaultButtonNarrationText(out);
        }
    }

    class BulletinBoardSpriteScreenButton extends BulletinBoardScreenButton {
        private final Identifier sprite;

        protected BulletinBoardSpriteScreenButton(int x, int y, Identifier sprite, Component message) {
            super(x, y, message);
            this.setTooltip(Tooltip.create(message));
            this.sprite = sprite;
        }

        @Override
        protected void renderIcon(GuiGraphics g) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX() + 2, getY() + 2, 16, 16);
        }

        @Override
        public void onPress(InputWithModifiers input) {}
    }
}