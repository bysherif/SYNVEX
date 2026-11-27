package com.example

import com.example.core.BeatManager
import com.example.core.PoolManager
import com.example.data.PassiveId
import com.example.data.UpgradeCategory
import com.example.data.WaveSchedule
import com.example.data.WeaponId
import com.example.player.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `evolution synergy triggers when active weapon reaches level 5 with matching passive`() {
        val player = PlayerState()
        player.reset()
        player.weaponLevels[WeaponId.BEAT_SHOCKWAVE] = 5
        player.passiveLevels[PassiveId.AMP_BOOSTER] = 1

        val cards = player.generateUpgradeOptions()
        assertTrue(
            "Expected SİSMİK DROP evolution synergy card",
            cards.any {
                it.category == UpgradeCategory.EVOLUTION_SYNERGY &&
                    it.weaponId == WeaponId.BEAT_SHOCKWAVE
            }
        )
    }

    @Test
    fun `pool manager non alloc overlap finds enemies accurately and recycles`() {
        val pool = PoolManager()
        val near = pool.enemies.acquire()!!
        near.x = 60f
        near.y = 0f
        near.radius = 15f

        val far = pool.enemies.acquire()!!
        far.x = 300f
        far.y = 0f
        far.radius = 15f

        var hitCount = 0
        pool.overlapEnemiesNonAlloc(0f, 0f, 100f) { enemy ->
            hitCount++
            enemy.takeDamage(999f, 0f, 0f)
        }
        assertEquals(1, hitCount)
        pool.compactAll()
        assertEquals(1, pool.enemies.activeCount)
    }

    @Test
    fun `wave schedule escalates across 0 to 5 minutes`() {
        assertEquals(1, WaveSchedule.currentWave(10f).waveNumber)
        assertEquals(2, WaveSchedule.currentWave(60f).waveNumber)
        assertEquals(3, WaveSchedule.currentWave(110f).waveNumber)
        assertEquals(4, WaveSchedule.currentWave(180f).waveNumber)
        assertEquals(5, WaveSchedule.currentWave(260f).waveNumber)
    }

    @Test
    fun `beat manager unlocks musical stems as combo increases`() {
        val beatManager = BeatManager(bpm = 128)
        beatManager.updateComboStems(0)
        assertEquals(128, beatManager.bpm)
        beatManager.updateComboStems(35)
        assertTrue(beatManager.secondsPerBeat in 0.46..0.47)
    }

    @Test
    fun `arena bounds clamp player strictly inside electric walls`() {
        val player = PlayerState()
        player.reset()
        player.x = 2000f
        player.y = -2000f

        val hitWall = com.example.game.ArenaBounds.clampPlayer(player)
        assertTrue("Expected player to collide with arena wall", hitWall)
        assertTrue(player.x <= com.example.game.ArenaBounds.HALF_WIDTH)
        assertTrue(player.y >= -com.example.game.ArenaBounds.HALF_HEIGHT)
    }

    @Test
    fun `dash grants invulnerability and nova charges to 100 percent on kills`() {
        val profile = com.example.data.PlayerProfile(
            metaLevels = mapOf(
                com.example.data.MetaUpgradeId.ARMOR_CORE to 2,
                com.example.data.MetaUpgradeId.TURBO_DRIVE to 2
            ),
            totalKills = 30
        )
        val player = PlayerState()
        player.reset(profile)

        assertTrue(player.maxHp > 140f)
        assertTrue(player.triggerDash())
        assertTrue(player.isDashing)
        assertTrue(player.invulnerableTimer > 0.3f)

        repeat(20) { player.registerKill() }
        assertTrue(player.isNovaReady)
        assertTrue(player.consumeNova())
        assertEquals(0f, player.novaCharge, 0.01f)
        assertTrue(com.example.data.AchievementId.FIRST_BLOOD.isCompleted(profile))
    }

    @Test
    fun `store offer catalog exposes valid coin crystal bundle and exchange packs`() {
        val allPacks = buildList {
            add(com.example.data.StoreOfferCatalog.featuredBundle)
            add(com.example.data.StoreOfferCatalog.freeGiftPack)
            addAll(com.example.data.StoreOfferCatalog.coinPacks)
            addAll(com.example.data.StoreOfferCatalog.crystalPacks)
            addAll(com.example.data.StoreOfferCatalog.exchangePacks)
        }
        assertEquals(allPacks.size, allPacks.map { it.id }.toSet().size)
        assertTrue(com.example.data.StoreOfferCatalog.coinPacks.all { it.coinAmount > 0 && it.isRealMoneyIap })
        assertTrue(com.example.data.StoreOfferCatalog.crystalPacks.all { it.crystalAmount > 0 && it.isRealMoneyIap })
        assertTrue(com.example.data.StoreOfferCatalog.exchangePacks.all { it.coinExchangeCost > 0 && it.crystalAmount > 0 && !it.isRealMoneyIap })
        assertTrue(com.example.data.StoreOfferCatalog.featuredBundle.coinAmount > 0 && com.example.data.StoreOfferCatalog.featuredBundle.crystalAmount > 0 && com.example.data.StoreOfferCatalog.featuredBundle.isRealMoneyIap)

        val skus = com.example.data.StoreOfferCatalog.allGooglePlayProductIds
        assertEquals(9, skus.size)
        assertEquals(skus.size, skus.toSet().size)
        skus.forEach { sku ->
            assertTrue(com.example.data.StoreOfferCatalog.findByProductId(sku) != null)
        }
    }

    @Test
    fun `stage catalog transitions across 6 world maps every 7 stages with distinct arena pillars`() {
        assertEquals(6, com.example.data.ArenaMapId.entries.size)
        assertEquals(42, com.example.data.StageCatalog.stages.size)
        com.example.data.ArenaMapId.entries.forEach { map ->
            assertEquals(7, com.example.data.StageCatalog.stagesForMap(map).size)
        }

        assertEquals(com.example.data.ArenaMapId.PLASMA_VOLCANO, com.example.data.StageCatalog.newlyUnlockedMapAfterStage(7))
        assertEquals(com.example.data.ArenaMapId.COSMIC_VOID, com.example.data.StageCatalog.newlyUnlockedMapAfterStage(14))
        assertEquals(com.example.data.ArenaMapId.FROST_CORE, com.example.data.StageCatalog.newlyUnlockedMapAfterStage(21))
        assertEquals(com.example.data.ArenaMapId.TOXIC_LAB, com.example.data.StageCatalog.newlyUnlockedMapAfterStage(28))
        assertEquals(com.example.data.ArenaMapId.SOLAR_EMPIRE, com.example.data.StageCatalog.newlyUnlockedMapAfterStage(35))
        assertEquals(null, com.example.data.StageCatalog.newlyUnlockedMapAfterStage(42))

        com.example.game.ArenaBounds.activeMap = com.example.data.ArenaMapId.CYBER_CITY
        assertEquals(4, com.example.game.ArenaBounds.activePillars.size)
        com.example.game.ArenaBounds.activeMap = com.example.data.ArenaMapId.PLASMA_VOLCANO
        assertEquals(5, com.example.game.ArenaBounds.activePillars.size)
        com.example.game.ArenaBounds.activeMap = com.example.data.ArenaMapId.COSMIC_VOID
        assertEquals(6, com.example.game.ArenaBounds.activePillars.size)
        com.example.game.ArenaBounds.activeMap = com.example.data.ArenaMapId.FROST_CORE
        assertEquals(6, com.example.game.ArenaBounds.activePillars.size)
        com.example.game.ArenaBounds.activeMap = com.example.data.ArenaMapId.TOXIC_LAB
        assertEquals(5, com.example.game.ArenaBounds.activePillars.size)
        com.example.game.ArenaBounds.activeMap = com.example.data.ArenaMapId.SOLAR_EMPIRE
        assertEquals(6, com.example.game.ArenaBounds.activePillars.size)
        com.example.game.ArenaBounds.activeMap = com.example.data.ArenaMapId.CYBER_CITY
    }

    @Test
    fun `vip subscription catalog grants daily yellow coins red crystals streak mega bonus and combat buffs`() {
        val plans = com.example.data.VipSubscriptionCatalog.plans
        assertEquals(2, plans.size)
        assertEquals(plans.size, com.example.data.VipSubscriptionCatalog.allGooglePlaySubIds.toSet().size)

        val monthly = com.example.data.VipSubscriptionCatalog.vipMonthly
        assertEquals(400, monthly.dailyCoins)
        assertEquals(10, monthly.dailyCrystals)
        assertTrue(monthly.megaDay7Coins >= monthly.dailyCoins * 2)
        assertTrue(monthly.welcomeCoins >= 1000 && monthly.welcomeCrystals >= 30)
        assertEquals(monthly, com.example.data.VipSubscriptionCatalog.findByProductId(monthly.googlePlayProductId))

        val baseProfile = com.example.data.PlayerProfile()
        val vipProfile = com.example.data.PlayerProfile(
            activeVipPlanId = monthly.id,
            vipAutoRenewing = true,
            vipClaimStreak = 6
        )
        assertTrue(!baseProfile.isVipSubscribed && !baseProfile.canClaimVipDailyReward())
        assertTrue(vipProfile.isVipSubscribed)
        assertTrue(vipProfile.canClaimVipDailyReward())
        assertTrue(vipProfile.nextVipClaimIsMegaBonus)

        val basePlayer = PlayerState().apply { reset(baseProfile) }
        val vipPlayer = PlayerState().apply { reset(vipProfile) }
        assertTrue(vipPlayer.maxHp > basePlayer.maxHp)
        assertTrue(vipPlayer.metaCoinMultiplier > basePlayer.metaCoinMultiplier)
    }
}
