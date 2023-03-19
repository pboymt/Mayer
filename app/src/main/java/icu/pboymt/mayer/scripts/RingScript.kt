package icu.pboymt.mayer.scripts

import icu.pboymt.mayer.assets.Templates
import icu.pboymt.mayer.runner.*
import icu.pboymt.mayer.utils.PrefKey
import icu.pboymt.mayer.utils.PrefKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Script(
    name = "铃铛",
    uuid = "RingScript",
    description = "铃铛脚本",
    author = "pboymt"
)
class RingScript(helper: MayerAccessibilityHelper) : MayerScript(helper) {

    data class Settings(
        /** 是否自动开始 */
        val autoStart: Boolean,
        /** 启动 B 服客户端 */
        val startWfBilibili: Boolean,
        /** 游戏包名 */
        val packageName: String,
        /** 最大战斗次数 */
        val fightCount: Int,
        /** 是否自动续战 */
        val autoContinue: Boolean,
        /** 是否自动回复体力 */
        val useStamina: Boolean,
        /** 体力回复次数 */
        val staminaCount: Int,
        /** 优先使用的体力恢复道具 */
        val preferStamina: String,
        /** 是否允许使用星导石恢复体力 */
        val staminaAllowStone: Boolean,
    )

    enum class State {
        START_GAME,
        WAIT_RING,
        OPEN_RING_LIST,
        ENTER_RING_ROOM,
        WAIT_RING_ROOM,
        BEFORE_FIGHT_READY,
        FIGHT_READY,
        WAIT_FIGHT,
        WAIT_FIGHT_FINISHED,
        WAIT_RETURN_TO_ROOM,
        WAIT_RETURN_TO_HOME,
        SELECT_STAMINA,
        WILL_STOP,
    }

    private lateinit var settings: Settings
    private var state: State = State.START_GAME

    // 脚本设置
    private var playCount = 0
    private var staminaCount = 0

    //    private var preferStamina = Templates.BTN_STAMINA_SMALL
    private var staminaList = mutableSetOf(
        Templates.BTN_STAMINA_GSMALL,
        Templates.BTN_STAMINA_SMALL,
        Templates.BTN_STAMINA_MEDIUM,
        Templates.BTN_STAMINA_LARGE,
        Templates.BTN_STAMINA_XNEWYEAR,
        Templates.BTN_STAMINA_STONE
    )

    override suspend fun loadSettingsFromDataStore() {
        super.loadSettingsFromDataStore()
        settings = ds.data.map { pref ->
            val autoStart = PrefKeys.Script.All.StartApp.let {
                pref[it.prefKey] ?: it.default
            } as Boolean
            val packageName = PrefKeys.Script.All.StartAppPackageName.let {
                pref[it.prefKey] ?: it.default
            } as String
            val startWfBilibili = PrefKeys.Script.All.StartWfBilibili.let {
                pref[it.prefKey] ?: it.default
            } as Boolean
            val fightCount = Prefs.FightCount.let {
                pref[it.prefKey] ?: it.default
            } as Int
            val autoContinue = Prefs.AutoContinue.let {
                pref[it.prefKey] ?: it.default
            } as Boolean
            val useStamina = Prefs.UseStamina.let {
                pref[it.prefKey] ?: it.default
            } as Boolean
            val staminaCount = Prefs.StaminaCount.let {
                pref[it.prefKey] ?: it.default
            } as Int
            val preferStamina = Prefs.PreferStamina.let {
                pref[it.prefKey] ?: it.default
            } as String
            val staminaAllowStone = Prefs.StaminaAllowStone.let {
                pref[it.prefKey] ?: it.default
            } as Boolean
            Settings(
                autoStart = autoStart,
                packageName = packageName,
                startWfBilibili = startWfBilibili,
                fightCount = fightCount,
                autoContinue = autoContinue,
                useStamina = useStamina,
                staminaCount = staminaCount,
                preferStamina = preferStamina,
                staminaAllowStone = staminaAllowStone
            )
        }.first()
    }

    override suspend fun beforePlay() {
        super.beforePlay()
        state = if (settings.autoStart) {
            State.START_GAME
        } else {
            State.WAIT_RING
        }
        if (staminaList.remove(settings.preferStamina)) {
            staminaList.add(settings.preferStamina)
        }
        if (!settings.staminaAllowStone) {
            staminaList.remove(Templates.BTN_STAMINA_STONE)
        }
    }

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override suspend fun play() {
        while (running) {
            when (state) {
                State.START_GAME -> waitForGame()
                State.WAIT_RING -> waitForRing()
                State.OPEN_RING_LIST -> openRingList()
                State.ENTER_RING_ROOM -> enterRingRoom()
                State.WAIT_RING_ROOM -> waitForRoom()
                State.BEFORE_FIGHT_READY -> beforeFightReady()
                State.FIGHT_READY -> fightReady()
                State.WAIT_FIGHT -> waitForFight()
                State.WAIT_FIGHT_FINISHED -> waitForFightFinished()
                State.WAIT_RETURN_TO_ROOM -> waitForReturnToRoom()
                State.WAIT_RETURN_TO_HOME -> waitForReturnToHome()
                State.SELECT_STAMINA -> selectStamina()
                State.WILL_STOP -> break
                else -> break
            }
        }
    }

    /**
     * 等待游戏启动
     */
    @Step(templates = [])
    suspend fun waitForGame() {
        log.i("当前应用: $currentPackage")
        val packageName = if (settings.startWfBilibili) {
            "com.leiting.wf.bilibili"
        } else {
            "com.leiting.wf"
        }
        if (currentPackage != packageName) {
            launchPackage(packageName)
            delay(3000L)
        } else {
            state = State.WAIT_RING
        }
    }

    /**
     * 等待铃铛
     */
    @Step(templates = [Templates.RING, Templates.RING_N])
    suspend fun waitForRing() {
        log.i("等待铃铛")
        if (click(Templates.RING) || click(Templates.RING_N)) {
            state = State.OPEN_RING_LIST
        }
        delay(3000L)
    }

    /**
     * 打开铃铛列表
     */
    @Step(templates = [Templates.BTN_RING_JOIN_ACCEPT, Templates.BTN_RING_JOIN_ACCEPT_N])
    suspend fun openRingList() {
        log.i("打开铃铛列表")
        if (exists(Templates.BTN_RING_JOIN_ACCEPT) || exists(Templates.BTN_RING_JOIN_ACCEPT_N)) {
            state = State.ENTER_RING_ROOM
            delay(1000L)
        } else {
            delay(2000L)
        }
    }

    /**
     * 进入房间
     */
    @Step(templates = [Templates.BTN_RING_JOIN_ACCEPT, Templates.BTN_RING_JOIN_ACCEPT_N])
    suspend fun enterRingRoom() {
        log.i("尝试进入房间")
        if (click(Templates.BTN_RING_JOIN_ACCEPT) || click(Templates.BTN_RING_JOIN_ACCEPT_N)) {
            state = State.WAIT_RING_ROOM
            delay(3000L)
        }
        delay(1000L)
    }

    /**
     * 等待进入房间
     */
    @Step(templates = [Templates.BTN_OK, Templates.HOME_BTN_CHAPTER, Templates.WAITING_ROOM_TEAM_FORM, Templates.RING_N, Templates.RING])
    suspend fun waitForRoom() {
        log.i("检查是否进入房间")
        if (exists(Templates.WAITING_ROOM_TEAM_FORM)) {
            state = State.BEFORE_FIGHT_READY
        } else if (exists(Templates.RING_N) || exists(Templates.RING)) {
            state = State.ENTER_RING_ROOM
        } else if (exists(Templates.HOME_BTN_CHAPTER)) {
            state = State.WAIT_RING
        } else if (exists(Templates.BTN_OK)) {
            click(Templates.BTN_OK)
            state = State.WAIT_RING
        }
        delay(2000L)
    }

    /**
     * 战斗前准备
     */
    @Step(templates = [Templates.WAITING_ROOM_AUTO_CONTINUE_NO, Templates.WAITING_ROOM_AUTO_CONTINUE_YES])
    suspend fun beforeFightReady() {
        log.i("进入战斗前准备")
        if (settings.autoContinue) {
            if (exists(Templates.WAITING_ROOM_AUTO_CONTINUE_YES)) {
                state = State.FIGHT_READY
            } else {
                click(Templates.WAITING_ROOM_AUTO_CONTINUE_NO)
            }
        } else {
            if (exists(Templates.WAITING_ROOM_AUTO_CONTINUE_NO)) {
                state = State.FIGHT_READY
            } else {
                click(Templates.WAITING_ROOM_AUTO_CONTINUE_YES)
            }
        }
        delay(1000L)
    }

    /**
     * 战斗准备
     */
    @Step(templates = [Templates.WAITING_ROOM_READY_NO])
    suspend fun fightReady() {
        log.i("完成战斗准备")
        if (click(Templates.WAITING_ROOM_READY_NO)) {
            state = State.WAIT_FIGHT
        }
        delay(2000L)
    }

    /**
     * 等待战斗
     */
    @Step(templates = [Templates.BTN_BATTLE_AUTO_SKILL_ON, Templates.BTN_OK])
    suspend fun waitForFight() {
        log.i("等待战斗")
        if (exists(Templates.BTN_BATTLE_AUTO_SKILL_ON)) {
            state = State.WAIT_FIGHT_FINISHED
        } else if (click(Templates.BTN_OK)) {
            state = State.WAIT_RING
        }
        delay(3000L)
    }

    /**
     * 等待战斗结束
     */
    @Step(templates = [Templates.BTN_BATTLE_AUTO_SKILL_ON])
    suspend fun waitForFightFinished() {
        log.i("等待战斗结束")
        // TODO: 还未实现判断战斗是否成功，目前是否失败均会增加战斗计数
        if (!exists(Templates.BTN_BATTLE_AUTO_SKILL_ON)) {
            playCount++
            state =
                if (settings.autoContinue && (settings.fightCount == 0 || playCount < settings.fightCount)) {
                    State.WAIT_RETURN_TO_ROOM
                } else {
                    State.WAIT_RETURN_TO_HOME
                }
        }
        delay(3000L)
    }

    /**
     * 等待返回房间
     */
    @Step(
        templates = [Templates.BTN_OK, Templates.HOME_BTN_CHAPTER, Templates.WAITING_ROOM_TEAM_FORM,
            Templates.BTN_CONTINUE, Templates.BTN_RETURN_ROOM, Templates.BTN_BATTLE_AUTO_SKILL_ON, Templates.LABEL_LACK_STAMINA]
    )
    suspend fun waitForReturnToRoom() {
        log.i("等待返回房间")
        if (exists(Templates.WAITING_ROOM_TEAM_FORM)) { // 回到房间
            state = State.BEFORE_FIGHT_READY
        } else if (exists(Templates.LABEL_LACK_STAMINA)) { // 发现体力不足
            state = State.SELECT_STAMINA
        } else if (exists(Templates.BTN_BATTLE_AUTO_SKILL_ON)) { // 发现战斗已经开始
            state = State.WAIT_FIGHT_FINISHED
        } else if (exists(Templates.BTN_OK)) { // 发现无法续战弹窗
            click(Templates.BTN_OK)
        } else if (exists(Templates.LABEL_LEVEL_UP)) { // 发现升级弹窗
            helper.click(display.width / 2, display.height / 3)
        } else if (exists(Templates.HOME_BTN_CHAPTER)) { // 回到主界面
            state = State.WAIT_RING
        } else {
            click(Templates.BTN_CONTINUE) || click(Templates.BTN_RETURN_ROOM)
        }
        delay(2000L)
    }

    /**
     * 等待返回主页
     */
    @Step(templates = [Templates.HOME_BTN_CHAPTER, Templates.BTN_CONTINUE, Templates.BTN_LEAVE_ROOM])
    suspend fun waitForReturnToHome() {
        log.i("等待返回主页")
        if (exists(Templates.HOME_BTN_CHAPTER)) {
            state = State.WAIT_RING
        } else {
            click(Templates.BTN_CONTINUE) || click(Templates.BTN_LEAVE_ROOM)
        }
        delay(2000L)
    }

    /**
     * 选择恢复药
     */
    @Step(
        templates = [Templates.BTN_STAMINA_GSMALL, Templates.BTN_STAMINA_LARGE,
            Templates.BTN_STAMINA_MEDIUM, Templates.BTN_STAMINA_SMALL, Templates.BTN_STAMINA_XNEWYEAR,
            Templates.BTN_STAMINA_STONE, Templates.BTN_USE]
    )
    suspend fun selectStamina() {
        if (!settings.useStamina) {
            log.i("未启用体力恢复，停止脚本")
            state = State.WILL_STOP
            return
        } else if (settings.staminaCount in (1..staminaCount)) {
            state = State.WILL_STOP
            log.i("体力恢复一次已达上限，停止脚本")
            return
        }
        if (click(Templates.BTN_OK)) {
            log.i("成功使用恢复药")
            staminaCount++
            state = State.WAIT_RETURN_TO_ROOM
            return
        } else if (click(Templates.BTN_USE)) {
            log.i("使用恢复药")
            return
        } else {
            log.i("选择恢复药")
            for (stamina in staminaList) {
                if (click(stamina)) {
                    break
                }
            }
        }
        delay(2000L)
    }

    companion object {
        const val NAME = "RingScript"
    }

    object Prefs {
        val FightCount = PrefKey(
            key = "mayer.pref.script.ring.fight_count",
            default = 0,
            title = "最大战斗次数"
        )
        val AutoContinue = PrefKey(
            key = "mayer.pref.script.ring.auto_continue",
            default = true,
            title = "是否自动续战"
        )
        val UseStamina = PrefKey(
            key = "mayer.pref.script.ring.use_stamina",
            default = true,
            title = "是否自动回复体力"
        )
        val StaminaCount = PrefKey(
            key = "mayer.pref.script.ring.stamina_count",
            default = 0,
            title = "体力回复次数"
        )
        val PreferStamina = PrefKey(
            key = "mayer.pref.script.ring.prefer_stamina",
            default = Templates.BTN_STAMINA_GSMALL,
            title = "优先使用的体力恢复道具"
        )
        val StaminaAllowStone = PrefKey(
            key = "mayer.pref.script.ring.stamina_allow_stone",
            default = true,
            title = "是否允许使用星导石恢复体力"
        )
    }

}