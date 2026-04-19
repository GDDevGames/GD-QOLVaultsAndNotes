//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package dev.gdawg.qolvaultsandnotes;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.core.Holder;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public class BulletinBoardScreen extends AbstractContainerScreen<BulletinBoardMenu> {
    static final Identifier INK_SAC_SLOT_SPRITE = Identifier.withDefaultNamespace("ink_sac");
    static final Identifier PAPER_SLOT_SPRITE = Identifier.withDefaultNamespace("paper");
    static final Identifier REDSTONE_TORCH_SPRITE = Identifier.withDefaultNamespace("redstone_torch");
    static final Identifier COPPER_TORCH_SPRITE = Identifier.withDefaultNamespace("copper_torch");
    static final Identifier LIGHT_BLUE_DYE_SPRITE = Identifier.withDefaultNamespace("light_blue_dye");
    static final Identifier YELLOW_DYE_SPRITE = Identifier.withDefaultNamespace("yellow_dye");
    static final Identifier PINK_DYE_SPRITE = Identifier.withDefaultNamespace("pink_dye");
    static final Identifier LIME_DYE_SPRITE = Identifier.withDefaultNamespace("lime_dye");
    static final Identifier BUTTON_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/beacon/button_disabled");
    static final Identifier BUTTON_SELECTED_SPRITE = Identifier.withDefaultNamespace("container/beacon/button_selected");
    static final Identifier BUTTON_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("container/beacon/button_highlighted");
    static final Identifier BUTTON_SPRITE = Identifier.withDefaultNamespace("container/beacon/button");
    static final Identifier RECIPEBOOK_BUTTON_SPRITE = Identifier.withDefaultNamespace("sprites/recipe_book/button");
    static final Identifier TEXT_FIELD_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field");
    static final Identifier TEXT_FIELD_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field_disabled");
    static final Identifier MAIN_BULLETIN_BOARD_LOCATION = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/bulletin_board/main_panel.png");
    static final Identifier SIDE_BULLETIN_BOARD_LOCATION = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/bulletin_board/side_panel.png");
    static final Identifier GREEN_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/green_note");
    static final Identifier BIG_GREEN_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_green_note");
    static final Identifier BLUE_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/blue_note");
    static final Identifier BIG_BLUE_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_blue_note");
    static final Identifier PINK_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/pink_note");
    static final Identifier BIG_PINK_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_pink_note");
    static final Identifier YELLOW_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/yellow_note");
    static final Identifier BIG_YELLOW_NOTE = Identifier.fromNamespaceAndPath(QOLVaultsAndNotes.MODID, "textures/gui/container/big_yellow_note");
    private final List<BulletinBoardScreenButton> sidePanelButtons = new ArrayList<>();
    //private final List<BulletinBoardNoteButton> noteButtons = new ArrayList<>(); Suggestion for making a list of all stored notes and they will be as buttons
    private BulletinBoardSpriteScreenButton toggleExtendedButton;
    private TextureAtlas calc;
    public boolean extendedMenu;
    private int menuStartX;
    private int menuStartY;

    public BulletinBoardScreen(BulletinBoardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        extendedMenu = false;
        this.imageWidth = 256;
        this.imageHeight = 128;
        menuStartX = (this.width - this.imageWidth) / 2;
        menuStartY = (this.height - this.imageHeight) / 2;
    }

    public void toggleExtendedMenu() {
        extendedMenu = !extendedMenu;
        updateToggleButtonPosition();
        updateSidePanelVisibility();
    }

    private void updateToggleButtonPosition() {
        if (extendedMenu) {
            // Extended: main panel is at menuStartX, side panel starts at menuStartX+256
            // Place button wherever it should go on the extended layout
            int totalWidth = 256 + 128;
            int extendedStartX = (this.width - totalWidth) / 2;
            int extendedStartY = (this.height - 128) / 2;
            toggleExtendedButton.setX(extendedStartX + 230);
            toggleExtendedButton.setY(extendedStartY + 54);
        } else {
            toggleExtendedButton.setX(this.leftPos + 230);
            toggleExtendedButton.setY(this.topPos + 54);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_282132_, int p_283078_, int p_283647_) {
        if(extendedMenu) {
            int totalWidth = 256 + 128;
            imageHeight = 128;
            menuStartX = (this.width - totalWidth) / 2;
            menuStartY = (this.height - this.imageHeight) / 2;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_BULLETIN_BOARD_LOCATION, menuStartX, menuStartY, 0.0F, 0.0F, 256, 128, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SIDE_BULLETIN_BOARD_LOCATION, menuStartX+256, menuStartY, 0.0F, 0.0F, 128, 151, 256, 256);
            /*ItemStack inkSac = new ItemStack(Items.INK_SAC, 0);
            ItemStack paper = new ItemStack(Items.PAPER, 2);*/

            guiGraphics.renderFakeItem(new ItemStack(Items.INK_SAC), menuStartX + 256 + 77, menuStartY + 130);
            guiGraphics.renderFakeItem(new ItemStack(Items.PAPER), menuStartX + 256 + 98, menuStartY + 130);

            guiGraphics.renderItemDecorations(minecraft.font, new ItemStack(Items.INK_SAC), menuStartX + 256 + 77, menuStartY + 130, "0");
            guiGraphics.renderItemDecorations(minecraft.font, new ItemStack(Items.PAPER), menuStartX + 256 + 98, menuStartY + 130, "0");

        } else {
            menuStartX = (this.width - this.imageWidth) / 2;
            menuStartY = (this.height - this.imageHeight) / 2;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_BULLETIN_BOARD_LOCATION, menuStartX, menuStartY, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //DO NOTHING. This is because we don't want the "Inventory" label to pop up, the inventory isn't supposed to be visible.
        return;
    }

    @Override
    protected void init() {
        super.init();
        sidePanelButtons.clear();

        BulletinBoardSpriteScreenButton screenButton = new BulletinBoardSpriteScreenButton(
                this.leftPos + 230, this.topPos + 54, BUTTON_SPRITE, Component.literal("")
        ){
            @Override
            public void onPress(InputWithModifiers input) {
                toggleExtendedMenu();
            }
            @Override
            protected void renderIcon(GuiGraphics guiGraphics) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, RECIPEBOOK_BUTTON_SPRITE, this.getX() + 2, this.getY() + 2, 16, 16);
                //guiGraphics.renderFakeItem(new ItemStack(Items.)
            }
        };
        toggleExtendedButton = screenButton;
        this.addRenderableWidget(screenButton);

        addSidePanelButton(new BulletinBoardSpriteScreenButton(
                0, 0,
                BUTTON_SPRITE, Component.literal("Button A")
        ) {
            @Override
            public void onPress(InputWithModifiers input) {
                // your action here
            }
        }, 11, 128);

        addSidePanelButton(new BulletinBoardSpriteScreenButton(
                0, 0,
                BUTTON_SPRITE, Component.literal("Button B")
        ) {
            @Override
            public void onPress(InputWithModifiers input) {
                // your action here
            }
        }, 33, 128);
        updateSidePanelVisibility();
    }

    private void addSidePanelButton(BulletinBoardScreenButton button, int sidePanelOffsetX, int sidePanelOffsetY) {
        int totalWidth = 256 + 128;
        int sidePanelX = (this.width - totalWidth) / 2 + 256;
        int sidePanelY = (this.height - 128) / 2;
        button.setX(sidePanelX + sidePanelOffsetX);
        button.setY(sidePanelY + sidePanelOffsetY);
        sidePanelButtons.add(button);
        this.addRenderableWidget(button);
    }

    private void updateSidePanelVisibility() {
        for (BulletinBoardScreenButton button : sidePanelButtons) {
            button.visible = extendedMenu;
        }
    }

    public void containerTick() {
        super.containerTick();
/*
        this.updateButtons();
*/
    }



    public void render(GuiGraphics p_283062_, int p_282876_, int p_282015_, float p_281395_) {
        super.render(p_283062_, p_282876_, p_282015_, p_281395_);
        this.renderTooltip(p_283062_, p_282876_, p_282015_);
    }

    abstract static class BulletinBoardScreenButton extends AbstractButton implements BulletinBoardButton {
        private boolean selected;

        protected BulletinBoardScreenButton(int x, int y) {
            super(x, y, 20, 20, CommonComponents.EMPTY);
        }

        protected BulletinBoardScreenButton(int x, int y, Component message) {
            super(x, y, 20, 20, message);
        }

        public void renderContents(GuiGraphics guiGraphics, int idkkk, int idkk, float p_283562_) {
            Identifier identifier;
            if (!this.active) {
                identifier = BulletinBoardScreen.BUTTON_DISABLED_SPRITE;
            } else if (this.selected) {
                identifier = BulletinBoardScreen.BUTTON_SELECTED_SPRITE;
            } else if (this.isHoveredOrFocused()) {
                identifier = BulletinBoardScreen.BUTTON_HIGHLIGHTED_SPRITE;
            } else {
                identifier = BulletinBoardScreen.BUTTON_SPRITE;
            }

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.width, this.height);
            this.renderIcon(guiGraphics);
        }

        protected abstract void renderIcon(GuiGraphics var1);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void updateWidgetNarration(NarrationElementOutput p_259705_) {
            this.defaultButtonNarrationText(p_259705_);
        }
    }

    class BulletinBoardSpriteScreenButton extends BulletinBoardScreenButton {
        private final Identifier sprite;


        protected BulletinBoardSpriteScreenButton(int x, int y, Identifier sprite, Component message) {
            super(x, y, message);
            this.setTooltip(Tooltip.create(message));
            this.sprite = sprite;
        }

        protected void renderIcon(GuiGraphics guiGraphics) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.getX() + 2, this.getY() + 2, 16, 16);
        }

        @Override
        public void updateStatus(int var1) {

        }

        @Override
        public void onPress(InputWithModifiers input) {
            toggleExtendedMenu();
        }
    }

    interface BulletinBoardButton {
        void updateStatus(int var1);
    }

}
