#!/usr/bin/env python3
"""Mechanical 26.1.2 API sweeps, round 22."""
import re
from pathlib import Path

ROOT = Path(r"C:\Users\CHEN\CascadeProjects\YSM26.1.2FA\ysm\src\main\java")

def all_java():
    return [p for p in ROOT.rglob("*.java")]

def ensure_import(text, imp):
    if imp in text:
        return text
    # insert after last existing import
    lines = text.split("\n")
    last = -1
    for i, l in enumerate(lines):
        if l.startswith("import "):
            last = i
    if last >= 0:
        lines.insert(last + 1, imp)
        return "\n".join(lines)
    return text

def remove_import(text, name_suffix):
    return re.sub(rf"import [^\n]*{re.escape(name_suffix)};\n", "", text)

changed = 0
for p in all_java():
    s = p.read_text(encoding="utf-8")
    o = s

    # 1-3. getMinecraft()
    s = s.replace("this.getMinecraft()", "this.minecraft")
    s = re.sub(r"\b(\w+)\.getMinecraft\(\)", "Minecraft.getInstance()", s)
    s = re.sub(r"(?<![\w.])getMinecraft\(\)", "minecraft", s)

    # 4. getFrameTime
    s = re.sub(r"\.getFrameTime\(\)", ".getDeltaTracker().getGameTimeDeltaPartialTick(true)", s)

    # 5. renderBackground call -> extractBackground
    s = re.sub(r"\brenderBackground\(graphics\);", "extractBackground(graphics, mouseX, mouseY, partialTick);", s)

    # 6. .render(graphics, mouseX, mouseY, partialTick) -> .extractRenderState(...)
    s = re.sub(r"\.render\(graphics, mouseX, mouseY, partialTick\)",
               ".extractRenderState(graphics, mouseX, mouseY, partialTick)", s)

    # 7. TagManager -> TagKey.create
    if "TagManager" in s or ".tags()" in s:
        # determine registry type per file
        if "TagManager<EntityType" in s:
            reg = "ENTITY_TYPE"
        elif "TagManager<Biome" in s or "BuiltInRegistries.BIOME.tags()" in s:
            reg = "BIOME"
        else:
            reg = "ITEM"
        # remove declaration lines
        s = re.sub(r"[ \t]*TagManager<[^>]+> tags = BuiltInRegistries\.[A-Z_]+\.tags\(\);\n", "", s)
        # remove null-check blocks: if (tags == null) { ... }
        s = re.sub(r"[ \t]*if \(tags == null\) \{\n(?:[ \t]*[^\n]*\n){1,3}?[ \t]*\}\n", "", s)
        s = s.replace("tags.createTagKey(", f"TagKey.create(Registries.{reg}, ")
        s = re.sub(r"BuiltInRegistries\.ITEM\.tags\(\)\.createTagKey\(", "TagKey.create(Registries.ITEM, ", s)
        s = re.sub(r"BuiltInRegistries\.BIOME\.tags\(\)\.createTagKey\(", "TagKey.create(Registries.BIOME, ", s)
        s = re.sub(r"BuiltInRegistries\.ENTITY_TYPE\.tags\(\)\.createTagKey\(", "TagKey.create(Registries.ENTITY_TYPE, ", s)
        s = remove_import(s, ".TagManager")
        if "Registries." in s:
            s = ensure_import(s, "import net.minecraft.core.registries.Registries;")

    # 8. UseAnim -> ItemUseAnimation
    if re.search(r"\bUseAnim\b", s):
        s = re.sub(r"\bUseAnim\b", "ItemUseAnimation", s)

    # 9. OggAudioStream -> JOrbisAudioStream
    s = re.sub(r"\bOggAudioStream\b", "JOrbisAudioStream", s)

    # 10. RenderSystem scissor
    s = s.replace("RenderSystem.enableScissor(", "RenderSystem.enableScissorForRenderTypeDraws(")
    s = s.replace("RenderSystem.disableScissor()", "RenderSystem.disableScissorForRenderTypeDraws()")

    # 11. applyModelViewMatrix gone (model view stack auto-applied)
    s = re.sub(r"[ \t]*RenderSystem\.applyModelViewMatrix\(\);\n", "", s)

    # 12. Lighting static -> instance
    if "Lighting.setupFor" in s or "Lighting.setup" in s:
        s = s.replace("Lighting.setupForEntityInInventory();",
                      "Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);")
        s = s.replace("Lighting.setupFor3DItems();",
                      "Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);")
        s = s.replace("import net.minecraft.client.Lighting;", "import com.mojang.blaze3d.platform.Lighting;")
        s = ensure_import(s, "import net.minecraft.client.Minecraft;")

    # 13. serverLevel() -> level() (ServerPlayer covariant)
    s = s.replace("sender.serverLevel()", "sender.level()")

    # 14. leftover LazyOptional .resolve()
    s = s.replace(".resolve()", "")

    # 15. shouldRiderSit removal
    s = re.sub(r"(\w+)\.getVehicle\(\) != null && \1\.getVehicle\(\)\.shouldRiderSit\(\)",
               r"\1.getVehicle() != null", s)

    # 16. entityRenderDispatcher in non-EntityRenderer classes
    if p.name in ("CustomPlayerRenderer.java", "GeoReplacedEntityRenderer.java"):
        s = s.replace("this.entityRenderDispatcher", "Minecraft.getInstance().getEntityRenderDispatcher()")

    # 17. ModList version string
    if "ModList.get().getModFileById" in s:
        s = re.sub(r"ModList\.get\(\)\.getModFileById\((\w+(?:\.\w+)*)\)\.versionString\(\)",
                   r"FabricLoader.getInstance().getModContainer(\1).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse(\"unknown\")", s)
        s = ensure_import(s, "import net.fabricmc.loader.api.FabricLoader;")
        if "ModList" not in s:
            s = remove_import(s, ".ModList")

    if s != o:
        p.write_text(s, encoding="utf-8")
        changed += 1
        print("swept:", p.relative_to(ROOT))

print("total changed:", changed)
