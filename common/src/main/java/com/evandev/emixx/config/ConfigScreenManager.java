package com.evandev.emixx.config;

import com.evandev.EmiPlusPlus;
import com.evandev.emixx.feature.creativemodetab.gui.CreativeModeTabConfigScreen;
import com.evandev.emixx.feature.stackgroup.gui.StackGroupConfigScreen;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.widget.config.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Supplier;

public class ConfigScreenManager {

    public static void injectConfigScreen(ConfigScreen configScreen, ListWidget list, ConfigSearch search) {
        Supplier<String> searcher = search::getSearch;
        GroupNameWidget root = new GroupNameWidget(EmiPlusPlus.MOD_ID, EmiPlusPlus.text("configuration.title"));
        list.addEntry(root);

        SubGroupNameWidget tabsGroup = addSubGroup(root, list, "creativeModeTabs");
        addBool(configScreen, list, root, tabsGroup, searcher, "creativeModeTabs", "enableCreativeModeTabs",
                () -> EmiPlusPlusConfig.enableCreativeModeTabs, v -> EmiPlusPlusConfig.enableCreativeModeTabs = v, false);
        addBool(configScreen, list, root, tabsGroup, searcher, "creativeModeTabs", "syncSelectedCreativeModeTab",
                () -> EmiPlusPlusConfig.syncSelectedCreativeModeTab, v -> EmiPlusPlusConfig.syncSelectedCreativeModeTab = v, false);
        addBool(configScreen, list, root, tabsGroup, searcher, "creativeModeTabs", "showCreativeTabNameInSearchbar",
                () -> EmiPlusPlusConfig.showCreativeTabNameInSearchbar, v -> EmiPlusPlusConfig.showCreativeTabNameInSearchbar = v, false);
        addAction(list, root, tabsGroup, searcher, "creativeModeTabs", "disabledCreativeModeTabs",
                () -> Minecraft.getInstance().setScreen(new CreativeModeTabConfigScreen()), true);

        SubGroupNameWidget sgGroup = addSubGroup(root, list, "stackGroups");
        addBool(configScreen, list, root, sgGroup, searcher, "stackGroups", "enableStackGroups",
                () -> EmiPlusPlusConfig.enableStackGroups, v -> EmiPlusPlusConfig.enableStackGroups = v, false);
        addBool(configScreen, list, root, sgGroup, searcher, "stackGroups", "enableCreateStackGroupButton",
                () -> EmiPlusPlusConfig.enableCreateStackGroupButton, v -> EmiPlusPlusConfig.enableCreateStackGroupButton = v, false);
        addAction(list, root, sgGroup, searcher, "stackGroups", "disabledStackGroups",
                () -> Minecraft.getInstance().setScreen(new StackGroupConfigScreen()), true);

        SubGroupNameWidget miscGroup = addSubGroup(root, list, "miscellaneous");
        addBool(configScreen, list, root, miscGroup, searcher, "miscellaneous", "emiOnlyInRecipeBook",
                () -> EmiPlusPlusConfig.emiOnlyInRecipeBook, v -> EmiPlusPlusConfig.emiOnlyInRecipeBook = v, false);
        addBool(configScreen, list, root, miscGroup, searcher, "miscellaneous", "disablePaginationWrapping",
                () -> EmiPlusPlusConfig.disablePaginationWrapping, v -> EmiPlusPlusConfig.disablePaginationWrapping = v, false);
        addBool(configScreen, list, root, miscGroup, searcher, "miscellaneous", "scrollInsteadOfPagination",
                () -> EmiPlusPlusConfig.scrollInsteadOfPagination, v -> EmiPlusPlusConfig.scrollInsteadOfPagination = v, false);
        addBool(configScreen, list, root, miscGroup, searcher, "miscellaneous", "showTitleInsteadOfPageNumbers",
                () -> EmiPlusPlusConfig.showTitleInsteadOfPageNumbers || EmiPlusPlusConfig.scrollInsteadOfPagination, v -> EmiPlusPlusConfig.showTitleInsteadOfPageNumbers = v || EmiPlusPlusConfig.scrollInsteadOfPagination, false);
        addBool(configScreen, list, root, miscGroup, searcher, "miscellaneous", "hidePageButtonWhenOnePage",
                () -> EmiPlusPlusConfig.hidePageButtonWhenOnePage, v -> EmiPlusPlusConfig.hidePageButtonWhenOnePage = v, true);
    }

    private static SubGroupNameWidget addSubGroup(GroupNameWidget root, ListWidget list, String key) {
        SubGroupNameWidget w = new SubGroupNameWidget(EmiPlusPlus.MOD_ID + ".groupName",
                EmiPlusPlus.text("configuration." + key));
        w.parent = root;
        list.addEntry(w);
        return w;
    }

    private static void addBool(ConfigScreen configScreen, ListWidget list,
                                GroupNameWidget root, SubGroupNameWidget group,
                                Supplier<String> searcher, String groupKey, String itemKey,
                                BooleanGet getter, BooleanSet setter, boolean endGroup) {
        Component title = EmiPlusPlus.text("configuration." + itemKey);
        List<ClientTooltipComponent> tooltip = List.of(ClientTooltipComponent.create(
                EmiPlusPlus.text("configuration." + itemKey + ".tooltip").getVisualOrderText()));
        BooleanWidget w = new BooleanWidget(title, tooltip, searcher, configScreen.new Mutator<>() {
            @Override
            public Boolean getValue() {
                return getter.get();
            }

            @Override
            public void setValue(Boolean v) {
                setter.set(v);
                EmiPlusPlusConfig.save();
            }
        });
        wire(w, root, group, groupKey, endGroup);
        list.addEntry(w);
    }

    private static void addAction(ListWidget list, GroupNameWidget root, SubGroupNameWidget group,
                                  Supplier<String> searcher, String groupKey, String itemKey,
                                  Runnable action, boolean endGroup) {
        Component title = EmiPlusPlus.text("configuration." + itemKey);
        List<ClientTooltipComponent> tooltip = List.of(ClientTooltipComponent.create(
                EmiPlusPlus.text("configuration." + itemKey + ".tooltip").getVisualOrderText()));
        ActionWidget w = new ActionWidget(title, tooltip, searcher, b -> action.run());
        wire(w, root, group, groupKey, endGroup);
        list.addEntry(w);
    }

    private static void wire(ConfigEntryWidget w, GroupNameWidget root, SubGroupNameWidget group,
                             String groupKey, boolean endGroup) {
        w.group = new EmiConfig.ConfigGroup() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return EmiConfig.ConfigGroup.class;
            }

            @Override
            public String value() {
                return groupKey;
            }
        };
        w.parentGroups.add(root);
        root.children.add(w);
        w.parentGroups.add(group);
        group.children.add(w);
        w.endGroup = endGroup;
    }

    @FunctionalInterface
    interface BooleanGet {
        boolean get();
    }

    @FunctionalInterface
    interface BooleanSet {
        void set(boolean v);
    }

    public static class ActionWidget extends ConfigEntryWidget {
        private final Button button;

        public ActionWidget(Component name, List<ClientTooltipComponent> tooltip,
                            Supplier<String> search, Button.OnPress onPress) {
            super(name, tooltip, search, 20);
            String manageKey = name.getContents() instanceof TranslatableContents tc
                    ? tc.getKey() + ".manage" : name.getString();
            this.button = Button.builder(Component.translatable(manageKey), onPress)
                    .bounds(0, 0, 150, 20).build();
            setChildren(List.of(button));
        }

        @Override
        public void update(int y, int x, int width, int height) {
            button.setX(x + width - button.getWidth());
            button.setY(y);
        }
    }
}
