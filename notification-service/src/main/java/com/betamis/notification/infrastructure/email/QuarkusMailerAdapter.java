package com.betamis.notification.infrastructure.email;

import com.betamis.notification.domain.model.ScheduledNotification;
import com.betamis.notification.domain.port.out.EmailSender;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@ApplicationScoped
public class QuarkusMailerAdapter implements EmailSender {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH).withZone(ZoneId.of("UTC"));

    private final Mailer mailer;
    private final Template matchReminder;
    private final String fromAddress;
    private final String baseUrl;

    public QuarkusMailerAdapter(Mailer mailer,
                                @Location("email/match_reminder") Template matchReminder,
                                @ConfigProperty(name = "notification.mail.from", defaultValue = "noreply@betamis.com")
                                String fromAddress,
                                @ConfigProperty(name = "notification.base-url", defaultValue = "https://betamis.com")
                                String baseUrl) {
        this.mailer = mailer;
        this.matchReminder = matchReminder;
        this.fromAddress = fromAddress;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendMatchReminder(String toEmail, String unsubscribeToken, ScheduledNotification notification) {
        long hoursUntil = ChronoUnit.HOURS.between(notification.getNotifyAt(), notification.getKickoffAt());
        String timeLabel = hoursUntil >= 20 ? "1 day" : "1 hour";

        String html = matchReminder
                .data("homeTeamId", notification.getHomeTeamId())
                .data("awayTeamId", notification.getAwayTeamId())
                .data("kickoffAt", FORMATTER.format(notification.getKickoffAt()))
                .data("timeLabel", timeLabel)
                .data("unsubscribeUrl", baseUrl + "/api/notifications/unsubscribe?token=" + unsubscribeToken)
                .render();

        mailer.send(Mail.withHtml(toEmail,
                "Match reminder: " + notification.getHomeTeamId() + " vs " + notification.getAwayTeamId(),
                html).setFrom(fromAddress));
    }
}
