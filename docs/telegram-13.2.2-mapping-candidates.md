# Telegram 13.2.2 Mapping

Manual mapping for the official Telegram 13.2.2 build, derived from the JADX
sources at:

```text
/home/yohlime/workdir/apks/tg/13.2.2/sources
```

There is **no official R8 `mapping.txt`** for this build. Everything below is
source-verified; members that are not verified yet are listed as unresolved
instead of guessed. The verified entries have been written to
`app/src/main/assets/obfuscation_mappings/telegram.json`.

> JADX renames colliding fields, e.g. `public long f34736e1;` with the comment
> `/* renamed from: e1 */` is the dex field `e1`. Always use the
> `renamed from` name in the mapping.

---

## Verified — `org.telegram.ui.ProfileActivity`

Class name is kept.

| Logical | Obfuscated | Evidence |
|---|---|---|
| `userId` | `e1` | `this.f34736e1 = arguments.getLong("user_id", 0L)` |
| `chatId` | `f1` | `this.f34744f1 = arguments.getLong("chat_id", 0L)` |
| `topicId` | `g1` | `this.f34751g1 = arguments.getLong("topic_id", 0L)` |
| `myProfile` | `s1` | `this.f34833s1 = arguments.getBoolean("my_profile", false)` |
| `onlineTextView` | `r` | `pw0` status view, `r.o(text, animate)` in `K5()` |
| `usernameRow` | `L3` | row index used for `h20.n(user)` and `@username` copy |

| Logical | Obfuscated | Evidence |
|---|---|---|
| `updateProfileData` | `L5` | `L5(boolean)` binds avatar (`"avatar"`), name and status |
| `updateOnlineText` | `K5` | no-arg refresh of `r` from cached status strings |
| `processOnClickOrPress` | `T4` | `boolean (float, float, int row, View)` row-click handler |
| `editRow` | `Z3` | `boolean (int row, View)` builds the native row popup |

Notes:

- The old 12.10.3 `onlineTextView` hint (`SimpleTextView[]`) no longer applies.
  In 13.2.2 the status is a single `pw0 extends a5` (`a5 → i80 → y5`), and
  `q0` is an avatar `Drawable` (`y8`), not the status view.
- `usernameRowClick` does not exist in this build. Nekogram's method of that
  name has no official counterpart, so `ProfileDetails` now falls back to
  `editRow` (native popup + appended "Copy ID") and then to
  `processOnClickOrPress` (module-built popup).

---

## Verified — `org.telegram.ui.SecretMediaViewer`

Class name is kept. Fields matched by type and by call sites.

| Logical | Obfuscated | Evidence |
|---|---|---|
| `activeViewer` | `w1` | `public static volatile SecretMediaViewer f34908w1` |
| `currentAccount` | `a` | `int`, used with `MessagesController.getInstance(account)` |
| `parentActivity` | `b` | `Activity`, used with `WindowManager` / `LaunchActivity` |
| `containerView` | `e` | `bf.e extends FrameLayout`, receives view children |
| `centerImage` | `h` | `public final ImageReceiver h = new ImageReceiver(null)` |
| `actionBar` | `G` | `nf.d1 extends ActionBar`, `G.setTitle(...)` |
| `messageObject` | `g0` | type `org.telegram.messenger.mj` (= `MessageObject`) |
| `secretDeleteTimer` | `n` | type `c11 extends FrameLayout`, alpha/click/invalidate |
| `isVideo` | `K` | set only in the non-voice document branch; drives video controls |

| Logical | Obfuscated | Evidence |
|---|---|---|
| `openMedia` | `h` | `SecretMediaViewer.f().h(messageObject, ..., ..., ...)` in `in.java` |

The old 12.10.3 candidates `actionBar=R`, `centerImage=Q`,
`messageObject=P`, `isVideo=I`, `secretDeleteTimer=X` were all wrong: `R`, `Q`
and `P` are unused fields of unrelated types, `I` is a different flag and `X`
is the description scroll container.

---

## Verified — `org.telegram.messenger.MessageObject`

`MessageObject.java` is gone in 13.2.2; the class is obfuscated to
`org.telegram.messenger.mj`.

| Logical | Obfuscated | Evidence |
|---|---|---|
| class | `org.telegram.messenger.mj` | `mj` holds `TLRPC.Message messageOwner` |
| `messageOwner` | `k` | `public TLRPC.Message f17773k` |
| `getId` | `e1` | returns `ephemeralAnchorMsgId` or `messageOwner.id` |
| `getDialogId` | `A0` | `return B0(this.messageOwner)` |
| `isSecretMedia` | `m5` | checks `TL_message_secret` TTL rules |

---

## Verified — `org.telegram.messenger.AndroidUtilities`

Obfuscated to `org.telegram.messenger.h0`.

| Logical | Obfuscated | Evidence |
|---|---|---|
| `addToClipboard` | `g` | `ClipboardManager.setPrimaryClip(...)`, `boolean (CharSequence)` |
| `dp` | `L` | `return (int) Math.ceil(density * value)` |

---

## Verified — `org.telegram.ui.ActionBar.ActionBar` / `ActionBarMenu`

| Logical class | Obfuscated | Evidence |
|---|---|---|
| `ActionBar` | `org.telegram.ui.ActionBar.l` | `actionBar` field type on `BaseFragment` (`j2`) |
| `ActionBarMenu` | `org.telegram.ui.ActionBar.y` | `extends LinearLayout`, returned by `l.s()` |

| Logical | Obfuscated | Evidence |
|---|---|---|
| `ActionBar.menu` | `F` | `getMenuView()` returns `this.F` |
| `ActionBar.createMenu` | `s` | lazy factory, `new y(getContext(), this)` |
| `ActionBarMenu.addItem` | `d` | `(int, Drawable) -> ActionBarMenuItem` |
| `ActionBarMenu.getItem` | `j` | `findViewWithTag(id)` |

The old `ActionBarMenu -> org.telegram.ui.ActionBar.z` is invalid: `z` is a
`View.OnClickListener` in 13.2.2.

---

## Verified — `ItemOptions` / `Bulletin` / `BulletinFactory`

| Logical class | Obfuscated | Evidence |
|---|---|---|
| `org.telegram.ui.Components.ItemOptions` | `org.telegram.ui.Components.w60` | `w60.H(BaseFragment, View)` factory, `add`/`show` |
| `org.telegram.ui.Components.Bulletin` | `org.telegram.ui.Components.bc` | factory return type, `j()` shows |
| `org.telegram.ui.Components.BulletinFactory` | `org.telegram.ui.Components.hc` | static `create*` methods returning `bc` |

| Logical | Obfuscated | Evidence |
|---|---|---|
| `ItemOptions.add` | `c` | `(int, CharSequence, Runnable, boolean)` |
| `ItemOptions.setGravity` | `V` | sets the popup gravity field |
| `ItemOptions.show` | `Z` | large attach/animate method |
| `Bulletin.show` | `j` | `k(false); return this` |
| `BulletinFactory.createSaveToGalleryBulletin` | `F` | `(FrameLayout, boolean) -> bc` |

`ItemOptions` is what the new username popup uses (`w60`), so hooking
`ItemOptions.show` is what appends "Copy ID" to the native menu.

---

## Verified — `org.telegram.ui.ChatActivity`

Class is `org.telegram.ui.xn` (found by the
`clear history by overwrite firstLoading=` string).

| Logical | Obfuscated | Evidence |
|---|---|---|
| `dialog_id` | `k6` | `bundle.putLong("dialog_id", this.f44183k6)` |
| `createView` | `createView` | `createView(Context)` keeps its name (override of `BaseFragment`) |
| `fillMessageMenu` | `F8` | `void (MessageObject, ArrayList, ArrayList, ArrayList)` |
| `addSponsoredMessages` | `G6` | no-arg, reads `MessagesController.O1(dialogId)` and the `https://t.me` regex |

`sendSecretMediaDelete` no longer exists as a ChatActivity method in 13.2.2.
The deletion path is `MessagesController.r4(MessageObject)` from the
notification handler, so the hook now fails soft instead of aborting the rest
of `PreventSecretMediaDeletion`.

---

## Verified — `MessagesController` / `UserConfig`

| Logical class | Obfuscated | Evidence |
|---|---|---|
| `org.telegram.messenger.MessagesController` | `org.telegram.messenger.wp` | `getMessagesStorage`, `getConnectionsManager`, `s1(account)` |
| `org.telegram.messenger.UserConfig` | `org.telegram.messenger.d20` | `i(account)`, `e()` -> user id, `f()` -> current user |

| Logical | Obfuscated | Evidence |
|---|---|---|
| `MessagesController.getInstance` | `s1` | `public static wp s1(int)` |
| `MessagesController.getSponsoredMessages` | `O1` | `(long dialogId)`, sends `TL_messages_getSponsoredMessages` |
| `MessagesController.checkPromoInfoInternal` | `N` | `(boolean)`, sends `TL_help_getPromoData` |
| `MessagesController.removePromoDialog` | `j2` | no-arg, sends `TL_help_hidePromoData` |
| `MessagesController.getGlobalMainSettings` | `e1` | `static () -> SharedPreferences` |
| `MessagesController.isChatNoForwards` | `o2` | `boolean (TLRPC.Chat)`, reads `chat.noforwards` |
| `MessagesController.isChatNoForwardsById` | `A2` | `boolean (long)`, user/channel dispatch |
| `MessagesController.getChat` | `M0` | `TLRPC.Chat (Long)` |
| `MessagesController.deleteMessages` | `b0` | 12-param delete entry point |
| `UserConfig.getInstance` | `i` | `public static d20 i(int)` |
| `UserConfig.getClientUserId` | `e` | `() -> long` |
| `UserConfig.getCurrentUser` | `f` | `() -> TLRPC.User` |
| `UserConfig.setCurrentUser` | `w` | `void (TLRPC.User)`, sets `f16047c` and `f16059n` |
| `UserConfig.isPremium` | `q` | `boolean ()`, returns `currentUser.premium` |
| `UserConfig.selectedAccount` | `t0` | `public static int` |

### Storage, notifications, files

| Logical | Obfuscated | Evidence |
|---|---|---|
| `MessagesStorage` | `org.telegram.messenger.is` | returned by `BaseController.getMessagesStorage()` |
| `MessagesStorage.database` | `b` | `public SQLiteDatabase f17089b` |
| `MessagesStorage.markMessagesAsDeletedInternal` | `P0` | `(long dialogId, ArrayList ids, boolean, boolean, int, int)`; 6 params in 13.2.2 |
| `NotificationCenter` | `org.telegram.messenger.zs` | returned by `BaseController.getNotificationCenter()` |
| `NotificationCenter.postNotificationName` | `k` | `void (int, Object...)`; `zs.v == 7` is still `messagesDeleted` |
| `NotificationsController` | `org.telegram.messenger.nt` | returned by `getNotificationsController()` |
| `FileLoader` | `org.telegram.messenger.w7` | returned by `BaseController.getFileLoader()` |
| `FileLoader.getInstance` | `G` | `static (int)` |
| `FileLoader.getPathToMessage` | `L` | `static File (TLRPC.Message)` |
| `FileLoader.getDirectory` | `j` | `static File (int type)` |
| `FileLoadOperation` | `org.telegram.messenger.g7` | instantiated by `FileLoader`, 128 KB chunk default |
| `FileLoadOperation.updateParams` | `t` | recomputes `downloadChunkSizeBig`, request counts and `maxCdnParts` |
| `FileLoadOperation.downloadChunkSizeBig` | `n` | `int`, default `131072` |
| `FileLoadOperation.maxDownloadRequests` | `o` | `int`, small-file request count |
| `FileLoadOperation.maxDownloadRequestsBig` | `p` | `int`, used for files >= 10 MB |
| `FileLoadOperation.maxCdnParts` | `q` | `int`, `2097152000 / downloadChunkSizeBig` |

`BoostDownload` is only installed for the official client (Nekogram, Cherrygram
and Octogram are excluded) and reads the `BoostDownload` remote pref
(`boostDownload` in settings). `FileLoadOperation.kt` also had a typo
(`downloadChunkSizeBigcolor`), fixed to `downloadChunkSizeBig`.
| `MediaController.saveFile` | `d0` | `(String, Context, int, String, String, Utilities.Callback)` |
| `Utilities$Callback` | `org.telegram.messenger.q20` | functional interface, flattened by R8; `run` -> `a` |

`Utilities$Callback` is not kept as a nested class in 13.2.2. Without the
mapping, saving a secret media crashed with
`ClassNotFoundException: org.telegram.messenger.Utilities$Callback`.

`ShowDeletedMessages` now resolves the 13.2.2 6-parameter storage method and
falls back to the old 5-parameter one. Its optional hooks
(`markDialogMessageAsDeleted`, `removeDeletedMessagesFromNotifications`) fail
soft because those methods are gone/renamed.

### HideStoryViewStatus in 13.2.2

There is no longer a `stories.ReadStoriesRequest` wrapper. The viewer sends
`TL_stories.TL_stories_readStories` and `TL_stories.TL_stories_incrementStoryViews`
directly:

```text
wg.f5.Y(...)  -> TL_stories_readStories           (null callback)
wg.w4        -> TL_stories_incrementStoryViews   (Bool callback)
```

The hook now matches these TLRPC classes in addition to the old wrappers.

---

## Verified — `LocaleController`

Strings moved out of `resources.arsc` in 13.2.2, but the controller and the
lookup methods are stable.

| Logical | Obfuscated | Evidence |
|---|---|---|
| class | `org.telegram.messenger.id` | `id.M0(res, ...)` is used everywhere as `getString` |
| `getInstance` | `E0` | `M0` calls `E0().O0(...)` |
| `getString` | `L0` | `public static String L0(int)` -> `M0(res, null)` |

`LocaleController.getString` now returns the caller's fallback when the
resource id is `0` or the result starts with `LOC_ERR:`. Without that, the
deleted-message menu on 13.2.2 showed `LOC_ERR:null`, because string resource
names are not available and `getResource("Copy", "string")` returns `0`.
| `getStringWithFallback` | `M0` | `public static String M0(int, String)` |
| `getFormatterDay` | `p0` | `ChatMessageCell.K3` formats the message time with `E0().p0()` |
| `applyLanguage` | `c` | `(LocaleInfo, boolean x4, int, cf.z)`, contains "applyLanguage:" strings |
| `LocaleInfo` | `org.telegram.messenger.ed` | constructor param of `applyLanguage` |
| `LocaleInfo.shortName` | `c` | `b()` returns `"local_" + this.f16268c` / this field |

### 13.2.2 string resources

`resources.arsc` no longer contains the string **names** (only the drawable
paths). Localized strings live in `assets/localization_<lang>.bin`, so
`getIdentifier("EditedMessage", "string")` returns `0`. Anything that relied
on looking up a string resource by name must use an id, a translated label
from the module, or structural detection. `MarkMessages` now detects the
edited marker from `MessageObject.editDate` plus the formatted time instead of
searching for the localized "edited" word.

---

## Verified — `Theme` / `ChatMessageCell`

| Logical class | Obfuscated | Evidence |
|---|---|---|
| `org.telegram.ui.ActionBar.Theme` | `org.telegram.ui.ActionBar.x6` | static paints/colors, `D0()` returns the active theme |
| `org.telegram.ui.ActionBar.Theme$ThemeInfo` | `org.telegram.ui.ActionBar.w6` | `q()` checks `"Night"` / `"Dark Blue"` |
| `org.telegram.ui.Cells.ChatMessageCell` | `org.telegram.ui.Cells.s1` | 34k lines, `setMessageObjectInternal`, `setAvatar`, time paint |

| Logical | Obfuscated | Evidence |
|---|---|---|
| `Theme.chat_timePaint` | `T2` | `TextPaint` used to measure the message time in `W2` |
| `Theme.getActiveTheme` | `D0` | returns `K` or `L` (`w6`) |
| `ThemeInfo.isDark` | `q` | theme-name based check |
| `ChatMessageCell.currentTimeString` | `E9` | `CharSequence`, assigned from `K3(...)` |
| `ChatMessageCell.timeTextWidth` | `A9` | `int`, time area width used for layout |
| `ChatMessageCell.timeWidth` | `B9` | `int`, width of the time `StaticLayout` |
| `ChatMessageCell.measureTime` | `W2` | `void (MessageObject)`, called from `setMessageObjectInternal` after `setAvatar` |

`ChatMessageCell.W2` also performs the media measurement, but running the
mark hook after it is safe: the time string and both width fields are already
updated and the marker replacement is idempotent.

---

## Verified — `ColoredImageSpan`

| Logical class | Obfuscated | Evidence |
|---|---|---|
| `org.telegram.ui.Components.ColoredImageSpan` | `org.telegram.ui.Components.eq` | `extends ReplacementSpan`, `(Drawable)` constructor |

| Logical | Obfuscated | Evidence |
|---|---|---|
| `setSize` | `c` | sets `E` and the drawable bounds |
| `setOverrideColor` | `a` | sets the colour key `h`; negative values use the paint colour |
| `overrideColor` | `y` | `int f26857y`; non-zero value is used directly as the color-filter colour in `draw` |

The field is the exact equivalent of Nekogram's `overrideColor`: setting it
makes the span use the module's ARGB colour instead of the time-text paint
colour, so both clients render the red/blue markers identically.

---

## Verified — stories

The whole stories package is obfuscated to `wg`.

| Logical | Obfuscated | Evidence |
|---|---|---|
| `Stories.PeerStoriesView$StoryItemHolder` | `wg.v2` | holds `TL_stories.StoryItem` and `e5`, checks `noforwards` |
| `StoryItemHolder.allowScreenshots` | `d` | `boolean ()`, false when `storyItem.noforwards` or the pinned chat is `noforwards` |
| `StoriesController` | — | removed: no code references `markStoryAsRead` any more |

`wg.q7` is the story viewer, `wg.n7` the per-story view holder, `wg.w2` a peer
stories page and `wg.a6` the pager that calls `q7.l(allowScreenshots)`.

---

## Verified — `PeerColorActivity`

| Logical | Obfuscated | Evidence |
|---|---|---|
| `org.telegram.ui.PeerColorActivity` | `org.telegram.ui.vn0` | `extends ActionBar.j2`, builds two `pn0` pages and sends `TL_account.updateColor` |
| `PeerColorActivity.apply` | `B0` | sends the name update and the `for_profile` update, then `finishFragment()` |
| `PeerColorActivity.namePage` | `h` | `pn0` created with type 1; compared against `h20.e/f` (`user.color`) |
| `PeerColorActivity.profilePage` | `n` | `pn0` created with type 0; compared against `h20.l` (`user.profile_color`) |
| `PeerColorActivity$Page` | `org.telegram.ui.pn0` | `FrameLayout` with the selected colour and emoji |
| `Page.selectedColor` | `h` | `int`, written to `TL_peerColor.color` in `B0` |
| `Page.selectedEmoji` | `n` | `long`, written to `background_emoji_id` in `B0` |

`ThemeActivity` keeps a `changeUserColor` row that opens `vn0`, confirming the
activity identity.

---

## Still stale

| Logical class | Old 12.10.3 target | Status |
|---|---|---|
| `org.telegram.ui.ActionBar.SimpleTextView` | `org.telegram.ui.ActionBar.o5` | `o5` exists with `setText(CharSequence)`; unused by the 13.x profile path |

`BaseFragment` was removed as an entry: the real class is `ActionBar.j2`, its
member names are kept, and no code loads it by name. `r2` is a synthetic touch
listener, so keeping that mapping was misleading.

## Known limitations

- `PreventSecretMediaDeletion` in 13.2.2 no longer has a
  `ChatActivity.sendSecretMediaDelete` to intercept (that method was removed).
  Protection on the official client relies on clearing the viewer `onClose`
  callback and rewriting the opened media's `ttl` to `Integer.MAX_VALUE`, which
  is the same trick the Nekogram branch uses (verified end to end).

  `MessagesStorage$emptyMediaLambda` is a **Nekogram-only** hook. 13.2.2
  official removed `MessagesStorage.emptyMessagesMedia` — there is no
  `(long, ArrayList)` media-wipe method or `media = TL_messageMediaEmpty`
  write left in `is`; media is no longer emptied separately from the message
  row. The dead official branch was removed from the hook. Empirically the
  media survives too: a view-once photo can still be reopened and saved after
  the viewer is closed.
- The mapping loader uses kotlinx.serialization; a single entry with
  `"obfuscated": null` makes the whole file fail to load and silently falls
  back to the `Default` resolver. Keep every `obfuscated` value non-null.

## How the remaining classes should be found

R8 obfuscates type names too, so Nekogram signatures produce false candidates.
Reliable options, in order:

1. The official R8 `mapping.txt` (would make this automatic)
2. String/body analysis per member, like the ones above
3. Cross-references from a known call site (e.g. `SecretMediaViewer.f().h(...)`)

## Verified behaviour on official Telegram 13.2.2

- Own profile shows the fake `offline` status (`updateProfileData` /
  `updateOnlineText` / `onlineTextView`).
- Username row popup shows native items plus `Copy ID`, and tapping it copies
  the id (`ItemOptions.show` + `AndroidUtilities.addToClipboard`).
- Deleted messages are shown in chats with the trash marker; edited messages
  show the pencil marker instead of the "edited" word (`MarkMessages`,
  `ChatMessageCell`, `Theme`, `ColoredImageSpan`).
- Opening a chat works with the `createView` + `dialog_id` mapping, and the
  secret-media gallery hook installs because the missing
  `sendSecretMediaDelete` now fails soft.
- Picking a colour in Chat Settings -> Change Name Color then "Apply Style"
  reaches `PeerColorActivity.apply` and stores `nameColor` / `profileColor`
  in the module config (`/data/data/org.telegram.messenger/shared_prefs/telegami.xml`).
- `AllowSaveVideos` resolves the story item holder (`wg.v2`) and its
  `allowScreenshots` method (`d`).
- The deleted-message context menu shows `Copy` / `Delete` (previously
  `LOC_ERR:null`), which confirms `fillMessageMenu` and the locale fallback.
- A live view-once photo in the official client: closing the viewer keeps the
  photo in the chat (`ttl` rewrite), and tapping the added gallery action saves
  it to `DCIM/IMG_*.jpg` without crashing.
- No `Failed to install` errors at startup for the mapped hooks.

The gallery action uses menu id `1000`; id `1` is Telegram's native edit action
in the secret-media viewer and must not be reused.

Resolved: `MessagesStorage$emptyMediaLambda` is Nekogram-only. The official
`MessagesStorage.emptyMessagesMedia` path was removed from Telegram in 13.2.2,
so the module no longer tries to hook it; retained deleted media keeps its
media (reopening and saving a closed view-once photo still works).
