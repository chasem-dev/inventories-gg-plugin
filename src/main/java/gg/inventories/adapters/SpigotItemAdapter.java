package gg.inventories.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gg.inventories.adapters.items.ItemAdapter;
import net.minecraft.locale.LocaleLanguage;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

public class SpigotItemAdapter extends ItemAdapter<ItemStack> {

    static JsonObject airJson = new JsonObject();

    static {
        airJson.addProperty("type", Material.AIR.name());
        airJson.addProperty("unlocalizedName", Material.AIR.getKey().toString());
        airJson.addProperty("source", Material.AIR.getKey().toString().split(":")[0]);
        airJson.addProperty("itemName", Material.AIR.getKey().toString().split(":")[1]);
    }

    @Override
    public JsonObject toJson(ItemStack stack) {

        JsonObject itemJson = new JsonObject();

        itemJson.addProperty("type", stack.getType().name());

        //Fake data for website.
        itemJson.addProperty("unlocalizedName", stack.getType().getKey().toString());
        itemJson.addProperty("source", stack.getType().getKey().toString().split(":")[0]);
        itemJson.addProperty("itemName", stack.getType().getKey().toString().split(":")[1]);

        if (stack.getType() == Material.AIR) {
            return itemJson;
        }

        if (stack.getDurability() > 0) {
            itemJson.addProperty("data", stack.getDurability());
            itemJson.addProperty("durability", stack.getDurability());
        }

        if (stack.getAmount() != 1) {
            itemJson.addProperty("amount", stack.getAmount());
        }

        if (stack.getType().getMaxDurability() > 0) {
            itemJson.addProperty("maxDurability", stack.getType().getMaxDurability());
        }
        if (stack.hasItemMeta()) {
            JsonObject metaJson = new JsonObject();

            ItemMeta meta = stack.getItemMeta();

            if (meta.hasDisplayName()) {
                itemJson.addProperty("displayName", meta.getDisplayName());
            }

            if (meta.hasLocalizedName()) {
                metaJson.addProperty("localizedName", meta.getLocalizedName());
            }

            if (meta.hasLore()) {
                JsonArray lore = new JsonArray();

                meta.getLore().forEach(line -> lore.add(new JsonPrimitive(line)));

                metaJson.add("lore", lore);
            }

            if (meta.hasEnchants()) {
                JsonArray enchants = new JsonArray();

                meta.getEnchants().forEach((enchantment, level) -> enchants.add(new JsonPrimitive(enchantment.getKey().getKey() + ":" + level)));

                metaJson.add("enchants", enchants);
            }

            if (!meta.getItemFlags().isEmpty()) {
                JsonArray flags = new JsonArray();

                meta.getItemFlags().forEach(itemFlag -> flags.add(new JsonPrimitive(itemFlag.name())));

                metaJson.add("flags", flags);
            }

            if (meta instanceof SkullMeta skullMeta) {
                if (skullMeta.hasOwner()) {
                    JsonObject skullData = new JsonObject();
                    skullData.addProperty("owner", skullMeta.getOwner());
                    skullData.addProperty("metaType", "SKULL");

                    metaJson.add("extraMeta", skullData);
                }
            } else if (meta instanceof BannerMeta bannerMeta) {

                if (bannerMeta.numberOfPatterns() > 0) {
                    //TODO:
                }

            } else if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {

                JsonObject esData = new JsonObject();

                esData.addProperty("metaType", "ENCHANTMENT_STORAGE");

                if (((EnchantmentStorageMeta) meta).hasStoredEnchants()) {
                    JsonArray enchants = new JsonArray();

                    enchantmentStorageMeta.getStoredEnchants().forEach((enchantment, level) -> enchants.add(new JsonPrimitive(enchantment.getKey().getKey() + ":" + level)));

                    esData.add("storedEnchants", enchants);
                }

                metaJson.add("extraMeta", esData);

            } else if (meta instanceof BookMeta bookMeta) {

                JsonObject bookData = new JsonObject();
                bookData.addProperty("metaType", "BOOK_META");

                if (bookMeta.hasAuthor() || bookMeta.hasPages() || bookMeta.hasTitle()) {
                    if (bookMeta.hasTitle()) {
                        bookData.addProperty("title", bookMeta.getTitle());
                    }

                    if (bookMeta.hasAuthor()) {
                        bookData.addProperty("author", bookMeta.getAuthor());
                    }

                    if (bookMeta.hasPages()) {
                        JsonArray pages = new JsonArray();
                        bookMeta.getPages().forEach(str -> pages.add(new JsonPrimitive(str)));
                        bookData.add("pages", pages);
                    }
                }

                metaJson.add("extraMeta", bookData);

            } else if (meta instanceof FireworkMeta) {

            } else if (meta instanceof FireworkEffectMeta) {

            } else if (meta instanceof PotionMeta potionMeta) {
                JsonObject potionData = new JsonObject();
                potionData.addProperty("metaType", "POTION_META");

                potionData.addProperty("potionType", potionMeta.getBasePotionData().getType().name());
                potionData.addProperty("potionLevel", potionMeta.getBasePotionData().isUpgraded() ? 2 : 1);

                if (potionMeta.hasCustomEffects()) {
                    JsonArray customEffects = new JsonArray();
                    potionMeta.getCustomEffects().forEach(potionEffect -> {
                        customEffects.add(new JsonPrimitive(potionEffect.getType().getName()
                                + ":" + potionEffect.getAmplifier()
                                + ":" + potionEffect.getDuration() / 20));
                    });

                    potionData.add("customEffects", customEffects);
                }

                metaJson.add("extraMeta", potionData);
            } else if (meta instanceof MapMeta) {

            } else if (meta instanceof CrossbowMeta) {

            } else if (meta instanceof TropicalFishBucketMeta) {

            } else if (meta instanceof SpawnEggMeta) {

            } else if (meta instanceof LeatherArmorMeta) {

            } else if (meta instanceof BlockStateMeta blockStateMeta) {
                JsonObject blockStateJson = new JsonObject();

                if (blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
                    blockStateJson.addProperty("metaType", "SHULKER_BOX");

                    JsonArray shulkerJson = new JsonArray();

                    for (ItemStack itemStack : shulkerBox.getSnapshotInventory().getContents()) {
                        if (itemStack != null && itemStack.getType() != Material.AIR) { //This might get stuck if its shulker inside of shulker?
                            shulkerJson.add(this.toJson(itemStack));
                        } else {
                            shulkerJson.add(airJson);
                        }
                    }

                    blockStateJson.add("inventory", shulkerJson);
                }

                metaJson.add("extraMeta", blockStateJson);
            }
            itemJson.add("itemMeta", metaJson);
        }

        if (!itemJson.has("displayName")) {
            itemJson.addProperty("displayName", getI18NDisplayName(stack));
        }

        //TODO: Item json

        return itemJson;
    }

    /**
     *
     * Works for all except (block.minecraft.white_wall_banner)
     *
     * @param item
     * @return
     */
    public String getI18NDisplayName(ItemStack item) {
        String translationKey = (item.getType().isBlock() ? "block." : "item.") + item.getType().getKey().getNamespace() + "." + item.getType().getKey().getKey();
        return LocaleLanguage.a().a(translationKey);
    }
}
