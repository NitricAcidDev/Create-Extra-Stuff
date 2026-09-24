package com.kreidev.createbbfueloverrides;

import java.util.function.Consumer;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Limits pipe/handler transfers while allowing the bucket path to insert a full bucket. */
public final class LimitedBurnerTank extends SmartFluidTank {
    private static final int MAX_EXTERNAL_TRANSFER = 100;
    private boolean unrestricted;

    public LimitedBurnerTank(int capacity, Consumer<FluidStack> callback) {
        super(capacity, callback);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (unrestricted || resource.isEmpty()) return super.fill(resource, action);
        FluidStack limited = resource.copy();
        limited.setAmount(Math.min(resource.getAmount(), MAX_EXTERNAL_TRANSFER));
        return super.fill(limited, action);
    }

    public int fillUnrestricted(FluidStack resource, IFluidHandler.FluidAction action) {
        unrestricted = true;
        try {
            return super.fill(resource, action);
        } finally {
            unrestricted = false;
        }
    }
}
