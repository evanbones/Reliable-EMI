package com.evandev.emixx.config;

import com.evandev.EmiPlusPlus;
import com.evandev.emixx.feature.creativemodetab.gui.CreativeModeTabConfigScreen;
import com.evandev.emixx.feature.stackgroup.gui.StackGroupConfigScreen;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EmiPlusPlusConfigScreen {

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(EmiPlusPlus.text("configuration.title"))
                .save(EmiPlusPlusConfig::save);

        ConfigCategory.Builder creativeModeTabs = ConfigCategory.createBuilder()
                .name(EmiPlusPlus.text("configuration.creativeModeTabs"))
                .option(createBoolOption("enableCreativeModeTabs", true,
                        () -> EmiPlusPlusConfig.enableCreativeModeTabs, v -> EmiPlusPlusConfig.enableCreativeModeTabs = v))
                .option(createBoolOption("syncSelectedCreativeModeTab", true,
                        () -> EmiPlusPlusConfig.syncSelectedCreativeModeTab, v -> EmiPlusPlusConfig.syncSelectedCreativeModeTab = v))
                .option(createBoolOption("showCreativeTabNameInSearchbar", true,
                        () -> EmiPlusPlusConfig.showCreativeTabNameInSearchbar, v -> EmiPlusPlusConfig.showCreativeTabNameInSearchbar = v))
                .option(ButtonOption.createBuilder()
                        .name(EmiPlusPlus.text("configuration.disabledCreativeModeTabs.manage"))
                        .description(OptionDescription.of(EmiPlusPlus.text("configuration.disabledCreativeModeTabs.tooltip")))
                        .action((screen, opt) -> Minecraft.getInstance().setScreen(new CreativeModeTabConfigScreen(screen)))
                        .build());

        boolean inWorld = Minecraft.getInstance().level != null;
        ConfigCategory.Builder stackGroups = ConfigCategory.createBuilder()
                .name(EmiPlusPlus.text("configuration.stackGroups"))
                .option(createBoolOption("enableStackGroups", true,
                        () -> EmiPlusPlusConfig.enableStackGroups, v -> EmiPlusPlusConfig.enableStackGroups = v))
                .option(createBoolOption("enableCreateStackGroupButton", true,
                        () -> EmiPlusPlusConfig.enableCreateStackGroupButton, v -> EmiPlusPlusConfig.enableCreateStackGroupButton = v))
                .option(ButtonOption.createBuilder()
                        .name(EmiPlusPlus.text("configuration.disabledStackGroups.manage"))
                        .description(OptionDescription.of(EmiPlusPlus.text(inWorld
                                ? "configuration.disabledStackGroups.tooltip"
                                : "configuration.disabledStackGroups.tooltip.unavailable")))
                        .action((screen, opt) -> Minecraft.getInstance().setScreen(new StackGroupConfigScreen(screen)))
                        .available(inWorld)
                        .build());

        Option<Boolean> showTitleInsteadOfPageNumbers = createBoolOption("showTitleInsteadOfPageNumbers", false,
                () -> EmiPlusPlusConfig.showTitleInsteadOfPageNumbers || EmiPlusPlusConfig.scrollInsteadOfPagination,
                v -> EmiPlusPlusConfig.showTitleInsteadOfPageNumbers = v || EmiPlusPlusConfig.scrollInsteadOfPagination);
        showTitleInsteadOfPageNumbers.setAvailable(!EmiPlusPlusConfig.scrollInsteadOfPagination);

        Option<Boolean> scrollInsteadOfPagination = Option.<Boolean>createBuilder()
                .name(EmiPlusPlus.text("configuration.scrollInsteadOfPagination"))
                .description(OptionDescription.of(EmiPlusPlus.text("configuration.scrollInsteadOfPagination.tooltip")))
                .binding(false, () -> EmiPlusPlusConfig.scrollInsteadOfPagination, v -> EmiPlusPlusConfig.scrollInsteadOfPagination = v)
                .controller(TickBoxControllerBuilder::create)
                .addListener((opt, event) -> {
                    if (event != OptionEventListener.Event.STATE_CHANGE) return;
                    boolean v = opt.pendingValue();
                    if (v) showTitleInsteadOfPageNumbers.requestSet(true);
                    showTitleInsteadOfPageNumbers.setAvailable(!v);
                })
                .build();

        ConfigCategory.Builder miscellaneous = ConfigCategory.createBuilder()
                .name(EmiPlusPlus.text("configuration.miscellaneous"))
                .option(createBoolOption("emiOnlyInRecipeBook", false,
                        () -> EmiPlusPlusConfig.emiOnlyInRecipeBook, v -> EmiPlusPlusConfig.emiOnlyInRecipeBook = v))
                .option(createBoolOption("disablePaginationWrapping", false,
                        () -> EmiPlusPlusConfig.disablePaginationWrapping, v -> EmiPlusPlusConfig.disablePaginationWrapping = v))
                .option(scrollInsteadOfPagination)
                .option(showTitleInsteadOfPageNumbers)
                .option(createBoolOption("hidePageButtonWhenOnePage", false,
                        () -> EmiPlusPlusConfig.hidePageButtonWhenOnePage, v -> EmiPlusPlusConfig.hidePageButtonWhenOnePage = v))
                .option(createBoolOption("incrementalScrollbarFill", false,
                        () -> EmiPlusPlusConfig.incrementalScrollbarFill, v -> EmiPlusPlusConfig.incrementalScrollbarFill = v));

        return builder
                .category(creativeModeTabs.build())
                .category(stackGroups.build())
                .category(miscellaneous.build())
                .build()
                .generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String key, boolean defaultValue,
                                                      Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(EmiPlusPlus.text("configuration." + key))
                .description(OptionDescription.of(EmiPlusPlus.text("configuration." + key + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }
}
