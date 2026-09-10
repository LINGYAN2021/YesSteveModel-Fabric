import os, re

ROOT = 'src/main/java'
count_files = 0

def sub_file(path):
    global count_files
    s = open(path, encoding='utf-8').read()
    o = s
    # Dist / OnlyIn
    s = s.replace('import net.minecraftforge.api.distmarker.Dist;', 'import net.fabricmc.api.EnvType;')
    s = s.replace('import net.minecraftforge.api.distmarker.OnlyIn;', 'import net.fabricmc.api.Environment;')
    s = re.sub(r'@OnlyIn\(Dist\.CLIENT\)', '@Environment(EnvType.CLIENT)', s)
    s = re.sub(r'@OnlyIn\(Dist\.DEDICATED_SERVER\)', '@Environment(EnvType.SERVER)', s)
    s = s.replace('Dist.CLIENT', 'EnvType.CLIENT')
    s = s.replace('Dist.DEDICATED_SERVER', 'EnvType.SERVER')
    # FMLEnvironment
    s = s.replace('import net.minecraftforge.fml.loading.FMLEnvironment;',
                  'import net.fabricmc.loader.api.FabricLoader;')
    s = s.replace('FMLEnvironment.production', '!FabricLoader.getInstance().isDevelopmentEnvironment()')
    s = s.replace('FMLEnvironment.dist.isClient()', 'FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT')
    s = s.replace('FMLEnvironment.dist == EnvType.CLIENT', 'FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT')
    s = s.replace('FMLEnvironment.dist', 'FabricLoader.getInstance().getEnvironmentType()')
    # FMLPaths
    s = s.replace('import net.minecraftforge.fml.loading.FMLPaths;',
                  'import net.fabricmc.loader.api.FabricLoader;')
    s = s.replace('FMLPaths.GAMEDIR.get()', 'FabricLoader.getInstance().getGameDir()')
    s = s.replace('FMLPaths.CONFIGDIR.get()', 'FabricLoader.getInstance().getConfigDir()')
    # ModList
    s = s.replace('import net.minecraftforge.fml.ModList;', 'import net.fabricmc.loader.api.FabricLoader;')
    s = re.sub(r'ModList\.get\(\)\.isLoaded\(', 'FabricLoader.getInstance().isModLoaded(', s)
    # ForgeRegistries -> BuiltInRegistries
    s = s.replace('import net.minecraftforge.registries.ForgeRegistries;',
                  'import net.minecraft.core.registries.BuiltInRegistries;')
    reg_map = {
        'ForgeRegistries.ITEMS': 'BuiltInRegistries.ITEM',
        'ForgeRegistries.BLOCKS': 'BuiltInRegistries.BLOCK',
        'ForgeRegistries.SOUND_EVENTS': 'BuiltInRegistries.SOUND_EVENT',
        'ForgeRegistries.MOB_EFFECTS': 'BuiltInRegistries.MOB_EFFECT',
        'ForgeRegistries.ENTITY_TYPES': 'BuiltInRegistries.ENTITY_TYPE',
        'ForgeRegistries.ENCHANTMENTS': 'BuiltInRegistries.ENCHANTMENT',
        'ForgeRegistries.POTIONS': 'BuiltInRegistries.POTION',
        'ForgeRegistries.PARTICLE_TYPES': 'BuiltInRegistries.PARTICLE_TYPE',
        'ForgeRegistries.BIOMES': 'BuiltInRegistries.BIOME',
        'ForgeRegistries.FLUIDS': 'BuiltInRegistries.FLUID',
        'ForgeRegistries.MENU_TYPES': 'BuiltInRegistries.MENU',
        'ForgeRegistries.RECIPE_SERIALIZERS': 'BuiltInRegistries.RECIPE_SERIALIZER',
        'ForgeRegistries.ATTRIBUTES': 'BuiltInRegistries.ATTRIBUTE',
    }
    for a, b in reg_map.items():
        s = s.replace(a, b)
    # ITagManager -> vanilla TagManager
    s = s.replace('import net.minecraftforge.registries.tags.ITagManager;', 'import net.minecraft.tags.TagManager;')
    s = re.sub(r'\bITagManager\b', 'TagManager', s)
    if s != o:
        open(path, 'w', encoding='utf-8').write(s)
        count_files += 1

for dirpath, _, files in os.walk(ROOT):
    for name in files:
        if name.endswith('.java'):
            sub_file(os.path.join(dirpath, name))
print('swept files:', count_files)
