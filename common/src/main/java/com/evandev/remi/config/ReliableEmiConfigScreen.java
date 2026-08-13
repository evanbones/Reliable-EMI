package com.evandev.remi.config;

import com.evandev.ReliableEmi;
import com.evandev.remi.feature.creativemodetab.gui.CreativeModeTabConfigScreen;
import com.evandev.remi.feature.stackgroup.gui.StackGroupConfigScreen;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReliableEmiConfigScreen {

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(ReliableEmi.text("configuration.title"))
                .save(ReliableEmiConfig::save);

        ConfigCategory.Builder creativeModeTabs = ConfigCategory.createBuilder()
                .name(ReliableEmi.text("configuration.creativeModeTabs"))
                .option(createBoolOption("enableCreativeModeTabs", true,
                        () -> ReliableEmiConfig.enableCreativeModeTabs, v -> ReliableEmiConfig.enableCreativeModeTabs = v))
                .option(createEnumOption("creativeTabSidebarTarget", ReliableEmiConfig.CreativeTabSidebarTarget.INDEX,
                        ReliableEmiConfig.CreativeTabSidebarTarget.class,
                        () -> ReliableEmiConfig.creativeTabSidebarTarget, v -> ReliableEmiConfig.creativeTabSidebarTarget = v))
                .option(createEnumOption("creativeTabTheme", ReliableEmiConfig.CreativeTabTheme.SYNCED,
                        ReliableEmiConfig.CreativeTabTheme.class,
                        () -> ReliableEmiConfig.creativeTabTheme, v -> ReliableEmiConfig.creativeTabTheme = v))
                .option(createBoolOption("syncSelectedCreativeModeTab", true,
                        () -> ReliableEmiConfig.syncSelectedCreativeModeTab, v -> ReliableEmiConfig.syncSelectedCreativeModeTab = v))
                .option(createBoolOption("showCreativeTabNameInSearchbar", true,
                        () -> ReliableEmiConfig.showCreativeTabNameInSearchbar, v -> ReliableEmiConfig.showCreativeTabNameInSearchbar = v))
                .option(createIntegerOption("maxSidebarTabs", 0,
                        () -> ReliableEmiConfig.maxSidebarTabs, v -> ReliableEmiConfig.maxSidebarTabs = v))
                .option(ButtonOption.createBuilder()
                        .name(ReliableEmi.text("configuration.disabledCreativeModeTabs.manage"))
                        .description(OptionDescription.of(ReliableEmi.text("configuration.disabledCreativeModeTabs.tooltip")))
                        .action((screen, opt) -> Minecraft.getInstance().setScreen(new CreativeModeTabConfigScreen(screen)))
                        .build());

        boolean inWorld = Minecraft.getInstance().level != null;
        ConfigCategory.Builder stackGroups = ConfigCategory.createBuilder()
                .name(ReliableEmi.text("configuration.stackGroups"))
                .option(createBoolOption("enableStackGroups", true,
                        () -> ReliableEmiConfig.enableStackGroups, v -> ReliableEmiConfig.enableStackGroups = v))
                .option(createBoolOption("stackGroupsIndex", true,
                        () -> ReliableEmiConfig.stackGroupsIndex, v -> ReliableEmiConfig.stackGroupsIndex = v))
                .option(createBoolOption("stackGroupsCraftables", true,
                        () -> ReliableEmiConfig.stackGroupsCraftables, v -> ReliableEmiConfig.stackGroupsCraftables = v))
                .option(createBoolOption("stackGroupsWorkstation", true,
                        () -> ReliableEmiConfig.stackGroupsWorkstation, v -> ReliableEmiConfig.stackGroupsWorkstation = v))
                .option(createBoolOption("stackGroupsFavorites", false,
                        () -> ReliableEmiConfig.stackGroupsFavorites, v -> ReliableEmiConfig.stackGroupsFavorites = v))
                .option(createBoolOption("enableCreateStackGroupButton", true,
                        () -> ReliableEmiConfig.enableCreateStackGroupButton, v -> ReliableEmiConfig.enableCreateStackGroupButton = v))
                .option(ButtonOption.createBuilder()
                        .name(ReliableEmi.text("configuration.disabledStackGroups.manage"))
                        .description(OptionDescription.of(ReliableEmi.text(inWorld
                                ? "configuration.disabledStackGroups.tooltip"
                                : "configuration.disabledStackGroups.tooltip.unavailable")))
                        .action((screen, opt) -> Minecraft.getInstance().setScreen(new StackGroupConfigScreen(screen)))
                        .available(inWorld)
                        .build());

        Option<Boolean> showTitleInsteadOfPageNumbers = createBoolOption("showTitleInsteadOfPageNumbers", false,
                () -> ReliableEmiConfig.showTitleInsteadOfPageNumbers || ReliableEmiConfig.scrollInsteadOfPagination,
                v -> ReliableEmiConfig.showTitleInsteadOfPageNumbers = v || ReliableEmiConfig.scrollInsteadOfPagination);
        showTitleInsteadOfPageNumbers.setAvailable(!ReliableEmiConfig.scrollInsteadOfPagination);

        Option<Boolean> verticalScrollbar = createBoolOption("verticalScrollbar", false,
                () -> ReliableEmiConfig.verticalScrollbar, v -> ReliableEmiConfig.verticalScrollbar = v);
        verticalScrollbar.setAvailable(ReliableEmiConfig.scrollInsteadOfPagination);

        Option<Boolean> scrollInsteadOfPagination = Option.<Boolean>createBuilder()
                .name(ReliableEmi.text("configuration.scrollInsteadOfPagination"))
                .description(OptionDescription.of(ReliableEmi.text("configuration.scrollInsteadOfPagination.tooltip")))
                .binding(false, () -> ReliableEmiConfig.scrollInsteadOfPagination, v -> ReliableEmiConfig.scrollInsteadOfPagination = v)
                .controller(TickBoxControllerBuilder::create)
                .addListener((opt, event) -> {
                    if (event != OptionEventListener.Event.STATE_CHANGE) return;
                    boolean v = opt.pendingValue();
                    if (v) showTitleInsteadOfPageNumbers.requestSet(true);
                    showTitleInsteadOfPageNumbers.setAvailable(!v);
                    verticalScrollbar.setAvailable(v);
                })
                .build();

        ConfigCategory.Builder miscellaneous = ConfigCategory.createBuilder()
                .name(ReliableEmi.text("configuration.miscellaneous"))
                .option(createBoolOption("dragCheatToInventory", true,
                        () -> ReliableEmiConfig.dragCheatToInventory, v -> ReliableEmiConfig.dragCheatToInventory = v))
                .option(createBoolOption("emiOnlyInRecipeBook", false,
                        () -> ReliableEmiConfig.emiOnlyInRecipeBook, v -> ReliableEmiConfig.emiOnlyInRecipeBook = v))
                .option(createBoolOption("disablePaginationWrapping", false,
                        () -> ReliableEmiConfig.disablePaginationWrapping, v -> ReliableEmiConfig.disablePaginationWrapping = v))
                .option(scrollInsteadOfPagination)
                .option(showTitleInsteadOfPageNumbers)
                .option(verticalScrollbar)
                .option(createBoolOption("hidePageButtonWhenOnePage", false,
                        () -> ReliableEmiConfig.hidePageButtonWhenOnePage, v -> ReliableEmiConfig.hidePageButtonWhenOnePage = v))
                .option(createBoolOption("incrementalScrollbarFill", false,
                        () -> ReliableEmiConfig.incrementalScrollbarFill, v -> ReliableEmiConfig.incrementalScrollbarFill = v));

        ConfigCategory.Builder searchWidget = ConfigCategory.createBuilder()
                .name(ReliableEmi.text("configuration.searchWidget"))
                .option(createBoolOption("searchWidgetAlignWithPanel", false,
                        () -> ReliableEmiConfig.searchWidgetAlignWithPanel, v -> ReliableEmiConfig.searchWidgetAlignWithPanel = v))
                .option(createIntegerOption("searchWidgetWidth", 0,
                        () -> ReliableEmiConfig.searchWidgetWidth, v -> ReliableEmiConfig.searchWidgetWidth = v))
                .option(createIntegerOption("searchWidgetLeftOffset", 0,
                        () -> ReliableEmiConfig.searchWidgetLeftOffset, v -> ReliableEmiConfig.searchWidgetLeftOffset = v))
                .option(createIntegerOption("searchWidgetTopOffset", 0,
                        () -> ReliableEmiConfig.searchWidgetTopOffset, v -> ReliableEmiConfig.searchWidgetTopOffset = v))
                .option(createIntegerOption("searchWidgetHorizontalPadding", 4,
                        () -> ReliableEmiConfig.searchWidgetHorizontalPadding, v -> ReliableEmiConfig.searchWidgetHorizontalPadding = v))
                .option(createIntegerOption("searchWidgetVerticalPadding", 2,
                        () -> ReliableEmiConfig.searchWidgetVerticalPadding, v -> ReliableEmiConfig.searchWidgetVerticalPadding = v))
                .option(createColorOption("searchWidgetTextColor", new Color(0xFFFFFF),
                        () -> new Color(ReliableEmiConfig.searchWidgetTextColor), v -> ReliableEmiConfig.searchWidgetTextColor = v.getRGB()))
                .option(createColorOption("searchWidgetSuggestionTextColor", new Color(0x808080),
                        () -> new Color(ReliableEmiConfig.searchWidgetSuggestionTextColor), v -> ReliableEmiConfig.searchWidgetSuggestionTextColor = v.getRGB()))
                .option(createBoolOption("searchWidgetUseVanillaTexture", false,
                        () -> ReliableEmiConfig.searchWidgetUseVanillaTexture, v -> ReliableEmiConfig.searchWidgetUseVanillaTexture = v));


        ConfigCategory.Builder tags = ConfigCategory.createBuilder()
                .name(ReliableEmi.text("configuration.tags"))
                .option(createBoolOption("enableCategorizedTagPages", true,
                        () -> ReliableEmiConfig.enableCategorizedTagPages, v -> ReliableEmiConfig.enableCategorizedTagPages = v))
                .option(createBoolOption("enableEntityTags", true,
                        () -> ReliableEmiConfig.enableEntityTags, v -> ReliableEmiConfig.enableEntityTags = v))
                .option(createBoolOption("enableTagSearchEnhancements", true,
                        () -> ReliableEmiConfig.enableTagSearchEnhancements, v -> ReliableEmiConfig.enableTagSearchEnhancements = v));

        return builder
                .category(creativeModeTabs.build())
                .category(stackGroups.build())
                .category(tags.build())
                .category(miscellaneous.build())
                .category(searchWidget.build())
                .build()
                .generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String key, boolean defaultValue,
                                                    Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(ReliableEmi.text("configuration." + key))
                .description(OptionDescription.of(ReliableEmi.text("configuration." + key + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> createIntegerOption(String key, int defaultValue,
                                                       Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(ReliableEmi.text("configuration." + key))
                .description(OptionDescription.of(ReliableEmi.text("configuration." + key + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(IntegerFieldControllerBuilder::create)
                .build();
    }

    private static Option<Color> createColorOption(String key, Color defaultValue,
                                                   Supplier<Color> getter, Consumer<Color> setter) {
        return Option.<Color>createBuilder()
                .name(ReliableEmi.text("configuration." + key))
                .description(OptionDescription.of(ReliableEmi.text("configuration." + key + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(ColorControllerBuilder::create)
                .build();
    }

    private static <T extends Enum<T>> Option<T> createEnumOption(String key, T defaultValue, Class<T> enumClass,
                                                                  Supplier<T> getter, Consumer<T> setter) {
        return Option.<T>createBuilder()
                .name(ReliableEmi.text("configuration." + key))
                .description(OptionDescription.of(ReliableEmi.text("configuration." + key + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(enumClass)
                        .formatValue(v -> ReliableEmi.text("enum." + key + "." + v.name().toLowerCase(Locale.ROOT))))
                .build();
    }
}
