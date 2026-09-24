package games.transmute.minicraft;

import TransmuteCore.util.verify.GameHarness;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Chest;
import minicraft.entity.mob.Zombie;
import minicraft.item.Items;
import minicraft.item.Recipes;
import minicraft.level.tile.Tiles;
import minicraft.screen.WorldGenDisplay;
import org.junit.Assert;
import org.junit.Test;

/** Smoke that adapted Minicraft+ domain systems are wired (tiles, items, recipes, entities). */
public class DomainFidelitySmokeTest {
  @Test
  public void domainSystemsPresent() {
    try (GameHarness harness =
        GameHarness.of(() -> new MinicraftGame(MinicraftGame.config(true)))) {
      harness.step(1);
      WorldGenDisplay.setSeedInput("99");
      SinglePlayerSession.startFreshWorld();

      Assert.assertNotNull(Tiles.get("Grass"));
      Assert.assertNotNull(Tiles.get("Rock"));
      Assert.assertNotNull(Tiles.get("Dirt"));
      Assert.assertNotNull(Items.get("Wood"));
      Assert.assertNotNull(Items.get("Wood Pickaxe"));
      Assert.assertFalse(Recipes.craftRecipes.isEmpty());

      Entity zombie = new Zombie(1);
      Assert.assertNotNull(zombie);
      Chest chest = new Chest();
      Assert.assertNotNull(chest);

      Assert.assertEquals(6, minicraft.core.Game.levels.length);
      Assert.assertNotNull(minicraft.core.Game.levels[minicraft.core.Game.currentLevel]);
    }
  }
}
