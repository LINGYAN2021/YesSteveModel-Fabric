package com.elfmcys.ysm.client.animation.condition;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public class ConditionalPassenger {
    private static final String EMPTY = "";
    private final ObjectOpenHashSet<Identifier> idTest = new ObjectOpenHashSet<>();
    private final ReferenceArrayList<TagKey<EntityType<?>>> tagTest = new ReferenceArrayList<>();
    private final String idPre;
    private final String tagPre;

    public ConditionalPassenger() {
        this.idPre = "passenger$";
        this.tagPre = "passenger#";
    }

    public void addTest(String name) {
        int preSize = this.idPre.length();
        if (name.length() <= preSize) {
            return;
        }
        String substring = name.substring(preSize);
        if (name.startsWith(idPre) && (Identifier.tryParse(substring) != null)) {
            idTest.add(Identifier.parse(substring));
        }
        if (name.startsWith(tagPre) && (Identifier.tryParse(substring) != null)) {
            TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(substring));
            tagTest.add(tagKey);
        }
    }

    public String doTest(LivingEntity livingEntity) {
        Entity passenger = livingEntity.getFirstPassenger();
        if (passenger == null || !passenger.isAlive()) {
            return EMPTY;
        }
        String result = doIdTest(passenger);
        if (result.isEmpty()) {
            return doTagTest(passenger);
        }
        return result;
    }

    private String doIdTest(Entity passenger) {
        if (idTest.isEmpty()) {
            return EMPTY;
        }
        Identifier registryName = BuiltInRegistries.ENTITY_TYPE.getKey(passenger.getType());
        if (registryName == null) {
            return EMPTY;
        }
        if (idTest.contains(registryName)) {
            return idPre + registryName;
        }
        return EMPTY;
    }

    private String doTagTest(Entity passenger) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        return tagTest.stream().filter(tag -> passenger.getType().builtInRegistryHolder().is(tag)).findFirst().map(itemTagKey -> tagPre + itemTagKey.location()).orElse(EMPTY);
    }
}
