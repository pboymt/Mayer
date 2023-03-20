package icu.pboymt.mayer.scripts

import androidx.annotation.Keep
import icu.pboymt.mayer.runner.MayerAccessibilityHelper
import icu.pboymt.mayer.runner.MayerScript
import icu.pboymt.mayer.runner.Script

@Script(
    name = "单人",
    uuid = "SingleScript",
    description = "单人脚本",
    author = "pboymt"
)
class SingleScript(helper: MayerAccessibilityHelper) : MayerScript(helper) {

    override suspend fun play() {

    }

    companion object {
        @Keep
        const val NAME = "SingleScript"
    }

}