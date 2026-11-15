---
label: 有声书
order: 27
---

# 有声书

借助文本转语音服务器，将书籍逐段转换为有声书：为旁白和每个角色分配一个声音，与书中各段保存在一起，并在阅读器中播放。你可以让它全自动运行（模型标注谁在说话以及如何说，语音服务器负责朗读），用于快速生成草稿；也可以在 **Audiobook studio** 中逐段处理，精确查看发送给语音服务器的内容并手动修改，最后将结果导出为带章节标记的 mp3 文件。

本扩展本身不生成语音：它与你自行运行的 **Chatterbox-TTS-Server** 通信（参见[设置语音服务器](audiobook-server.md)）。书籍的文本从不被修改，也不会发送给除你指定的服务器之外的任何人；音频存储在你自己的安装环境中，作为与各段关联的资源。

<a id="what-you-need"></a>

## 所需条件

- 一台语音服务器；如果它与你的语言模型共用显卡，还需要为其腾出空间并在用完后将其停止的请求（参见[设置语音服务器](audiobook-server.md)）。
- 声音：每个你想克隆的声音都需要一段简短的**参考音频**（一个人的 12 至 30 秒录音，不含音乐），或者使用服务器已有的某个声音文件。旁白需要平静、稳定的朗读声音。
- 对于有多个声音的书籍，需要一个能够为文本做标注的语言模型。默认使用该书所用的模型。
- **ffmpeg** 是可选的。没有它，一切都可以使用 mp3 工作。如果服务器上装有它（它包含在 Docker 镜像中；在桌面环境中，它位于 `PATH` 或常见的文件夹如 `/opt/homebrew/bin` 中；若安装在其他位置，则由变量 `MARGINALIA_FFMPEG` 指定路径），导出时可获得 **m4b** 格式，管理员也可以将音频存储为 wav。

<a id="the-settings"></a>

## 设置

*Settings → Extension Settings → Audiobook Settings* 是你账户的设置面板；修改后请保存 *Settings* 页面。

![带有一个后端和角色的面板](../../images/plugin-audiobook-settings.png)

<a id="backends"></a>

### 后端

后端是一台语音服务器及其所使用的模型。点击 **+** 添加一个后端。**Duplicate** 会以新名称复制所选后端（包括地址、密钥、GPU 锁、声音、默认值以及批次前后的请求），这是在同一台机器上快速获得另一模型后端的方法，例如在 Chatterbox 旁边添加 Higgs；之后你需要修改模型、地址以及启动和停止请求。

| 设置项 | 作用 |
|---|---|
| 名称 | 在转换对话框中显示的名称。 |
| 地址 | 语音服务器的基础地址，或转发到它的服务的地址，例如 `http://localhost:8004`。 |
| Api key | 若服务器（或你的代理）需要，则作为 Bearer 令牌发送。它以加密形式存储且不会再次显示；如需替换，请输入新的密钥，或点击 **Remove the key**。 |
| 语音模型 | **Chatterbox Turbo**（英语，支持如 `[sigh]` 之类的声音标签，速度最快，为默认选项）、**Chatterbox**（英语，带有语气的 *exaggeration* 参数）、**Chatterbox Multilingual**（23 种语言）或 **Higgs Audio v3 in ComfyUI**（多种语言，能根据台词情绪发声，并能表现哭泣、喘息或呻吟等声音；见[下文](#higgs-audio-v3-emotions-and-sounds)）。后端的 [GPU 锁](../administration/gpus.md)（或批次前的请求）必须保持该模型处于加载状态。需要其他模型的书籍应使用另一个后端。 |
| GPU 锁 | 语音服务器所运行的 GPU 的 [GPU 锁](../administration/gpus.md)，当它与语言模型或其他服务器（包括 [Image Generator](image-generator.md)）共用时使用：每个批次和样本都会轮流使用 GPU，锁会先加载语音服务器。仅当管理员设置了 GPU 锁时才会显示。 |
| 服务器位于本机或我的网络中 | 允许使用本地网络地址。若服务器在云端，请保持关闭：这样它就无法被诱导回调你自己的机器。 |
| 请求超时 | 等待一段语音返回的时长。 |
| 在此数量（段数）之后重启服务器 | 适用于会泄漏内存、并在长批次中被系统终止的语音服务器：每处理完这么多段后，会在批次中间依次重新执行批次后的请求，然后是批次前的请求（停止、启动、加载），工作随后继续在全新的服务器上进行。0（默认值）表示从不这样做。无论此设置如何，服务器崩溃后都会重新启动，参见[故障排除](audiobook-server.md#troubleshooting)。 |
| 批次前后请求的密钥 | 批次前后的请求以 `Authorization: Bearer` 携带的密钥。它以加密形式存储且不会再次显示（点击 **Remove the key** 可清除），因此无需写入每个请求的请求头中（否则会被明文读取）。留空时使用上方的 *Api key*，当 LiteLLM 等代理统一提供所有服务时，这正是你所需要的。若某个请求自带 `Authorization` 请求头，则保留该请求头。 |
| 旁白声音 | 没有角色表的书籍的旁白。服务器上的声音文件写作 `predefined:Henry.wav`。 |
| 精度 | 仅适用于 Higgs：模型计算时使用的精度。**fp32**（默认值）是安全的选择：在 AMD 显卡上，**bf16** 会使段落变成乱码、音效错误以及声音出错。bf16 所需的显存约为一半（约 8 GB，而非 16 GB），在支持的环境中速度更快。 |
| 将音频片段的文本发送给 Higgs | 仅适用于 Higgs，实验性功能：会一并发送[音频片段的文本](#voices-and-clips)。请保持关闭：测试中它改变了声音。 |
| 检查段落长度 | 默认开启。若某段远长于其文本（即超过 4 秒加每词 0.6 秒：模型继续说话，或发出声音而非读出文字），则会使用下一个随机种子重新生成，最多重试 **Retries** 次；保留第一个长度合适的版本，否则保留最短的版本。 |
| 使用语音识别检查文字 | 默认关闭；需要[语音识别](#speech-recognition)。每个批次结束后，所有段落都会被转写并与其文本比对。若某段的错误词比例超过 **Max. word errors (%)**，则使用下一个随机种子重新生成并再次检查，最多进行 **Retries** 轮；保留错词最少的版本。转换结束时会显示重新生成的段落数，以及仍有差异的段落数。 |
| 段落默认值 | 段落开始时使用的温度、CFG 权重、语速和分块大小（studio 可为单个段落单独修改）。Higgs 仅使用温度参数。 |
| 批次前、批次后 | 在 GPU 锁内，围绕每批段落（以及每个样本）发出的请求。释放显卡和启动语音服务器由 GPU 锁负责；参见[一块 GPU 同时用于模型和语音](audiobook-server.md#one-gpu-for-the-model-and-the-speech)。留空则不执行任何操作。 |

**Test connection** 执行的操作与批次相同：它会获取 GPU 锁（因此已停止的语音服务器会被启动），发出批次前的请求，询问服务器是否在线且已加载模型，然后发出批次后的请求。若没有锁或请求，则仅进行询问。

若书中有其他文字的段落（英文书中的日语），且你为其配置了后端，则会将这些段落连同其语言一起发送给多语言模型；若没有配置后端，则仍使用该书的模型。Higgs 书籍会保留这些段落：Higgs 自行朗读，且使用同一声音。
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3：情绪与声音

使用 **ComfyUI 中的 Higgs Audio v3** 后端时，语言模型的标签会被听出来。后端地址就是你的 ComfyUI 的地址（需要安装 TTS Audio Suite 节点，参见 [搭建语音服务器](audiobook-server.md#higgs-audio-v3-in-comfyui)）。

- 一段语音的**演绎方式**会被说出来：悲伤、愤怒、喜悦、逗乐、紧张、恐惧、惊讶、温柔、讽刺、哀求、绝望、
  厌恶、情欲、耳语或大喊。强烈的演绎（强度 0.8 及以上）表现力更强，轻微的（低于 0.2）则平淡地朗读。
  旁白始终平淡地朗读。
- 语言模型可以在语音中加入比 Chatterbox Turbo 更多的**声音效果**：大笑、轻笑、咯咯笑、叹气、倒吸气、咳嗽、清嗓子、
  抽鼻子、打喷嚏、呻吟、哭、啜泣、尖叫、喘气、呻吟声、哼唱和嘘声。
- 程序还会读取**每段语音周围的叙述**（英文），并为模型未加声音的语音补上叙述中所写的声音：
  *"You look funny," she giggled.* 会得到一声咯咯笑，*She burst out laughing. "Careful!"* 会得到一阵大笑，*a soft moan escaped her lips. "Yes..."* 会得到一声呻吟，
  *panting* 或 *out of breath* 会得到喘气，*cried quietly* 或 *burst into tears* 会得到哭声。那些只是字面上像声音的词则会被忽略：*"Stop!" she cried.*
  （这是一个感叹）、*moaned about the weather*（抱怨天气）、*the wind moaned*（风在呼啸）、*the floorboards groaned*（地板吱嘎作响）、*stifled a giggle*（忍住笑）、*didn't laugh*（没有笑），以及由叙述赋予另一个有名字的角色的声音。*"You look funny," he mocked her.* 会得到讽刺或逗乐的演绎，但没有声音效果。叙述本身按原文朗读。
- **旁白始终平淡地朗读**：没有声音效果，也没有情绪，即使是模型交给旁白去读的语音也是如此。
- Higgs 不需要为每种情绪准备一段参考音频：一个声音的一段音频就够了。它会复制参考音频的录音特征，因此干净的音频比 Chatterbox 更重要（参见 [好的参考音频](#a-good-clip)）。
- 每段语音都由 ComfyUI 生成，之后会清空其显存（每段几秒钟），这样可以避免某些显卡崩溃。
- Higgs 常常会在最后一个字之后继续发声（一次换气，或另一个声音的开头），而参考音频在这一半途中就结束了，因此听起来会在停顿前出现一个短促的"呼"声。使用 **ffmpeg** 时，扩展功能会剪掉这段尾巴，并为每段语音做淡出；没有 ffmpeg 时，各段语音保持 Higgs 生成的原样。
- Higgs Audio v3 模型采用**仅限研究和非商业用途的许可证**：如果你要出售有声书，需要向 Boson AI 获取许可。

<a id="speech-recognition"></a>

### 语音识别

**语音识别**会把一段语音所说的内容写成文字。它会检查一个批次中的各段语音（当后端启用了**用语音识别检查文字**时），并识别[语音片段的文本](#voices-and-clips)。选择它的运行位置：

| 设置 | 作用 |
|---|---|
| 语音识别 | **ComfyUI（Granite ASR）**：你的 ComfyUI 中 TTS Audio Suite 的 Granite 语音模型（地址即 ComfyUI 的地址）。**OpenAI 兼容（whisper）**：任何提供 `/v1/audio/transcriptions` 的服务器，例如 whisper 服务器（faster-whisper、speaches）或 OpenAI API。 |
| 地址、API 密钥 | ComfyUI 的地址，或 API 的基础地址（可带或不带 `/v1`）；密钥会加密保存。地址为空：不进行语音识别。 |
| GPU 锁 | 语音识别所占用的 [GPU 锁](../administration/gpus.md)。若在同一块显卡上使用 fp32 的 Higgs，请为其设置一个单独的锁，其步骤会重启 ComfyUI：ComfyUI 会把语音模型保留在服务器内存中，若在旁边再加载语音识别，服务器会内存不足。 |
| 识别模型 | 留空：ComfyUI 使用 `granite-speech-4.1-2b`，OpenAI 兼容服务使用 `whisper-1`。 |
| 超时、服务器在这台机器上或在我的网络中 | 与后端相同。 |

**测试连接**会检查 ComfyUI 是否具备所需节点，或服务器是否有响应。批次中的各段语音在批次完成后检查，此时语音服务器已被释放，因此语音识别和语音模型永远不会同时占用显卡。

<a id="cleanup-of-clips"></a>

### 片段清理

**片段清理**在装有 [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) 的 ComfyUI 上清理参考音频片段（与 Higgs 作品使用的是同一个）。结果会成为[声音与片段](#voices-and-clips)中该片段的一个新**版本**；原片段保持不变。

| 设置 | 作用 |
|---|---|
| 地址、API 密钥 | ComfyUI 的地址；密钥会加密保存。地址为空：不进行清理。 |
| GPU 锁 | 清理所占用的 [GPU 锁](../administration/gpus.md)。请为其设置单独的锁，而不要使用语音或语音识别的锁（其步骤会重启 ComfyUI），以确保清理模型永远不会与语音模型同时加载。 |
| 超时、服务器在这台机器上或在我的网络中 | 与后端相同（超时针对单次清理，默认 10 分钟：ComfyUI 首次使用某个模型时会下载它）。 |
| 清理 | 在声音与片段中按名称选择的清理方案（预设）。每个方案是依次运行的一组步骤。**恢复默认清理**会恢复随程序提供的方案。 |

一个步骤可以是以下之一：

- **分离模型**：套件中的一个模型，将音频分为两部分，即*提取*部分和*剩余*部分，并保留其中之一。哪一部分包含人声取决于模型：人声模型（`MelBandRoformer`）提取人声，降噪模型提取噪声，因此应保留*剩余*部分。**强度**（0 到 20）决定去除的程度。
- **语音修复**：修复受损的人声（电话录音、旧录音）。它比分离更多地改变人声的声音。
- **ffmpeg（此处）**：**用 ffmpeg 清理**的滤镜，由应用程序在 ComfyUI 的步骤之间运行。

程序自带的清理方案有：*从音乐中提取人声*（从电影或歌曲中提取人声）、*去除噪声*、*修复人声*，以及*电影或游戏片段*（先从音乐中提取人声，再去除噪声）。**测试连接**会检查 ComfyUI 是否具备所需节点，并用服务器上的模型填充模型列表。模型会在清理首次使用时由 ComfyUI 下载。

<a id="shared-speech-servers"></a>

### 共享语音服务器

在多用户的安装环境中，管理员可以为所有人保留语音服务器，这样一台 GPU 机器只需设置一次，其他人无需地址或密钥。管理员在同一面板中进行设置（最后几节，仅管理员可见）：**共享语音服务器**会打开一个窗口，其中的字段与你自己的后端相同，另外还可以设置谁能使用它：**所有用户**，或列表中选定的用户。该窗口中有一个开关，**用户可添加自己的语音服务器**，用于决定下方的后端是否仍被允许。

你能看到的内容：

- 面板顶部的**正在使用的后端**列出你可以使用的共享服务器（标注为 *(shared)*）以及你自己的服务器。*转换为音频*窗口和工作室会使用你选择的那一个；命令也会使用它。
- 你只能看到共享服务器的名称，而看不到它的地址、密钥或请求。同一服务器地址上所有用户的批次会相互等待，因此两个用户无法同时启动同一块 GPU。
- 当管理员取消你对某服务器的使用权限时，仍指向该服务器的任务会被拒绝；请选择另一个服务器。
- 当管理员关闭你自己的服务器时，面板会给出提示。你添加的服务器会被保留，并在开关重新打开后再次可用。
- 共享服务器的*旁白声音*应为该服务器上的一个语音文件（`predefined:Henry.wav`）：参考音频片段是某一用户的资源，其他用户无法读取。你角色的声音照常工作。
### Actors

Un **actor** est une voix à vous : un extrait de référence (l'une de vos [ressources](../resources.md) audio, ou un nouveau fichier téléversé ici) ou un fichier de voix du serveur, avec ses propres voix pour une **humeur** (un chuchotement, un cri) ou une **langue** (un extrait qui parle japonais). La langue l'emporte sur l'humeur, et sans voix pour elles, c'est la voix de l'actor qui est lue. Vos actors sont conservés dans votre compte ; chaque livre peut attribuer un actor à son narrateur et à ses personnages.

Le bouton **Aperçu** à côté d'une voix (dans l'actor, les paramètres et la distribution) joue quelques phrases prononcées avec elle (une courte, dans la langue de la voix, pour une langue). Si le GPU est occupé par autre chose, le verrou GPU charge d'abord le serveur de synthèse vocale. La flèche à côté propose d'autres échantillons : **avec des sons** (un soupir, un rire, un halètement) pour les modèles qui les prononcent (Chatterbox Turbo, Higgs Audio v3), et pour Higgs, chaque **humeur** de l'étiquetage (triste, chuchoté, sarcastique...). Les actors sont listés par leur nom.

Chaque échantillon ne prend le GPU que pour lui-même. Le GPU reste chargé avec le serveur de synthèse vocale après un échantillon, de sorte que les échantillons suivants arrivent immédiatement, jusqu'à ce qu'une requête du modèle de langage (ou d'un autre serveur) ait besoin du GPU. Ouvrir **Voix et extraits** charge déjà le serveur de synthèse vocale, si bien que son premier échantillon n'attend pas.

![Un actor avec une voix pour une humeur](../../images/plugin-audiobook-actor.png)

<a id="voices-and-clips"></a>

### Voix et extraits

**Voix et extraits** (à côté des actors) est une fenêtre unique pour vos actors et les extraits dont leurs voix sont clonées. Elle comporte trois onglets, côte à côte au départ : **Actors**, **Extraits** et **Extrait**. Faites glisser un onglet pour le placer à côté, sous ou avec un autre ; la fenêtre se rouvre telle que vous l'avez laissée.

- **Actors** : chacun avec sa voix, le nombre d'autres voix (humeurs, langues) qu'il possède et la pire qualité de ses extraits ; ajoutez, modifiez ou supprimez-en un.
- **Extraits** : vos extraits dans des **dossiers** (par exemple un par personnage), avec les **versions** créées à partir d'un extrait placées dessous, chacune avec sa durée, sa **qualité** et l'endroit où elle est utilisée (actors, avec l'humeur ou la langue, et les narrateurs de vos backends). **Ajouter un extrait** en téléverse un.
- **Extrait** : l'extrait sélectionné.
  - **Nom** et **Dossier** : renommez l'extrait (le fichier garde son nom) et placez-le dans un dossier, un nouveau en tapant son nom. Une version reste dans le dossier de l'extrait dont elle est issue, et indique ce dont elle est issue et comment.
  - **Lire** l'extrait, lisez ce qui ne va pas avec lui, **Remplacer...** le ou **Supprimer** le (seulement lorsque rien ne l'utilise ; ses versions remontent alors à l'extrait dont elles sont issues).
  - **Nettoyer avec ffmpeg** crée une version où le sifflement et le ronflement constants sont atténués. Cela ne supprime pas la musique, les effets ni les autres voix.
  - **Nettoyer avec l'IA** crée une version avec le [nettoyage](#cleanup-of-clips) choisi sur ComfyUI. Cela s'exécute en arrière-plan : vous pouvez continuer à travailler, et un message indique quand la version est prête.
  - **Utiliser cette version** place une version partout où l'extrait dont elle est issue est utilisé. Les parties sont refaites avec la nouvelle voix, et l'ancien extrait reste jusqu'à ce que vous le supprimiez.
  - **Texte de l'extrait** : les mots prononcés dans l'extrait. Tapez-les, ou appuyez sur **Reconnaître** pour que la [reconnaissance vocale](#speech-recognition) les transcrive, corrigez-les puis **Enregistrer**. La boîte de dialogue de l'actor affiche le même champ pour l'extrait de la voix de l'actor. Une version et un remplacement reprennent le texte. Higgs ne le reçoit que lorsque le backend le transmet (désactivé par défaut).
- **Remplacer...** place un autre extrait, l'un des vôtres ou un que vous téléversez ici, partout où l'extrait est utilisé.

Les sélecteurs d'une voix (de l'actor, du narrateur d'un backend) affichent un extrait sous la forme *dossier / extrait › version*.

<a id="a-good-clip"></a>

#### Un bon extrait

Un modèle de voix copie l'**enregistrement**, pas seulement la voix : la pièce, un sifflement, la musique sous une scène de film reviennent dans chaque réplique, souvent sous la forme d'un doux « whoosh » sous les mots que nul filtre du résultat ne supprime. Donc :

- Utilisez un enregistrement propre d'un seul locuteur avec de vraies pauses (un échantillon d'acteur de doublage, une lecture faite chez soi dans une pièce calme), de 12 à 30 secondes. Un extrait découpé dans un film ou une série est le pire des cas.
- Les marques de qualité sont mesurées à partir de l'extrait : **Propre** (les pauses sont au moins 45 dB sous la parole), **Correct** (35 à 45 dB, ou plus long que 30 s, ce que Chatterbox coupe), **Médiocre** (son de fond, sous 35 dB ; déformé ; plus court que 8 s). Un extrait qui n'est pas propre est signalé lorsque vous l'ajoutez.
- Nettoyez un extrait avec l'IA ([Nettoyage des extraits](#cleanup-of-clips)) : lors du test, le nettoyage *Extrait de film ou de jeu* a fait passer un extrait avec sifflement, ronflement et écho de *Médiocre* à *Propre* (les pauses sont passées de 38 à 89 dB sous la parole). Écoutez la version avant de l'utiliser : un modèle de séparation peut aussi enlever une respiration ou la fin d'un mot doux. Le nettoyage ffmpeg n'atténue qu'un bruit constant : sur un extrait de film, il a gagné environ 3 dB et l'extrait est resté *Médiocre*.
- Sans ComfyUI, nettoyez un extrait à la main : la réduction de bruit d'[Audacity](https://www.audacityteam.org/) avec un profil de bruit pris dans une pause de l'extrait, ou un séparateur de voix comme [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) pour un extrait avec de la musique.

<a id="converting-new-parts-by-itself"></a>

### Convertir les nouvelles parties automatiquement

*Convertir les nouvelles parties en audio automatiquement* est la valeur par défaut pour tous vos livres ; voir [Conversion automatique](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## La distribution et la prononciation d'un livre

Les deux sont conservées avec le livre (elles voyagent dans sa sauvegarde) et se modifient depuis la boîte de dialogue **Convertir en audio**.

**Distribution** indique qui lit : un actor pour le **narrateur**, un pour les **autres locuteurs** (quiconque le modèle nomme et que la distribution ne prévoit pas ; vide : le narrateur lit) et un pour chaque **personnage**. Un personnage a un nom et d'autres noms que le texte peut utiliser pour lui (*Tom* pour *Tomáš Novák*), séparés par des virgules ; une réplique est attribuée au personnage lorsque son nom, l'un des autres noms, ou un mot du nom qu'aucun autre personnage ne partage, correspond, sans tenir compte de la casse ni des accents. **Ajouter les personnages du livre** remplit la liste à partir du point de vue et des personnages présents dans les parties.

![La distribution d'un livre](../../images/plugin-audiobook-cast.png)

**Prononciation** est la façon dont le texte est écrit *pour la voix* ; le livre lui-même n'est jamais modifié. Le **dictionnaire** remplace des mots entiers (la casse compte) par ce qui doit être dit pour eux : une réécriture phonétique pour un nom ou un mot inventé, ou une lecture en kana pour le japonais. Pour un livre en anglais, les règles lisent aussi à voix haute les autres éléments : nombres, ordinaux, années, heures, sommes d'argent et abréviations en toutes lettres, un titre qui n'est qu'un nombre comme *Chapter four. The Arch* (le mot placé avant le nombre est le vôtre), les marques markdown comme `*` sont supprimées, et une rupture de scène (`***`, `---`) n'est pas lue mais produit une pause plus longue. Désactivez les règles pour un livre dans une autre langue ; le dictionnaire s'applique toujours. **Essayer un texte** montre comment un passage sera lu. Un passage dont le texte prononcé change est refait, les autres sont conservés.

![La prononciation d'un livre](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## 将书籍转换为有声读物

*故事编辑器的工具菜单（齿轮按钮）→ **Convert to audio***。

![开始前的对话框](../../images/plugin-audiobook-convert.png)

1. 选择 **backend** 并按 **Load**：列出当前分支中带有文本的各部分（第一部分为 1），并显示每部分的状态：
   *Not generated*、*Labelled*（已保存模型的标注，尚无音频）、*Generated*（带有播放按钮和下载按钮），或 *Generated (the text changed)*（音频由较旧的文本生成，将重新生成）；工作室标记为定稿的部分会加上 *(final)*。
2. 普通版本请保持 **Label the parts with a language model** 关闭：旁白会朗读每个段落。开启后，模型会决定每段对话由谁说、以什么方式说（平静、耳语、大喊……）以及在何处插入声音标签，角色表则负责分配声音。模型由书籍选定；如果愿意，也可以换一个。各部分会先被标注，然后朗读。如果某部分的文本已有标注，即使该选项关闭，也会按这些标注朗读（不再询问模型），因此在关闭该选项的情况下重新运行已标注的书籍，不会把对话变成普通旁白；如果要普通旁白或重新标注，请使用 **Remove labels**。
3. 勾选各部分（表头的复选框可全部勾选），然后为它们**规划**操作。**Plan** 列会显示规划的内容，但此时尚未执行任何操作：
   **Generate**（生成缺少的内容；已是最新的部分不会重新生成）、**Regenerate**（删除音频、移除标注，然后生成）、**Delete audio**（删除该部分的音频片段和合并后的音频，标注保留）、**Remove labels**（下次生成该部分时会重新询问模型）、**Clear the plan**（清除规划）。同一部分的步骤可以叠加（不带 Generate 的 Delete audio 和 Remove labels 只做清理）；标记为定稿的部分会被排除在所有规划之外。
4. **Start** 会执行规划。当操作会删除音频或标注时，程序会先询问。需要生成的内容会进入你的队列，关闭窗口或离开页面后队列仍会继续运行；等待中或正在队列中运行的部分不会被纳入规划。该部分的状态、已生成的片段以及任务消息会显示在该部分的行上。
   模型为某部分添加标注时，该行会显示 *Labelling pieces 17-32 of 120*（模型每次处理 16 个片段）；**点击**已标注部分的状态，即可实时查看每次请求中模型的推理过程和回答。这些内容会随任务保存在内存中，直到清除已完成的任务（*Clear finished*）或重启应用程序。

![队列：已生成的片段，以及部分完成时的下载链接](../../images/plugin-audiobook-queue.png)

每个部分的生成过程如下，便于你了解预期结果：

- 文本由**程序**而非模型拆分，分为旁白片段和直接对话片段（对话以直引号、弯引号或日式角括号标出；标题视为旁白）。模型只会为编号的片段添加**标注**。只有在带有声音标签的文本与原文仅有标签差异、其他内容完全未变时，才会使用该文本，因此模型无法改动你的书籍。
- **对话由谁说，由模型决定；旁白由旁白者朗读。**信件、便条、消息或角色的内心想法没有引号，因此程序会将其视为旁白。只有当这类片段**以斜体开头**（`*...*` 或 `_..._`），且模型根据上下文（如署名、"笔迹一望便知"）指明了角色时，才会由该角色（角色表中的声音）朗读。第三人称叙述即使是从角色视角出发（"She watched as Percy crossed the room"），也始终由旁白者朗读：对于未使用斜体的片段，程序会忽略模型指定的角色。用于强调、声音或外来词的斜体同样属于旁白。文字始终是你的原文。
- **发给模型的指令是可以修改的模板。***设置 → 扩展设置 → Audiobook Settings → Labelling prompt* 是你的默认模板；书籍（*Prompts* 标签页 → *Extension Settings* → *Audiobook*）或文件夹可以拥有各自的模板。留空则依次使用上一级文件夹的模板、你的默认模板，最后是内置模板。模板的编写方式与其他[提示词](../books/prompts.md)相同：变量为 `{{characters}}`、`{{deliveries}}`、`{{tags}}` 和 `{{wordsPerTag}}`，提示弹窗中有 **Insert Default Template**。修改指令后，下次生成时，相关部分的标注会被重新询问。
- **各部分的音量会被统一**，前提是安装了程序 ffmpeg（与生成 `.m4b` 所用的是同一个程序）：每个部分合并后的音频都会被调整到相同的响度（EBU R128，线性模式，因此动态范围得以保留），默认为 **-18 LUFS**，可在 *设置 → 扩展设置 → Audiobook Settings* 中修改，也可以在该处关闭此功能。各片段保持服务器生成时的原样，只对合并后的部分进行音量调整，因此有声书在不同部分之间不会出现音量跳变。**此前已生成的部分**在你重新转换之前不会被改动：转换时会从已保存的片段重新合并并调整音量，且不会向语音服务器发出任何请求（也不会卸载语言模型）。关闭音量统一功能后，已调整过的部分保持不变。如果某部分的 ffmpeg 处理失败，该部分会保留原音频，结果中会注明。
- 一次点击所生成的全部部分，会按**语音模型分批**处理：批次前的请求只执行一次，然后朗读所有片段，最后执行批次后的请求，也只执行一次。这样可以避免显卡为每个部分都切换模型。
- 每个片段都会**根据其内容保存**（文本、声音、模型、语言、设置、随机种子）。已是最新的部分不会产生任何开销；改动过的段落是唯一需要重新生成的片段；失败的部分在你点击 **Retry** 时，会保留已生成的片段。
- 片段会根据其所处位置（标题之后、段落之间、*he said* 之前）添加停顿，然后合并为每个部分一个 mp3 文件，并与该部分一同保存。
- 你发起的生成任务运行期间，语音生成会等待，因此语言模型不会在其运行过程中被卸载。
- 你在工作室中标记为**定稿**的部分不会被改动，对话框会显示被跳过的数量。
- **整本有声书**会将所有已有最新音频的部分合并为一个 mp3，每个部分对应一个章节标记，并在列表下方显示下载链接（未纳入的部分会被列出）。同一文件也是导出格式 *one mp3 with chapter marks*。
- **Stop all** 会取消等待中的部分，并立即停止正在运行的部分：对语言模型的请求（标注）或对语音服务器的请求（正在朗读的片段）会被直接中止，而不是等待其完成。在开始前就被停止的批次不会发送其之前的请求，因此语言模型保持加载。**Download** 可下载已完成部分的 mp3。
<a id="the-audiobook-studio"></a>

## 录音工作室

*工具菜单 → **有声书工作室*** （所选片段的声音及其试听位于其扬声器旁边）是制作最终有声书的地方，按部分逐一进行：你可以看到脚本、修改它、试听一个片段并制作该部分。

![选中了一段语音的工作室](../../images/plugin-audiobook-studio.png)

- 在标注或制作某部分期间，一个提示条会显示有任务正在进行，而会启动更多工作的按钮则处于等待状态。
- **标注**会生成该部分的脚本（如果你要求，则由语言模型生成，否则由旁白生成），并在表格中显示各个片段：说话人、演绎方式、强度、朗读文本，以及你修改过的片段的标记。运行期间，旁边会出现一个**停止**按钮，用于结束对语言模型的请求。
- 选中一个片段即可编辑其**说话人**、**朗读文本**、**演绎方式**、**强度**（声音的夸张程度，适用于具有该参数的模型；在 Higgs 上，演绎方式和强度是朗读文本开头的标记，如 `<|emotion:sadness|>`，修改时它们会随之变化）、**语言**、**种子**、**之后的停顿**。点击**应用**保存修改并**锁定**该片段：新的标注会保留已锁定的片段不变。**解锁**则将其交还给标注。*语音服务器收到的请求*会显示确切的请求内容。
- **试听**生成某个片段的音频（仅该片段，为此会启动语音服务器）；**另一个版本**会使用新的种子重新生成，并且**版本**会被保留，供你选择喜欢的那一个（*使用此版本并试听*）。好的声音往往取决于种子。
- **用此脚本制作本部分**按原样拼接各个片段，不进行新的标注。
- 部分的**状态**为*草稿*、*已审阅*或*定稿*。定稿的部分不会再次标注或转换，导出时可以仅限于定稿的部分。

<a id="exporting"></a>

## 导出

有声书是书籍**导出故事**中的一种格式：**有声书（每章一个 mp3，打包为 zip）**、**有声书（一个带章节标记的 mp3）**，以及在安装了 ffmpeg 时提供的**有声书（一个带章节的 m4b）**。它们由最新部分的音频制成：没有音频的部分，或者音频早于其文本或工作室中的脚本的部分，会被排除，窗口会说明是哪些部分。mp3 文件带有 ID3 标签（书名、章节、音轨），单个文件带有章节标记，VLC 及大多数有声书应用都能显示；zip 包含一个播放列表。复选框**有声书：仅限标记为定稿的部分**可将导出限制在你在工作室中完成的部分。

**m4b** 是 Apple Books 及大多数手机有声书应用所需的格式：采用 AAC 音频（64 kbit/s，足以满足语音需求），装在带有章节、书名、作者和类型的 MP4 容器中。它由 ffmpeg 生成，因此该格式仅在安装了 ffmpeg 的服务器上提供。以 mp3 存储的音频会为此再次编码；以 wav 存储的音频只编码一次。

<a id="how-the-audio-is-stored"></a>

### 音频的存储方式

管理员可以在*设置 → 扩展设置 → 有声书设置*（最后一节，仅管理员可见）中为整个安装实例选择存储方式：

- **mp3**（默认）：体积小，无需解码器即可拼接，无需 ffmpeg。m4b 是第二次有损编码。
- **wav**：无损，大小约为十倍（一小时语音约 170 MB），需要 ffmpeg。各片段以 wav 格式从语音服务器获取并以 wav 拼接；浏览器直接播放该文件；mp3 和 zip 导出由 ffmpeg 编码（96 kbit/s），m4b 只编码一次。

该选择从此刻起对之后制作的内容生效。之前制作的部分保留其音频，并按原样理解；两种类型混合的部分（工作室中较早锁定的片段）会以 wav 拼接，两种类型部分的导出可以进行，但需要 ffmpeg。

整本书的导出是从服务器上各部分的已存储文件生成临时文件，并从服务器下载，因此其长度不受应用程序内存的限制；但在生成期间，临时文件夹需要约两倍于本书音频的空间（若为 wav，则每小时约 350 MB）。

![包含有声书格式的导出对话框](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## 自动转换

一本书可以在某部分定稿后自动生成该新部分的音频：在自动标注、重新起草及翻译（如果该书有翻译）之后，作为[该部分之后的流程](../books/story-editor.md#what-follows-a-generated-part)的最后一步（在运行面板中可见，直到音频生成完成；其**停止**会将该部分移出队列）。默认情况下它是**关闭**的。你可以在书籍的*提示词*标签页的扩展设置中选择，也可以为一个书籍文件夹选择，或在有声书设置中为你的所有书籍选择；以最近的设置为准：

- **否**，
- **是：由旁白朗读该部分**，
- **是：由本书的模型标注角色，演员阵容决定声音**；没有模型的书籍将使用普通旁白，并会向你说明。

![书籍中的设置](../../images/plugin-audiobook-auto.png)

该部分会进入你的队列，并带上本书的演员阵容和发音。一条通知会告诉你它已进入队列，另一条会告诉你其音频已就绪或失败；它们只会显示在发起生成的页面中，但任务无论如何都会出现在*转换为音频*窗口中。

<a id="listening"></a>

## 收听

已发布的书籍（或阅读器）若有带音频的部分，会显示一个播放条：播放、暂停、停止、速度，以及在文本中标出正在播放的部分。它从所选内容或当前可见的第一个部分开始，按顺序播放各部分，并在当前部分播放时预先获取下一部分；设备的媒体控制也能识别该书。由旧版文本生成的部分音频不会提供。音频会提供给书籍的所有者，并且在书籍发布期间，提供给所有能打开它的人；撤回发布的书籍会立即停止提供音频（在下一部分加载播放器时生效）。

![阅读器中的播放器](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### 没有音频的部分

播放器会在没有音频的部分停下，以免跳过故事的某部分，并会说明原因。当安装了扩展 [朗读](tts-reader.md) 时，播放条还会显示一个扬声器按钮，**用浏览器的语音朗读该部分（以及之后没有音频的部分）**。除非你按下它，否则不会用机器语音朗读任何内容。随后它会朗读该部分以及之后没有音频的各部分，使用你设备的语音；当遇到有音频的部分时，播放器会重新接管。同一次收听中之后没有音频的部分会以同样方式朗读，无需再次询问；**停止**会使其静音，而重新启动播放器则会再次询问。语音、速度和音调均沿用朗读扩展的设置。
<a id="commands"></a>

## 命令

与[命令](../commands/index.md)相同的内容（故事编辑器的命令行、模板中的 `command` 宏、智能体）。部分按转换对话框中的编号排列，或按其句柄命名。

| 命令 | 作用 |
|---|---|
| `/audiobook-status [from [to]]` | 当前使用的语音服务器、本书自动转换的方式、有多少部分已有音频（无、就绪、早于文本、早于脚本）、工作室中每个列出部分的状态，以及队列。仅在书很长时较慢。 |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | 将部分放入队列（不带参数时为整本书，仅给出 `from` 时为一个部分）。`labelling` `true` 使用本书的模型；`what` 即 `audio` 或 `script`（仅为工作室生成脚本，不朗读任何内容）。使用 `wait` `true` 时，命令会一直运行直到任务结束，显示进度并说明每项任务的结束方式；停止命令会取消尚未开始的任务。 |
| `/audiobook-stop` | 取消等待中的部分；正在运行的部分会在完成当前片段后停止。 |
| `/audiobook-set-part-status part status` | 在工作室中执行 `draft`、`reviewed` 或 `final`；该部分需要脚本。 |
| `/audiobook-cast` | 旁白、备用声音，以及演员表中的角色及其配音演员。 |

![命令的结果](../../images/plugin-audiobook-command.png)

音频是为**你的**书制作的，而不是为[智能体](../agents.md)隐藏的工作副本制作的：智能体可以读取演员表，但其他命令会拒绝它并提示你必须自行运行。请从命令行运行这些命令，或让智能体负责准备书稿（标注、审校），然后由你自己进行转换。

<a id="notifications"></a>

## 通知

当一批转换完成后，浏览器可以通知你（[通知](../account.md#notifications)，这样在语音服务器工作时你可以离开该标签页）。在 *Settings* 页面的通知列表中有两行：**Convert to audio**（你在对话框中或用命令启动的批次）和 **Convert to audio (automatic)**（自动制作成音频的新部分）。你中止的批次不会发出通知；有失败部分的批次会提示失败。

![通知列表](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## 尚未实现的功能

- 仅支持 mp3 和 wav（不支持 opus）；各部分的响度取决于声音本身。
- 没有音效或环境声。
- 两种语音引擎：Chatterbox 和 Higgs Audio v3（在 ComfyUI 中）。
- 无法在片段之外试听声音。
- 演员表不属于书籍备份中你的 *演员* 的一部分：恢复的书会保留其演员表，但所指名的演员必须存在于你的账户中。
