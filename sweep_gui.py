import os, re

ROOT = 'src/main/java'

RENAMES = [
    ('.drawString(', '.text('),
    ('.drawCenteredString(', '.centeredText('),
    ('.drawWordWrap(', '.textWithWordWrap('),
    ('.renderComponentTooltip(', '.setComponentTooltipForNextFrame('),
    ('.renderTooltip(', '.setTooltipForNextFrame('),
    ('.renderFakeItem(', '.fakeItem('),
    ('.renderItemDecorations(', '.itemDecorations('),
    ('.renderItem(', '.item('),
    ('.renderOutline(', '.outline('),
    ('.hLine(', '.horizontalLine('),
    ('.vLine(', '.verticalLine('),
    ('.blitSprite(', '.blitSprite(RenderPipelines.GUI_TEXTURED, '),
    ('.blit(', '.blit(RenderPipelines.GUI_TEXTURED, '),
    ('.pose().pushPose()', '.pose.pushMatrix()'),
    ('.pose().popPose()', '.pose.popMatrix()'),
    ('graphics.pose()', 'graphics.pose'),
    ('guiGraphics.pose()', 'guiGraphics.pose'),
]

def convert(path):
    s = open(path, encoding='utf-8').read()
    if 'GuiGraphics' not in s:
        return False
    o = s
    s = s.replace('import net.minecraft.client.gui.GuiGraphics;', 'import net.minecraft.client.gui.GuiGraphicsExtractor;')
    s = re.sub(r'\bGuiGraphics(?!Extractor)\b', 'GuiGraphicsExtractor', s)
    # Screen.render -> extractRenderState
    s = re.sub(r'\bvoid render\(GuiGraphicsExtractor ', 'void extractRenderState(GuiGraphicsExtractor ', s)
    s = re.sub(r'\bvoid renderBackground\(GuiGraphicsExtractor ', 'void extractBackground(GuiGraphicsExtractor ', s)
    # widget content method by superclass
    if re.search(r'extends\s+(AbstractWidget|StateSwitchingButton)\b', s):
        s = re.sub(r'\bvoid renderWidget\(GuiGraphicsExtractor ', 'void extractWidgetRenderState(GuiGraphicsExtractor ', s)
    elif re.search(r'extends\s+\w*Button\b', s):
        s = re.sub(r'\bvoid renderWidget\(GuiGraphicsExtractor ', 'void extractContents(GuiGraphicsExtractor ', s)
    for a, b in RENAMES:
        s = s.replace(a, b)
    # RenderPipelines import if we injected pipeline arg
    if 'RenderPipelines.GUI_TEXTURED' in s and 'import net.minecraft.client.renderer.RenderPipelines;' not in s:
        lines = s.split('\n')
        for i, l in enumerate(lines):
            if l.startswith('import '):
                last = i
        lines.insert(last + 1, 'import net.minecraft.client.renderer.RenderPipelines;')
        s = '\n'.join(lines)
    if s != o:
        open(path, 'w', encoding='utf-8').write(s)
        return True
    return False

count = 0
for dirpath, _, files in os.walk(ROOT):
    for name in files:
        if name.endswith('.java'):
            p = os.path.join(dirpath, name)
            try:
                if convert(p):
                    count += 1
            except Exception as e:
                print('ERR', p, e)
print('gui swept:', count)
