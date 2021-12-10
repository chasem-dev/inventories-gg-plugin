package gg.inventories.adapters.player;

import com.google.gson.JsonObject;
import gg.inventories.adapters.items.ItemAdapter;

import java.lang.reflect.InvocationTargetException;

public abstract class PlayerAdapter <P,I extends ItemAdapter> {

    private I itemAdapter;

    public abstract JsonObject toJson(P player);

    public abstract I getItemAdapter();
}