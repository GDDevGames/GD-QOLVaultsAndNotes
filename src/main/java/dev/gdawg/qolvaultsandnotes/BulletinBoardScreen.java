package dev.gdawg.qolvaultsandnotes;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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

    // --- Textures ---
    static final Identifier MAIN_BULLETIN_BOARD_LOCATION = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/bulletin_board/main_panel.png");
    static final Identifier SIDE_BULLETIN_BOARD_LOCATION = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/bulletin_board/side_panel.png");

    // Note textures — index matches colour int (0=yellow,1=green,2=blue,3=pink)
    static final Identifier[] NOTE_TEXTURES = {
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/yellow_note"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/green_note"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/blue_note"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/pink_note")
    };
    static final Identifier[] BIG_NOTE_TEXTURES = {
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_yellow_note"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_green_note"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_blue_note"),
            Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_pink_note")
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
    private static final int[] NOTE_SLOT_X = {8, 68, 128, 188, 8, 68, 128, 188};
    private static final int[] NOTE_SLOT_Y = {8, 8, 8, 8, 68, 68, 68, 68};
    private static final int NOTE_SLOT_W = 52;
    private static final int NOTE_SLOT_H = 52;

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
                leftPos + 256 + 10, topPos + 20,
                103, 12,
                Component.literal("Title"));
        titleField.setCanLoseFocus(true);
        titleField.setTextColor(-1);
        titleField.setTextColorUneditable(-1);
        titleField.setInvertHighlightedTextColor(false);
        titleField.setBordered(false);
        titleField.setMaxLength(50);
        titleField.setValue("");
        titleField.visible = false;
        addRenderableWidget(titleField);

        // Toggle extended button (always visible)
        BulletinBoardSpriteScreenButton toggleBtn = new BulletinBoardSpriteScreenButton(
                this.leftPos + 233, this.topPos + 54,
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
        pinButton = new BulletinBoardScreenButton(0, 0, Component.literal("Pin")) {
            @Override
            public void onPress(InputWithModifiers input) {
                onPinClicked();
            }
            @Override
            protected void renderIcon(GuiGraphics g) {
                g.renderFakeItem(new ItemStack(Items.REDSTONE_TORCH), getX() + 2, getY());
            }
        };
        addSidePanelButton(pinButton, 11, 128);

        // Unpin button (copper torch)
        unpinButton = new BulletinBoardScreenButton(0, 0, Component.literal("Unpin")) {
            @Override
            public void onPress(InputWithModifiers input) {
                onUnpinClicked();
            }
            @Override
            protected void renderIcon(GuiGraphics g) {
                g.renderFakeItem(new ItemStack(Items.COPPER_TORCH), getX() + 2, getY());
            }
        };
        addSidePanelButton(unpinButton, 33, 128);

        // Colour cycle button
        colourButton = new BulletinBoardScreenButton(0, 0, Component.literal("Colour")) {
            @Override
            public void onPress(InputWithModifiers input) {
                currentColour = (currentColour + 1) % 4;
            }
            @Override
            protected void renderIcon(GuiGraphics g) {
                // Show the current colour dye item
                ItemStack dye = switch (currentColour) {
                    case 1 -> new ItemStack(Items.GREEN_DYE);
                    case 2 -> new ItemStack(Items.BLUE_DYE);
                    case 3 -> new ItemStack(Items.PINK_DYE);
                    default -> new ItemStack(Items.YELLOW_DYE);
                };
                g.renderFakeItem(dye, getX() + 2, getY());
            }
        };
        addSidePanelButton(colourButton, 103, 10); // top right near title field

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
                return true;
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
                return true;
            } else {
                editingBody = false;
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
            titleField.keyPressed(keyEvent);
            return true;
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
                if (line.length() < 20) {
                    bodyLines.set(bodyCaretLine,
                            line.substring(0, bodyCaretPos) + c + line.substring(bodyCaretPos));
                    bodyCaretPos++;
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
            if (bodyCaretLine < 9) {
                String rest = line.substring(bodyCaretPos);
                bodyLines.set(bodyCaretLine, line.substring(0, bodyCaretPos));
                bodyCaretLine++;
                bodyLines.add(bodyCaretLine, rest);
                if (bodyLines.size() > 10) bodyLines.remove(10);
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
        if (key == 264 && bodyCaretLine < 9) { // Down
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
                    startX + 256 + 10, startY + 18, 103, 16);

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
                    g.drawString(font, display, nx + 2, ny + 4, 0x404020, false);
                }
            }
        }

        // Draw big note preview in side panel
        if (extendedMenu) {
            int sidePanelX = (this.width - (256 + 128)) / 2 + 256;
            int sidePanelY = (this.height - 128) / 2;
            int noteX = sidePanelX + 10;
            int noteY = sidePanelY + 36;
            g.blit(RenderPipelines.GUI_TEXTURED,
                    BIG_NOTE_TEXTURES[currentColour],
                    noteX, noteY, 0, 0, 108, 90, 108, 90);

            // Draw title on big note
            String title = titleField.getValue();
            if (!title.isEmpty()) {
                g.drawString(font, title, noteX + 4, noteY + 4, 0x404020, false);
            }

            // Draw body lines on big note
            for (int i = 0; i < bodyLines.size(); i++) {
                String line = bodyLines.get(i);
                if (!line.isEmpty()) {
                    g.drawString(font, line, noteX + 4, noteY + 16 + i * 9, 0x404020, false);
                }
                // Draw caret
                if (editingBody && i == bodyCaretLine) {
                    int caretX = noteX + 4 + font.width(line.substring(0, bodyCaretPos));
                    int caretY = noteY + 16 + i * 9;
                    g.fill(caretX, caretY, caretX + 1, caretY + 9, 0xFF404020);
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