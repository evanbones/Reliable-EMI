package com.evandev.emixx.mixin;

import com.evandev.emixx.config.EmiPlusPlusConfig;
import com.evandev.emixx.integration.emi.ScreenManager;
import com.evandev.emixx.util.SidebarPanelWithScrollOffset;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.config.SidebarPages;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SidebarButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public abstract class EmiScreenManagerSidebarPanelMixin implements SidebarPanelWithScrollOffset {

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public abstract List<EmiScreenManager.ScreenSpace> getSpaces();

    @Shadow
    public EmiScreenManager.ScreenSpace space;

    @Final
    @Shadow
    public SizedButtonWidget pageLeft, pageRight;

    @Final
    @Shadow
    public SidebarButtonWidget cycle;

    @Final
    @Shadow
    public SidebarPages pages;

    @Shadow
    public int page;

    @Unique
    private int emixx$scrollOffsetRows = 0;

    public int emixx$getScrollOffset() {
        return this.emixx$scrollOffsetRows * this.space.tw;
    }

    @Unique
    public int emixx$getScrollOffsetRows() {
        return this.emixx$scrollOffsetRows;
    }

    @Unique
    public int emixx$getTotalScrollRows() {
        return Math.max((space.getStacks().size() - 1) / space.tw + 1 - space.th, 0);
    }

    @WrapOperation(method = "drawHeader", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/EmiRenderHelper;getPageText(III)Lnet/minecraft/network/chat/Component;", remap = true))
    private Component replaceIndexHeader(int page, int total, int maxWidth, Operation<Component> original) {
        if (getType() == SidebarType.INDEX && ScreenManager.customIndexTitle != null) {
            return ScreenManager.customIndexTitle;
        } else if (EmiPlusPlusConfig.showTitleInsteadOfPageNumbers){
            return getType().getText();
        }

        return original.call(page, total, maxWidth);
    }

    @Inject(at = @At("TAIL"), method = "setSpaces")
    private void addEmiPlusPlusWidgets(EmiScreenManager.ScreenSpace main, List<EmiScreenManager.ScreenSpace> subpanels, CallbackInfo ci) {
        getSpaces().stream()
                .filter(space -> space.getType() == SidebarType.INDEX)
                .findFirst()
                .ifPresent(ScreenManager::onIndexScreenSpaceCreated);
    }

    @WrapOperation(method="render", at = @At(value = "INVOKE", target="dev/emi/emi/screen/EmiScreenManager$ScreenSpace.render (Ldev/emi/emi/runtime/EmiDrawContext;IIFI)V", ordinal = 0, remap = true))
    private void addScrollOffsetToScreen(EmiScreenManager.ScreenSpace space, EmiDrawContext cy, int stack, int xo, float yo, int hx, Operation<Void> original) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            original.call(space, cy, stack, xo, yo, this.emixx$getScrollOffset());
        } else {
            original.call(space, cy, stack, xo, yo, hx);
        }
    }

    @WrapOperation(method="drawHeader", at = @At(value = "INVOKE", target="Ldev/emi/emi/EmiRenderHelper;drawScroll(Ldev/emi/emi/runtime/EmiDrawContext;IIIIIII)V", remap = true))
    private void drawScrollBar(EmiDrawContext context, int x, int y, int width, int height, int progress, int total, int color, Operation<Void> original) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            progress = this.emixx$getScrollOffsetRows();
            total = this.emixx$getTotalScrollRows() + this.space.th;

            double segment = (double) width / total;
            int start = (int) (x + segment * progress);
            int end = (int) (start + segment * this.space.th);

            if (progress == this.emixx$getTotalScrollRows()) {
                end = x + width;
                start = (int) (end - Math.max(segment * this.space.th, 1));
            }

            context.fill(start, y, end - start, height, color);
        } else {
            original.call(context, x, y, width, height, progress, total, color);
        }
    }

    @ModifyVariable(method = "drawHeader", at = @At(value = "STORE"), name = "scrollLeft")
    private static int modifyScrollX(int scrollLeft) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            return scrollLeft - 18;
        } else {
            return scrollLeft;
        }
    }

    @ModifyVariable(method = "drawHeader", at = @At(value = "STORE"), name = "scrollWidth")
    private static int modifyScrollWidth(int scrollWidth) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            return scrollWidth + 36;
        } else {
            return scrollWidth;
        }
    }

    @Inject(method = "wrapPage", at = @At("HEAD"), cancellable = true)
    public void clampScrollOffset(CallbackInfo ci) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            ci.cancel();
            if (emixx$scrollOffsetRows >= this.emixx$getTotalScrollRows()) {
                emixx$scrollOffsetRows = this.emixx$getTotalScrollRows();
                space.batcher.repopulate();
            } else if (emixx$scrollOffsetRows <= 0) {
                emixx$scrollOffsetRows = 0;
                space.batcher.repopulate();
            }
        } else if (EmiPlusPlusConfig.disablePaginationWrapping) {
            ci.cancel();
            int totalPages = (space.getStacks().size() - 1) / space.pageSize + 1;
            if (this.page >= totalPages) {
                this.page = totalPages - 1;
                space.batcher.repopulate();
            } else if (this.page < 0) {
                this.page = 0;
                space.batcher.repopulate();
            }
        }
    }

    @Inject(method = "updateWidgetVisibility", at = @At("TAIL"))
    public void hideButtons(CallbackInfo ci) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            pageLeft.visible = false;
            pageRight.visible = false;
        }
        if (EmiPlusPlusConfig.hidePageButtonWhenOnePage && this.pages.pages.size() == 1) {
            cycle.visible = false;
        }
    }

    @WrapOperation(method="<init>", at = @At(value="NEW", target = "dev/emi/emi/screen/widget/SizedButtonWidget", ordinal = 0))
    private SizedButtonWidget pageLeftButton(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, Button.OnPress action, Operation<SizedButtonWidget> original) {
        BooleanSupplier hasPrevPage = () -> {
            if (EmiPlusPlusConfig.disablePaginationWrapping) {
                return isActive.getAsBoolean() && this.page > 0;
            } else {
                return isActive.getAsBoolean();
            }
        };
        return original.call(x, y, width, height, u, v, hasPrevPage, action);
    }

    @WrapOperation(method="<init>", at = @At(value="NEW", target = "dev/emi/emi/screen/widget/SizedButtonWidget", ordinal = 1))
    private SizedButtonWidget pageRightButton(int x, int y, int width, int height, int u, int v, BooleanSupplier isActive, Button.OnPress action, Operation<SizedButtonWidget> original) {
        BooleanSupplier hasNextPage = () -> {
            if (EmiPlusPlusConfig.disablePaginationWrapping) {
                int totalPages = (this.space.getStacks().size() - 1) / this.space.pageSize;
                return isActive.getAsBoolean() && this.page < totalPages;
            } else {
                return isActive.getAsBoolean();
            }
        };
        return original.call(x, y, width, height, u, v, hasNextPage, action);
    }

    @Inject(method = "updateWidgetPosition", at = @At("TAIL"))
    public void moveCycleButton(CallbackInfo ci) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            cycle.setX(space.tx);
        }
    }

    @Inject(method = "scroll", at = @At("HEAD"), cancellable = true)
    public void scroll(int delta, CallbackInfo ci) {
        if (EmiPlusPlusConfig.scrollInsteadOfPagination) {
            ci.cancel();

            if (this.space == null) {
                return;
            }

            if (this.space.pageSize == 0) {
                return;
            }

            if (this.emixx$getTotalScrollRows() == 0) {
                return;
            }

            emixx$scrollOffsetRows += delta;

            if (emixx$scrollOffsetRows >= this.emixx$getTotalScrollRows()) {
                emixx$scrollOffsetRows = this.emixx$getTotalScrollRows();
            } else if (emixx$scrollOffsetRows <= 0) {
                emixx$scrollOffsetRows = 0;
            }

            space.batcher.repopulate();
        } else if (EmiPlusPlusConfig.disablePaginationWrapping) {
            ci.cancel();

            if (this.space == null) {
                return;
            }
            if (space.pageSize == 0) {
                return;
            }
            page += delta;
            int pageSize = space.pageSize;
            int totalPages = (space.getStacks().size() - 1) / pageSize + 1;
            if (totalPages <= 1) {
                return;
            }
            if (page >= totalPages) {
                page = totalPages - 1;
            } else if (page < 0) {
                page = 0;
            }
            space.batcher.repopulate();
        }
    }
}
