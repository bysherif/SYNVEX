package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.PoolManager
import com.example.data.PassiveId
import com.example.data.UpgradeCategory
import com.example.data.WeaponId
import com.example.player.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SYNVEX", appName)
    }

    @Test
    fun `evolution synergy card appears when weapon is level 5 and matching passive is owned`() {
        val player = PlayerState()
        player.reset()
        player.weaponLevels[WeaponId.BEAT_SHOCKWAVE] = 5
        player.passiveLevels[PassiveId.AMP_BOOSTER] = 1

        val options = player.generateUpgradeOptions()
        assertTrue(
            "Expected SİSMİK DROP evolution synergy card to be offered",
            options.any {
                it.category == UpgradeCategory.EVOLUTION_SYNERGY &&
                    it.weaponId == WeaponId.BEAT_SHOCKWAVE
            }
        )
    }

    @Test
    fun `pool manager non alloc overlap finds enemies in radius`() {
        val pool = PoolManager()
        val enemy = pool.enemies.acquire()!!
        enemy.x = 50f
        enemy.y = 0f
        enemy.radius = 15f

        var hits = 0
        pool.overlapEnemiesNonAlloc(0f, 0f, 100f) {
            hits++
        }
        assertEquals(1, hits)
    }
}
