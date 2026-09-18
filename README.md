# Yes Steve Model

> **本仓库是第三方移植分支，非官方。** 基于上游 [YesSteveModel](https://github.com/YesSteveModel/YesSteveModel) 的 `3.0-dev` 框架，
> 由 KIMIK3 与 DEEPSEEK 移植到 **Minecraft 26.1.2 / Fabric**。
> 仅支持开源模型与 v1/v2 旧版加密模型；第一人称手臂、自定义音效尚未实现。详见文末「移植说明」。

## ⚠️ 警告

当前公开版本还未完成，不保证稳定性、数据安全、跨平台行为、API、代码结构或后续版本兼容性。请勿用于生产环境或重要存档，测试前务必备份游戏目录、世界和模型。

所有公开格式、Schema、内部协议和缓存布局均尚未冻结，在正式发布前大概率会有 break change，并且不做向后兼容。

## 项目状态

本项目正在进行大规模重构，目前公开代码主要用于审阅、协作和验证设计。

目前模型格式、动画、渲染、gui 已趋于稳定，但网络协议和模型管理的代码还未提交。当前仓库中已有的模型管理和网络相关代码是为了让开发环境能跑起来而临时生成的代码，未经严格验证，仅提供最低限度的可用性，并且近期会有大幅度零兼容替换。贡献者不应向这部分贡献代码，下游项目也不应视其为可依赖接口。

目前仅协作者可贡献代码，如有贡献意愿可加入 YSM 开发者交流群了解详情。须知当前代码结构还未稳定，贡献者的本地开发进程可能得跟着主线一起重构。待模型管理和网络协议完成开发、旧版能力完成迁移，将会开放贡献。

更多信息见 [迁移概览](docs/migration-overview.md) 

## 与旧版相比

| 类别     | 重点                                                                                                                                                    |
| ------ |---------------------------------------------------------------------------------------------------------------------------------------------------------|
| 新增能力   | 公开的模型资产标准；细粒度资产分发；动态资源管理；更多平台支持。                                                                                        |
| 既有能力改进 | 模型业务回归 Java，native 收缩为能力层；内容身份、连接、资源所有权、失效和恢复边界显式化。                                                              |
| 暂缺旧能力  | 模型音频和 v3 加密模型暂未完成迁移；第一人称、附着 layer 和部分模组联动也未完全恢复。                                                                   |
| 迁移重点   | 完成模型管理和网络协议，接通模型音频、补充 v3 加密模型的独立导入、将 x64 基线降至 x86-64-v1，并适配 Windows 7。模组联动、手臂模型、layer 等旧代码迁移。 |
|        |                                                                                                                                                         |
| 未来方向   | 扩展 API、模型签名、通用外部模型源、GPU Compute Pipeline、独立 Backend。                                                                                |

## 其他文档

- [术语表](docs/glossary.md) / [文档政策](docs/governance/documentation-policy.md)：统一名称与信息取舍规则。
- 独立格式标准：
    - [Asset Container](docs/standards/asset-container.md)
    - [Model Schema](docs/standards/model-schema/README.md)：[Manifest 与身份](docs/standards/model-schema/manifest-and-identity.md)、[资产与验证](docs/standards/model-schema/assets-and-validation.md)
    - [一致性要求](docs/standards/conformance.md)
- 顶层设计与架构：
    - [动画系统](docs/concepts/animation.md) / [动画架构](docs/architecture/animation/README.md)
    - [渲染系统](docs/concepts/rendering.md) / [渲染架构](docs/architecture/rendering/README.md)

## 许可证

- 除另有声明的内容外，本仓库的原创代码按 [Apache License 2.0](LICENSE) 开源。
- Asset Container Spec、Model Schema、规范性 Proto 快照及一致性要求是独立于 YSM 和 Minecraft 的标准，按 [CC0 1.0 Universal](LICENSES/CC0-1.0.txt) 发布。
- 内置模型资产不属于 Apache-2.0；每个资产目录中的 `ysm.json` 是其许可证的权威清单。
- 项目包含直接拷贝或修改的第三方代码以及随包依赖，详见[NOTICE.md](NOTICE.md)。

Apache-2.0 不覆盖上述独立标准、内置资产或第三方作品；对应文件中的单独声明优先。

---

## 移植说明

本节由移植作者补充，不改动上文任何官方内容。

### 这是什么

上游只发布 Forge 版本。本分支把 `3.0-dev` 的代码移植到 **Minecraft 26.1.2 + Fabric**（Java 25），
移植与调试由 **KIMIK3 与 DEEPSEEK** 完成，代码基础来自上游的 `3.0-dev` 分支。

### 当前能力与缺口

| 能力 | 状态 |
| --- | --- |
| 开源模型（`.ysm` 及目录形式） | 支持 |
| v1 / v2 旧版加密模型 | 支持（原生层解密） |
| **v3 加密模型** | **不支持**，原生层 `LegacyBundle::Validate` 只接受版本 1 / 2，Java 侧的导入实现是上游留的占位符 |
| **第一人称手臂** | **未实现**，仍是原版手臂 |
| **自定义音效**（模型自带音频） | **未接入、未验证** |
| Iris 光影 | 基础兼容已恢复（顶点格式 / 阴影 pass / entity id）；光影下的 PBR 贴图未接入 |
| 第三人称模型、纸娃娃、模型选择界面、动画轮盘 | 可用 |

### 关键技术实现

- **Forge → Fabric**：事件总线、capability、网络层、配置系统、注册全部改写为 Fabric API。
- **26.1.2 延迟 GUI 渲染管线**：适配 extract → submit → prepare → draw 四段式。模型选择界面的
  每格预览改为「按用途派生独立的 `PictureInPictureRenderState` 子类」，因为原版每个
  `PictureInPictureRenderer` 实例只有一张离屏纹理、且 `blitTexture` 延后到 `draw()` 执行，
  同帧多个同类状态会互相覆盖，表现为所有格子显示同一个模型。
- **原生矩阵字节序**：`getMatBuffer` 的 `nio()` 未指定 `nativeOrder()`，JOML 按大端写、
  原生层按小端读，矩阵全零导致背面剔除裁掉所有面（模型完全不显示）。
- **投影矩阵回传原生层**：`ProjectionMatrixBuffer.writeBuffer` 的结果挂在 `GpuBufferSlice` 上，
  原生渲染时经 `RenderSystem.getProjectionMatrixBuffer()` 取回，供纸娃娃与界面预览使用。
- **旧版模型兼容**：额外扫描 `config/yes_steve_model/custom`，并为其分配独立的 catalog root kind
  与 model source id（复用已有值会触发目录同步死循环）；旧包轮盘动作在 manifest 未声明时，
  从 `extra.animation.json` 的动作名兜底生成。
- **模型选择持久化**：修正 NBT 键名不一致（写 `model_hash` 却读 `model_id`），否则重登会丢模型只留旧贴图。
- **贴图采样**：模型贴图强制临近取样（NEAREST），与旧版表现一致，避免缩小采样糊掉。
- **动画响应**：按下轮盘 / 快捷键立即本地播放，不等服务端回包；烘焙动画缓存键加入 render target id，修缓存乒乓。
- **Iris 光影兼容**：开光影后实体 RenderType 会换成 Iris 的扩展顶点格式（在普通 ENTITY 顶点后追加
  `entity_id` / `mid_tex_uv` / `tangent`），字节数随之变化，原生层必须按对应布局解释缓冲。
  这里用反射读取 `IrisVertexFormats.ENTITY` 做同一性比较，再按顶点字节数 56 / 55 / 54 分派到
  原生侧的 `IRIS_56` / `IRIS_55` / `IRIS_54` 布局，同时接回阴影 pass 判定与 entity id。
  全部走反射是为了不给构建引入 Iris 的编译期依赖（未安装时自动退化）。

### 构建

前置：**JDK 25**。

仓库内嵌的 `native/ysm.dll` 是 **Windows x64** 原生库，所以开箱即用的构建目前**仅限 Windows x64**。
其他平台需要自行从 [YesSteveModel-Native](https://github.com/YesSteveModel/YesSteveModel-Native)
构建对应平台的原生库，替换 `native/` 下的文件，或通过 `YSM_NATIVE_PATH` 环境变量 /
`local.properties` 的 `ysm.native_path` 指向本机的库文件。

```bash
git clone https://github.com/LINGYAN2021/YesSteveModel-Fabric.git
cd YesSteveModel-Fabric
git checkout port/fabric-26.1.2

# Windows
gradlew.bat shadowJar -x test
# Linux / macOS
./gradlew shadowJar -x test
```

产物为 `build/libs/ysm-3.0-dev-fabric+mc26.1.2.jar`，放入游戏 `mods/` 目录即可。

CLONE 后无需任何额外准备：缺失的 `local.properties` 会自动生成模板，`src/generated/` 下的内置模型索引
由 `generateBuiltinModelIndex` 任务在构建时重建（该任务依赖 `native/ysm.dll`，因此原生库随仓库分发）。

### 许可证

本分支沿用上游许可证，未作改动：除非另有声明，原创代码按 [Apache License 2.0](LICENSE) 开源；
内置模型资产、独立标准与第三方作品的授权范围见 [LICENSE](LICENSE) 与 [NOTICE.md](NOTICE.md)。
