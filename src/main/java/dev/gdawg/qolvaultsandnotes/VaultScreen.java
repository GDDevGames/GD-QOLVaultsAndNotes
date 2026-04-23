/// ----- VaultScreen -----
/// GUI for the scrollable vault.
/// ------------------------------------
package dev.gdawg.qolvaultsandnotes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class VaultScreen extends AbstractContainerScreen<VaultMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(
        QOLVaultsAndNotes.MODID, "textures/gui/container/vault.png");

    // Pixel coords
    private static final int SCROLLBAR_LEFT = 174;
    private static final int SCROLLBAR_TOP = 18;
    private static final int SCROLLBAR_RIGHT = 186;
    private static final int SCROLLBAR_BOTTOM = 125;
    private static final int SCROLLBAR_WIDTH = SCROLLBAR_RIGHT - SCROLLBAR_LEFT;   // 13
    private static final int SCROLLBAR_HEIGHT = SCROLLBAR_BOTTOM - SCROLLBAR_TOP;  // 107

    // Total scrollable rows: 8 total - 6 visible = 2
    private static final int SCROLLABLE_ROWS = 2;

    private float scrollOffset = 0.0f;
    private boolean isScrolling = false;

    public VaultScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 194;
        this.imageHeight = 222;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = imageHeight - 94; // Daniel: important to change manually because the default is for smaller screens
    }

    @Override
    protected void init() {
        super.init();
        this.menu.scrollTo(0);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            GUI_TEXTURE,
            this.leftPos,
            this.topPos,
            0.0F, 0.0F,
            this.imageWidth,
            this.imageHeight,
            256, 256
        );

        // Draw scrollbar knob
        int scrollbarX = this.leftPos + SCROLLBAR_LEFT;
        int scrollbarTrackHeight = SCROLLBAR_HEIGHT; // total track pixels
        int knobHeight = 15; // height of the knob in pixels
        int knobY = this.topPos + SCROLLBAR_TOP + (int)(scrollOffset * (scrollbarTrackHeight - knobHeight));
        // guiGraphics.fill(scrollbarX, knobY, scrollbarX + SCROLLBAR_WIDTH, knobY + knobHeight, 0xFF888888); Debug color
        guiGraphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            Identifier.withDefaultNamespace("container/creative_inventory/scroller"), // Daniel: maybe make it into a variable up top? static?
            scrollbarX,
            knobY,
            SCROLLBAR_WIDTH,
            15
        );
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0 && isInsideScrollbar(event.x(), event.y())) {
            isScrolling = true;
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseClicked(event, consumed);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (event.button() == 0) {
            isScrolling = false;
        }
        int rowOffset = Math.round(scrollOffset * 2);
        scrollOffset = rowOffset / 2.0f; // This snaps the visual position after the mouse has released
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if (isScrolling) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Mth.clamp(scrollOffset - (float)(scrollY / SCROLLABLE_ROWS), 0.0f, 1.0f);
        applyScroll();
        return true;
    }

    private void updateScrollFromMouse(double mouseY) {
        int knobHeight = 15;
        int trackTop = this.topPos + SCROLLBAR_TOP;
        int trackHeight = SCROLLBAR_HEIGHT - knobHeight;
        scrollOffset = Mth.clamp((float)(mouseY - trackTop - knobHeight / 2.0) / trackHeight, 0.0f, 1.0f);
        applyScroll();
    }

    private void applyScroll() {
        int rowOffset = Math.round(scrollOffset * 2);
        this.menu.scrollTo(rowOffset);
        ClientPacketDistributor.sendToServer(new VaultScrollPacket(rowOffset));
    }

    private boolean isInsideScrollbar(double mouseX, double mouseY) {
        int x = this.leftPos + SCROLLBAR_LEFT;
        int y = this.topPos + SCROLLBAR_TOP;
        return mouseX >= x && mouseX <= x + SCROLLBAR_WIDTH
            && mouseY >= y && mouseY <= y + SCROLLBAR_HEIGHT;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, -12566464, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }
}