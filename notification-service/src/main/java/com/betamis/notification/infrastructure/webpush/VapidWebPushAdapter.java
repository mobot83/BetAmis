package com.betamis.notification.infrastructure.webpush;

import com.betamis.notification.domain.model.ScheduledNotification;
import com.betamis.notification.domain.port.out.WebPushSender;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * VAPID web push sender.
 *
 * TODO: integrate a VAPID library (e.g. nl.martijndwars:web-push via JitPack,
 *       or a custom BouncyCastle implementation) once the library source is settled.
 *       For now this logs the push payload without actually sending, so all other
 *       notification features (email, scheduling, preferences) can be tested end-to-end.
 */
@ApplicationScoped
public class VapidWebPushAdapter implements WebPushSender {

    private final String vapidPublicKey;

    public VapidWebPushAdapter(
            @ConfigProperty(name = "notification.vapid.public-key") String vapidPublicKey) {
        this.vapidPublicKey = vapidPublicKey;
    }

    @Override
    public void sendMatchReminder(String subscriptionJson, ScheduledNotification notification) {
        // TODO: replace with real VAPID send once library is wired
        Log.infof("Web push (VAPID) not yet implemented — would notify match %s", notification.getMatchId());
    }
}
