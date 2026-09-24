package minicraft.screen;

/**
 * Persisted multiplayer prefs (IP / UUID / username) for save/load compatibility. Full
 * account-login UI from upstream Minicraft+ was removed as dead code.
 */
public final class MultiplayerDisplay {
  public static String savedIP = "";
  public static String savedUUID = "";
  public static String savedUsername = "";

  private MultiplayerDisplay() {}
}
