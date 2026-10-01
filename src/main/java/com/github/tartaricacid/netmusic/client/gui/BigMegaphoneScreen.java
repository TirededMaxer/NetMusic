package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.client.network.ClientNetWorkHandler;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneControlMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.StringUtils;

public class BigMegaphoneScreen extends Screen {
    private static final int WIDTH = 240;

    private final BlockPos blockPos;

    private int leftPos;
    private int topPos;

    private EditBox urlTextField;
    private EditBox nameTextField;

    private Component tips = Component.empty();
    private boolean loadedFromBlockEntity = false;

    public BigMegaphoneScreen(BlockPos blockPos) {
        super(Component.translatable("block.netmusic.big_megaphone"));
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - 150) / 2;

        this.initUrlEditBox();
        this.initNameEditBox();

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.select_station"),
                        b -> this.openStationPicker())
                .pos(this.leftPos, this.topPos + 89).size(WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.start"),
                        b -> this.sendAction(BigMegaphoneControlMessage.Action.START))
                .pos(this.leftPos, this.topPos + 64).size(116, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.stop"),
                        b -> this.sendAction(BigMegaphoneControlMessage.Action.STOP))
                .pos(this.leftPos + 124, this.topPos + 64).size(116, 20).build());

        this.initFromBlockEntity();
    }

    private void initUrlEditBox() {
        String previousText = this.urlTextField == null ? "" : this.urlTextField.getValue();
        boolean focused = this.urlTextField != null && this.urlTextField.isFocused();
        this.urlTextField = new EditBox(this.font, this.leftPos, this.topPos + 14, WIDTH, 18,
                Component.translatable("gui.netmusic.big_megaphone.url.accessibility"));
        this.urlTextField.setMaxLength(1024);
        this.urlTextField.setTextColor(0xFFF3EFE0);
        this.urlTextField.setFocused(focused);
        this.urlTextField.setValue(previousText);
        this.addRenderableWidget(this.urlTextField);
    }

    private void initNameEditBox() {
        String previousText = this.nameTextField == null ? "" : this.nameTextField.getValue();
        boolean focused = this.nameTextField != null && this.nameTextField.isFocused();
        this.nameTextField = new EditBox(this.font, this.leftPos, this.topPos + 37, WIDTH, 18,
                Component.translatable("gui.netmusic.big_megaphone.name.accessibility"));
        this.nameTextField.setMaxLength(256);
        this.nameTextField.setTextColor(0xFFF3EFE0);
        this.nameTextField.setFocused(focused);
        this.nameTextField.setValue(previousText);
        this.addRenderableWidget(this.nameTextField);
    }

    private void openStationPicker() {
        this.minecraft.setScreen(new RadioStationScreen(this));
    }

    @Override
    public void tick() {
        super.tick();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(level.getBlockEntity(this.blockPos) instanceof TileEntityBigMegaphone)) {
            this.onClose();
        }
    }

    private void initFromBlockEntity() {
        if (this.loadedFromBlockEntity) {
            return;
        }
        Minecraft minecraft = this.minecraft;
        if (minecraft.level == null) {
            return;
        }
        BlockEntity blockEntity = minecraft.level.getBlockEntity(this.blockPos);
        if (blockEntity instanceof TileEntityBigMegaphone megaphone) {
            this.urlTextField.setValue(megaphone.getStreamUrl());
            this.nameTextField.setValue(megaphone.getDisplayName());
            this.loadedFromBlockEntity = true;
            return;
        }
        this.onClose();
    }

    public void applyStation(String name, String url) {
        this.tips = Component.empty();
        if (this.urlTextField != null) {
            this.urlTextField.setValue(url);
        }
        if (this.nameTextField != null) {
            this.nameTextField.setValue(name);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.centeredText(this.font, this.tips, this.width / 2, this.topPos + 119, 0xFFCF0000);

        super.extractRenderState(graphics, mouseX, mouseY, a);

        if (StringUtils.isBlank(this.urlTextField.getValue()) && !this.urlTextField.isFocused()) {
            MutableComponent placeHolder = Component.translatable("gui.netmusic.big_megaphone.url.tips").withStyle(ChatFormatting.ITALIC);
            graphics.text(this.font, placeHolder, this.leftPos + 5, this.topPos + 19, 0xFFAAAAAA, false);
        }

        if (StringUtils.isBlank(this.nameTextField.getValue()) && !this.nameTextField.isFocused()) {
            MutableComponent placeHolder = Component.translatable("gui.netmusic.big_megaphone.name.tips").withStyle(ChatFormatting.ITALIC);
            graphics.text(this.font, placeHolder, this.leftPos + 5, this.topPos + 42, 0xFFAAAAAA, false);
        }
    }

    private void sendAction(BigMegaphoneControlMessage.Action action) {
        this.tips = Component.empty();
        String url = this.urlTextField.getValue().trim();
        String name = this.nameTextField.getValue().trim();

        if (action != BigMegaphoneControlMessage.Action.STOP) {
            if (StringUtils.isBlank(url)) {
                this.tips = Component.translatable("gui.netmusic.big_megaphone.url.empty");
                return;
            }
            if (!BigMegaphoneUtil.isValidStreamUrl(url)) {
                this.tips = Component.translatable("gui.netmusic.big_megaphone.url.invalid");
                return;
            }
            if (StringUtils.isBlank(name)) {
                this.tips = Component.translatable("gui.netmusic.big_megaphone.name.empty");
                return;
            }
        }

        ClientNetWorkHandler.sendToServer(new BigMegaphoneControlMessage(this.blockPos, url, name, action));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.urlTextField.mouseClicked(event, doubleClick)) {
            this.setFocused(this.urlTextField);
            return true;
        }
        if (this.nameTextField.mouseClicked(event, doubleClick)) {
            this.setFocused(this.nameTextField);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event)) {
            if (this.urlTextField.isFocused() || this.nameTextField.isFocused()) {
                return true;
            }
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
