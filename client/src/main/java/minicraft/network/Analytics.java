package minicraft.network;

import kong.unirest.Empty;
import kong.unirest.HttpResponse;
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
	public Future<HttpResponse<Empty>> ping() {
		return ping(1);
	}

	@Nullable
	public Future<HttpResponse<Empty>> ping(int value) {
		return CompletableFuture.completedFuture(null);
	}
}
