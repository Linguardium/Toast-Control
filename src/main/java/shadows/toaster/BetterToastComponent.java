package shadows.toaster;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Iterator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.util.Mth;

public class BetterToastComponent extends ToastComponent {

	private Deque<BetterToastInstance<?>> topDownList = new ArrayDeque<>();

	public BetterToastComponent() {
		super(Minecraft.getInstance());
		this.queued = new ControlledDeque();
		this.occupiedSlots = new BitSet(ToastConfig.INSTANCE.toastCount.get());
	}

	@Override
	public void render(PoseStack stack) {
		if (!this.minecraft.options.hideGui) {
			int width = this.minecraft.getWindow().getGuiScaledWidth();
			this.visible.removeIf(inst -> {
				if (inst != null && inst.render(width, stack)) {
					this.occupiedSlots.clear(inst.index, inst.index + inst.slotCount);
					this.topDownList.remove(inst);
					return true;
				}
				return false;
			});

			if (!this.queued.isEmpty() && this.freeSlots() > 0) {
				this.queued.removeIf(toast -> {
					int j = toast.slotCount();
					int k = this.findFreeIndex(j);
					if (k != -1) {
						this.visible.add(new BetterToastInstance<>(toast, k, j));
						this.occupiedSlots.set(k, k + j);
						this.topDownList.forEach(t -> t.animationTime = -1L);
						this.topDownList.addFirst((BetterToastInstance<?>) this.visible.get(k));
						return true;
					}
					return false;
				});
			}

		}
	}

	@Override
	public void clear() {
		super.clear();
		this.topDownList.clear();
	}

	@Override
	public int findFreeIndex(int pSlotCount) {
		if (this.freeSlots() >= pSlotCount) {
			int i = 0;

			for (int j = 0; j < ToastConfig.INSTANCE.toastCount.get(); ++j) {
				if (this.occupiedSlots.get(j)) {
					i = 0;
				} else {
					++i;
					if (i == pSlotCount) { return j + 1 - i; }
				}
			}
		}

		return -1;
	}

	@Override
	public int freeSlots() {
		return ToastConfig.INSTANCE.toastCount.get() - this.occupiedSlots.cardinality();
	}

	public class BetterToastInstance<T extends Toast> extends ToastInstance<T> {

		protected int forcedShowTime = 0;

		protected BetterToastInstance(T toast, int index, int slotCount) {
			super(toast, index, slotCount);
			ToastControl.tracker.add(this);
		}

		public boolean tick() {
			return this.forcedShowTime++ > ToastConfig.INSTANCE.forceTime.get();
		}

		protected float getVisibility(long sysTime) {
			float f = Mth.clamp((sysTime - this.animationTime) / 600F, 0F, 1F);
			f = f * f;
			if (ToastConfig.INSTANCE.noSlide.get()) return 1;
			return this.forcedShowTime > ToastConfig.INSTANCE.forceTime.get() && this.visibility == Toast.Visibility.HIDE ? 1F - f : f;
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean render(int scaledWidth, PoseStack pStack) {
			long sysTime = Util.getMillis();
			int trueIdx = 0;

			if (ToastConfig.INSTANCE.topDown.get()) {
				Iterator<BetterToastInstance<?>> it = BetterToastComponent.this.topDownList.iterator();
				while (it.hasNext()) {
					var next = it.next();
					if (next == this) {
						break;
					}
					trueIdx++;
				}
			}

			if (this.animationTime == -1L) {
				this.animationTime = sysTime;
				this.visibility.playSound(BetterToastComponent.this.minecraft.getSoundManager());
			}

			if (this.visibility == Toast.Visibility.SHOW && this.getVisibility(sysTime) != 1) {
				this.visibleTime = sysTime;
			}

			PoseStack stack = RenderSystem.getModelViewStack();
			stack.pushPose();

			if (ToastConfig.INSTANCE.topDown.get()) {
				int x = ToastConfig.INSTANCE.startLeft.get() ? 0 : scaledWidth - this.toast.width();
				stack.translate(x, (trueIdx - 1) * this.toast.height() + this.toast.height() * this.getVisibility(sysTime), 800 + this.index);
			} else if (ToastConfig.INSTANCE.startLeft.get()) {
				stack.translate(-this.toast.width() + this.toast.width() * this.getVisibility(sysTime), this.index * this.toast.height(), 800 + this.index);
			} else {
				stack.translate(scaledWidth - this.toast.width() * this.getVisibility(sysTime), this.index * this.toast.height(), 800 + this.index);
			}

			stack.translate(ToastConfig.INSTANCE.offsetX.get(), ToastConfig.INSTANCE.offsetY.get(), 0);
			RenderSystem.applyModelViewMatrix();
			RenderSystem.enableBlend();
			Toast.Visibility visibility = Toast.Visibility.SHOW;
			if (this.animationTime != -1) visibility = this.toast.render(pStack, BetterToastComponent.this, sysTime - this.visibleTime);
			RenderSystem.disableBlend();
			stack.popPose();
			RenderSystem.applyModelViewMatrix();

			if (this.forcedShowTime > ToastConfig.INSTANCE.forceTime.get() && visibility != this.visibility) {
				this.animationTime = sysTime - (long) ((1 - this.getVisibility(sysTime)) * 600);
				this.visibility = visibility;
				this.visibility.playSound(BetterToastComponent.this.minecraft.getSoundManager());
			}

			return this.forcedShowTime > ToastConfig.INSTANCE.forceTime.get() && this.visibility == Toast.Visibility.HIDE && sysTime - this.animationTime > 600L;
		}
	}

}
