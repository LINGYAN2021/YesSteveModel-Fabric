package com.elfmcys.ysm.format.schema.model;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.natives.image.Image;
import com.elfmcys.ysm.format.schema.file.AssetFileWriter;
import mixel.manifest.ManifestOuterClass;

import java.io.IOException;

public class ModelFileWriter extends AssetFileWriter {
    public ModelFileWriter() {
        setSchemaId(ModelFileConstant.SCHEMA_ID);
        setProperty(ModelFileConstant.PROP_VERSION, ModelFileConstant.CURRENT_VERSION.toString());
        var container = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(YesSteveModel.MOD_ID);
        if (container.isPresent()) {
            var metadata = container.get().getMetadata();
            setProperty(ModelFileConstant.PROP_VENDOR, String.format("%s %s",
                    metadata.getId(), metadata.getVersion().getFriendlyString()));
        } else {
            setProperty(ModelFileConstant.PROP_VENDOR, "dev");
        }
    }

    public void setManifest(ManifestOuterClass.Manifest manifest) throws IOException {
        addProtoChunk(ModelFileConstant.MANIFEST_CHUNK_NAME, manifest, 0);
        generateSummary(manifest);
    }

    private void generateSummary(ManifestOuterClass.Manifest manifest) {
        if (manifest.hasInfo() && manifest.getInfo().hasMetadata() && manifest.getInfo().getMetadata().hasName()) {
            setSummary(manifest.getInfo().getMetadata().getName());
        } else {
            setSummary("");
        }
    }

    public void setThumbnail(Image img) {
        addImage(ModelFileConstant.THUMB_BUTTON_CHUNK_NAME, img);
    }

    public void setIcon(Image img) {
        addImage(ModelFileConstant.THUMB_ICON_CHUNK_NAME, img);
    }
}
