package com.nitricacid.traininteractive;

import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Read native attachment geometry without adding any temporary entity to the world. */
public final class TavernSeatPositions {
    private static final WeakHashMap<Level, WeakReference<SitEntity>> SEATS = new WeakHashMap<>();
    private TavernSeatPositions() {}
    public static synchronized double passengerOffset(Entity passenger, Level level) {
        var reference = SEATS.get(level);
        SitEntity seat = reference == null ? null : reference.get();
        if (seat == null) {
            seat = new SitEntity(SitEntity.TYPE, level);
            SEATS.put(level, new WeakReference<>(seat));
        }
        return seat.getPassengerRidingPosition(passenger).y - seat.getY() - passenger.getVehicleAttachmentPoint(seat).y;
    }
}
