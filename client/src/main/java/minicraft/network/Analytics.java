package minicraft.network;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Analytics pings are disabled in the transmute port (no external telemetry).
 */
public enum Analytics {

	GameStartup,
	SaveFileUpdate,
	SinglePlayerGame,
	MultiplayerGame,
	LocalSession,
	WorldCreation,
	LoginAttempt,
	LoginSuccess,
	SessionAttempt,
	SessionJoin,
	FirstAirWizardDeath,
	FirstAirWizardIIDeath,
	AirWizardDeath,
	AirWizardIIDeath,
	Crashes,
	SinglePlayerDeath,
	MultiplayerDeath;

	@Nullable
	public Future<?> ping() {
		return ping(1);
	}

	@Nullable
	public Future<?> ping(int value) {
		return CompletableFuture.completedFuture(null);
	}
}
