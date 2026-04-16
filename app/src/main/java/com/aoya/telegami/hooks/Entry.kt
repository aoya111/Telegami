package com.aoya.telegami.hooks

import com.aoya.telegami.BuildConfig
import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Constants
import com.highcapable.kavaref.KavaRef
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.ClassLoaderProvider
import com.highcapable.kavaref.extension.hasClass
import com.highcapable.kavaref.runtime.KavaRefRuntime
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object Entry : IYukiHookXposedInit {
    override fun onInit() =
        configs {
            // isDebug = BuildConfig.DEBUG
        }

    override fun onHook() =
        encase {
            if (arrayOf(Constants.APP_ID, Constants.APP_DEBUG_ID).contains(packageName)) {
                loadApp(name = packageName) {
                    "${Constants.APP_ID}.service.PrefManager".toClass().resolve().firstMethod { name = "getActiveVersion" }.hook {
                        replaceTo(BuildConfig.VERSION_CODE)
                    }
                }
            }
            if (!Constants.SUPPORTED_TELEGRAM_PACKAGES.contains(packageName)) return@encase
            loadApp {
                KavaRef.logLevel = KavaRefRuntime.LogLevel.OFF
                onAppLifecycle(false) {
                    onCreate {
                        Telegami.init(moduleAppFilePath, appContext!!)
                        ClassLoaderProvider.classLoader = appClassLoader

                        loadHooker(Settings)
                        loadHooker(LocaleController)
                        loadHooker(MarkMessages)
                        loadHooker(AllowScreenshots)
                        loadHooker(ApplyColor)
                        loadHooker(ApplyColor)
                        loadHooker(ProfileDetails)
                        loadHooker(NekoBlock)
                        loadHooker(HideBetaUpdate)

                        loadHooker(HideSeenStatus)
                        loadHooker(HideStoryViewStatus)
                        loadHooker(HideOnlineStatus)
                        loadHooker(HideTyping)
                        loadHooker(ShowDeletedMessages)
                        loadHooker(PreventSecretMediaDeletion)
                        loadHooker(UnlockChannelFeatures)
                        loadHooker(AllowSaveVideos)
                        loadHooker(DisableAds)
                        loadHooker(FakePremium)
                    }
                }
            }
        }
}
