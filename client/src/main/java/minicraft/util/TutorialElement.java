package minicraft.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import minicraft.screen.TutorialDisplayHandler;

public class TutorialElement extends AdvancementElement {
  public TutorialElement(
      String key,
      String description,
      Map<String, AdvancementElement.ElementCriterion> criteria,
      ElementRewards rewards) {
    super(key, description, criteria, rewards, new HashSet<>(), new HashMap<>(), new HashSet<>());
  }

  /** Updating and refreshing by the data in this element. */
  public void update() {
    super.update();
    TutorialDisplayHandler.updateCompletedElement(this);
  }
}
