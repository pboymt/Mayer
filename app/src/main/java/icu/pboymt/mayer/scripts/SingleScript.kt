package icu.pboymt.mayer.scripts

import androidx.annotation.Keep
import icu.pboymt.mayer.core.MayerAccessibilityHelper
import icu.pboymt.mayer.core.MayerScript
import icu.pboymt.mayer.core.Script

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